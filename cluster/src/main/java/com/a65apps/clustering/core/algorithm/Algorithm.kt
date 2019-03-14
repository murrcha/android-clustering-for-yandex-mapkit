package com.a65apps.clustering.core.algorithm

import com.a65apps.clustering.core.Marker

interface Algorithm<P> {
    fun addMarker(marker: Marker)
    fun addMarkers(markers: Collection<Marker>)
    fun clearMarkers()
    fun removeMarker(marker: Marker)
    fun removeMarkers(markers: Collection<Marker>)
    fun getMarkers(): Collection<Marker>
    fun calculate(parameter: P): Set<Marker>
    fun setRatioForClustering(value: Float)
}
