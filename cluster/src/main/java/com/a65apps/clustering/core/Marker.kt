package com.a65apps.clustering.core

class Marker(private val point: Point,
             private val title: String,
             private val description: String) : ClusterItem {
    override fun position(): Point = point

    override fun title(): String = title

    override fun snippet(): String = description
}