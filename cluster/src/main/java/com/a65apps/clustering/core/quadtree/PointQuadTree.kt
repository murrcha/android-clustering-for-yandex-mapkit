package com.a65apps.clustering.core.quadtree

import com.a65apps.clustering.core.geometry.Bounds
import com.a65apps.clustering.core.geometry.Point

private const val MAX_ELEMENTS = 500
private const val MAX_DEPTH = 40

/**
 * A quad tree which tracks items with a Point geometry.
 * See http://en.wikipedia.org/wiki/Quadtree for details on the data structure.
 * This class is not thread safe.
 */
class PointQuadTree<T : PointQuadTree.Item>(private val bounds: Bounds, private val depth: Int) {
    constructor(bounds: Bounds) : this(bounds, 0)

    constructor(minX: Double, maxX: Double, minY: Double, maxY: Double) : this(
            Bounds(minX, maxX, minY, maxY))

    constructor(minX: Double, maxX: Double, minY: Double, maxY: Double, depth: Int) : this(
            Bounds(minX, maxX, minY, maxY), depth)

    interface Item {
        val point: Point
    }

    private val items: MutableSet<T> = mutableSetOf()
    private val children: MutableList<PointQuadTree<T>> = mutableListOf()

    /**
     * Insert an item.
     */
    fun add(item: T) {
        val point = item.point
        if (bounds.contains(point.x, point.y)) {
            insert(point.x, point.y, item)
        }
    }

    private fun insert(x: Double, y: Double, item: T) {
        if (children.isNotEmpty()) {
            if (y < bounds.midY) {
                if (x < bounds.midX) { // top left
                    children[0].insert(x, y, item)
                } else { // top right
                    children[1].insert(x, y, item)
                }
            } else {
                if (x < bounds.midX) { // bottom left
                    children[2].insert(x, y, item)
                } else {
                    children[3].insert(x, y, item)
                }
            }
            return
        }

        items.add(item)
        if (items.size > MAX_ELEMENTS && depth < MAX_DEPTH) {
            split()
        }
    }

    /**
     * Split this quad.
     */
    private fun split() {
        children.add(PointQuadTree(bounds.minX, bounds.midX, bounds.minY, bounds.midY,
                depth + 1))
        children.add(PointQuadTree(bounds.midX, bounds.maxX, bounds.minY, bounds.midY,
                depth + 1))
        children.add(PointQuadTree(bounds.minX, bounds.midX, bounds.midY, bounds.maxY,
                depth + 1))
        children.add(PointQuadTree(bounds.midX, bounds.maxX, bounds.midY, bounds.maxY,
                depth + 1))

        val items = items
        this.items.clear()

        for (item in items) {
            // re-insert items into child quads.
            insert(item.point.x, item.point.y, item)
        }
    }

    /**
     * Remove the given item from the set.
     *
     * @return whether the item was removed.
     */
    fun remove(item: T): Boolean {
        val point = item.point
        return if (bounds.contains(point.x, point.y)) {
            remove(point.x, point.y, item)
        } else {
            false
        }
    }

    private fun remove(x: Double, y: Double, item: T): Boolean {
        return if (children.isNotEmpty()) {
            if (y < bounds.midY) {
                if (x < bounds.midX) { // top left
                    children[0].remove(x, y, item)
                } else { // top right
                    children[1].remove(x, y, item)
                }
            } else {
                if (x < bounds.midX) { // bottom left
                    children[2].remove(x, y, item)
                } else {
                    children[3].remove(x, y, item)
                }
            }
        } else {
            items.remove(item)
        }
    }

    /**
     * Removes all points from the quadTree
     */
    fun clear() {
        children.clear()
        items.clear()
    }

    /**
     * Search for all items within a given bounds.
     */
    fun search(searchBounds: Bounds): Collection<T> {
        val results = mutableListOf<T>()
        search(searchBounds, results)
        return results
    }

    private fun search(searchBounds: Bounds, results: MutableCollection<T>) {
        if (!bounds.intersects(searchBounds)) {
            return
        }

        if (children.isNotEmpty()) {
            for (quad in children) {
                quad.search(searchBounds, results)
            }
        } else if (items.isNotEmpty()) {
            if (searchBounds.contains(bounds)) {
                results.addAll(items)
            } else {
                for (item in items) {
                    if (searchBounds.contains(item.point)) {
                        results.add(item)
                    }
                }
            }
        }
    }
}
