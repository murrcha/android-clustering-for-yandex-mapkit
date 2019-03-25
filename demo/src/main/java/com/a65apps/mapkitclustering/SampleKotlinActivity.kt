package com.a65apps.mapkitclustering

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.a65apps.clustering.core.Cluster
import com.a65apps.clustering.core.DefaultCluster
import com.a65apps.clustering.core.VisibleRect
import com.a65apps.clustering.core.algorithm.*
import com.a65apps.clustering.core.view.ClusterRenderer
import com.a65apps.clustering.yandex.YandexClusterManager
import com.a65apps.clustering.yandex.extention.toLatLng
import com.a65apps.clustering.yandex.view.ClusterPinProvider
import com.a65apps.clustering.yandex.view.TapListener
import com.a65apps.clustering.yandex.view.YandexClusterRenderer
import com.a65apps.clustering.yandex.view.YandexRenderConfig
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.PlacemarkMapObject
import kotlinx.android.synthetic.main.activity_main.*

class SampleKotlinActivity : AppCompatActivity() {
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var amount: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var clusterPinProvider: ClusterPinProvider
    private lateinit var clusterManager: YandexClusterManager
    private lateinit var clusterRenderer: ClusterRenderer<YandexRenderConfig>
    private lateinit var parameter: DefaultAlgorithmParameter
    private lateinit var map: Map

    private var toast: Toast? = null
    private var selectedCluster: Cluster? = null
    private val testMarkers = mutableSetOf<Cluster>()
    private val tapListener = object : TapListener {
        override fun clusterTapped(cluster: Cluster, mapObject: PlacemarkMapObject) {
            showToast(cluster.toString())
            selectedCluster = if (cluster.isCluster()) {
                cluster.items().first()
            } else {
                cluster
            }
        }
    }

    private val inputListener = object : InputListener {
        override fun onMapLongTap(map: Map, point: Point) {
            testMarkers.add(DefaultCluster(point.toLatLng()))
            clusterManager.addItem(DefaultCluster(point.toLatLng()))
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
        title = "Kotlin version"
        initViews()

        clusterPinProvider = CustomPinProvider(this)
        map = mapView.map
        val renderConfig = YandexRenderConfig()
        clusterRenderer = YandexClusterRenderer(map, clusterPinProvider, renderConfig,
                tapListener)
        parameter = DefaultAlgorithmParameter(
                VisibleRect(
                        map.visibleRegion.topLeft.toLatLng(),
                        map.visibleRegion.bottomRight.toLatLng()),
                map.cameraPosition.zoom.toInt()
        )
        initClusterManager(NonHierarchicalDistanceBasedAlgorithm(CustomClusterProvider()))
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
            R.id.set_markers -> setTestPoints(100)
            R.id.clear_markers -> clearTestPoints()
            R.id.switch_activity -> switchActivity()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun switchActivity() {
        val intent = Intent(this, SampleJavaActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setTestPoints(amount: Int) {
        val markers = mutableSetOf<Cluster>()
        clusterManager.clearItems()
        for (i in 0 until amount) {
            val point = TestData.randomPoint()
            markers.add(DefaultCluster(point.toLatLng(), null))
        }
        clusterManager.setItems(markers)
    }

    private fun clearTestPoints() {
        clusterManager.clearItems()
    }

    private fun addTestPoint() {
        val point = TestData.randomPoint()
        val marker = DefaultCluster(point.toLatLng(), null)
        testMarkers.add(marker)
        clusterManager.addItem(marker)
    }

    private fun removeTestPoint() {
        selectedCluster?.let {
            clusterManager.removeItem(selectedCluster as Cluster)
        }
    }

    private fun addTestPoints(amount: Int) {
        for (i in 0 until amount) {
            val marker = DefaultCluster(TestData.randomPoint().toLatLng())
            testMarkers.add(marker)
        }
        clusterManager.addItems(testMarkers)
    }

    private fun removeTestPoints() {
        clusterManager.removeItems(testMarkers)
        testMarkers.clear()
    }

    private fun showToast(text: String) {
        toast?.cancel()
        toast = Toast.makeText(this, text, Toast.LENGTH_SHORT).also { it.show() }
    }

    private fun initViews() {
        val bottomSheet = findViewById<LinearLayout>(R.id.bottom_sheet)
        bottomSheet.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
            } else if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
        val bar = bottomSheet.findViewById(R.id.clusters_amount) as SeekBar
        amount = bottomSheet.findViewById(R.id.amount) as TextView
        bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar,
                                           progress: Int,
                                           fromUser: Boolean) {
                amount.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        bottomSheetBehavior = BottomSheetBehavior.from<LinearLayout>(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        radioGroup = bottomSheet.findViewById(R.id.radio_group)
        val setParams = bottomSheet.findViewById(R.id.set_params) as Button
        setParams.setOnClickListener {
            clusterManager.clearItems()
            initClusterManager(setAlgorithm())
            setTestPoints(Integer.valueOf(amount.text.toString()))
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun initClusterManager(algorithm: Algorithm<DefaultAlgorithmParameter>) {
        clusterManager = YandexClusterManager(clusterRenderer, algorithm, parameter)
        map.addCameraListener(clusterManager)
        map.addInputListener(inputListener)
        map.move(TestData.CAMERA_POSITION)
    }

    private fun setAlgorithm(): Algorithm<DefaultAlgorithmParameter> {
        val provider = CustomClusterProvider()
        val radioButtonId = radioGroup.checkedRadioButtonId
        when (radioButtonId) {
            R.id.cache_distance_based -> return CacheNonHierarchicalDistanceBasedAlgorithm(provider)
            R.id.view_based -> return NonHierarchicalViewBasedAlgorithm(provider)
            R.id.grid_based -> return GridBasedAlgorithm(provider)
            else -> return NonHierarchicalDistanceBasedAlgorithm(provider)
        }
    }
}
