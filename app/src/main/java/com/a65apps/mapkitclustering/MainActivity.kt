package com.a65apps.mapkitclustering

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.a65apps.clustering.core.Marker
import com.a65apps.clustering.yandex.view.ClusterPinProvider
import com.a65apps.clustering.yandex.view.YandexClusterRenderer
import com.yandex.mapkit.MapKitFactory
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var clusterPinProvider: ClusterPinProvider
    private lateinit var clusterRenderer: YandexClusterRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey(BuildConfig.YANDEX_MAP_KIT_KEY)
        MapKitFactory.initialize(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clusterPinProvider = MainClusterPinProvider(this)
        clusterRenderer = YandexClusterRenderer(mapView.map, clusterPinProvider)
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()

        showTestPoints()
    }

    private fun showTestPoints() {
        //todo решить, что делать с кодом (для Радика)
        val items0 = mutableSetOf<Marker>()
        val items1 = mutableSetOf<Marker>()
        val difItems = mutableListOf<Marker>()

        /*TestData.POINTS_LIST_0.forEach {
            items0.add(YandexItem(it.toPoint(), "", ""))
        }
        TestData.POINTS_LIST_1.forEach {
            items1.add(YandexItem(it.toPoint(), "", ""))
        }
        TestData.POINTS_LIST_DIF.forEach {
            difItems.add(YandexItem(it.toPoint(), "", ""))
        }

        clusterRenderer.updateClusters(setOf(
                YandexCluster(LatLng(TestData.CLUSTER_POINT_0.latitude,
                        TestData.CLUSTER_POINT_0.longitude),
                        items0),
                YandexCluster(LatLng(TestData.CLUSTER_POINT_1.latitude,
                        TestData.CLUSTER_POINT_1.longitude),
                        items1),
                YandexCluster(LatLng(TestData.CLUSTER_POINT_0.latitude,
                        TestData.CLUSTER_POINT_0.longitude),
                        difItems)))*/

        mapView.map.move(TestData.CAMERA_POSITION)
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}
