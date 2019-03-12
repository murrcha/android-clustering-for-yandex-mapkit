package com.a65apps.clustering.core

import com.a65apps.clustering.core.algorithm.Algorithm
import com.a65apps.clustering.core.algorithm.NonHierarchicalDistanceBasedAlgorithm
import com.a65apps.clustering.core.view.ClusterRenderer
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
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
        val actualMarkersCount = clusterCount(actualMarkers)
        val newMarkerCount = clusterCount(newMarkers)
        if (actualMarkersCount != newMarkerCount) {
            callRenderer(newMarkers, newMarkers.size <= actualMarkers.size)
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
        return runBlocking {
            updateClustersAsync().await()
        }
    }

    private fun updateClustersAsync(): Deferred<Set<Marker>> {
        return GlobalScope.async {
            algorithm.calculate(visibleRectangularRegion)
        }
    }

    private fun callRenderer(newMarkers: Set<Marker>, isCollapsing: Boolean) {
        val clusters: Clusters
        clusters = if (actualMarkers.isNotEmpty()) {
            val transitionMap = buildTransitionMap(actualMarkers, newMarkers, isCollapsing)
            Clusters(actualMarkers, newMarkers, transitionMap, isCollapsing)
        } else {
            Clusters(actualMarkers, newMarkers, emptyMap(), isCollapsing)
        }
        renderer.updateClusters(clusters)
        actualMarkers.clear()
        actualMarkers.addAll(newMarkers)
    }

    private fun onModifyRawMarkers(isClear: Boolean) {
        actualMarkers.clear()
        if (!isClear) {
            actualMarkers.addAll(updateClusters())
        }
        renderer.setMarkers(actualMarkers)
    }

    private fun buildTransitionMap(actualMarkers: Set<Marker>,
                                   newMarkers: Set<Marker>,
                                   isCollapsing: Boolean): Map<Marker, Set<Marker>> {
        val transitionMap = mutableMapOf<Marker, Set<Marker>>()
        val src = if (isCollapsing) newMarkers else actualMarkers
        val dst = if (isCollapsing) actualMarkers else newMarkers
        for (marker in dst) {
            if (src.contains(marker)) {
                continue
            }
            val closest = findClosestCluster(marker, src)
            transitionMap[closest] = transitionMap[closest]?.plus(marker) ?: setOf(marker)
        }
        return transitionMap
    }

    private fun findClosestCluster(marker: Marker, markers: Set<Marker>): Marker {
        var minDistance = Double.MAX_VALUE
        var markerCandidate: Marker? = null
        var firstMarker: Marker? = null
        for (mapObjectMarker in markers) {
            if (firstMarker == null) {
                firstMarker = mapObjectMarker
            }
            if (mapObjectMarker.isCluster()) {
                val distance = distanceBetween(mapObjectMarker, marker)
                if (markerCandidate == null || distance < minDistance) {
                    minDistance = distance
                    markerCandidate = mapObjectMarker
                }
            }
        }
        if (markerCandidate == null) {
            markerCandidate = firstMarker
        }
        return markerCandidate!!
    }

    private fun distanceBetween(a: Marker, b: Marker): Double {
        return (a.getGeoCoor().latitude - b.getGeoCoor().latitude) *
                (a.getGeoCoor().latitude - b.getGeoCoor().latitude) +
                (a.getGeoCoor().longitude - b.getGeoCoor().longitude) *
                (a.getGeoCoor().longitude - b.getGeoCoor().longitude)
    }

    private fun clusterCount(markers: Set<Marker>): Int {
        var count = 0
        for (marker in markers) {
            if (marker.isCluster()) {
                count++
            }
        }
        return count
    }
}
