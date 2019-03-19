package com.a65apps.clustering.core

import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object LatLngTest : Spek({
    describe("LatLng tests") {
        val latitude = 56.863069
        val longitude = 53.219774
        val latLng = LatLng(latitude, longitude)

        it("returns latitude of latLng") {
            assertThat(latLng.latitude).isEqualTo(latitude)
        }

        it("returns longitude of latLng") {
            assertThat(latLng.longitude).isEqualTo(longitude)
        }

        it("compare latLng with other latLng") {
            assertThat(latLng).isEqualTo(LatLng(latitude, longitude))
            assertThat(latLng).isNotEqualTo(LatLng( longitude, latitude))
        }
    }
})
