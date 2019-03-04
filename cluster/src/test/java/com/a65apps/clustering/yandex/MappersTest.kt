package com.a65apps.clustering.yandex

import com.a65apps.clustering.core.LatLng
import com.a65apps.clustering.yandex.extention.toLatLng
import com.a65apps.clustering.yandex.extention.toPoint
import com.yandex.mapkit.geometry.Point
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object MappersTest : Spek({
    describe("Mappers tests") {
        val latitude = 56.863069
        val longitude = 53.219774
        val emptyValue = 0.0

        describe("LatLng to Point") {
            val latLng = LatLng(latitude, longitude)
            val point = latLng.toPoint()
            it("returns Point") {
                assertThat(point).isInstanceOf(Point::class.java)
                assertThat(point.latitude).isEqualTo(latitude)
                assertThat(point.longitude).isEqualTo(longitude)
            }
        }

        describe("Point to LatLng") {
            val point = Point(latitude, longitude)
            val latLng = point.toLatLng()
            it("returns LatLng") {
                assertThat(latLng).isInstanceOf(LatLng::class.java)
                assertThat(latLng.latitude).isEqualTo(latitude)
                assertThat(latLng.longitude).isEqualTo(longitude)
            }
        }

        describe("Empty Point to LatLng") {
            val point = Point()
            val latLng = point.toLatLng()
            it("returns empty Point") {
                assertThat(latLng).isInstanceOf(LatLng::class.java)
                assertThat(latLng.latitude).isEqualTo(emptyValue)
                assertThat(latLng.longitude).isEqualTo(emptyValue)
            }
        }

        describe("Empty LatLng to Point") {
            val latLng = LatLng(emptyValue, emptyValue)
            val point = latLng.toPoint()
            it("returns empty Point") {
                assertThat(point).isInstanceOf(Point::class.java)
                assertThat(point.latitude).isEqualTo(emptyValue)
                assertThat(point.longitude).isEqualTo(emptyValue)
            }
        }
    }
})
