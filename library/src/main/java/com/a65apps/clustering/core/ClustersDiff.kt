package com.a65apps.clustering.core

interface ClustersDiff {
    fun newClusters(): Set<Cluster>
    fun currentClusters(): Set<Cluster>
    fun transitions(): Map<Cluster, Set<Cluster>>
    fun collapsing(): Boolean
}
