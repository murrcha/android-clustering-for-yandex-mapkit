package com.a65apps.clustering.yandex

import com.yandex.mapkit.geometry.Point

fun com.a65apps.clustering.core.Point.toPoint() = Point(latitude, longitude)

fun Point.toPoint() = com.a65apps.clustering.core.Point(latitude, longitude)
