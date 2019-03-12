package com.a65apps.clustering.yandex.view

import com.a65apps.clustering.core.Marker
import com.yandex.mapkit.map.PlacemarkMapObject

interface TapListener {
    fun markerTapped(marker: Marker, mapObject: PlacemarkMapObject)
}