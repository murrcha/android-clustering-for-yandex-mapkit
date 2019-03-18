package com.a65apps.clustering.core

import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.random.Random

object DefaultClustersDiffTest : Spek({
    fun initClusters(cluster: Cluster, clusters: MutableSet<Cluster>) {
        for (i in 0 until 10) {
            val pin = DefaultCluster(LatLng(Random.nextDouble(), Random.nextDouble()))
            clusters.add(pin)
            cluster.addItem(pin)
        }
    }

    describe("Tests:") {
        val currentClusters = emptySet<Cluster>()
        val newClusters = emptySet<Cluster>()
        val emptyDiff = DefaultClustersDiff(currentClusters, newClusters)
        describe("Empty diff") {
            it("returns current clusters size 0") {
                assertThat(emptyDiff.currentClusters().size).isEqualTo(0)
            }

            it("returns new clusters size 0") {
                assertThat(emptyDiff.newClusters().size).isEqualTo(0)
            }

            it("returns transitions map size 0") {
                assertThat(emptyDiff.transitions().size).isEqualTo(0)
            }

            it("returns collapsing true") {
                assertThat(emptyDiff.collapsing()).isTrue()
            }

            describe("Collapsing diff") {
                val currentClusters = mutableSetOf<Cluster>()
                val newClusters = mutableSetOf<Cluster>()
                val cluster = DefaultCluster(LatLng(Random.nextDouble(), Random.nextDouble()))
                initClusters(cluster, currentClusters)
                newClusters.add(cluster)
                val collapsingDiff = DefaultClustersDiff(currentClusters, newClusters)

                it("returns current clusters size 10") {
                    assertThat(collapsingDiff.currentClusters().size).isEqualTo(10)
                }

                it("returns new clusters size 1") {
                    assertThat(collapsingDiff.newClusters().size).isEqualTo(1)
                }

                it("returns collapsing true") {
                    assertThat(collapsingDiff.collapsing()).isTrue()
                }

                it("returns transitions size 1") {
                    assertThat(collapsingDiff.transitions().keys.size).isEqualTo(1)
                }

                it("returns transitions values size by key 10") {
                    assertThat(collapsingDiff.transitions().getValue(cluster).size).isEqualTo(10)
                }

                it("returns transitions values by key contains all current clusters") {
                    assertThat(collapsingDiff.transitions().getValue(cluster)
                            .containsAll(currentClusters)).isTrue()
                }

                describe("Expanding diff") {
                    val currentClusters = mutableSetOf<Cluster>()
                    val newClusters = mutableSetOf<Cluster>()
                    val cluster = DefaultCluster(LatLng(Random.nextDouble(), Random.nextDouble()))
                    initClusters(cluster, newClusters)
                    currentClusters.add(cluster)
                    val expandingDiff = DefaultClustersDiff(currentClusters, newClusters)

                    it("returns current clusters size 1") {
                        assertThat(expandingDiff.currentClusters().size).isEqualTo(1)
                    }

                    it("returns new clusters size 10") {
                        assertThat(expandingDiff.newClusters().size).isEqualTo(10)
                    }

                    it("returns collapsing false") {
                        assertThat(expandingDiff.collapsing()).isFalse()
                    }

                    it("returns transitions size 1") {
                        assertThat(expandingDiff.transitions().keys.size).isEqualTo(1)
                    }

                    it("returns transitions values size by key 10") {
                        assertThat(expandingDiff.transitions().getValue(cluster).size).isEqualTo(10)
                    }

                    it("returns transitions values by key contains all new clusters") {
                        assertThat(expandingDiff.transitions().getValue(cluster)
                                .containsAll(newClusters)).isTrue()
                    }
                }
            }
        }
    }
})
