package com.a65apps.clustering.core.algorithm

import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.DefaultCluster
import com.a65apps.clustering.core.DefaultClusterProvider
import com.a65apps.clustering.core.LatLng
import com.a65apps.clustering.core.geometry.Point
import com.a65apps.clustering.core.projection.SphericalMercatorProjection
import java.util.*

private const val DEFAULT_GRID_SIZE = 100

open class GridBasedAlgorithm(
        private val clusterProvider: ClusterProvider = DefaultClusterProvider()) :
        Algorithm<DefaultAlgorithmParameter> {
    private val currentItems: MutableSet<Cluster> = Collections.synchronizedSet(mutableSetOf())
    private val gridSize = DEFAULT_GRID_SIZE

    override fun addItem(item: Cluster) {
        synchronized(currentItems) {
            currentItems.add(item)
        }
    }

    override fun addItems(items: Collection<Cluster>) {
        synchronized(currentItems) {
            currentItems.addAll(items)
        }
    }

    override fun clearItems() {
        synchronized(currentItems) {
            currentItems.clear()
        }
    }

    override fun removeItem(item: Cluster) {
        synchronized(currentItems) {
            currentItems.remove(item)
        }
    }

    override fun removeItems(items: Collection<Cluster>) {
        synchronized(currentItems) {
            currentItems.removeAll(items)
        }
    }

    override fun calculate(parameter: DefaultAlgorithmParameter): Set<Cluster> {
        val numCells = Math.ceil(256 * Math.pow(2.0, parameter.zoom.toDouble()) / gridSize).toLong()
        val proj = SphericalMercatorProjection(numCells.toDouble())

        val clusters = mutableSetOf<Cluster>()
        val results = mutableSetOf<Cluster>()

        val sparseArray = mutableMapOf<Long, Cluster>()

        synchronized(currentItems) {
            for (item in currentItems) {
                val p = proj.toPoint(item.geoCoor())
                val coord = getCoord(numCells, p.x, p.y)
                var cluster = sparseArray[coord]
                if (cluster == null) {
                    val latLng = getLatLng(proj, p)
                    cluster = clusterProvider.get(DefaultCluster(latLng))
                    sparseArray[coord] = cluster
                    clusters.add(cluster)
                }
                cluster.addItem(item)
            }
            clusters.forEach {
                if (it.isCluster()) {
                    results.add(it)
                } else {
                    results.addAll(it.items())
                }
            }
        }
        return results
    }

    override fun setRatioForClustering(value: Float) {
    }

    private fun getLatLng(proj: SphericalMercatorProjection, point: Point): LatLng {
        return proj.toLatLng(point)
    }

    private fun getCoord(numCells: Long, x: Double, y: Double): Long {
        return (numCells * Math.floor(x) + Math.floor(y)).toLong()
    }
}
