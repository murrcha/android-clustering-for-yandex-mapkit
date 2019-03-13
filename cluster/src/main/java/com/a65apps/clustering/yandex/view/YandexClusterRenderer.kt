package com.a65apps.clustering.yandex.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.util.Log
import com.a65apps.clustering.core.ClustersDiff
import com.a65apps.clustering.core.LatLng
import com.a65apps.clustering.core.Marker
import com.a65apps.clustering.core.Markers
import com.a65apps.clustering.core.view.AnimationParams
import com.a65apps.clustering.core.view.ClusterRenderer
import com.a65apps.clustering.yandex.extention.addPlacemark
import com.a65apps.clustering.yandex.extention.toLatLng
import com.a65apps.clustering.yandex.extention.toPoint
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map

class YandexClusterRenderer(map: Map,
                            private val imageProvider: ClusterPinProvider,
                            private var animationParams: AnimationParams,
                            private val mapObjectTapListener: TapListener? = null,
                            name: String = "CLUSTER_LAYER")
    : ClusterRenderer {
    private val layer: MapObjectCollection = map.addMapObjectLayer(name)
    private val mapObjects = mutableMapOf<Marker, PlacemarkMapObject>()
    private var clusterAnimator: AnimatorSet = AnimatorSet()
    private var tapListener = if (mapObjectTapListener != null) {
        object : MapObjectTapListener {
            override fun onMapObjectTap(mapObject: MapObject, point: Point): Boolean {
                for ((marker, markerMapObject) in mapObjects) {
                    if (mapObject == markerMapObject) {
                        mapObjectTapListener.markerTapped(marker, markerMapObject)
                        return true
                    }
                }
                return false
            }
        }
    } else {
        null
    }

    override fun updateClusters(diffs: ClustersDiff) {
        if (mapObjects.isEmpty() || !animationParams.animationEnabled ||
                diffs.actualMarkers.isEmpty() || diffs.newMarkers.isEmpty()) {
            simpleUpdate(diffs.newMarkers)
        } else {
            val transitions = diffs.transitions
            clusterAnimator.cancel()
            clusterAnimator = AnimatorSet()
            if (diffs.isCollapsing) {
                for ((cluster, markers) in transitions) {
                    clusterAnimator.play(animateMarkersToCluster(cluster, markers))
                }
            } else {
                for ((cluster, markers) in transitions) {
                    clusterAnimator.play(animateClusterToMarkers(cluster, markers))
                }
            }
            clusterAnimator.duration = animationParams.duration
            clusterAnimator.interpolator = animationParams.interpolator
            clusterAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    //TODO: убрать логирование
                    //--------------------------
                    val expectedPinCount = 717
                    val markersCount = Markers.count(mapObjects.keys)
                    Log.d("MARKER", "RENDERER CLUSTER COUNT ${mapObjects.size}")
                    Log.d("MARKER", "RENDERER PINS COUNT BEFORE CHECKING $markersCount")
                    if (markersCount != expectedPinCount) {
                        Log.e("MARKER",
                                "COUNT DIFF ${Math.abs(expectedPinCount - markersCount)}")
                        Log.e("MARKER", "RENDERER PINS COUNT $markersCount")
                    }
                    //---------------------------

                    checkPins(diffs.newMarkers)

                    //--------------------------
                    val newMarkersCount = Markers.count(mapObjects.keys)
                    Log.d("MARKER", "RENDERER PINS COUNT AFTER CHECKING $newMarkersCount")
                    Log.d("MARKER", "-------------------------------------------------------")
                    //--------------------------
                    clusterAnimator.removeListener(this)
                }
            })
            clusterAnimator.start()
        }
    }

    private fun checkPins(clusters: Set<Marker>) {
        val start = System.currentTimeMillis()
        val iterator = mapObjects.iterator()
        while (iterator.hasNext()) {
            val mapObject = iterator.next()
            if (!clusters.contains(mapObject.key)) {
                if (mapObject.value.isValid) {
                    layer.remove(mapObject.value)
                }
                iterator.remove()
            }
        }
        for (marker in clusters) {
            if (!mapObjects.containsKey(marker)) {
                createPlacemark(marker)
            }
        }
        val end = System.currentTimeMillis()
        Log.d("MARKER", "PIN CHECKING TIME ${end - start} ms")
    }

    override fun setMarkers(markers: Set<Marker>) {
        markers.forEach {
            createPlacemark(it)
        }
    }

    override fun animation(animationParams: AnimationParams) {
        this.animationParams = animationParams
    }

    override fun onAdd() {
        tapListener?.let {
            layer.addTapListener(it)
        }
    }

    override fun onRemove() {
        tapListener?.let {
            layer.addTapListener(it)
        }
    }

    private fun simpleUpdate(newMarkers: Set<Marker>) {
        layer.clear()
        for (marker in newMarkers) {
            createPlacemark(marker)
        }
    }

    //Вызывается для перемещения маркеров в кластер
    private fun markersToCluster(cluster: Marker, markers: Set<Marker>) {
        if (animationParams.animationEnabled) {
            animateMarkersToCluster(cluster, markers)
        } else {
            setMarkersToCluster(cluster, markers)
        }
    }

    //Перемещение маркеров в кластер с анимацией
    private fun animateMarkersToCluster(cluster: Marker, markers: Set<Marker>): Animator {
        //коллекция маркеров которые будут анимироваться в кластер
        val movedMarkers = mutableListOf<PlacemarkMapObject>()
        val startCoordinates = mutableListOf<LatLng>()
        markers.forEach { marker ->
            mapObjects[marker]?.let { mapObject ->
                movedMarkers.add(mapObject)
                startCoordinates.add(mapObject.geometry.toLatLng())
            }
        }
        val clusterPoint = cluster.getGeoCoor().toPoint()
        val animator = ValueAnimator.ofFloat(0f, 1f)
        val animatorUpdateListener = ValueAnimator.AnimatorUpdateListener {
            val factor = it.animatedValue as Float
            for (i in 0 until movedMarkers.size) {
                val capacity = if (animationParams.removeWithOpacityEnabled) 1f - factor else 1f
                val mapObject = movedMarkers[i]
                val start = startCoordinates[i]

                val kx = clusterPoint.latitude - start.latitude
                val ky = clusterPoint.longitude - start.longitude

                val point = Point(start.latitude + (factor * kx),
                        start.longitude + (factor * ky))
                updateObjectGeometry(mapObject, point, capacity)
            }
        }
        animator.addUpdateListener(animatorUpdateListener)
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(anim: Animator?) {
                removePlacemarks(markers)
                animator.removeUpdateListener(animatorUpdateListener)
                animator.removeListener(this)
            }

            override fun onAnimationStart(animation: Animator?) {
                createPlacemark(cluster)
            }
        })
        return animator
    }

    //Перемещение маркеров в кластер без анимации
    private fun setMarkersToCluster(cluster: Marker, markers: Set<Marker>) {
        createPlacemark(cluster)
        removePlacemarks(markers)
    }

    //Вызывается для перемещения маркеров из кластера
    private fun clusterToMarkers(cluster: Marker, markers: Set<Marker>) {
        if (animationParams.animationEnabled) {
            animateClusterToMarkers(cluster, markers)
        } else {
            setClusterToMarkers(cluster, markers)
        }
    }

    //Перемещение маркеров из кластера с анимацией
    private fun animateClusterToMarkers(cluster: Marker, markers: Set<Marker>): Animator {
        removePlacemark(cluster)
        //коллекция маркеров которые будут анимироваться в кластер
        val movedMarkers = mutableListOf<PlacemarkMapObject>()
        val to = mutableListOf<Point>()
        val clusterPoint = cluster.getGeoCoor()
        markers.forEach {
            to.add(it.getGeoCoor().toPoint())
        }
        val animator = ValueAnimator.ofFloat(0f, 1f)
        val animatorUpdateListener = ValueAnimator.AnimatorUpdateListener {
            val factor = it.animatedValue as Float
            for (i in 0 until movedMarkers.size) {
                val mapObject = movedMarkers[i]
                val point = to[i]
                val kx = point.latitude - clusterPoint.latitude
                val ky = point.longitude - clusterPoint.longitude
                val lat = clusterPoint.latitude + (factor * kx)
                val lon = clusterPoint.longitude + (factor * ky)
                updateObjectGeometry(mapObject, Point(lat, lon))
            }
        }
        animator.addUpdateListener(animatorUpdateListener)
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(anim: Animator?) {
                animator.removeUpdateListener(animatorUpdateListener)
                animator.removeListener(this)
            }

            override fun onAnimationStart(animation: Animator?) {
                markers.forEach {
                    movedMarkers.add(createPlacemark(it, cluster.getGeoCoor()))
                }
            }
        })
        return animator
    }

    //Перемещение маркеров из кластера без анимации
    private fun setClusterToMarkers(cluster: Marker, markers: Set<Marker>) {
        removePlacemark(cluster)
        markers.forEach { createPlacemark(it) }
    }

    private fun removePlacemarks(markers: Set<Marker>) {
        for (marker in markers) {
            removePlacemark(marker)
        }
    }

    private fun removePlacemark(marker: Marker) {
        mapObjects[marker]?.let {
            if (it.isValid) {
                layer.remove(it)
                mapObjects.remove(marker)
            }
        }
    }

    private fun createPlacemark(marker: Marker): PlacemarkMapObject {
        return createPlacemark(marker, marker.getGeoCoor())
    }

    private fun createPlacemark(marker: Marker, coords: LatLng): PlacemarkMapObject {
        removePlacemark(marker)
        val image = imageProvider.get(marker)
        val placemark = layer.addPlacemark(coords, image.provider(), image.style)
        mapObjects[marker] = placemark
        return placemark
    }

    private fun updateObjectGeometry(mapObject: PlacemarkMapObject, point: Point,
                                     opacity: Float = 1f) {
        if (mapObject.isValid) {
            mapObject.geometry = point
            mapObject.opacity = opacity
        }
    }
}
