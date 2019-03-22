package com.a65apps.clustering.core.algorithm

import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.DefaultClusterProvider

open class CacheNonHierarchicalDistanceBasedAlgorithm(
        clusterProvider: ClusterProvider = DefaultClusterProvider()) :
        NonHierarchicalDistanceBasedAlgorithm(clusterProvider) {
    private val cacheResult = mutableMapOf<Int, Set<Cluster>>()

    override fun addItem(item: Cluster) {
        super.addItem(item)
        clearCache()
    }

    override fun addItems(items: Collection<Cluster>) {
        super.addItems(items)
        clearCache()
    }

    override fun removeItem(item: Cluster) {
        super.removeItem(item)
        clearCache()
    }

    override fun removeItems(items: Collection<Cluster>) {
        super.removeItems(items)
        clearCache()
    }

    override fun clearItems() {
        super.clearItems()
        clearCache()
    }

    override fun calculate(parameter: DefaultAlgorithmParameter): Set<Cluster> {
        val zoom = parameter.zoom
        return if (cacheResult.isNotEmpty() && cacheResult.containsKey(zoom)) {
            cacheResult[zoom]!!
        } else {
            val result = super.calculate(parameter)
            cacheResult[zoom] = result
            cacheResult[zoom]!!
        }
    }

    private fun clearCache() {
        if (cacheResult.isNotEmpty()) {
            cacheResult.clear()
        }
    }
}