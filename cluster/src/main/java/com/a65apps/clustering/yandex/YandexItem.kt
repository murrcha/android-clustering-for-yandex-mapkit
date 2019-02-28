package com.a65apps.clustering.yandex

import com.a65apps.clustering.core.ClusterItem
import com.a65apps.clustering.core.Point

data class YandexItem(val position: Point,
                      val title: String,
                      val snippet: String) : ClusterItem {
    override fun position() = position

    override fun title() = title

    override fun snippet() = snippet
}
