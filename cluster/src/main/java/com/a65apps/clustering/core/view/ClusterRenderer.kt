package com.a65apps.clustering.core.view

import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.ClusterItem

interface ClusterRenderer<T : ClusterItem> {
    fun clusterChanged(cluster: Set<Cluster<T>>)
    fun animation(withAnimation: Boolean)
    fun onAdd()
    fun onRemove()
}