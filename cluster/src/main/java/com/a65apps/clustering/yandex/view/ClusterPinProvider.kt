package com.a65apps.clustering.yandex.view

import com.a65apps.clustering.core.Marker

interface ClusterPinProvider {
    fun get(marker: Marker): PinProvider
    fun getX(): PinProvider
}
