package com.a65apps.mapkitclustering

import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.algorithm.ClusterProvider

class CustomClusterProvider : ClusterProvider {
    override fun get(cluster: Cluster): Cluster {
        return CustomCluster(cluster.geoCoor(), cluster.payload())
    }
}
