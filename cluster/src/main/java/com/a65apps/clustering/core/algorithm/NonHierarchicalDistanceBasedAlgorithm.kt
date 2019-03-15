package com.a65apps.clustering.core.algorithm

import android.util.Log
import com.a65apps.clustering.core.*
import com.a65apps.clustering.core.geometry.Bounds
import com.a65apps.clustering.core.geometry.Point
import com.a65apps.clustering.core.projection.SphericalMercatorProjection
import com.a65apps.clustering.core.quadtree.PointQuadTree
import kotlin.math.sqrt

private val PROJECTION = SphericalMercatorProjection(1.0)
private const val DEFAULT_RATIO_FOR_CLUSTERING = 0.5f

/**
 * A simple clustering algorithm with O(nlog n) performance. Resulting clusters are not
 * hierarchical.
 * <p/>
 * High level algorithm:<br>
 * 1. Iterate over items in the order they were added (candidate clusters).<br>
 * 2. Create a cluster with the center of the item. <br>
 * 3. Add all items that are within a certain distance to the cluster. <br>
 * 4. Move any items out of an existing cluster if they are closer to another cluster. <br>
 * 5. Remove those items from the list of candidate clusters.
 * <p/>
 * Clusters have the center of the first element (not the centroid of the items within it).
 */
open class NonHierarchicalDistanceBasedAlgorithm(
        private val clusterProvider: ClusterProvider = DefaultClusterProvider()) :
        Algorithm {

    private var ratioForClustering = DEFAULT_RATIO_FOR_CLUSTERING

    /**
     * Any modifications should be synchronized on mQuadTree.
     */
    private val quadItems: MutableSet<QuadItem> = mutableSetOf()

    /**
     * Any modifications should be synchronized on mQuadTree.
     */
    private val quadTree = PointQuadTree<QuadItem>(0.0, 1.0, 0.0, 1.0)

    override fun addMarker(cluster: Cluster) {
        val quadItem = QuadItem(cluster as DefaultCluster)
        synchronized(quadTree) {
            quadItems.add(quadItem)
            quadTree.add(quadItem)
        }
    }

    override fun addMarkers(clusters: Collection<Cluster>) = clusters.forEach { addMarker(it) }

    override fun removeMarker(cluster: Cluster) {
        // QuadItem delegates hashcode() and equals() to its item so,
        //   removing any QuadItem to that item will remove the item
        val quadItem = QuadItem(cluster as DefaultCluster)
        synchronized(quadTree) {
            quadItems.remove(quadItem)
            quadTree.remove(quadItem)
        }
    }

    override fun removeMarkers(clusters: Collection<Cluster>) {
        synchronized(quadTree) {
            clusters.forEach {
                val quadItem = QuadItem(it as DefaultCluster)
                quadItems.remove(quadItem)
                quadTree.remove(quadItem)
            }
        }
    }

    override fun clearMarkers() {
        synchronized(quadTree) {
            quadItems.clear()
            quadTree.clear()
        }
    }

    override fun calculate(visibleRectangularRegion: VisibleRectangularRegion): Set<Cluster> {
        val zoomSpecificSpan = getZoomSpecificSpan(visibleRectangularRegion)

        val visitedCandidates = mutableSetOf<QuadItem>()
        val resultingQuadItems = mutableSetOf<QuadItem>()
        val distanceToCluster = mutableMapOf<QuadItem, Double>()
        val itemToCluster = mutableMapOf<QuadItem, Cluster>()
        val resultingMarkers = mutableSetOf<Cluster>()

        synchronized(quadTree) {
            for (candidate in quadItems) {
                if (visitedCandidates.contains(candidate)) {
                    // Candidate is already part of another cluster.
                    continue
                }

                val searchBounds = createBoundsFromSpan(candidate.point, zoomSpecificSpan)
                val clusterItems: Collection<QuadItem>
                clusterItems = quadTree.search(searchBounds)
                if (clusterItems.size == 1) {
                    // Only the current cluster is in range. Just add the single item to the results.
                    resultingQuadItems.add(candidate)
                    visitedCandidates.add(candidate)
                    distanceToCluster[candidate] = 0.0
                    continue
                }

                val cluster = clusterProvider.get(candidate.cluster)
                resultingQuadItems.add(QuadItem(cluster))

                for (clusterItem in clusterItems) {
                    val existingDistance = distanceToCluster[clusterItem]
                    val distance = distanceSquared(clusterItem.point, candidate.point)
                    if (existingDistance != null) {
                        // Item already belongs to another cluster. Check if it's closer to this cluster.
                        if (existingDistance < distance) {
                            continue
                        }
                        // Move item to the closer cluster.
                        itemToCluster[clusterItem]?.removeItem(clusterItem.cluster)
                    }
                    distanceToCluster[clusterItem] = distance
                    cluster.addItem(clusterItem.cluster)
                    itemToCluster[clusterItem] = cluster
                }
                visitedCandidates.addAll(clusterItems)
            }

            resultingQuadItems.forEach {
                if (it.cluster.isCluster()) {
                    resultingMarkers.add(it.cluster)
                } else {
                    resultingMarkers.addAll(it.cluster.items())
                }
            }
        }
        Log.d("MARKER", "ALGORITHM PINS COUNT ${Markers.count(resultingMarkers)}")
        return resultingMarkers
    }

    private fun getZoomSpecificSpan(visibleRectangularRegion: VisibleRectangularRegion): Double {
        val point1 = PROJECTION.toPoint(visibleRectangularRegion.topLeft)
        val point2 = PROJECTION.toPoint(visibleRectangularRegion.bottomRight)
        return sqrt(distanceSquared(point1, point2)) * ratioForClustering
    }

    override fun getMarkers(): Collection<Cluster> {
        val markers = mutableListOf<Cluster>()
        synchronized(quadTree) {
            quadItems.forEach { markers.add(it.cluster) }
        }
        return markers
    }

    override fun setRatioForClustering(value: Float) {
        ratioForClustering = value
    }

    private fun distanceSquared(a: Point, b: Point): Double {
        return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y)
    }

    private fun createBoundsFromSpan(p: Point, span: Double): Bounds {
        val halfSpan = span / 2
        return Bounds(
                p.x - halfSpan, p.x + halfSpan,
                p.y - halfSpan, p.y + halfSpan)
    }

    private class QuadItem(val cluster: Cluster) : PointQuadTree.Item {
        val position: LatLng = cluster.geoCoor()
        override val point: Point
            get() = PROJECTION.toPoint(position)

        override fun hashCode(): Int {
            return cluster.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            return (other as? QuadItem)?.cluster?.equals(cluster) ?: false
        }
    }
}
