package com.a65apps.clustering.core

interface Cluster<T : ClusterItem> {
    fun position(): LatLng
    fun items(): Collection<T>
    fun size(): Int
}