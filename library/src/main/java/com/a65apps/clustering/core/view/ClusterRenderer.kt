package com.a65apps.clustering.core.view

import com.a65apps.clustering.core.Cluster

interface ClusterRenderer<in C : RenderConfig> {
    fun updateClusters(newClusters: Set<Cluster>)
    fun setClusters(clusters: Set<Cluster>)
    fun config(renderConfig: C)
    fun onAdd()
    fun onRemove()
}
