package com.a65apps.clustering.core

interface ClusterItem {
    fun position(): LatLng
    fun title(): String
    fun snippet(): String
}