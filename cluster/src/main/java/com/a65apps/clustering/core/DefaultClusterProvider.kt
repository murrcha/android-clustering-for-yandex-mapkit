package com.a65apps.clustering.core

import com.a65apps.clustering.core.algorithm.ClusterProvider

open class DefaultClusterProvider : ClusterProvider {
    override fun get(cluster: Cluster): Cluster {
        return DefaultCluster(cluster.geoCoor(), cluster.payload())
    }
}
