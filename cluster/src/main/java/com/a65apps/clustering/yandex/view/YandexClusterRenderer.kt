package com.a65apps.clustering.yandex.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.ClustersDiff
import com.a65apps.clustering.core.LatLng
import com.a65apps.clustering.core.view.ClusterRenderer
import com.a65apps.clustering.yandex.extention.addPlacemark
import com.a65apps.clustering.yandex.extention.toLatLng
import com.a65apps.clustering.yandex.extention.toPoint
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map

class YandexClusterRenderer(map: Map,
                            private val imageProvider: ClusterPinProvider,
                            private var yandexRenderConfig: YandexRenderConfig,
                            private val mapObjectTapListener: TapListener? = null,
                            name: String = "CLUSTER_LAYER")
    : ClusterRenderer<ClustersDiff, YandexRenderConfig> {
    private val layer: MapObjectCollection = map.addMapObjectLayer(name)
    private val mapObjects = mutableMapOf<Cluster, PlacemarkMapObject>()
    private var clusterAnimator: AnimatorSet = AnimatorSet()
    private var tapListener = if (mapObjectTapListener != null) {
        object : MapObjectTapListener {
            override fun onMapObjectTap(mapObject: MapObject, point: Point): Boolean {
                for ((marker, markerMapObject) in mapObjects) {
                    if (mapObject == markerMapObject) {
                        mapObjectTapListener.clusterTapped(marker, markerMapObject)
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
        if (mapObjects.isEmpty() || !yandexRenderConfig.animationEnabled ||
                diffs.currentClusters().isEmpty() || diffs.newClusters().isEmpty()) {
            simpleUpdate(diffs.newClusters())
        } else {
            val transitions = diffs.transitions()
            clusterAnimator.cancel()
            clusterAnimator = AnimatorSet()
            if (diffs.collapsing()) {
                for ((cluster, markers) in transitions) {
                    clusterAnimator.play(animateMarkersToCluster(cluster, markers))
                }
            } else {
                for ((cluster, markers) in transitions) {
                    clusterAnimator.play(animateClusterToMarkers(cluster, markers))
                }
            }
            clusterAnimator.duration = yandexRenderConfig.duration
            clusterAnimator.interpolator = yandexRenderConfig.interpolator
            clusterAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    checkPins(diffs.newClusters())
                    clusterAnimator.removeListener(this)
                }
            })
            clusterAnimator.start()
        }
    }

    private fun checkPins(clusters: Set<Cluster>) {
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
    }

    override fun setClusters(clusters: Set<Cluster>) {
        mapObjects.clear()
        layer.clear()
        clusters.forEach {
            createPlacemark(it)
        }
    }

    override fun config(renderConfig: YandexRenderConfig) {
        this.yandexRenderConfig = renderConfig
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

    private fun simpleUpdate(newClusters: Set<Cluster>) {
        layer.clear()
        for (cluster in newClusters) {
            createPlacemark(cluster)
        }
    }

    //Вызывается для перемещения маркеров в кластер
    private fun markersToCluster(cluster: Cluster, clusters: Set<Cluster>) {
        if (yandexRenderConfig.animationEnabled) {
            animateMarkersToCluster(cluster, clusters)
        } else {
            setMarkersToCluster(cluster, clusters)
        }
    }

    //Перемещение маркеров в кластер с анимацией
    private fun animateMarkersToCluster(cluster: Cluster, clusters: Set<Cluster>): Animator {
        //коллекция маркеров которые будут анимироваться в кластер
        val movedMarkers = mutableListOf<PlacemarkMapObject>()
        val startCoordinates = mutableListOf<LatLng>()
        clusters.forEach { marker ->
            mapObjects[marker]?.let { mapObject ->
                movedMarkers.add(mapObject)
                startCoordinates.add(mapObject.geometry.toLatLng())
            }
        }
        val clusterPoint = cluster.geoCoor().toPoint()
        val animator = ValueAnimator.ofFloat(0f, 1f)
        val animatorUpdateListener = ValueAnimator.AnimatorUpdateListener {
            val factor = it.animatedValue as Float
            for (i in 0 until movedMarkers.size) {
                val capacity = if (yandexRenderConfig.removeWithOpacityEnabled) 1f - factor else 1f
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
                removePlacemarks(clusters)
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
    private fun setMarkersToCluster(cluster: Cluster, clusters: Set<Cluster>) {
        createPlacemark(cluster)
        removePlacemarks(clusters)
    }

    //Перемещение маркеров из кластера с анимацией
    private fun animateClusterToMarkers(cluster: Cluster, clusters: Set<Cluster>): Animator {
        removePlacemark(cluster)
        //коллекция маркеров которые будут анимироваться в кластер
        val movedMarkers = mutableListOf<PlacemarkMapObject>()
        val to = mutableListOf<Point>()
        val clusterPoint = cluster.geoCoor()
        clusters.forEach {
            to.add(it.geoCoor().toPoint())
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
                clusters.forEach {
                    movedMarkers.add(createPlacemark(it, cluster.geoCoor()))
                }
            }
        })
        return animator
    }

    private fun removePlacemarks(clusters: Set<Cluster>) {
        for (cluster in clusters) {
            removePlacemark(cluster)
        }
    }

    private fun removePlacemark(cluster: Cluster) {
        mapObjects[cluster]?.let {
            if (it.isValid) {
                layer.remove(it)
                mapObjects.remove(cluster)
            }
        }
    }

    private fun createPlacemark(cluster: Cluster): PlacemarkMapObject {
        return createPlacemark(cluster, cluster.geoCoor())
    }

    private fun createPlacemark(cluster: Cluster, latLng: LatLng): PlacemarkMapObject {
        removePlacemark(cluster)
        val image = imageProvider.get(cluster)
        val placemark = layer.addPlacemark(latLng, image.provider(), image.style)
        mapObjects[cluster] = placemark
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
