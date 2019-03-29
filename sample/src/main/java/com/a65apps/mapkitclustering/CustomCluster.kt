package com.a65apps.mapkitclustering

import com.a65apps.clustering.core.DefaultCluster
import com.a65apps.clustering.core.LatLng

class CustomCluster(geoCoord: LatLng, payload: Any?) : DefaultCluster(geoCoord, payload) {
    override fun isCluster(): Boolean {
        return size() > 2
    }
}
