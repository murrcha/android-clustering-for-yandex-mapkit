package com.a65apps.clustering.yandex.view

import com.a65apps.clustering.core.Cluster

/**
 * Provides cluster/pin appearance
 */
interface ClusterPinProvider {
    fun get(cluster: Cluster): YandexPinProvider
}
