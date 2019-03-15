package com.a65apps.clustering.core

data class ClustersDiff(val actualClusters: Set<Cluster>,
                        val newClusters: Set<Cluster> = emptySet(),
                        val transitions: Map<Cluster, Set<Cluster>> = emptyMap(),
                        val isCollapsing: Boolean = false) : MarkersDiff {
    override fun newMarkers(): Set<Cluster> {
        return newClusters
    }
}
