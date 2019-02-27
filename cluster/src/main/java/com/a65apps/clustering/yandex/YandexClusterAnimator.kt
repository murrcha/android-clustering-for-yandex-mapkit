package com.a65apps.clustering.yandex

import com.a65apps.clustering.core.ClusterAnimator
import com.a65apps.clustering.core.Point
import com.yandex.mapkit.map.PlacemarkMapObject
import java.lang.ref.WeakReference

class YandexClusterAnimator(private val placemarkReference: WeakReference<PlacemarkMapObject>) :
        ClusterAnimator {
    override fun move(newPosition: Point) {
        val placemark = placemarkReference.get()
        if (placemark != null && placemark.isValid) {
            placemark.geometry = newPosition.toPoint()
        }
    }
}