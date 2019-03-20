package com.a65apps.clustering.core.geometry

import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object BoundsTest : Spek({
    describe("Tests:") {
        val minX = 1.0
        val maxX = 10.0
        val minY = 1.0
        val maxY = 10.0
        val testBounds = Bounds(minX, maxX, minY, maxY)

        it("returns contains(x, y) true") {
            assertThat(testBounds.contains(2.0, 2.0)).isTrue()
        }

        it("returns contains(point) true") {
            val point = Point(2.0, 2.0)
            assertThat(testBounds.contains(point)).isTrue()
        }

        it("returns contains(bounds) true") {
            val bounds = Bounds(2.0, 4.0, 2.0, 4.0)
            assertThat(testBounds.contains(bounds)).isTrue()
        }

        it("returns contains(x, y) false") {
            assertThat(testBounds.contains(11.0, 10.0)).isFalse()
        }

        it("returns contains(point) false") {
            val point = Point(11.0, 2.0)
            assertThat(testBounds.contains(point)).isFalse()
        }

        it("returns contains(bounds) false") {
            val bounds = Bounds(10.0, 12.0, 10.0, 12.0)
            assertThat(testBounds.contains(bounds)).isFalse()
        }

        it("returns intersects(minX, maxX, minY, maxY) true") {
            assertThat(testBounds.intersects(9.0, 12.0, 9.0, 12.0)).isTrue()
        }

        it("returns intersects(bounds) true") {
            val bounds = Bounds(9.0, 12.0, 9.0, 12.0)
            assertThat(testBounds.intersects(bounds)).isTrue()
        }

        it("returns intersects(minX, maxX, minY, maxY) false") {
            assertThat(testBounds.intersects(10.0, 12.0, 10.0, 12.0)).isFalse()
        }

        it("returns intersects(bounds) false") {
            val bounds = Bounds(10.0, 12.0, 10.0, 12.0)
            assertThat(testBounds.intersects(bounds)).isFalse()
        }
    }
})
