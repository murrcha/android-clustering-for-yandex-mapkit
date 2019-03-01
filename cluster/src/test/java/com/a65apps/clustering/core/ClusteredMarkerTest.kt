package com.a65apps.clustering.core

import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object ClusteredMarkerTest : Spek({
    describe("Clustered marker tests") {
        val latitude = 56.863069
        val longitude = 53.219774
        val latLng = LatLng(latitude, longitude)
        val payload = Any()
        val marker = ClusteredMarker(latLng, payload)

        it("returns geo position of marker") {
            assertThat(marker.getGeoCoor()).isEqualTo(latLng)
            assertThat(marker.getGeoCoor().latitude).isEqualTo(latitude)
            assertThat(marker.getGeoCoor().longitude).isEqualTo(longitude)
        }

        it("returns payload of marker") {
            assertThat(marker.getPayload()).isEqualTo(payload)
        }

        it("returns is cluster false") {
            assertThat(marker.isCluster()).isFalse()
        }

        it("returns children count = 0") {
            assertThat(marker.getChildrenCount()).isEqualTo(0)
        }

        it("marker is instance of ClusterItem") {
            assertThat(marker).isInstanceOf(Marker::class.java)
        }

        describe("marker with children") {
            val cluster = ClusteredMarker(latLng, payload, setOf(marker))

            it("returns is cluster true") {
                assertThat(cluster.isCluster()).isTrue()
            }

            it("returns children count > 0") {
                assertThat(cluster.getChildrenCount()).isGreaterThan(0)
            }
        }
    }
})
