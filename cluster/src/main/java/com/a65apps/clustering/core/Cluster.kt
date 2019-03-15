package com.a65apps.clustering.core

interface Cluster {
    fun geoCoor(): LatLng
    fun payload(): Any?
    fun size(): Int
    fun isCluster(): Boolean
    fun items(): Set<Cluster>
    fun addItem(cluster: Cluster): Boolean
    fun removeItem(cluster: Cluster): Boolean
}
