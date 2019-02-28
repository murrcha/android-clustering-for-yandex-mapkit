package com.a65apps.clustering.core.view

import com.a65apps.clustering.core.Clusters

interface ClusterRenderer {
    fun clusterChanged(clusters: Clusters)
    fun animation(withAnimation: Boolean)
    fun onAdd()
    fun onRemove()
}
