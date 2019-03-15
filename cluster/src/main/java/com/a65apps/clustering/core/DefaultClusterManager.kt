package com.a65apps.clustering.core

import com.a65apps.clustering.core.algorithm.Algorithm
import com.a65apps.clustering.core.view.ClusterRenderer
import com.a65apps.clustering.core.view.RenderConfig
import kotlinx.coroutines.*
import java.util.concurrent.locks.ReentrantReadWriteLock

open class DefaultClusterManager<in C : RenderConfig>(
        private val renderer: ClusterRenderer<DefaultClustersDiff, C>,
        private var algorithm: Algorithm,
        private var visibleRectangularRegion: VisibleRectangularRegion) : ClusterManager {
    init {
        renderer.onAdd()
    }

    private var actualClusters: MutableSet<Cluster> = mutableSetOf()
    private val algorithmLock = ReentrantReadWriteLock()
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private var calculateJob: Job? = null

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

    private suspend fun calcDiffs(): DefaultClustersDiff? {
        return coroutineScope {
            val newClusters = updateClusters()
            val actualClusterCount = clusterCount(actualClusters)
            val newClusterCount = clusterCount(newClusters)
            val isCollapsing = newClusters.size <= actualClusters.size
            var diffs: DefaultClustersDiff? = null
            if (actualClusterCount != newClusterCount ||
                    actualClusters.size != newClusters.size) {
                diffs = buildTransitionMap(actualClusters, newClusters, isCollapsing)
            }
            diffs
        }
    }

    override fun clearItems() {
        algorithmLock.writeLock().lock()
        try {
            algorithm.clearItems()
            onModifyRawClusters(true)
        } finally {
            algorithmLock.writeLock().unlock()
        }
    }

    override fun setItems(clusters: Set<Cluster>) {
        algorithmLock.writeLock().lock()
        try {
            algorithm.addItems(clusters)
            calculateClusters()
        } finally {
            algorithmLock.writeLock().unlock()
        }
    }

    override fun addItem(cluster: Cluster) {
        algorithmLock.writeLock().lock()
        try {
            algorithm.addItem(cluster)
            onModifyRawClusters(false)
        } finally {
            algorithmLock.writeLock().unlock()
        }
    }

    override fun removeItem(cluster: Cluster) {
        algorithmLock.writeLock().lock()
        try {
            algorithm.removeItem(cluster)
            onModifyRawClusters(false)
        } finally {
            algorithmLock.writeLock().unlock()
        }
    }

    override fun addItems(clusters: Set<Cluster>) {
        algorithmLock.writeLock().lock()
        try {
            algorithm.addItems(clusters)
            onModifyRawClusters(false)
        } finally {
            algorithmLock.writeLock().unlock()
        }
    }

    override fun removeItems(clusters: Set<Cluster>) {
        algorithmLock.writeLock().lock()
        try {
            algorithm.removeItems(clusters)
            onModifyRawClusters(false)
        } finally {
            algorithmLock.writeLock().unlock()
        }
    }

    private fun updateClusters(): Set<Cluster> {
        return algorithm.calculate(visibleRectangularRegion)
    }

    private fun callRenderer(diffs: DefaultClustersDiff) {
        renderer.updateClusters(diffs)
        actualClusters.clear()
        actualClusters.addAll(diffs.newClusters())
    }

    private fun onModifyRawClusters(isClear: Boolean) {
        actualClusters.clear()
        if (!isClear) {
            actualClusters.addAll(updateClusters())
        }
        renderer.setClusters(actualClusters)
    }

    private fun buildTransitionMap(actualClusters: Set<Cluster>,
                                   newClusters: Set<Cluster>,
                                   isCollapsing: Boolean): DefaultClustersDiff {
        val transitionMap = mutableMapOf<Cluster, Set<Cluster>>()
        if (actualClusters.isEmpty() || newClusters.isEmpty()) {
            return DefaultClustersDiff(actualClusters, newClusters, transitionMap, isCollapsing)
        }
        val src = if (isCollapsing) newClusters else actualClusters
        val dst = if (isCollapsing) actualClusters else newClusters
        for (cluster in dst) {
            if (src.contains(cluster)) {
                continue
            }
            val closest = findClosestCluster(cluster, src)
            transitionMap[closest] = transitionMap[closest]?.plus(cluster) ?: setOf(cluster)
        }
        return DefaultClustersDiff(actualClusters, newClusters, transitionMap, isCollapsing)
    }

    private fun findClosestCluster(cluster: Cluster, clusters: Set<Cluster>): Cluster {
        var minDistance = Double.MAX_VALUE
        var clusterCandidate: Cluster? = null
        var firstCluster: Cluster? = null
        for (mapObjectCluster in clusters) {
            if (firstCluster == null) {
                firstCluster = mapObjectCluster
            }
            if (mapObjectCluster.isCluster()) {
                val distance = distanceBetween(mapObjectCluster, cluster)
                if (clusterCandidate == null || distance < minDistance) {
                    minDistance = distance
                    clusterCandidate = mapObjectCluster
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
        for (cluster in clusters) {
            if (cluster.isCluster()) {
                count++
            }
        }
        return count
    }
}
