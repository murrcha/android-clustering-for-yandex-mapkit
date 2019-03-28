package com.a65apps.clustering.yandex.view

import com.yandex.mapkit.map.IconStyle
import com.yandex.runtime.image.AnimatedImageProvider
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.ui_view.ViewProvider

/**
 * Returns Yandex MapKit icon provider with style
 */
open class YandexPinProvider {
    private var imageProvider: ImageProvider? = null
    private var viewProvider: ViewProvider? = null
    private var animatedImageProvider: AnimatedImageProvider? = null
    val style: IconStyle?

    private constructor(imageProvider: ImageProvider,
                        iconStyle: IconStyle?) {
        this.imageProvider = imageProvider
        style = iconStyle
    }

    private constructor(viewProvider: ViewProvider,
                        iconStyle: IconStyle?) {
        this.viewProvider = viewProvider
        style = iconStyle
    }

    private constructor(animatedImageProvider: AnimatedImageProvider,
                        iconStyle: IconStyle) {
        this.animatedImageProvider = animatedImageProvider
        style = iconStyle
    }

    companion object {
        @JvmStatic
        fun from(imageProvider: ImageProvider,
                 iconStyle: IconStyle? = null): YandexPinProvider {
            return YandexPinProvider(imageProvider, iconStyle)
        }

        @JvmStatic
        fun from(viewProvider: ViewProvider,
                 iconStyle: IconStyle? = null): YandexPinProvider {
            return YandexPinProvider(viewProvider, iconStyle)
        }

        @JvmStatic
        fun from(animatedImageProvider: AnimatedImageProvider,
                 iconStyle: IconStyle): YandexPinProvider {
            return YandexPinProvider(animatedImageProvider, iconStyle)
        }
    }

    fun provider(): Any {
        return imageProvider ?: viewProvider ?: animatedImageProvider
        ?: throw IllegalStateException("undefined pin provider")
    }
}
