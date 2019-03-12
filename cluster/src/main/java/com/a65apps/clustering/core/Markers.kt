package com.a65apps.clustering.core

import android.os.Looper
import android.util.Log

//TODO: утилитный класс. Удалить после получения стабильной версии
class Markers {
    companion object {
        // Метод для проверки количества пинов до и после кластеризации
        fun count(markers: Set<Marker>): Int {
            var count = 0
            for (m in markers) {
                if (m.isCluster()) {
                    count += m.getChildrenCount()
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