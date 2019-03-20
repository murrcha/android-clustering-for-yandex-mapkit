package com.a65apps.clustering.yandex

import com.a65apps.clustering.core.DefaultClusterManager
import com.a65apps.clustering.core.DefaultClustersDiff
import com.a65apps.clustering.core.VisibleRect
import com.a65apps.clustering.core.algorithm.Algorithm
import com.a65apps.clustering.core.algorithm.NonHierarchicalDistanceBasedAlgorithm
import com.a65apps.clustering.core.view.ClusterRenderer
import com.a65apps.clustering.yandex.extention.toLatLng
import com.a65apps.clustering.yandex.view.YandexRenderConfig
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateSource
import com.yandex.mapkit.map.Map

open class YandexClusterManager(
        renderer: ClusterRenderer<DefaultClustersDiff, YandexRenderConfig>,
        algorithm: Algorithm = NonHierarchicalDistanceBasedAlgorithm(),
        visibleRect: VisibleRect) :
        DefaultClusterManager<YandexRenderConfig>(renderer, algorithm, visibleRect),
        CameraListener {
    override fun onCameraPositionChanged(map: Map, cameraPosition: CameraPosition,
                                         updateSource: CameraUpdateSource, isFinal: Boolean) {
        when {
            isFinal -> calculateClusters(VisibleRect(map.visibleRegion.topLeft.toLatLng(),
                    map.visibleRegion.bottomRight.toLatLng()))
        }
    }
}
