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
        val marker = DefaultCluster(latLng, payload)

        it("returns geo position of marker") {
            assertThat(marker.geoCoor()).isEqualTo(latLng)
            assertThat(marker.geoCoor().latitude).isEqualTo(latitude)
            assertThat(marker.geoCoor().longitude).isEqualTo(longitude)
        }

        it("returns payload of marker") {
            assertThat(marker.payload()).isEqualTo(payload)
        }

        it("returns is cluster false") {
            assertThat(marker.isCluster()).isFalse()
        }

        it("returns size = 0") {
            assertThat(marker.size()).isEqualTo(1)
        }

        it("marker is instance of ClusterItem") {
            assertThat(marker).isInstanceOf(Cluster::class.java)
        }

        describe("marker with children") {
            val cluster = DefaultCluster(latLng, payload)
            cluster.addItem(marker)

            it("returns children size > 0") {
                assertThat(cluster.size()).isGreaterThan(0)
            }
        }
    }
})
