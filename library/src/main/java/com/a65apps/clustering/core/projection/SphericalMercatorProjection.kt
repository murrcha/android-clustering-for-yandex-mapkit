package com.a65apps.clustering.core.projection

import com.a65apps.clustering.core.LatLng
import com.a65apps.clustering.core.geometry.Point
import kotlin.math.*

class SphericalMercatorProjection(private val worldWidth: Double) {
    fun toPoint(latLng: LatLng): Point {
        val x = latLng.longitude / 360 + .5
        val siny = sin(Math.toRadians(latLng.latitude))
        val y = 0.5 * ln((1 + siny) / (1 - siny)) / -(2 * PI) + .5

        return Point(x * worldWidth, y * worldWidth)
    }

    fun toLatLng(point: Point): LatLng {
        val x = point.x / worldWidth - 0.5
        val lng = x * 360

        val y = .5 - point.y / worldWidth
        val lat = 90 - Math.toDegrees(atan(exp(-y * 2.0 * PI)) * 2)

        return LatLng(lat, lng)
    }
}
