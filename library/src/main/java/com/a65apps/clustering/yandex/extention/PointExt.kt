package com.a65apps.clustering.yandex.extention

import com.a65apps.clustering.core.LatLng
import com.yandex.mapkit.geometry.Point

fun Point.toLatLng() = LatLng(latitude, longitude)
