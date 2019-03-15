package com.a65apps.clustering.core.algorithm

import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.VisibleRectangularRegion

interface Algorithm {
    fun addMarker(cluster: Cluster)
    fun addMarkers(clusters: Collection<Cluster>)
    fun clearMarkers()
    fun removeMarker(cluster: Cluster)
    fun removeMarkers(clusters: Collection<Cluster>)
    fun getMarkers(): Collection<Cluster>
    fun calculate(visibleRectangularRegion: VisibleRectangularRegion): Set<Cluster>
    fun setRatioForClustering(value: Float)
}
