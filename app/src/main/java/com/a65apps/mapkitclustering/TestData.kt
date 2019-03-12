package com.a65apps.mapkitclustering

import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import java.util.*

object TestData {
    val POINTS_LIST: MutableSet<Point> = mutableSetOf()
    val CAMERA_CENTER: Point = Point(56.851620, 53.215409)
    val CAMERA_POSITION: CameraPosition = CameraPosition(CAMERA_CENTER,
            12.0f, 0.0f, 0.0f)

    const val MIN_LAT = 56.807725f
    const val MAX_LAT = 56.896513f
    const val MIN_LON = 53.205989f
    const val MAX_LON = 53.233493f

    init {
        for (i in 0 until 717) {
            val r = Random()
            val lat = MIN_LAT + r.nextFloat() * (MAX_LAT - MIN_LAT)
            val lon = MIN_LON + r.nextFloat() * (MAX_LON - MIN_LON)
            POINTS_LIST.add(Point(lat.toDouble(), lon.toDouble()))
        }
    }
}
