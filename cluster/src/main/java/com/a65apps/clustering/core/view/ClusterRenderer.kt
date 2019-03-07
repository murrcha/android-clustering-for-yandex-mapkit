package com.a65apps.clustering.core.view

import com.a65apps.clustering.core.Clusters
import com.a65apps.clustering.core.Marker

interface ClusterRenderer {
    fun updateClusters(clusters: Clusters)
    fun setMarkers(markers: Set<Marker>)
    fun animation(animationParams: AnimationParams)
    fun onAdd()
    fun onRemove()
}
