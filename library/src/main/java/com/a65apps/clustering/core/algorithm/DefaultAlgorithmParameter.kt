package com.a65apps.clustering.core.algorithm

import com.a65apps.clustering.core.VisibleRect

class DefaultAlgorithmParameter(visibleRect: VisibleRect, zoom: Int) {
    private val visibleRect = visibleRect
    private val zoom = zoom

    fun visibleRect(): VisibleRect {
        return visibleRect
    }

    fun zoom(): Int {
        return zoom
    }
}