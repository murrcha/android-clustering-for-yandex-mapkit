package com.a65apps.clustering.core.algorithm

import com.a65apps.clustering.core.DefaultClusterProvider
import com.a65apps.clustering.core.geometry.Bounds

/**
 * This algorithm works the same way as {@link NonHierarchicalDistanceBasedAlgorithm} but works, only in
 * visible area. It requires to be reclustered on camera movement because clustering is done only for visible area.
 */
open class NonHierarchicalViewBasedAlgorithm(clusterProvider: ClusterProvider =
                                                     DefaultClusterProvider()) :
        NonHierarchicalDistanceBasedAlgorithm(clusterProvider) {
    override fun getClusteringItems(): Collection<QuadItem> {
        return quadTree.search(visibleBounds())
    }

    private fun visibleBounds(): Bounds {
        val rect = parameter!!.visibleRect
        val topLeft = PROJECTION.toPoint(rect.topLeft)
        val bottomRight = PROJECTION.toPoint(rect.bottomRight)
        val minX = topLeft.x
        val maxX = bottomRight.x
        val minY = topLeft.y
        val maxY = bottomRight.y
        val delta = (maxX - minX) / 2
        return Bounds(minX - delta, maxX + delta,
                minY - delta, maxY + delta)
    }
}
