package com.a65apps.clustering.core

data class DefaultClustersDiff(val currentClusters: Set<Cluster>,
                               val newClusters: Set<Cluster> = emptySet()) : ClustersDiff {
    private val isCollapsing: Boolean = newClusters.size <= currentClusters.size
    private val transitions: Map<Cluster, Set<Cluster>> =
            buildTransitionMap(currentClusters, newClusters)

    override fun transitions(): Map<Cluster, Set<Cluster>> {
        return transitions
    }

    override fun collapsing(): Boolean = isCollapsing

    override fun newClusters(): Set<Cluster> = newClusters

    override fun currentClusters(): Set<Cluster> = currentClusters

    private fun buildTransitionMap(actualClusters: Set<Cluster>,
                                   newClusters: Set<Cluster>): Map<Cluster, Set<Cluster>> {
        if (actualClusters.isEmpty() || newClusters.isEmpty()) {
            return emptyMap()
        }
        return if (isCollapsing) {
            collapsingMap(actualClusters, newClusters)
        } else {
            expandingMap(actualClusters, newClusters)
        }
    }

    private fun expandingMap(actualClusters: Set<Cluster>,
                             newClusters: Set<Cluster>): Map<Cluster, Set<Cluster>> {
        val transitionMap = mutableMapOf<Cluster, Set<Cluster>>()
        for (new in newClusters) {
            if (actualClusters.contains(new)) {
                continue
            }
            var added = false
            for (actual in actualClusters) {
                if (actual.isCluster()) {
                    if ((!new.isCluster() && actual.items().contains(new)) ||
                            (new.isCluster() && actual.items().containsAll(new.items()))) {
                        transitionMap[actual] = transitionMap[actual]?.plus(new) ?: setOf(new)
                        added = true
                        break
                    }
                }
            }
            if (!added) {
                val closest = findClosestCluster(new, actualClusters)
                transitionMap[closest] = transitionMap[closest]?.plus(new) ?: setOf(new)
            }
        }
        return transitionMap
    }

    private fun collapsingMap(actualClusters: Set<Cluster>,
                              newClusters: Set<Cluster>): Map<Cluster, Set<Cluster>> {
        val transitionMap = mutableMapOf<Cluster, Set<Cluster>>()
        for (actual in actualClusters) {
            if (newClusters.contains(actual)) {
                continue
            }
            var added = false
            for (new in newClusters) {
                if (new.isCluster()) {
                    if ((!actual.isCluster() && new.items().contains(actual)) ||
                            (actual.isCluster() && new.items().containsAll(actual.items()))) {
                        transitionMap[new] = transitionMap[new]?.plus(actual) ?: setOf(actual)
                        added = true
                        break
                    }
                }
            }
            if (!added) {
                val closest = findClosestCluster(actual, newClusters)
                transitionMap[closest] = transitionMap[closest]?.plus(actual) ?: setOf(actual)
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
