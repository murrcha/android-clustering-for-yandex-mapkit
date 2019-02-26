package com.a65apps.clustering.core

interface ClusterItem {
    fun position(): Positionable
    fun title(): String
    fun snippet(): String
}