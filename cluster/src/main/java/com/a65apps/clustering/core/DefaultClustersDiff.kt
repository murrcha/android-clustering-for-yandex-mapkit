package com.a65apps.clustering.core

data class DefaultClustersDiff(val currentClusters: Set<Cluster>,
                               val newClusters: Set<Cluster> = emptySet()) : ClustersDiff {
    private var isCollapsing: Boolean = newClusters.size <= currentClusters.size
    private var transitions: Map<Cluster, Set<Cluster>> =
            buildTransitionMap(currentClusters, newClusters)

    override fun transitions(): Map<Cluster, Set<Cluster>> {
        return transitions
    }

    override fun collapsing(): Boolean = isCollapsing

    override fun newClusters(): Set<Cluster> = newClusters

    override fun currentClusters(): Set<Cluster> = currentClusters

    private fun buildTransitionMap(actualClusters: Set<Cluster>,
                                   newClusters: Set<Cluster>): Map<Cluster, Set<Cluster>> {
        val transitionMap = mutableMapOf<Cluster, Set<Cluster>>()
        if (actualClusters.isEmpty() || newClusters.isEmpty()) {
            return transitionMap
        }
        val src = if (isCollapsing) newClusters else actualClusters
        val dst = if (isCollapsing) actualClusters else newClusters
        for (cluster in dst) {
            if (src.contains(cluster)) {
                continue
            }
            val closest = findClosestCluster(cluster, src)
            transitionMap[closest] = transitionMap[closest]?.plus(cluster) ?: setOf(cluster)
        }
        return transitionMap
    }

    private fun findClosestCluster(cluster: Cluster, clusters: Set<Cluster>): Cluster {
        var minDistance = Double.MAX_VALUE
        var clusterCandidate: Cluster? = null
        var firstCluster: Cluster? = null
        for (mapObjectCluster in clusters) {
            if (firstCluster == null) {
                firstCluster = mapObjectCluster
            }
            if (mapObjectCluster.isCluster()) {
                val distance = distanceBetween(mapObjectCluster, cluster)
                if (clusterCandidate == null || distance < minDistance) {
                    minDistance = distance
                    clusterCandidate = mapObjectCluster
                }
            }
        }
        if (clusterCandidate == null) {
            clusterCandidate = firstCluster
        }
        return clusterCandidate!!
    }

    private fun distanceBetween(a: Cluster, b: Cluster): Double {
        return (a.geoCoor().latitude - b.geoCoor().latitude) *
                (a.geoCoor().latitude - b.geoCoor().latitude) +
                (a.geoCoor().longitude - b.geoCoor().longitude) *
                (a.geoCoor().longitude - b.geoCoor().longitude)
    }
}
