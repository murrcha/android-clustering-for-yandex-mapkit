package com.a65apps.clustering.core

interface ClusterManager {
    fun setMarkers(clusters: Set<Cluster>)
    fun clearMarkers()
    fun addMarker(cluster: Cluster)
    fun removeMarker(cluster: Cluster)
    fun addMarkers(clusters: Set<Cluster>)
    fun removeMarkers(clusters: Set<Cluster>)
}
