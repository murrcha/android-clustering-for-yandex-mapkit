package com.a65apps.clustering.core

data class ClusteredMarker(private val geoCoor: LatLng, private val payload: Any?,
                           val rawMarkers: Set<Marker> = emptySet()) : Marker {
    override fun getGeoCoor(): LatLng = geoCoor

    override fun getPayload(): Any? = payload

    override fun isCluster(): Boolean = rawMarkers.isNotEmpty()

    override fun getChildrenCount(): Int = rawMarkers.size
}
