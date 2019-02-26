package com.a65apps.clustering.core

class Marker(private val latLng: LatLng,
             private val title: String?,
             private val description: String?) : ClusterItem {

    override fun position(): LatLng = latLng

    override fun title(): String? = title

    override fun snippet(): String? = description
}