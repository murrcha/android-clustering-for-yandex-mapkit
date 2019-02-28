package com.a65apps.mapkitclustering

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.a65apps.clustering.core.Point
import com.a65apps.clustering.yandex.YandexCluster
import com.a65apps.clustering.yandex.YandexItem
import com.a65apps.clustering.yandex.toPoint
import com.a65apps.clustering.yandex.view.ClusterPinProvider
import com.a65apps.clustering.yandex.view.YandexClusterRenderer
import com.yandex.mapkit.MapKitFactory
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var clusterPinProvider: ClusterPinProvider<YandexItem>
    private lateinit var clusterRenderer: YandexClusterRenderer<YandexItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey(BuildConfig.YANDEX_MAP_KIT_KEY)
        MapKitFactory.initialize(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clusterPinProvider = MainClusterPinProvider(this)
        clusterRenderer = YandexClusterRenderer(mapView.map, clusterPinProvider,
                TestData.MIN_CLUSTER_SIZE)
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        setUpMap()

        showPoints()
    }

    private fun setUpMap() {
        mapView.map.move(TestData.CAMERA_POSITION)
    }

    private fun showPoints() {
        val items0 = mutableSetOf<YandexItem>()
        val items1 = mutableSetOf<YandexItem>()
        val difItems = mutableListOf<YandexItem>()

        TestData.POINTS_LIST_0.forEach {
            items0.add(YandexItem(it.toPoint(), "", ""))
        }
        TestData.POINTS_LIST_1.forEach {
            items1.add(YandexItem(it.toPoint(), "", ""))
        }
        TestData.POINTS_LIST_DIF.forEach {
            difItems.add(YandexItem(it.toPoint(), "", ""))
        }

        clusterRenderer.clusterChanged(setOf(
                YandexCluster(Point(TestData.CLUSTER_POINT_0.latitude,
                        TestData.CLUSTER_POINT_0.longitude),
                        items0),
                YandexCluster(Point(TestData.CLUSTER_POINT_1.latitude,
                        TestData.CLUSTER_POINT_1.longitude),
                        items1),
                YandexCluster(Point(TestData.CLUSTER_POINT_0.latitude,
                        TestData.CLUSTER_POINT_0.longitude),
                        difItems)))
    }

    override fun onStop() {
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}
