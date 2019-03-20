package com.a65apps.clustering.core

import com.a65apps.clustering.core.algorithm.Algorithm
import com.a65apps.clustering.core.view.ClusterRenderer
import com.a65apps.clustering.core.view.RenderConfig
import kotlinx.coroutines.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

open class DefaultClusterManager<in C : RenderConfig>(
        private val renderer: ClusterRenderer<DefaultClustersDiff, C>,
        private var algorithm: Algorithm,
        private var visibleRect: VisibleRect) : ClusterManager {
    init {
        renderer.onAdd()
    }

    private var currentClusters = mutableSetOf<Cluster>()
    private val algorithmLock = ReentrantReadWriteLock()
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private var calculateJob: Job? = null

    fun calculateClusters(visibleRect: VisibleRect) {
        this.visibleRect = visibleRect
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
            if (clustersChanged(newClusters)) {
                DefaultClustersDiff(currentClusters, newClusters)
            } else {
                null
            }
        }
    }

    private fun clustersChanged(newClusters: Set<Cluster>): Boolean {
        val currentClustersCount = clusterCount(currentClusters)
        val newClusterCount = clusterCount(newClusters)
        return currentClustersCount != newClusterCount ||
                currentClusters.size != newClusters.size
    }

    override fun clearItems() {
        algorithmLock.writeLock().withLock {
            algorithm.clearItems()
            onModifyRawClusters(true)
        }
    }

    override fun setItems(clusters: Set<Cluster>) {
        algorithmLock.writeLock().withLock {
            algorithm.addItems(clusters)
            calculateClusters()
        }
    }

    override fun addItem(cluster: Cluster) {
        algorithmLock.writeLock().withLock {
            algorithm.addItem(cluster)
            onModifyRawClusters(false)
        }
    }

    override fun removeItem(cluster: Cluster) {
        algorithmLock.writeLock().withLock {
            algorithm.removeItem(cluster)
            onModifyRawClusters(false)
        }
    }

    override fun addItems(clusters: Set<Cluster>) {
        algorithmLock.writeLock().withLock {
            algorithm.addItems(clusters)
            onModifyRawClusters(false)
        }
    }

    override fun removeItems(clusters: Set<Cluster>) {
        algorithmLock.writeLock().withLock {
            algorithm.removeItems(clusters)
            onModifyRawClusters(false)
        }
    }

    private fun updateClusters(): Set<Cluster> = algorithm.calculate(visibleRect)

    private fun callRenderer(diffs: DefaultClustersDiff) {
        renderer.updateClusters(diffs)
        currentClusters.clear()
        currentClusters.addAll(diffs.newClusters())
    }

    private fun onModifyRawClusters(isClear: Boolean) {
        currentClusters.clear()
        if (!isClear) {
            currentClusters.addAll(updateClusters())
        }
        renderer.setClusters(currentClusters)
    }

    private fun clusterCount(clusters: Set<Cluster>): Int = clusters.filter {
        it.isCluster()
    }.count()
}
