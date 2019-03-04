package com.a65apps.clustering.core

import android.content.Context
import com.a65apps.clustering.core.algorithm.Algorithm
import com.a65apps.clustering.core.algorithm.NonHierarchicalDistanceBasedAlgorithm
import java.util.concurrent.locks.ReentrantReadWriteLock

class ClusterManager(private val context: Context, private var visibleRectangularRegion: VisibleRectangularRegion) {
    private var actualMarkers: MutableSet<Marker> = mutableSetOf()

    private var algorithm: Algorithm = NonHierarchicalDistanceBasedAlgorithm()
    private val algorithmLock = ReentrantReadWriteLock()

    fun getNewMarker(geoCoor: LatLng, payload: Any? = null): Marker =
            ClusteredMarker(geoCoor, payload)

    fun calculateClusters(visibleRectangularRegion: VisibleRectangularRegion) {
        this.visibleRectangularRegion = visibleRectangularRegion
        calculateClusters()
    }

    fun calculateClusters() {
        val newMarkers = updateClusters()
        val actualClusterCount = getClustersCount(actualMarkers)
        val newClusterCount = getClustersCount(newMarkers)
        if (actualClusterCount != newClusterCount) {
            callRenderer(newMarkers, newClusterCount < actualClusterCount)
        }
    }

    fun clearMarkers() {
        algorithmLock.writeLock().lock()
        try {
            algorithm.clearMarkers()
            onModifyRawMarkers(true)
        } finally {
            algorithmLock.writeLock().unlock()
        }
    }

    fun setMarkers(markers: MutableSet<Marker>) {
        algorithmLock.writeLock().lock()
        try {
            algorithm.addMarkers(markers)
            calculateClusters()
        } finally {
            algorithmLock.writeLock().unlock()
        }

    }

    fun addMarker(marker: Marker) {
        algorithmLock.writeLock().lock()
        try {
            algorithm.addMarker(marker)
            onModifyRawMarkers(false)
        } finally {
            algorithmLock.writeLock().unlock()
        }
    }

    fun removeMarker(marker: Marker) {
        algorithmLock.writeLock().lock()
        try {
            algorithm.removeMarker(marker)
            onModifyRawMarkers(false)
        } finally {
            algorithmLock.writeLock().unlock()
        }
    }

    private fun updateClusters(): Set<Marker> {
        //TODO выполнить в рабочем потоке!
        return algorithm.calculate(visibleRectangularRegion)
    }

    private fun callRenderer(newMarkers: Set<Marker>, isCollapsed: Boolean) {
        val transitionMap = buildTransitionMap(actualMarkers, newMarkers, isCollapsed)
        val clusters = Clusters(actualMarkers, newMarkers, transitionMap, isCollapsed)
        //TODO выполнить метод renderer'а updateClusters(clusters)
        actualMarkers.clear()
        actualMarkers.addAll(newMarkers)
    }

    private fun onModifyRawMarkers(isClear: Boolean) {
        actualMarkers.clear()
        if (!isClear) {
            actualMarkers.addAll(updateClusters())
        }
        //TODO выполнить метод renderer'а setMarkers(actualMarkers)
    }

    private fun getClustersCount(markers: Set<Marker>): Int {
        var count = 0
        markers.forEach {
            if (it.isCluster()) {
                count++
            }
        }
        return count
    }

    private fun buildTransitionMap(actualMarkers: Set<Marker>, newMarkers: Set<Marker>,
                                   isCollapsed: Boolean): Map<Marker, Set<Marker>> {
        val transitionMap = mutableMapOf<Marker, Set<Marker>>()
        val moreClusteredSet = if (isCollapsed) newMarkers else actualMarkers
        val lessClusteredSet = if (isCollapsed) actualMarkers else newMarkers

        for (item in moreClusteredSet) {
            if (!item.isCluster()) {
                continue
            }

            val moreClusteredItem = item as ClusteredMarker
            val itemsForCollapse = mutableSetOf<ClusteredMarker>()
            lessClusteredSet.forEach {
                val lessClusteredItem = it as ClusteredMarker
                if (!lessClusteredItem.isCluster() && moreClusteredItem.rawMarkers.contains(lessClusteredItem)) {
                    itemsForCollapse.add(lessClusteredItem)
                }
                if (lessClusteredItem.isCluster() && moreClusteredItem.rawMarkers.containsAll(lessClusteredItem.rawMarkers)) {
                    itemsForCollapse.add(lessClusteredItem)
                }
            }

            transitionMap[moreClusteredItem] = itemsForCollapse
        }
        return transitionMap
    }
}
