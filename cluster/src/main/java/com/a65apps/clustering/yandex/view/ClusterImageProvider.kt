package com.a65apps.clustering.yandex.view

import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.ClusterItem
import com.yandex.runtime.image.ImageProvider

interface ClusterImageProvider<T : ClusterItem> {
    fun get(cluster: Cluster<T>): ImageProvider
    fun get(clusterItem: ClusterItem): ImageProvider
    fun getX(): ImageProvider
}
