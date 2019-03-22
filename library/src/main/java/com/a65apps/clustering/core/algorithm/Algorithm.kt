package com.a65apps.clustering.core.algorithm

import com.a65apps.clustering.core.Cluster

interface Algorithm<P> {
    fun addItem(item: Cluster)
    fun addItems(items: Collection<Cluster>)
    fun clearItems()
    fun removeItem(item: Cluster)
    fun removeItems(items: Collection<Cluster>)
    fun calculate(parameter: P): Set<Cluster>
    fun setRatioForClustering(value: Float)
}
