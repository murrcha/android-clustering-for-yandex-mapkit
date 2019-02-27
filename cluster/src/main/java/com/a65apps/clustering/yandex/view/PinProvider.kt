package com.a65apps.clustering.yandex.view

import com.yandex.mapkit.map.IconStyle
import com.yandex.runtime.image.AnimatedImageProvider
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.ui_view.ViewProvider

class PinProvider {
    private var imageProvider: ImageProvider? = null
    private var viewProvider: ViewProvider? = null
    private var animatedImageProvider: AnimatedImageProvider? = null
    private var iconStyle: IconStyle? = null

    private constructor(imageProvider: ImageProvider,
                        iconStyle: IconStyle?) {
        this.imageProvider = imageProvider
        this.iconStyle = iconStyle
    }

    private constructor(viewProvider: ViewProvider,
                        iconStyle: IconStyle?) {
        this.viewProvider = viewProvider
        this.iconStyle = iconStyle
    }

    private constructor(animatedImageProvider: AnimatedImageProvider,
                        iconStyle: IconStyle) {
        this.animatedImageProvider = animatedImageProvider
        this.iconStyle = iconStyle
    }

    companion object {
        @JvmStatic
        fun from(imageProvider: ImageProvider,
                 iconStyle: IconStyle? = null): PinProvider {
            return PinProvider(imageProvider, iconStyle)
        }

        @JvmStatic
        fun from(viewProvider: ViewProvider,
                 iconStyle: IconStyle? = null): PinProvider {
            return PinProvider(viewProvider, iconStyle)
        }

        @JvmStatic
        fun from(animatedImageProvider: AnimatedImageProvider,
                 iconStyle: IconStyle): PinProvider {
            return PinProvider(animatedImageProvider, iconStyle)
        }
    }

    fun provider(): Any {
        val ip = this.imageProvider
        ip?.let { return ip }

        val vp = this.viewProvider
        vp?.let { return vp }

        val ap = this.animatedImageProvider
        ap?.let { return ap }

        throw IllegalStateException("undefined image provider")
    }

    fun style(): IconStyle? {
        return iconStyle
    }
}