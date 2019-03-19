package com.a65apps.clustering.core

open class DefaultClustersDiff(private val currentClusters: Set<Cluster>,
                               private val newClusters: Set<Cluster> = emptySet()) : ClustersDiff {
    private val isCollapsing: Boolean = newClusters.size <= currentClusters.size
    private val transitions: Map<Cluster, Set<Cluster>> =
            transitionsMap(currentClusters, newClusters)

    override fun transitions(): Map<Cluster, Set<Cluster>> {
        return transitions
    }

    override fun collapsing(): Boolean = isCollapsing

    override fun newClusters(): Set<Cluster> = newClusters

    override fun currentClusters(): Set<Cluster> = currentClusters

    private fun transitionsMap(currentClusters: Set<Cluster>,
                               newClusters: Set<Cluster>): Map<Cluster, Set<Cluster>> {
        if (currentClusters.isEmpty() || newClusters.isEmpty()) {
            return emptyMap()
        }
        return if (isCollapsing) {
            buildTransitionsMap(newClusters, currentClusters)
        } else {
            buildTransitionsMap(currentClusters, newClusters)
        }
    }

    private fun buildTransitionsMap(dst: Set<Cluster>,
                                    src: Set<Cluster>): Map<Cluster, Set<Cluster>> {
        val transitionMap = mutableMapOf<Cluster, Set<Cluster>>()
        for (child in src) {
            if (dst.contains(child)) {
                continue
            }
            var added = false
            for (parent in dst) {
                if (parent.isCluster()) {
                    if ((!child.isCluster() && parent.items().contains(child)) ||
                            (child.isCluster() && parent.items().containsAll(
                                    child.items()))) {
                        transitionMap[parent] =
                                transitionMap[parent]?.plus(child) ?: setOf(child)
                        added = true
                        break
                    }
                }
            }
            if (!added) {
                val closest = findClosestCluster(child, dst)
                transitionMap[closest] =
                        transitionMap[closest]?.plus(child) ?: setOf(child)
            }
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
