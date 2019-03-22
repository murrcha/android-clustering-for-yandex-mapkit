package com.a65apps.clustering.core

import com.a65apps.clustering.core.algorithm.Algorithm
import com.a65apps.clustering.core.algorithm.DefaultAlgorithmParameter
import com.a65apps.clustering.core.view.ClusterRenderer
import com.a65apps.clustering.core.view.RenderConfig
import kotlinx.coroutines.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

open class DefaultClusterManager<in C : RenderConfig>(
        private val renderer: ClusterRenderer<C>,
        private var algorithm: Algorithm<DefaultAlgorithmParameter>,
        private var algorithmParameter: DefaultAlgorithmParameter) : ClusterManager {
    init {
        renderer.onAdd()
    }

    private val algorithmLock = ReentrantReadWriteLock()
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private var calculateJob: Job? = null

    fun calculateClusters(algorithmParameter: DefaultAlgorithmParameter) {
        this.algorithmParameter = algorithmParameter
        calculateClusters()
    }

    private fun calculateClusters() {
        calculateJob?.cancel()
        calculateJob = uiScope.launch {
            val newItems = withContext(Dispatchers.Default) {
                calculateNewItems()
            }
            callRenderer(newItems)
        }
    }

    private suspend fun calculateNewItems(): Set<Cluster> {
        return coroutineScope {
            algorithm.calculate(algorithmParameter)
        }
    }

    override fun clearItems() {
        algorithmLock.writeLock().withLock {
            algorithm.clearItems()
            onModifyRawClusters()
        }
    }

    override fun setItems(clusters: Set<Cluster>) {
        algorithmLock.writeLock().withLock {
            algorithm.addItems(clusters)
            onModifyRawClusters()
        }
    }

    override fun addItem(cluster: Cluster) {
        algorithmLock.writeLock().withLock {
            algorithm.addItem(cluster)
            onModifyRawClusters()
        }
    }

    override fun removeItem(cluster: Cluster) {
        algorithmLock.writeLock().withLock {
            algorithm.removeItem(cluster)
            onModifyRawClusters()
        }
    }

    override fun addItems(clusters: Set<Cluster>) {
        algorithmLock.writeLock().withLock {
            algorithm.addItems(clusters)
            onModifyRawClusters()
        }
    }

    override fun removeItems(clusters: Set<Cluster>) {
        algorithmLock.writeLock().withLock {
            algorithm.removeItems(clusters)
            onModifyRawClusters()
        }
    }

    private fun callRenderer(newClusters: Set<Cluster>) {
        renderer.updateClusters(newClusters)
    }

    private fun onModifyRawClusters() {
        calculateClusters()
    }
}
