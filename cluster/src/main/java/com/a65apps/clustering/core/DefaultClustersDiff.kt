package com.a65apps.clustering.core

data class DefaultClustersDiff(val currentClusters: Set<Cluster>,
                               val newClusters: Set<Cluster> = emptySet(),
                               val transitions: Map<Cluster, Set<Cluster>> = emptyMap(),
                               val isCollapsing: Boolean = false) : ClustersDiff {
    override fun transitions(): Map<Cluster, Set<Cluster>> = transitions

    override fun collapsing(): Boolean = isCollapsing

    override fun newClusters(): Set<Cluster> = newClusters

    override fun currentClusters(): Set<Cluster> = currentClusters
}
