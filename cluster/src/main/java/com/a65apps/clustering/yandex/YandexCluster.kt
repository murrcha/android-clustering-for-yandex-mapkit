package com.a65apps.clustering.yandex

import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.Point

data class YandexCluster(val position: Point,
                         val items: Collection<YandexItem>) : Cluster<YandexItem> {
    override fun position() = position

    override fun items() = items

    override fun size() = items.size
}
