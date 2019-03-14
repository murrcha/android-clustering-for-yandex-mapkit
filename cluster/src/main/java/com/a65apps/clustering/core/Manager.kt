package com.a65apps.clustering.core

interface Manager {
    fun setMarkers(markers: Set<Marker>)
    fun clearMarkers()
    fun addMarker(marker: Marker)
    fun removeMarker(marker: Marker)
    fun addMarkers(markers: Set<Marker>)
    fun removeMarkers(markers: Set<Marker>)
}
