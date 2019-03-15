package com.a65apps.clustering.yandex.view

import com.a65apps.clustering.core.Cluster
import com.yandex.mapkit.map.PlacemarkMapObject

interface TapListener {
    fun markerTapped(cluster: Cluster, mapObject: PlacemarkMapObject)
}