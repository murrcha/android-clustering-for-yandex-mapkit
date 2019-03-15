package com.a65apps.mapkitclustering

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.DefaultCluster
import com.a65apps.clustering.core.VisibleRectangularRegion
import com.a65apps.clustering.core.algorithm.NonHierarchicalDistanceBasedAlgorithm
import com.a65apps.clustering.core.view.AnimationParams
import com.a65apps.clustering.yandex.YandexDefaultClusterClusterManager
import com.a65apps.clustering.yandex.extention.toLatLng
import com.a65apps.clustering.yandex.view.ClusterPinProvider
import com.a65apps.clustering.yandex.view.TapListener
import com.a65apps.clustering.yandex.view.YandexClusterRenderer
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.PlacemarkMapObject
import kotlinx.android.synthetic.main.activity_main.*

const val ANIMATION_DURATION = 240L

class MainActivity : AppCompatActivity() {
    private lateinit var clusterPinProvider: ClusterPinProvider
    private lateinit var clusterManager: YandexDefaultClusterClusterManager
    private var toast: Toast? = null
    private var selectedCluster: Cluster? = null
    private val testMarkers = mutableSetOf<Cluster>()

    private val inputListener = object : InputListener {
        override fun onMapLongTap(map: Map, point: Point) {
            clusterManager.addMarker(DefaultCluster(point.toLatLng()))
            showToast(point.toString())
        }

        override fun onMapTap(map: Map, point: Point) {
            showToast(point.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey(BuildConfig.YANDEX_MAP_KIT_KEY)
        MapKitFactory.initialize(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val map = mapView.map
        clusterPinProvider = MainClusterPinProvider(this)
        clusterManager = YandexDefaultClusterClusterManager(YandexClusterRenderer(map,
                clusterPinProvider,
                AnimationParams(true, true,
                        ANIMATION_DURATION, null),
                object : TapListener {
                    override fun markerTapped(cluster: Cluster, mapObject: PlacemarkMapObject) {
                        showToast(cluster.toString())
                        selectedCluster = if (cluster.isCluster()) {
                            cluster.items().first()
                        } else {
                            cluster
                        }
                    }
                }),
                NonHierarchicalDistanceBasedAlgorithm(MainClusterProvider()),
                VisibleRectangularRegion(map.visibleRegion.topLeft.toLatLng(),
                        map.visibleRegion.bottomRight.toLatLng()))
        map.addCameraListener(clusterManager)
        map.addInputListener(inputListener)
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.add_marker -> addTestPoint()
            R.id.remove_marker -> removeTestPoint()
            R.id.add_markers -> addTestPoints(10)
            R.id.remove_markers -> removeTestPoints()
            R.id.set_markers -> setTestPoints()
            R.id.clear_markers -> clearTestPoints()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun setTestPoints() {
        val markers = mutableSetOf<Cluster>()
        TestData.POINTS_LIST.forEach {
            markers.add(DefaultCluster(it.toLatLng(), TestData.POINTS_LIST.indexOf(it)))
        }
        clusterManager.setMarkers(markers)
        mapView.map.move(TestData.CAMERA_POSITION)
    }

    private fun clearTestPoints() {
        clusterManager.clearMarkers()
    }

    private fun addTestPoint() {
        val point = TestData.randomPoint()
        val marker = DefaultCluster(point.toLatLng(), null)
        clusterManager.addMarker(marker)
    }

    private fun removeTestPoint() {
        selectedCluster?.let {
            clusterManager.removeMarker(selectedCluster as Cluster)
        }
    }

    private fun addTestPoints(amount: Int) {
        for (i in 0 until amount) {
            val marker = DefaultCluster(TestData.randomPoint().toLatLng())
            testMarkers.add(marker)
        }
        clusterManager.addMarkers(testMarkers)
    }

    private fun removeTestPoints() {
        clusterManager.removeMarkers(testMarkers)
        testMarkers.clear()
    }

    private fun showToast(text: String) {
        toast?.cancel()
        toast = Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT)
        toast?.show()
    }
}
