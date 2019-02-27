package com.a65apps.mapkitclustering

import android.content.Context
import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.ClusterItem
import com.a65apps.clustering.yandex.YandexItem
import com.a65apps.clustering.yandex.view.ClusterImageProvider
import com.yandex.runtime.image.ImageProvider

class YandexClusterImageProvider(private val context: Context) : ClusterImageProvider<YandexItem> {
    override fun get(cluster: Cluster<YandexItem>): ImageProvider {
        return ImageProvider.fromResource(context, R.drawable.cluster)
    }

    override fun get(clusterItem: ClusterItem): ImageProvider {
        return ImageProvider.fromResource(context, R.drawable.pin)
    }

    override fun getX(): ImageProvider {
        return ImageProvider.fromResource(context, R.drawable.abc_ic_star_black_16dp)
    }
}
