package com.a65apps.clustering.core

import com.a65apps.clustering.core.algorithm.DefaultAlgorithmParameter
import com.a65apps.clustering.core.algorithm.NonHierarchicalDistanceBasedAlgorithm
import com.a65apps.clustering.yandex.view.YandexClusterRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.mockito.Mockito.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.random.Random

object DefaultClusterManagerTest : Spek({
    describe("Tests:") {
        @UseExperimental(kotlinx.coroutines.ObsoleteCoroutinesApi::class)
        val mainThreadSurrogate = newSingleThreadContext("UI")
        before {
            @UseExperimental(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
            Dispatchers.setMain(mainThreadSurrogate)
        }

        after {
            @UseExperimental(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
            Dispatchers.resetMain()
            mainThreadSurrogate.close()
        }

        val renderer = mock(YandexClusterRenderer::class.java)
        val algorithm = mock(NonHierarchicalDistanceBasedAlgorithm::class.java)
        val visibleRect = mock(VisibleRect::class.java)
        val zoom = 10
        val parameter = DefaultAlgorithmParameter(visibleRect, zoom)
        val manager = DefaultClusterManager(renderer, algorithm, parameter)

        context("Manager call clearItems") {
            it("Algorithm call clearItems") {
                runBlocking {
                    launch(Dispatchers.Main) {
                        manager.clearItems()
                        verify(algorithm, times(1)).clearItems()
                        verifyNoMoreInteractions(algorithm)
                    }
                }
            }
        }

        context("Manager call setItems") {
            it("Algorithm call addItems") {
                runBlocking {
                    launch(Dispatchers.Main) {
                        manager.setItems(emptySet())
                        verify(algorithm, times(1)).addItems(emptySet())
                        verifyNoMoreInteractions(algorithm)
                    }
                }
            }
        }

        context("Manager call addItem") {
            it("Algorithm call addItem") {
                runBlocking {
                    launch(Dispatchers.Main) {
                        val item = DefaultCluster(LatLng(Random.nextDouble(), Random.nextDouble()))
                        manager.addItem(item)
                        verify(algorithm, times(1)).addItem(item)
                    }
                }
            }
        }

        context("Manager call removeItem") {
            it("Algorithm call removeItem") {
                runBlocking {
                    launch(Dispatchers.Main) {
                        val item = DefaultCluster(LatLng(Random.nextDouble(), Random.nextDouble()))
                        manager.removeItem(item)
                        verify(algorithm, times(1)).removeItem(item)
                    }
                }
            }
        }

        context("Manager call addItems") {
            it("Algorithm call addItems") {
                runBlocking {
                    launch(Dispatchers.Main) {
                        manager.addItems(emptySet())
                        verify(algorithm, times(2)).addItems(emptySet())
                    }
                }
            }
        }

        context("Manager call removeItems") {
            it("Algorithm call removeItems") {
                runBlocking {
                    launch(Dispatchers.Main) {
                        manager.removeItems(emptySet())
                        verify(algorithm, times(1)).removeItems(emptySet())
                    }
                }
            }
        }
    }
})
