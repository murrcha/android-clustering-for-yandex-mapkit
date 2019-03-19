package com.a65apps.clustering.core.algorithm

import com.a65apps.clustering.core.Cluster

interface ClusterProvider {
    fun get(cluster: Cluster): Cluster
}
