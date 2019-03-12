package com.a65apps.mapkitclustering

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.a65apps.clustering.core.ClusteredMarker
import com.a65apps.clustering.core.Marker
import com.a65apps.clustering.core.VisibleRectangularRegion
import com.a65apps.clustering.core.view.AnimationParams
import com.a65apps.clustering.yandex.YandexClusterManager
import com.a65apps.clustering.yandex.extention.toLatLng
import com.a65apps.clustering.yandex.view.ClusterPinProvider
import com.a65apps.clustering.yandex.view.TapListener
import com.a65apps.clustering.yandex.view.YandexClusterRenderer
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.map.PlacemarkMapObject
import kotlinx.android.synthetic.main.activity_main.*

const val ANIMATION_DURATION = 240L

class MainActivity : AppCompatActivity() {
    private lateinit var clusterPinProvider: ClusterPinProvider
    private lateinit var clusterManager: YandexClusterManager
    private var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey(BuildConfig.YANDEX_MAP_KIT_KEY)
        MapKitFactory.initialize(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val map = mapView.map
        clusterPinProvider = MainClusterPinProvider(this)
        clusterManager = YandexClusterManager(YandexClusterRenderer(map,
                clusterPinProvider,
                object : TapListener {
                    override fun markerTapped(marker: Marker, mapObject: PlacemarkMapObject) {
                        toast?.cancel()
                        toast = Toast.makeText(applicationContext, marker.toString(),
                                Toast.LENGTH_SHORT)
                        toast?.show()
                    }
                },
                AnimationParams(true, true,
                        ANIMATION_DURATION, null)),
                VisibleRectangularRegion(map.visibleRegion.topLeft.toLatLng(),
                        map.visibleRegion.bottomRight.toLatLng()))
        map.addCameraListener(clusterManager)
        showTestPoints()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    private fun showTestPoints() {
        val markers = mutableSetOf<Marker>()
        TestData.POINTS_LIST.forEach {
            markers.add(ClusteredMarker(it.toLatLng(), null))
        }
        clusterManager.setMarkers(markers)
        mapView.map.move(TestData.CAMERA_POSITION)
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}
