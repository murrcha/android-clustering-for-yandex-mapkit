package com.a65apps.clustering.yandex.extention

import com.a65apps.clustering.core.LatLng
import com.yandex.mapkit.geometry.Point
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object PointExtTest : Spek({
    describe("Tests: ") {
        val latitude = 56.863069
        val longitude = 53.219774
        val emptyValue = 0.0

        context("Point to LatLng") {
            val point = Point(latitude, longitude)
            val latLng = point.toLatLng()
            it("returns LatLng") {
                assertThat(latLng).isInstanceOf(LatLng::class.java)
                assertThat(latLng.latitude).isEqualTo(latitude)
                assertThat(latLng.longitude).isEqualTo(longitude)
            }
        }

        context("Empty Point to LatLng") {
            val point = Point()
            val latLng = point.toLatLng()
            it("returns empty Point") {
                assertThat(latLng).isInstanceOf(LatLng::class.java)
                assertThat(latLng.latitude).isEqualTo(emptyValue)
                assertThat(latLng.longitude).isEqualTo(emptyValue)
            }
        }
    }
})
