package com.a65apps.clustering.core

import com.a65apps.clustering.core.algorithm.Algorithm
import com.a65apps.clustering.core.view.ClusterRenderer
import com.a65apps.clustering.core.view.RenderConfig
import kotlinx.coroutines.*
import java.util.concurrent.locks.ReentrantReadWriteLock

open class DefaultClusterClusterManager<in C : RenderConfig>(
        private val renderer: ClusterRenderer<ClustersDiff, C>,
        private var algorithm: Algorithm,
        private var visibleRectangularRegion: VisibleRectangularRegion) : ClusterManager {
    init {
        renderer.onAdd()
    }

    private var actualClusters: MutableSet<Cluster> = mutableSetOf()
    private val algorithmLock = ReentrantReadWriteLock()
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private var calculateJob: Job? = null

    fun getNewMarker(geoCoor: LatLng, payload: Any? = null): Cluster =
            DefaultCluster(geoCoor, payload)

    fun calculateClusters(visibleRectangularRegion: VisibleRectangularRegion) {
        this.visibleRectangularRegion = visibleRectangularRegion
        calculateJob?.cancel()
        calculateJob = calculateClusters()
    }

    private fun calculateClusters() = uiScope.launch {
        val diffs = withContext(Dispatchers.Default) {
            calcDiffs()
        }
        diffs?.let {
            callRenderer(diffs)
        }
    }

    private suspend fun calcDiffs(): ClustersDiff? {
        return coroutineScope {
            val newMarkers = updateClusters()
            val actualClusterCount = clusterCount(actualClusters)
            val newClusterCount = clusterCount(newMarkers)
            val isCollapsing = newMarkers.size <= actualClusters.size
            var diffs: ClustersDiff? = null
            if (actualClusterCount != newClusterCount ||
                    actualClusters.size != newMarkers.size) {
                diffs = buildTransitionMap(actualClusters, newMarkers, isCollapsing)
            }
            diffs
        }
    }

    override fun clearMarkers() {
        algorithmLock.writeLock().lock()
        try {
            algorithm.clearMarkers()
            onModifyRawMarkers(true)
        } finally {
            algorithmLock.writeLock().unlock()
        }
    }

    override fun setMarkers(clusters: Set<Cluster>) {
        algorithmLock.writeLock().lock()
        try {
            algorithm.addMarkers(clusters)
            calculateClusters()
        } finally {
            algorithmLock.writeLock().unlock()
        }
    }

    override fun addMarker(cluster: Cluster) {
        algorithmLock.writeLock().lock()
        try {
            algorithm.addMarker(cluster)
            onModifyRawMarkers(false)
        } finally {
            algorithmLock.writeLock().unlock()
        }
    }

    override fun removeMarker(cluster: Cluster) {
        algorithmLock.writeLock().lock()
        try {
            algorithm.removeMarker(cluster)
            onModifyRawMarkers(false)
        } finally {
            algorithmLock.writeLock().unlock()
        }
    }

    override fun addMarkers(clusters: Set<Cluster>) {
        algorithmLock.writeLock().lock()
        try {
            algorithm.addMarkers(clusters)
            onModifyRawMarkers(false)
        } finally {
            algorithmLock.writeLock().unlock()
        }
    }

    override fun removeMarkers(clusters: Set<Cluster>) {
        algorithmLock.writeLock().lock()
        try {
            algorithm.removeMarkers(clusters)
            onModifyRawMarkers(false)
        } finally {
            algorithmLock.writeLock().unlock()
        }
    }

    private fun updateClusters(): Set<Cluster> {
        return algorithm.calculate(visibleRectangularRegion)
    }

    private fun callRenderer(diffs: ClustersDiff) {
        renderer.updateClusters(diffs)
        actualClusters.clear()
        actualClusters.addAll(diffs.newMarkers())
    }

    private fun onModifyRawMarkers(isClear: Boolean) {
        actualClusters.clear()
        if (!isClear) {
            actualClusters.addAll(updateClusters())
        }
        renderer.setMarkers(actualClusters)
    }

    private fun buildTransitionMap(actualClusters: Set<Cluster>,
                                   newClusters: Set<Cluster>,
                                   isCollapsing: Boolean): ClustersDiff {
        val transitionMap = mutableMapOf<Cluster, Set<Cluster>>()
        if (actualClusters.isEmpty() || newClusters.isEmpty()) {
            return ClustersDiff(actualClusters, newClusters, transitionMap, isCollapsing)
        }
        val src = if (isCollapsing) newClusters else actualClusters
        val dst = if (isCollapsing) actualClusters else newClusters
        for (marker in dst) {
            if (src.contains(marker)) {
                continue
            }
            val closest = findClosestCluster(marker, src)
            transitionMap[closest] = transitionMap[closest]?.plus(marker) ?: setOf(marker)
        }
        return ClustersDiff(actualClusters, newClusters, transitionMap, isCollapsing)
    }

    private fun findClosestCluster(cluster: Cluster, clusters: Set<Cluster>): Cluster {
        var minDistance = Double.MAX_VALUE
        var clusterCandidate: Cluster? = null
        var firstCluster: Cluster? = null
        for (mapObjectMarker in clusters) {
            if (firstCluster == null) {
                firstCluster = mapObjectMarker
            }
            if (mapObjectMarker.isCluster()) {
                val distance = distanceBetween(mapObjectMarker, cluster)
                if (clusterCandidate == null || distance < minDistance) {
                    minDistance = distance
                    clusterCandidate = mapObjectMarker
                }
            }
        }
        if (clusterCandidate == null) {
            clusterCandidate = firstCluster
        }
        return clusterCandidate!!
    }

    private fun distanceBetween(a: Cluster, b: Cluster): Double {
        return (a.geoCoor().latitude - b.geoCoor().latitude) *
                (a.geoCoor().latitude - b.geoCoor().latitude) +
                (a.geoCoor().longitude - b.geoCoor().longitude) *
                (a.geoCoor().longitude - b.geoCoor().longitude)
    }

    private fun clusterCount(clusters: Set<Cluster>): Int {
        var count = 0
        for (marker in clusters) {
            if (marker.isCluster()) {
                count++
            }
        }
        return count
    }
}
