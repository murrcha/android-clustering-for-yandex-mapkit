package com.a65apps.clustering.core

class ClusterManager {
    private var rawMarkers: MutableSet<Marker> = mutableSetOf()
    private var actualMarkers: Set<Marker> = emptySet()

    fun getNewMarker(geoCoor: LatLng, payload: Any? = null): Marker =
            ClusteredMarker(geoCoor, payload)

    fun cluster(visibleRectangularRegion: VisibleRectangularRegion) {
        //todo реализовать
    }

    fun clearItems() {
        //todo реализовать
    }

    fun setMarkers(markers: MutableSet<Marker>) {
        rawMarkers = markers
        //todo реализовать
    }

    fun addMarker(marker: Marker): Boolean {
        //todo реализовать
        return true
    }

    fun removeItem(marker: Marker): Boolean {
        //todo реализовать
        return true
    }
}
