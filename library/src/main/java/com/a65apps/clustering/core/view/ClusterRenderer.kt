package com.a65apps.clustering.core.view

import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.ClustersDiff

interface ClusterRenderer<in D : ClustersDiff, in C : RenderConfig> {
    fun updateClusters(diffs: D)
    fun setClusters(clusters: Set<Cluster>)
    fun config(renderConfig: C)
    fun onAdd()
    fun onRemove()
}
