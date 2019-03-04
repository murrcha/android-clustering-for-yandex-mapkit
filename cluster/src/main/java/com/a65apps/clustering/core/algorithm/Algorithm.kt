package com.a65apps.clustering.core.algorithm

import com.a65apps.clustering.core.Marker
import com.a65apps.clustering.core.VisibleRectangularRegion

interface Algorithm {
    fun addMarker(marker: Marker)
    fun addMarkers(markers: Collection<Marker>)
    fun clearMarkers()
    fun removeMarker(marker: Marker)
    fun getMarkers(): Collection<Marker>
    fun calculate(visibleRectangularRegion: VisibleRectangularRegion): Set<Marker>
    fun setRatioForClustering(value: Float)
}
