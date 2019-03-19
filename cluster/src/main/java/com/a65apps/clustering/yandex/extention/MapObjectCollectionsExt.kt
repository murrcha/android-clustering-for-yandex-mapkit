package com.a65apps.clustering.yandex.extention

import com.a65apps.clustering.core.LatLng
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.AnimatedImageProvider
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.ui_view.ViewProvider

fun MapObjectCollection.addPlacemark(coords: LatLng, provider: Any,
                                     iconStyle: IconStyle?)
        : PlacemarkMapObject {
    val point = coords.toPoint()
    when (provider) {
        is ImageProvider -> return addPlacemark(point, provider, iconStyle)
        is ViewProvider -> return addPlacemark(point, provider, iconStyle)
        is AnimatedImageProvider -> return addPlacemark(point, provider, iconStyle)
    }
    throw IllegalArgumentException("Icon provider must be ImageProvider," +
            " ViewProvider or AnimatedImageProvider")
}

fun MapObjectCollection.addPlacemark(point: Point, imageProvider: ImageProvider,
                                     iconStyle: IconStyle?): PlacemarkMapObject {
    return if (iconStyle != null) {
        this.addPlacemark(point, imageProvider, iconStyle)
    } else {
        this.addPlacemark(point, imageProvider)
    }
}

fun MapObjectCollection.addPlacemark(point: Point, viewProvider: ViewProvider,
                                     iconStyle: IconStyle?): PlacemarkMapObject {
    return if (iconStyle != null) {
        this.addPlacemark(point, viewProvider, iconStyle)
    } else {
        this.addPlacemark(point, viewProvider)
    }
}

fun MapObjectCollection.addPlacemark(point: Point,
                                     animatedImageProvider: AnimatedImageProvider,
                                     iconStyle: IconStyle?): PlacemarkMapObject {
    return if (iconStyle != null) {
        this.addPlacemark(point, animatedImageProvider, iconStyle)
    } else {
        throw IllegalArgumentException("null IconStyle with AnimatedImageProvider")
    }
}
