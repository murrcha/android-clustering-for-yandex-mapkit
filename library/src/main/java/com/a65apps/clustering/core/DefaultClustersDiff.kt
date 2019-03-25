package com.a65apps.clustering.core

import com.a65apps.clustering.core.log.CMLogger

open class DefaultClustersDiff(current: Set<Cluster>,
                               new: Set<Cluster> = emptySet()) : ClustersDiff {
    private val currentClusters = current.toSet()
    private val newClusters = new.toSet()
    private val isCollapsing = newClusters.size <= currentClusters.size
    private val transitions = transitionsMap(currentClusters, newClusters)

    override fun transitions() = transitions

    override fun collapsing() = isCollapsing

    override fun newClusters() = newClusters

    override fun currentClusters() = currentClusters

    private fun transitionsMap(currentClusters: Collection<Cluster>,
                               newClusters: Collection<Cluster>): Map<Cluster, Set<Cluster>> {
        if (currentClusters.isEmpty() || newClusters.isEmpty()) {
            return emptyMap()
        }
        return if (isCollapsing) {
            buildTransitionsMap(newClusters, currentClusters)
        } else {
            buildTransitionsMap(currentClusters, newClusters)
        }
    }

    private fun buildTransitionsMap(dst: Collection<Cluster>,
                                    src: Collection<Cluster>): Map<Cluster, Set<Cluster>> {
        CMLogger.logMessage("start building transitionMap dst:${dst.size} src:${src.size}")
        val transitionMap = HashMap<Cluster, Set<Cluster>>()
        for (child in src) {
            if (dst.contains(child)) {
                continue
            }
            val closest = findClosestCluster(child, dst)
            transitionMap[closest] =
                    transitionMap[closest]?.plus(child) ?: setOf(child)
        }
        CMLogger.logMessage("end building transitionMap")
        return transitionMap
    }

    private fun findClosestCluster(cluster: Cluster, clusters: Collection<Cluster>): Cluster {
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
