package com.a65apps.clustering.core

/**
 * Difference between current clusters group and new clusters group
 */
interface ClustersDiff {
    fun newClusters(): Set<Cluster>
    fun currentClusters(): Set<Cluster>
    /**
     * Represents set of changes as expanding one cluster to group of clusters, or
     * collapsing group of clusters to one cluster
     */
    fun transitions(): Map<Cluster, Set<Cluster>>
    fun collapsing(): Boolean
}
