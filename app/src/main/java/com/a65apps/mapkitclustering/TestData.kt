package com.a65apps.mapkitclustering

import com.a65apps.mapkitcluster.ClusterAnimator
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import java.util.*

object TestData {
    const val POINTS_COUNT = 100
    val POINT = Point(56.863069, 53.219774)
    val POINTS_LIST: MutableList<Point> = ArrayList(1000)
    val CLUSTER_POINT_0: Point
    val CAMERA_POSITION: CameraPosition
    const val MIN_LAT = 56.837725f
    const val MAX_LAT = 56.866513f
    const val MIN_LON = 53.205989f
    const val MAX_LON = 53.233493f

    init {
        for (i in 0 until POINTS_COUNT) {
            val r = Random()
            val lat = MIN_LAT + r.nextFloat() * (MAX_LAT - MIN_LAT)
            val lon = MIN_LON + r.nextFloat() * (MAX_LON - MIN_LON)
            POINTS_LIST.add(Point(lat.toDouble(), lon.toDouble()))
        }

        CLUSTER_POINT_0 = ClusterAnimator.calcCenter(POINTS_LIST)
        CAMERA_POSITION = CameraPosition(CLUSTER_POINT_0, 14.0f, 0.0f, 0.0f)
    }
}
