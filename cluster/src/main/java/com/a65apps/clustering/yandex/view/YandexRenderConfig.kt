package com.a65apps.clustering.yandex.view

import android.os.Build
import android.view.animation.Interpolator
import com.a65apps.clustering.core.view.RenderConfig

open class YandexRenderConfig(
        val animationEnabled: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP,
        val removeWithOpacityEnabled: Boolean = false,
        val duration: Long = 240,
        val interpolator: Interpolator? = null
) : RenderConfig
