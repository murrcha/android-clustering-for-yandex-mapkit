package com.a65apps.mapkitclustering

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.yandex.view.ClusterPinProvider
import com.a65apps.clustering.yandex.view.YandexPinProvider
import com.a65apps.mapkitclustering.view.ClusterPinView
import com.yandex.mapkit.map.IconStyle
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.ui_view.ViewProvider

class MainClusterPinProvider(context: Context) : ClusterPinProvider {
    private val clusterIconStyle =
            IconStyle(PointF(0.5f, 0.5f), null, null,
                    null, null, null, null)
    private val pinIconStyle =
            IconStyle(PointF(0.5f, 1f), null, null,
                    null, null, null, null)
    private val clusterResource =
            YandexPinProvider.from(ImageProvider.fromResource(context, R.drawable.cluster),
                    clusterIconStyle)
    private val clusterView = ClusterPinView(context)
    private val pinResource =
            YandexPinProvider.from(ImageProvider.fromResource(context, R.drawable.pin),
                    pinIconStyle)
    @SuppressLint("PrivateResource")
    private val xResource =
            YandexPinProvider.from(
                    ImageProvider.fromResource(context, R.drawable.abc_ic_star_black_16dp))

    override fun get(cluster: Cluster): YandexPinProvider {
        return if (cluster.isCluster()) {
            clusterView.setText(cluster.size().toString())
            YandexPinProvider.from(ViewProvider(clusterView))
        } else {
            pinResource
        }
    }
}
