package com.a65apps.clustering.core.quadtree

import com.a65apps.clustering.core.DefaultCluster
import com.a65apps.clustering.core.LatLng
import com.a65apps.clustering.core.algorithm.NonHierarchicalDistanceBasedAlgorithm.QuadItem
import com.a65apps.clustering.core.geometry.Bounds
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.random.Random

object PointQuadTreeTest : Spek({
    describe("Tests:") {
        val bounds = Bounds(0.0, 1.0, 0.0, 1.0)
        val quadTree = PointQuadTree<QuadItem>(0.0, 1.0, 0.0, 1.0)
        val cluster = DefaultCluster(LatLng(Random.nextDouble(), Random.nextDouble()))
        val quadItem = QuadItem(cluster)
        it("Call add item to tree") {
            quadTree.add(quadItem)
            quadTree.add(quadItem)
            assertThat(quadTree.search(bounds).size).isEqualTo(1)
        }

        it("Call remove item from tree") {
            quadTree.remove(quadItem)
            assertThat(quadTree.search(bounds).isEmpty()).isTrue()
        }

        it("Call clear tree") {
            quadTree.add(quadItem)
            quadTree.clear()
            assertThat(quadTree.search(bounds).isEmpty()).isTrue()
        }

        it("Call search in empty tree") {
            val collection = quadTree.search(bounds)
            assertThat(collection.isEmpty()).isTrue()
        }

        it("Call search in not empty tree") {
            quadTree.add(quadItem)
            val collection = quadTree.search(bounds)
            assertThat(collection.isNotEmpty()).isTrue()
        }
    }
})
