package com.a65apps.clustering.core.algorithm

import android.util.Log
import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.DefaultClusterProvider

class CacheNonHierarchicalDistanceBasedAlgorithm(
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
        val zoom = parameter.zoom()
        return if (cacheResult.isNotEmpty() && cacheResult.containsKey(zoom)) {
            Log.d("CACHE", "Return result from cache")
            cacheResult[zoom]!!
        } else {
            Log.d("CACHE", "Calculate result, save to cache and return from cache")
            val result = super.calculate(parameter)
            cacheResult[zoom] = result
            cacheResult[zoom]!!
        }
    }

    private fun clearCache() {
        if (cacheResult.isNotEmpty()) {
            Log.d("CACHE", "CLEAR CACHE")
            cacheResult.clear()
        }
    }
}