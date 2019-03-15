package com.a65apps.clustering.yandex.view

import com.a65apps.clustering.core.Cluster

interface ClusterPinProvider {
    fun get(cluster: Cluster): PinProvider
    fun getX(): PinProvider
}
