package com.a65apps.clustering.yandex

import com.a65apps.clustering.core.ClusterItem
import com.a65apps.clustering.core.Point

data class YandexItem(val position: Point,
                      val title: String,
                      val snippet: String) : ClusterItem {
    override fun position(): Point {
        return position
    }

    override fun title(): String {
        return title
    }

    override fun snippet(): String {
        return snippet
    }
}