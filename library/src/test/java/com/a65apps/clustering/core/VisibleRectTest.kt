package com.a65apps.clustering.core

import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object VisibleRectTest : Spek({
    describe("Tests:") {
        val topLeft = LatLng(0.0, 0.0)
        val bottomRight = LatLng(500.0, 500.0)
        val visibleRectangularRegion = VisibleRect(topLeft, bottomRight)
        it("returns topLeft") {
            assertThat(visibleRectangularRegion.topLeft).isEqualTo(topLeft)
        }
        it("returns bottomRight") {
            assertThat(visibleRectangularRegion.bottomRight).isEqualTo(bottomRight)
        }
    }
})
