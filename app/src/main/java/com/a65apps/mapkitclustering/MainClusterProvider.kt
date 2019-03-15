package com.a65apps.mapkitclustering

import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.algorithm.ClusterProvider

class MainClusterProvider : ClusterProvider {
    override fun get(cluster: Cluster): Cluster {
        return MainCluster(cluster.geoCoor(), cluster.payload())
    }
}