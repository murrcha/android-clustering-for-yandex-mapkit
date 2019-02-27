package com.a65apps.clustering.yandex

import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.Point

data class YandexCluster(val position: Point,
                         val items: Collection<YandexItem>) : Cluster<YandexItem> {
    override fun position(): Point {
        return position
    }

    override fun items(): Collection<YandexItem> {
        return items
    }

    override fun size(): Int {
        return items.size
    }
}