package com.a65apps.clustering.core.view

import com.a65apps.clustering.core.ClustersDiff
import com.a65apps.clustering.core.Marker

interface ClusterRenderer {
    fun updateClusters(diffs: ClustersDiff)
    fun setMarkers(markers: Set<Marker>)
    fun animation(animationParams: AnimationParams)
    fun onAdd()
    fun onRemove()
}
