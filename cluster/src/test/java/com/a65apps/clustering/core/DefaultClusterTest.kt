package com.a65apps.clustering.core

import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.*

object DefaultClusterTest : Spek({
    describe("Tests:") {
        val latitude = 56.863069
        val longitude = 53.219774
        val latLng = LatLng(latitude, longitude)
        val payload = Any()
        val emptyCluster = DefaultCluster(latLng, payload)

        it("returns geo position of cluster") {
            assertThat(emptyCluster.geoCoor()).isEqualTo(latLng)
            assertThat(emptyCluster.geoCoor().latitude).isEqualTo(latitude)
            assertThat(emptyCluster.geoCoor().longitude).isEqualTo(longitude)
        }

        it("returns payload of cluster") {
            assertThat(emptyCluster.payload()).isEqualTo(payload)
        }

        it("returns is cluster false") {
            assertThat(emptyCluster.isCluster()).isFalse()
        }

        it("returns size = 0") {
            assertThat(emptyCluster.size()).isEqualTo(1)
        }

        it("cluster is instance of Cluster") {
            assertThat(emptyCluster).isInstanceOf(Cluster::class.java)
        }

        describe("cluster with children") {
            val notEmptyCluster = DefaultCluster(latLng, payload)
            val random = Random()
            for (i in 0 until 10) {
                val latLng = LatLng(random.nextDouble(), random.nextDouble())
                notEmptyCluster.addItem(DefaultCluster(latLng))
            }

            it("returns children size > 0") {
                assertThat(notEmptyCluster.size()).isGreaterThan(0)
            }

            it("returns children size 10") {
                assertThat(notEmptyCluster.size()).isEqualTo(10)
            }

            it("returns isCluster true") {
                assertThat(notEmptyCluster.isCluster()).isTrue()
            }

            describe("after remove 1 item from cluster") {
                val item = notEmptyCluster.items().last()

                it("returns true after remove item") {
                    assertThat(notEmptyCluster.removeItem(item)).isTrue()
                }

                it("returns children size 9") {
                    assertThat(notEmptyCluster.size()).isEqualTo(9)
                }

                it("returns isCluster false") {
                    assertThat(notEmptyCluster.isCluster()).isFalse()
                }
            }
        }
    }
})
