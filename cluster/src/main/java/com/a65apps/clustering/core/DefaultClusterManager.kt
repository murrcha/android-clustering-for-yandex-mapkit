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
            var diffs: DefaultClustersDiff? = null
            if (actualClusterCount != newClusterCount ||
                    actualClusters.size != newClusters.size) {
                diffs = DefaultClustersDiff(actualClusters, newClusters)
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
