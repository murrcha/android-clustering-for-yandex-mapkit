package com.a65apps.mapkitclustering

import com.a65apps.clustering.yandex.ClusterAnimator
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import java.util.*

object TestData {
    const val POINTS_COUNT = 100
    const val MIN_CLUSTER_SIZE = 3
    val POINT = Point(56.863069, 53.219774)
    val POINTS_LIST_0: MutableList<Point> = ArrayList(1000)
    val POINTS_LIST_1: MutableList<Point> = ArrayList(1000)
    val POINTS_LIST_DIF: MutableList<Point> = ArrayList(1000)
    val CLUSTER_POINT_0: Point
    val CLUSTER_POINT_1: Point
    val CAMERA_POSITION: CameraPosition
    const val MIN_LAT = 56.807725f
    const val MAX_LAT = 56.896513f
    const val MIN_LON = 53.205989f
    const val MAX_LON = 53.233493f

    init {
        for (i in 0 until 5) {
            val r = Random()
            val lat = MIN_LAT + r.nextFloat() * (MAX_LAT - MIN_LAT)
            val lon = MIN_LON + r.nextFloat() * (MAX_LON - MIN_LON)
            POINTS_LIST_0.add(Point(lat.toDouble(), lon.toDouble()))
        }

        for (i in 0 until 117) {
            val r = Random()
            val lat = MIN_LAT + r.nextFloat() * (MAX_LAT - MIN_LAT)
            val lon = MIN_LON + r.nextFloat() * (MAX_LON - MIN_LON)
            POINTS_LIST_1.add(Point(lat.toDouble(), lon.toDouble()))
        }

        for (i in 0 until 2) {
            val r = Random()
            val lat = MIN_LAT + r.nextFloat() * (MAX_LAT - MIN_LAT)
            val lon = MIN_LON + r.nextFloat() * (MAX_LON - MIN_LON)
            POINTS_LIST_DIF.add(Point(lat.toDouble(), lon.toDouble()))
        }

        CLUSTER_POINT_0 = ClusterAnimator.calcCenter(POINTS_LIST_0)
        CLUSTER_POINT_1 = ClusterAnimator.calcCenter(POINTS_LIST_1)
        CAMERA_POSITION = CameraPosition(CLUSTER_POINT_0, 12.0f, 0.0f, 0.0f)
    }
}
