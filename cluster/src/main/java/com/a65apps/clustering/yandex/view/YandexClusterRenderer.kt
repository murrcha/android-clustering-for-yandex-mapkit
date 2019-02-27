package com.a65apps.clustering.yandex.view

import android.graphics.PointF
import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.ClusterItem
import com.a65apps.clustering.core.view.ClusterRenderer
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.runtime.image.ImageProvider

class YandexClusterRenderer<T : ClusterItem>(
        map: Map,
        private val imageProvider: ClusterImageProvider<T>,
        private val minClusterSize: Int)
    : ClusterRenderer<T> {
    private val layer: MapObjectCollection = map.addMapObjectLayer(LAYER_NAME)

    companion object {
        const val LAYER_NAME = "CLUSTER_NAME"
    }

    override fun clusterChanged(clusters: Set<Cluster<T>>) {
        clusters.forEach { cluster ->
            var point: Point
            var image: ImageProvider
            if (cluster.size() >= minClusterSize) {
                point = Point(cluster.position().latitude,
                        cluster.position().longitude)
                image = imageProvider.get(cluster)
                layer.addPlacemark(point, image, IconStyle(PointF(0.5f, 0.5f),
                        null, null, null, null,
                        null, null))
                layer.addPlacemark(point, imageProvider.getX())
            } else {
                cluster.items().forEach { clusterItem ->
                    point = Point(clusterItem.position().latitude,
                            clusterItem.position().longitude)
                    image = imageProvider.get(clusterItem)
                    layer.addPlacemark(point, image, IconStyle(PointF(0.5f, 1f),
                            null, null, null, null,
                            null, null))
                    layer.addPlacemark(point, imageProvider.getX())
                }
            }
        }
    }

    override fun animation(withAnimation: Boolean) {
    }

    override fun onAdd() {
    }

    override fun onRemove() {
    }
}