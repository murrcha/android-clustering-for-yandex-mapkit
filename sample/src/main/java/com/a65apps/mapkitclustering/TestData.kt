package com.a65apps.mapkitclustering

import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import java.util.*

object TestData {
    val POINTS_LIST: MutableSet<Point> = mutableSetOf()
    val CAMERA_CENTER: Point = Point(56.851620, 53.215409)
    val CAMERA_POSITION: CameraPosition = CameraPosition(CAMERA_CENTER,
            12.0f, 0.0f, 0.0f)

    val MIN_LAT = CAMERA_CENTER.latitude - 0.05f
    val MAX_LAT = CAMERA_CENTER.latitude + 0.05f
    val MIN_LON = CAMERA_CENTER.longitude - 0.01
    val MAX_LON = CAMERA_CENTER.longitude + 0.01

    init {
        for (i in 0 until 117) {
            POINTS_LIST.add(randomPoint())
        }
    }

    fun randomPoint(): Point {
        val r = Random()
        val lat = MIN_LAT + r.nextFloat() * (MAX_LAT - MIN_LAT)
        val lon = MIN_LON + r.nextFloat() * (MAX_LON - MIN_LON)
        return Point(lat, lon)
    }
}
