package com.a65apps.clustering.core

import com.a65apps.clustering.core.algorithm.Algorithm
import com.a65apps.clustering.core.algorithm.NonHierarchicalDistanceBasedAlgorithm
import com.a65apps.clustering.core.view.ClusterRenderer
import java.util.concurrent.locks.ReentrantReadWriteLock

open class ClusterManager(private val renderer: ClusterRenderer,
                          private var visibleRectangularRegion: VisibleRectangularRegion) {
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
        renderer.updateClusters(clusters)
        actualMarkers.clear()
        actualMarkers.addAll(newMarkers)
    }

    private fun onModifyRawMarkers(isClear: Boolean) {
        actualMarkers.clear()
        if (!isClear) {
            actualMarkers.addAll(updateClusters())
        }
        //TODO выполнить метод renderer'а setMarkers(actualMarkers)
        renderer.setMarkers(actualMarkers)
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

    /*private fun buildTransitionMap(actualMarkers: Set<Marker>, newMarkers: Set<Marker>,
                                   isCollapsed: Boolean): Map<Marker, Set<Marker>> {
        val transitionMap = mutableMapOf<Marker, Set<Marker>>()
        val moreClusteredSet = if (isCollapsed) actualMarkers else newMarkers
        val lessClusteredSet = if (isCollapsed) newMarkers else actualMarkers

        for (item in moreClusteredSet) {
            if (!item.isCluster()) {
                continue
            }

            val moreClusteredItem = item as ClusteredMarker
            val itemsForCollapse = mutableSetOf<ClusteredMarker>()
            lessClusteredSet.forEach {
                val lessClusteredItem = it as ClusteredMarker
                if (!lessClusteredItem.isCluster() && moreClusteredItem.rawMarkers.contains(
                                lessClusteredItem)) {
                    itemsForCollapse.add(lessClusteredItem)
                }
                if (lessClusteredItem.isCluster() && moreClusteredItem.rawMarkers.containsAll(
                                lessClusteredItem.rawMarkers)) {
                    itemsForCollapse.add(lessClusteredItem)
                }
            }

            transitionMap[moreClusteredItem] = itemsForCollapse
        }
        return transitionMap
    }*/

    private fun buildTransitionMap(actualMarkers: Set<Marker>, newMarkers: Set<Marker>,
                                   isCollapsed: Boolean): Map<Marker, Set<Marker>> {
        return if (isCollapsed) {
            buildCollapsedMap(actualMarkers, newMarkers)
        } else {
            buildExpandedMap(actualMarkers, newMarkers)
        }
    }

    private fun buildExpandedMap(actualMarkers: Set<Marker>,
                                 newMarkers: Set<Marker>): Map<Marker, Set<Marker>> {
        val transitionMap = mutableMapOf<Marker, Set<Marker>>()

        for (old in actualMarkers) {
            if (!old.isCluster()) {
                continue
            }

            val oldCluster = old as ClusteredMarker
            val expandedItems = mutableSetOf<Marker>()
            for (new in newMarkers) {
                if (new.isCluster()) {
                    continue
                }
                if (oldCluster.rawMarkers.contains(new)) {
                    expandedItems.add(new)
                }
            }
            if (expandedItems.isNotEmpty()) {
                transitionMap[old] = expandedItems
            }
        }
        return transitionMap
    }

    private fun buildCollapsedMap(actualMarkers: Set<Marker>,
                                  newMarkers: Set<Marker>): Map<Marker, Set<Marker>> {
        val transitionMap = mutableMapOf<Marker, Set<Marker>>()

        for (new in newMarkers) {
            if (!new.isCluster()) {
                continue
            }

            val newCluster = new as ClusteredMarker
            val collapsedItems = mutableSetOf<Marker>()
            if (actualMarkers.containsAll(newCluster.rawMarkers)) {
                collapsedItems.addAll(newCluster.rawMarkers)
            }
            transitionMap[newCluster] = collapsedItems
        }
        return transitionMap
    }
}
