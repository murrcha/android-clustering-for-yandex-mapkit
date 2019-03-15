package com.a65apps.clustering.core

interface ClusterManager {
    fun setItems(clusters: Set<Cluster>)
    fun clearItems()
    fun addItem(cluster: Cluster)
    fun removeItem(cluster: Cluster)
    fun addItems(clusters: Set<Cluster>)
    fun removeItems(clusters: Set<Cluster>)
}
