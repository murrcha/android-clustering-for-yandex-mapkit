package com.a65apps.clustering.core.projection

import com.a65apps.clustering.core.LatLng
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object SphericalMercatorProjectionTest : Spek({
    describe("Tests:") {
        val projection = SphericalMercatorProjection(1.0)
        val latitude = 56.807725
        val longitude = 53.205989

        val point = projection.toPoint(LatLng(latitude, longitude))
        it("LatLng toPoint") {
            assertThat(point.x).isCloseTo(0.647794, within(0.000001))
            assertThat(point.y).isCloseTo(0.307338, within(0.000001))
        }

        val latLng = projection.toLatLng(point)
        it("Point toLatLng") {
            assertThat(latLng.latitude).isCloseTo(latitude, within(0.000001))
            assertThat(latLng.longitude).isCloseTo(longitude, within(0.00001))
        }
    }
})