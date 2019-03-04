package com.a65apps.clustering.yandex.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.util.Log
import com.a65apps.clustering.core.Clusters
import com.a65apps.clustering.core.LatLng
import com.a65apps.clustering.core.Marker
import com.a65apps.clustering.core.view.ClusterRenderer
import com.a65apps.clustering.yandex.extention.addPlacemark
import com.a65apps.clustering.yandex.extention.toPoint
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.PlacemarkMapObject
import java.lang.ref.WeakReference

class YandexClusterRenderer(
        map: Map,
        private val imageProvider: ClusterPinProvider,
        private var withAnimation: Boolean = false)
    : ClusterRenderer {
    private val layer: MapObjectCollection = map.addMapObjectLayer(LAYER_NAME)
    private val mapObjects = mutableMapOf<Marker, WeakReference<PlacemarkMapObject>>()

    companion object {
        const val LAYER_NAME = "CLUSTER_LAYER"
    }

    override fun updateClusters(clusters: Clusters) {
        Log.d("transitions", "-----------------------------------------------------------")
        Log.d("transitions", "isCollapsing = " + clusters.isCollapsed)
        for (entry in clusters.transitions) {
            Log.d("transition", "cluster " + entry.key)
            Log.d("transition", "points " + entry.value)
        }
        Log.d("transitions", "-----------------------------------------------------------")
        if (!withAnimation || clusters.actualMarkers.isEmpty()) {
            simpleUpdate(clusters)
        }

        /*if (clusters.actualMarkers.isEmpty()) {
            for (marker in clusters.newMarkers) {
                createPlacemark(marker)
            }
        } else {
            val isCollapsing = clusters.isCollapsed
            clusters.transitions.forEach { entry ->
                if (isCollapsing) {
                    markersToCluster(entry.key, entry.value)
                } else {
                    clusterToMarkers(entry.key, entry.value)
                }
            }
        }*/
    }

    override fun setMarkers(markers: Set<Marker>) {
        markers.forEach {
            createPlacemark(it)
        }
    }

    override fun animation(withAnimation: Boolean) {
        this.withAnimation = withAnimation
    }

    override fun onAdd() {
        //проброс ЖЦ
    }

    override fun onRemove() {
        //проброс ЖЦ
    }

    private fun simpleUpdate(clusters: Clusters) {
        layer.clear()
        for (marker in clusters.newMarkers) {
            createPlacemark(marker)
        }
    }

    //Вызывается для перемещения маркеров в кластер
    private fun markersToCluster(cluster: Marker, markers: Set<Marker>) {
        if (withAnimation) {
            animateMarkersToClusterWith(cluster, markers)
        } else {
            setMarkersToCluster(cluster, markers)
        }
    }

    //Перемещение маркеров в кластер с анимацией
    private fun animateMarkersToClusterWith(cluster: Marker, markers: Set<Marker>) {
        //коллекция маркеров которые будут анимироваться в кластер
        val movedMarkers = mutableListOf<PlacemarkMapObject>()
        val startCoordinates = mutableListOf<LatLng>()
        markers.forEach {
            movedMarkers.add(getOrCreatePlacemark(it))
            startCoordinates.add(it.getGeoCoor())
        }
        val clusterPoint = cluster.getGeoCoor().toPoint()

        val animator = ValueAnimator.ofFloat(0f, 1f)
        val animatorUpdateListener = ValueAnimator.AnimatorUpdateListener {
            val factor = it.animatedValue as Float
            for (i in 0 until movedMarkers.size) {
                val mapObject = movedMarkers[i]
                val start = startCoordinates[i]

                val kx = clusterPoint.latitude - start.latitude
                val ky = clusterPoint.longitude - start.longitude

                val point = Point(start.latitude + (factor * kx),
                        start.longitude + (factor * ky))
                updateObjectGeometry(mapObject, point)
            }
        }
        animator.addUpdateListener(animatorUpdateListener)
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                removePlacemarksIfExists(markers)
                animator.removeUpdateListener(animatorUpdateListener)
                animator.removeListener(this)
            }
        })
        animator.start()
    }

    //Перемещение маркеров в кластер без анимации
    private fun setMarkersToCluster(cluster: Marker, markers: Set<Marker>) {
        getOrCreatePlacemark(cluster)
        removePlacemarksIfExists(markers)
    }

    //Вызывается для перемещения маркеров из кластера
    private fun clusterToMarkers(cluster: Marker, markers: Set<Marker>) {
        if (withAnimation) {
            animateClusterToMarkers(cluster, markers)
        } else {
            setClusterToMarkers(cluster, markers)
        }
    }

    //Перемещение маркеров из кластера с анимацией
    private fun animateClusterToMarkers(cluster: Marker, markers: Set<Marker>) {
        removePlacemarkIfExists(cluster)

        //коллекция маркеров которые будут анимироваться в кластер
        val movedMarkers = mutableListOf<PlacemarkMapObject>()
        val to = mutableListOf<Point>()
        val clusterPoint = cluster.getGeoCoor()
        markers.forEach {
            movedMarkers.add(getOrCreatePlacemark(it))
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
            override fun onAnimationEnd(animation: Animator?) {
                animator.removeUpdateListener(animatorUpdateListener)
                animator.removeListener(this)
            }
        })
        animator.start()
    }

    //Перемещение маркеров из кластера без анимации
    private fun setClusterToMarkers(cluster: Marker, markers: Set<Marker>) {
        markers.forEach { getOrCreatePlacemark(it) }
        removePlacemarkIfExists(cluster)
    }

    private fun getOrCreatePlacemark(marker: Marker): PlacemarkMapObject {
        val mapObjectReference = mapObjects[marker]?.get()
        return mapObjectReference ?: createPlacemark(marker)
    }

    private fun removePlacemarksIfExists(markers: Set<Marker>) {
        markers.forEach { removePlacemarkIfExists(it) }
    }

    private fun removePlacemarkIfExists(marker: Marker) {
        mapObjects[marker]?.get()?.let {
            layer.remove(it)
            mapObjects.remove(marker)
        }
    }

    private fun createPlacemark(marker: Marker): PlacemarkMapObject {
        val image = imageProvider.get(marker)
        val placemark = layer.addPlacemark(marker, image.provider(), image.style)
        mapObjects[marker] = WeakReference(placemark)
        return placemark
    }

    private fun updateObjectGeometry(mapObject: PlacemarkMapObject, point: Point) {
        mapObject.geometry = point
    }
}
