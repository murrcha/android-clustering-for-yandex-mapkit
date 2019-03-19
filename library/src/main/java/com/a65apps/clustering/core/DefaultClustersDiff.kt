package com.a65apps.clustering.core

open class DefaultClustersDiff(private val currentClusters: Set<Cluster>,
                               private val newClusters: Set<Cluster> = emptySet()) : ClustersDiff {
    private val isCollapsing = newClusters.size <= currentClusters.size
    private val transitions = transitionsMap(currentClusters, newClusters)

    override fun transitions() = transitions

    override fun collapsing() = isCollapsing

    override fun newClusters() = newClusters

    override fun currentClusters() = currentClusters

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
                    if (clusterContainsAnother(parent, child)) {
                        transitionMap[parent] = transitionMap[parent]?.plus(child) ?: setOf(child)
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

    private fun clusterContainsAnother(parent: Cluster, child: Cluster): Boolean {
        return (!child.isCluster() && parent.items().contains(child)) ||
                (child.isCluster() && parent.items().containsAll(child.items()))
    }

    private fun findClosestCluster(cluster: Cluster, clusters: Set<Cluster>): Cluster {
        var minDistance = Double.MAX_VALUE
        var clusterCandidate: Cluster? = null
        val firstCluster: Cluster? = clusters.firstOrNull()
        clusters.filter {
            it.isCluster()
        }.forEach {
            val distance = distanceBetween(it, cluster)
            if (clusterCandidate == null || distance < minDistance) {
                minDistance = distance
                clusterCandidate = it
            }
        }
        if (clusterCandidate == null) {
            clusterCandidate = firstCluster
        }
        return clusterCandidate!!
    }

    private fun distanceBetween(a: Cluster, b: Cluster): Double {
        val aGeoCoor = a.geoCoor()
        val bGeoCoor = b.geoCoor()
        val abLat = aGeoCoor.latitude - bGeoCoor.latitude
        val abLon = aGeoCoor.longitude - bGeoCoor.longitude
        return abLat * abLat + abLon * abLon
    }
}
