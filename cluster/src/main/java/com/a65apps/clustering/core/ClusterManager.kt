package com.a65apps.clustering.core

interface ClusterManager<T : ClusterItem> {
    fun cluster()
    fun clearItems()
    fun addItems(items: Collection<T>)
    fun addItem(item: T)
    fun removeItem(item: T)
}
