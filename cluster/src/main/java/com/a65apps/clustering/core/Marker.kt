package com.a65apps.clustering.core

class Marker(private val point: Point,
             private val title: String,
             private val description: String) : ClusterItem {
    override fun position() = point

    override fun title() = title

    override fun snippet() = description
}
