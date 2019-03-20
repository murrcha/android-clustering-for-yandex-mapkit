package com.a65apps.clustering.yandex.extention

import com.a65apps.clustering.core.LatLng
import com.yandex.mapkit.geometry.Point
import org.assertj.core.api.Assertions
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object LatLngExtTest : Spek({
    describe("Tests:") {
        val latitude = 56.863069
        val longitude = 53.219774
        val emptyValue = 0.0

        context("LatLng to Point") {
            val latLng = LatLng(latitude, longitude)
            val point = latLng.toPoint()
            it("returns Point") {
                Assertions.assertThat(point).isInstanceOf(Point::class.java)
                Assertions.assertThat(point.latitude).isEqualTo(latitude)
                Assertions.assertThat(point.longitude).isEqualTo(longitude)
            }
        }

        context("Empty LatLng to Point") {
            val latLng = LatLng(emptyValue, emptyValue)
            val point = latLng.toPoint()
            it("returns empty Point") {
                Assertions.assertThat(point).isInstanceOf(Point::class.java)
                Assertions.assertThat(point.latitude).isEqualTo(emptyValue)
                Assertions.assertThat(point.longitude).isEqualTo(emptyValue)
            }
        }
    }
})
