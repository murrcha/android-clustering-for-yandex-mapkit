package com.a65apps.clustering.yandex.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.ClustersDiff
import com.a65apps.clustering.core.DefaultClustersDiff
import com.a65apps.clustering.core.LatLng
import com.a65apps.clustering.core.view.ClusterRenderer
import com.a65apps.clustering.yandex.extention.addPlacemark
import com.a65apps.clustering.yandex.extention.toLatLng
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map
import kotlinx.coroutines.*

open class YandexClusterRenderer(private val map: Map,
                                 private val imageProvider: ClusterPinProvider,
                                 private var yandexRenderConfig: YandexRenderConfig,
                                 private val mapObjectTapListener: TapListener? = null,
                                 name: String = "CLUSTER_LAYER")
    : ClusterRenderer<YandexRenderConfig> {
    private val layer: MapObjectCollection = map.addMapObjectLayer(name)
    private val currentClusters = mutableSetOf<Cluster>()
    private val mapObjects = mutableMapOf<Cluster, PlacemarkMapObject>()
    private var clusterAnimator = AnimatorSet()
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
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private var job: Job? = null

    override fun updateClusters(newClusters: Set<Cluster>) {
        if (!clustersChanged(newClusters)) {
            return
        }
        job?.cancel()
        if (isSimpleUpdate(newClusters)) {
            simpleUpdate(newClusters)
        } else {
            job = animateUpdate(newClusters)
        }
    }

    private fun animateUpdate(newClusters: Set<Cluster>) = uiScope.launch {
        val diffs = withContext(Dispatchers.Default) {
            coroutineScope {
                calcDiffs(newClusters)
            }
        }
        animateDiffs(diffs)
        updateCurrent(newClusters)
    }

    private fun animateDiffs(diffs: ClustersDiff) {
        val transitions = diffs.transitions()
        clusterAnimator.cancel()
        clusterAnimator = AnimatorSet()
        for ((cluster, markers) in transitions) {
            clusterAnimation(cluster, markers, diffs.collapsing())?.let {
                clusterAnimator.play(it)
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

    private fun clusterAnimation(cluster: Cluster, markers: Set<Cluster>,
                                 isCollapsing: Boolean): Animator? {
        return if (isCollapsing) {
            animateMarkersToCluster(cluster, markers)
        } else {
            animateClusterToMarkers(cluster, markers)
        }
    }

    private fun isSimpleUpdate(newClusters: Set<Cluster>): Boolean =
            mapObjects.isEmpty() || !yandexRenderConfig.animationEnabled ||
                    currentClusters.isEmpty() || newClusters.isEmpty()

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

    protected fun calcDiffs(newClusters: Set<Cluster>): ClustersDiff {
        return DefaultClustersDiff(currentClusters, newClusters)
    }

    private fun simpleUpdate(newClusters: Set<Cluster>) {
        layer.clear()
        for (cluster in newClusters) {
            createPlacemark(cluster)
        }
        updateCurrent(newClusters)
    }

    //Перемещение маркеров в кластер с анимацией
    private fun animateMarkersToCluster(cluster: Cluster, clusters: Set<Cluster>): Animator? {
        //коллекция маркеров которые будут анимироваться в кластер
        val movedMarkers = mutableMapOf<PlacemarkMapObject, LatLng>()
        //val startCoordinates = mutableListOf<LatLng>()
        val clusterPoint = cluster.geoCoor()
        clusters.forEach { marker ->
            mapObjects[marker]?.let { mapObject ->
                val start = mapObject.geometry.toLatLng()
                if (needAnimateTransition(start, clusterPoint)) {
                    movedMarkers[mapObject] = start
                }
            }
        }
        if (movedMarkers.isEmpty()) {
            return null
        }
        val animator = ValueAnimator.ofFloat(0f, 1f)
        val animatorUpdateListener = ValueAnimator.AnimatorUpdateListener {
            val factor = it.animatedValue as Float
            for ((mapObject, start) in movedMarkers) {
                val capacity = if (yandexRenderConfig.removeWithOpacityEnabled) 1f - factor else 1f

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

    //Перемещение маркеров из кластера с анимацией
    private fun animateClusterToMarkers(cluster: Cluster, clusters: Set<Cluster>): Animator? {
        //коллекция маркеров которые будут анимироваться в кластер
        val movedMarkers = mutableMapOf<PlacemarkMapObject, LatLng>()
        val clusterPoint = cluster.geoCoor()
        clusters.forEach {
            val end = it.geoCoor()
            if (needAnimateTransition(clusterPoint, end)) {
                movedMarkers[createPlacemark(it, clusterPoint)] = end
            }
        }
        if (movedMarkers.isEmpty()) {
            return null
        }
        val animator = ValueAnimator.ofFloat(0f, 1f)
        val animatorUpdateListener = ValueAnimator.AnimatorUpdateListener {
            val factor = it.animatedValue as Float
            for ((mapObject, point) in movedMarkers) {
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
                removePlacemark(cluster)
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

    private fun needAnimateTransition(start: LatLng, end: LatLng): Boolean {
        return !yandexRenderConfig.optimizeAnimations || pointInVisibleRegion(
                start) || pointInVisibleRegion(end)
    }

    private fun pointInVisibleRegion(point: LatLng): Boolean {
        val minLongitude = map.visibleRegion.topLeft.longitude
        val maxLongitude = map.visibleRegion.topRight.longitude
        val minLatitude = map.visibleRegion.bottomLeft.latitude
        val maxLatitude = map.visibleRegion.topLeft.latitude
        return point.longitude in minLongitude..maxLongitude &&
                point.latitude in minLatitude..maxLatitude
    }

    private fun clustersChanged(newClusters: Set<Cluster>): Boolean {
        val currentClustersCount = clusterCount(currentClusters)
        val newClusterCount = clusterCount(newClusters)
        return currentClustersCount != newClusterCount ||
                currentClusters.size != newClusters.size
    }

    private fun clusterCount(clusters: Set<Cluster>): Int = clusters
            .toMutableSet()
            .filter {
                it.isCluster()
            }.count()

    private fun updateCurrent(newClusters: Set<Cluster>) {
        synchronized(currentClusters) {
            currentClusters.clear()
            currentClusters.addAll(newClusters)
        }
    }
}
