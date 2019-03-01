package com.a65apps.clustering.yandex

import com.a65apps.clustering.core.LatLng
import com.yandex.mapkit.geometry.Point

fun LatLng.toPoint() = Point(latitude, longitude)

fun Point.toLatLng() = LatLng(latitude, longitude)
