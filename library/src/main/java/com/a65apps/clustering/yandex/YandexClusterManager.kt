package com.a65apps.clustering.yandex

import android.util.Log
import com.a65apps.clustering.core.DefaultClusterManager
import com.a65apps.clustering.core.DefaultClustersDiff
import com.a65apps.clustering.core.VisibleRect
import com.a65apps.clustering.core.algorithm.Algorithm
import com.a65apps.clustering.core.algorithm.CacheNonHierarchicalDistanceBasedAlgorithm
import com.a65apps.clustering.core.algorithm.DefaultAlgorithmParameter
import com.a65apps.clustering.core.view.ClusterRenderer
import com.a65apps.clustering.yandex.extention.toLatLng
import com.a65apps.clustering.yandex.view.YandexRenderConfig
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateSource
import com.yandex.mapkit.map.Map

open class YandexClusterManager(
        renderer: ClusterRenderer<DefaultClustersDiff, YandexRenderConfig>,
        val algorithm: Algorithm<DefaultAlgorithmParameter> = CacheNonHierarchicalDistanceBasedAlgorithm(),
        algorithmParameter: DefaultAlgorithmParameter) :
        DefaultClusterManager<YandexRenderConfig>(renderer, algorithm, algorithmParameter),
        CameraListener {
    private var lastZoom: Int = 0

    override fun onCameraPositionChanged(map: Map, cameraPosition: CameraPosition,
                                         updateSource: CameraUpdateSource, isFinal: Boolean) {
        when {
            isFinal -> {
                val currentZoom = cameraPosition.zoom.toInt()
                if (lastZoom != currentZoom) {
                    lastZoom = currentZoom
                    Log.d("CAMERA POSITION", "Current zoom = $lastZoom")
                }
                val visibleRect = VisibleRect(map.visibleRegion.topLeft.toLatLng(),
                        map.visibleRegion.bottomRight.toLatLng())
                val algorithmParameter = DefaultAlgorithmParameter(visibleRect, lastZoom)
                calculateClusters(algorithmParameter)
            }
        }
    }
}
