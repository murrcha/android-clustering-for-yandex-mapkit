package com.a65apps.clustering.yandex.view

import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.ClusterItem

interface ClusterPinProvider<T : ClusterItem> {
    fun get(cluster: Cluster<T>): PinProvider
    fun get(clusterItem: ClusterItem): PinProvider
    fun getX(): PinProvider
}
