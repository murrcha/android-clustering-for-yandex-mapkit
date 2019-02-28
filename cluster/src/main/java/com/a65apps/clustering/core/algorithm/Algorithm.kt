package com.a65apps.clustering.core.algorithm

import com.a65apps.clustering.core.Clusters
import com.a65apps.clustering.core.Marker
import com.a65apps.clustering.core.VisibleRectangularRegion

interface Algorithm {
    fun addItem(item: Marker)
    fun addItems(items: Collection<Marker>)
    fun clearItems()
    fun removeItem(item: Marker)
    fun getItems(): Collection<Marker>
    fun calculate(visibleRectangularRegion: VisibleRectangularRegion): Clusters
}
