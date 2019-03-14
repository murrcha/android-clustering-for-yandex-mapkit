package com.a65apps.clustering.yandex

import com.a65apps.clustering.core.ClusterManager
import com.a65apps.clustering.core.ClustersDiff
import com.a65apps.clustering.core.VisibleRectangularRegion
import com.a65apps.clustering.core.algorithm.NonHierarchicalDistanceBasedAlgorithm
import com.a65apps.clustering.core.view.AnimationParams
import com.a65apps.clustering.core.view.ClusterRenderer
import com.a65apps.clustering.yandex.extention.toLatLng
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateSource
import com.yandex.mapkit.map.Map

class YandexClusterManager(renderer: ClusterRenderer<ClustersDiff, AnimationParams>,
                           visibleRectangularRegion: VisibleRectangularRegion) :
        ClusterManager<VisibleRectangularRegion, AnimationParams>(renderer, NonHierarchicalDistanceBasedAlgorithm(),
                visibleRectangularRegion), CameraListener {
    override fun onCameraPositionChanged(map: Map, cameraPosition: CameraPosition,
                                         updateSource: CameraUpdateSource, isFinal: Boolean) {
        if (isFinal) calculateClusters(VisibleRectangularRegion(
                map.visibleRegion.topLeft.toLatLng(),
                map.visibleRegion.bottomRight.toLatLng()))
    }
}