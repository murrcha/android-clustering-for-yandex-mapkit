package com.a65apps.clustering.core.view

import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.MarkersDiff

interface ClusterRenderer<in D: MarkersDiff, in C: RenderConfig> {
    fun updateClusters(diffs: D)
    fun setMarkers(clusters: Set<Cluster>)
    fun config(renderConfig: C)
    fun onAdd()
    fun onRemove()
}
