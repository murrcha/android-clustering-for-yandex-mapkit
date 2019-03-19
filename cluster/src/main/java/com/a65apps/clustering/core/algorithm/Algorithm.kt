package com.a65apps.clustering.core.algorithm

import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.VisibleRectangularRegion

interface Algorithm {
    fun addItem(item: Cluster)
    fun addItems(items: Collection<Cluster>)
    fun clearItems()
    fun removeItem(item: Cluster)
    fun removeItems(items: Collection<Cluster>)
    fun calculate(visibleRectangularRegion: VisibleRectangularRegion): Set<Cluster>
    fun setRatioForClustering(value: Float)
}
