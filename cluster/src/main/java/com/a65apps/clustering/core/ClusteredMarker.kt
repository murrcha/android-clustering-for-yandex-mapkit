package com.a65apps.clustering.core

data class ClusteredMarker(private val geoCoor: LatLng, private val payload: Any? = null) : Marker {
    val rawMarkers: MutableSet<Marker> = mutableSetOf()

    override fun getGeoCoor(): LatLng = geoCoor

    override fun getPayload(): Any? = payload

    override fun isCluster(): Boolean = rawMarkers.isNotEmpty()

    override fun getChildrenCount(): Int = rawMarkers.size

    //TODO возможно потребуется явно определить методы equal() и hashcode()
}
