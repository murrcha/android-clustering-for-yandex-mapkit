package com.a65apps.clustering.core

import android.os.Looper
import android.util.Log

//TODO: утилитный класс. Удалить после получения стабильной версии
class Clusters {
    companion object {
        // Метод для проверки количества пинов до и после кластеризации
        fun count(clusters: Set<Cluster>): Int {
            var count = 0
            for (m in clusters) {
                if (m.isCluster()) {
                    count += m.size()
                } else {
                    count++
                }
            }
            return count
        }

        fun threadMustBeMain() {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                Log.e("MARKER", "NOT MAIN THREAD")
            } else {
                Log.d("MARKER", "MAIN THREAD")
            }
        }
    }
}