package com.a65apps.clustering.core.algorithm

import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.VisibleRect

interface Algorithm {
    fun addItem(item: Cluster)
    fun addItems(items: Collection<Cluster>)
    fun clearItems()
    fun removeItem(item: Cluster)
    fun removeItems(items: Collection<Cluster>)
    fun calculate(visibleRect: VisibleRect): Set<Cluster>
    fun setRatioForClustering(value: Float)
}
