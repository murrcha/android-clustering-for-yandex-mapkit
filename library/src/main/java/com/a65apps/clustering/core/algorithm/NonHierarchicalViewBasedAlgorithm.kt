package com.a65apps.clustering.core.algorithm

import com.a65apps.clustering.core.DefaultClusterProvider
import com.a65apps.clustering.core.geometry.Bounds

open class NonHierarchicalViewBasedAlgorithm(clusterProvider: ClusterProvider =
                                                     DefaultClusterProvider()) :
        NonHierarchicalDistanceBasedAlgorithm(clusterProvider) {
    override fun getClusteringItems(): Collection<QuadItem> {
        return quadTree.search(visibleBounds())
    }

    private fun visibleBounds(): Bounds {
        val rect = visibleRect
        rect?.let {
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
        return Bounds(0.0, 0.0, 0.0, 0.0)
    }
}
