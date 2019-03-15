package com.a65apps.clustering.yandex

import com.a65apps.clustering.core.DefaultClusterClusterManager
import com.a65apps.clustering.core.ClustersDiff
import com.a65apps.clustering.core.VisibleRectangularRegion
import com.a65apps.clustering.core.algorithm.Algorithm
import com.a65apps.clustering.core.view.AnimationParams
import com.a65apps.clustering.core.view.ClusterRenderer
import com.a65apps.clustering.yandex.extention.toLatLng
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateSource
import com.yandex.mapkit.map.Map

open class YandexDefaultClusterClusterManager(renderer: ClusterRenderer<ClustersDiff, AnimationParams>,
                                              algorithm: Algorithm,
                                              visibleRectangularRegion: VisibleRectangularRegion) :
        DefaultClusterClusterManager<AnimationParams>(renderer, algorithm, visibleRectangularRegion),
        CameraListener {
    override fun onCameraPositionChanged(map: Map, cameraPosition: CameraPosition,
                                         updateSource: CameraUpdateSource, isFinal: Boolean) {
        if (isFinal) calculateClusters(VisibleRectangularRegion(
                map.visibleRegion.topLeft.toLatLng(),
                map.visibleRegion.bottomRight.toLatLng()))
    }
}