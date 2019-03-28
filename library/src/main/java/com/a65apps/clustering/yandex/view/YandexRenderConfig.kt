package com.a65apps.clustering.yandex.view

import android.view.animation.Interpolator
import com.a65apps.clustering.core.view.RenderConfig

/**
 * Implementation of RenderConfig for Yandex MapKit
 */
open class YandexRenderConfig(
        val animationEnabled: Boolean = true,
        val removeWithOpacityEnabled: Boolean = animationEnabled,
        val duration: Long = 240,
        val interpolator: Interpolator? = null,
        val optimizeAnimations: Boolean = false
) : RenderConfig
