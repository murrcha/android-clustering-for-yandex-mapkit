package com.a65apps.clustering.core.view

import android.view.animation.Interpolator

data class AnimationParams(
        val animationEnabled: Boolean = false,
        val removeWithOpacityEnabled: Boolean = false,
        val duration: Long = 240,
        val interpolator: Interpolator? = null
)
