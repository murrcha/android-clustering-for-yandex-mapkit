package com.a65apps.clustering.core

import com.a65apps.clustering.core.algorithm.Algorithm
import com.a65apps.clustering.core.view.ClusterRenderer
import kotlinx.coroutines.*
import java.util.concurrent.locks.ReentrantReadWriteLock

open class ClusterManager<P>(private val renderer: ClusterRenderer,
                             private var algorithm: Algorithm<P>,
                             private var parameter: P) {
    init {
        renderer.onAdd()
    }

    private var actualMarkers: MutableSet<Marker> = mutableSetOf()
    private val algorithmLock = ReentrantReadWriteLock()
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private var calculateJob: Job? = null

    fun getNewMarker(geoCoor: LatLng, payload: Any? = null): Marker =
            ClusteredMarker(geoCoor, payload)

    fun calculateClusters(parameter: P) {
        this.parameter = parameter
        calculateJob?.cancel()
        calculateJob = calculateClusters()
    }

    private fun calculateClusters() = uiScope.launch {
        val diffs = withContext(Dispatchers.Default) {
            calcDiffs()
        }
        diffs?.let {
            callRenderer(diffs)
        }
    }

    private suspend fun calcDiffs(): ClustersDiff? {
        return coroutineScope {
            val newMarkers = updateClusters()
            val actualClusterCount = clusterCount(actualMarkers)
            val newClusterCount = clusterCount(newMarkers)
            val isCollapsing = newMarkers.size <= actualMarkers.size
            var diffs: ClustersDiff? = null
            if (actualClusterCount != newClusterCount ||
                    actualMarkers.size != newMarkers.size) {
                diffs = buildTransitionMap(actualMarkers, newMarkers, isCollapsing)
            }
            diffs
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
        return algorithm.calculate(parameter)
    }

    private fun callRenderer(diffs: ClustersDiff) {
        renderer.updateClusters(diffs)
        actualMarkers.clear()
        actualMarkers.addAll(diffs.newMarkers)
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
                                   isCollapsing: Boolean): ClustersDiff {
        val transitionMap = mutableMapOf<Marker, Set<Marker>>()
        if (actualMarkers.isEmpty() || newMarkers.isEmpty()) {
            return ClustersDiff(actualMarkers, newMarkers, transitionMap, isCollapsing)
        }
        val src = if (isCollapsing) newMarkers else actualMarkers
        val dst = if (isCollapsing) actualMarkers else newMarkers
        for (marker in dst) {
            if (src.contains(marker)) {
                continue
            }
            val closest = findClosestCluster(marker, src)
            transitionMap[closest] = transitionMap[closest]?.plus(marker) ?: setOf(marker)
        }
        return ClustersDiff(actualMarkers, newMarkers, transitionMap, isCollapsing)
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
