package com.a65apps.clustering.core

interface Marker {
    fun getGeoCoor(): LatLng
    fun getPayload(): Any?
    fun isCluster(): Boolean
    fun getChildrenCount(): Int
    fun childrens(): Set<Marker>
    fun contains(marker: Marker): Boolean
}
