package com.a65apps.mapkitclustering

import android.content.Context
import android.graphics.PointF
import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.yandex.view.ClusterPinProvider
import com.a65apps.clustering.yandex.view.YandexPinProvider
import com.a65apps.mapkitclustering.view.ClusterPinView
import com.yandex.mapkit.map.IconStyle
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.ui_view.ViewProvider

class CustomPinProvider(context: Context) : ClusterPinProvider {
    private val pinIconStyle = IconStyle(PointF(0.5f, 1f), null, null,
            null, null, null, null)
    private val clusterView = ClusterPinView(context)
    private val pinResource = ImageProvider.fromResource(context, R.drawable.pin)
    private val pinProvider = YandexPinProvider.from(pinResource, pinIconStyle)
    private val providers = mutableMapOf(Pair(1, pinProvider))

    override fun get(cluster: Cluster): YandexPinProvider {
        val size = cluster.size()
        return providers[size] ?: createClusterProvider(size)
    }

    private fun createClusterProvider(size: Int): YandexPinProvider {
        clusterView.setText(size.toString())
        val newProvider = YandexPinProvider.from(ViewProvider(clusterView))
        providers[size] = newProvider
        return newProvider
    }
}
