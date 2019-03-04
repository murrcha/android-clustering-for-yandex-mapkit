package com.a65apps.clustering.core.geometry

/**
 * Represents an area in the cartesian plane.
 */
data class Bounds(val minX: Double, val maxX: Double, val minY: Double, val maxY: Double) {
    val midX = (minX + maxX) / 2
    val midY = (minY + maxY) / 2

    fun contains(x: Double, y: Double) = x in minX..maxX && y in minY..maxY

    fun contains(point: Point) = contains(point.x, point.y)

    fun contains(bounds: Bounds) = bounds.minX in minX..maxX && bounds.minY in minY..maxY

    fun intersects(minX: Double, maxX: Double, minY: Double, maxY: Double): Boolean {
        return minX < this.maxX && this.minX < maxX && minY < this.maxY && this.minY < maxY
    }

    fun intersects(bounds: Bounds): Boolean {
        return intersects(bounds.minX, bounds.maxX, bounds.minY, bounds.maxY)
    }
}
