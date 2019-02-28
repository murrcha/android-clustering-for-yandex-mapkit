package com.a65apps.clustering.core

interface Cluster<T : ClusterItem> {
    fun position(): Point
    fun items(): Collection<T>
    fun size(): Int
}
