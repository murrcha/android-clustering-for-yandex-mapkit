package com.a65apps.clustering.core

interface Cluster<T : ClusterItem> {
    fun position(): Positionable
    fun items(): Collection<T>
    fun size(): Int
}