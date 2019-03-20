package com.a65apps.clustering.core.geometry

import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object PointTest : Spek({
    describe("Tests:") {
        val x = 1.1
        val y = 2.2
        val point = Point(x, y)
        it("returns x") {
            assertThat(point.x).isEqualTo(x)
        }

        it("returns y") {
            assertThat(point.y).isEqualTo(y)
        }
    }
})
