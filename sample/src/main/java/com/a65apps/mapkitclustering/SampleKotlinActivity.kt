package com.a65apps.mapkitclustering

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
    private lateinit var bottomSheet: ConstraintLayout
    private lateinit var amount: TextView
    private lateinit var spinner: Spinner
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
        bottomSheet = findViewById(R.id.bottom_sheet)
        bottomSheet.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
            } else if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
        amount = bottomSheet.findViewById(R.id.amount) as TextView
        val bar = bottomSheet.findViewById(R.id.clusters_amount) as SeekBar
        bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar,
                                           progress: Int,
                                           fromUser: Boolean) {
                val value = progress / 10 * 10
                amount.text = value.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        bottomSheetBehavior = BottomSheetBehavior.from<ConstraintLayout>(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        val setParams = bottomSheet.findViewById(R.id.set_params) as Button
        setParams.setOnClickListener {
            clusterManager.clearItems()
            initClusterManager(setAlgorithm())
            setTestPoints(Integer.valueOf(amount.text.toString()))
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        spinner = bottomSheet.findViewById(R.id.algorithm_spinner)
        val minus = bottomSheet.findViewById<Button>(R.id.minus)
        minus.setOnClickListener {
            if (bar.progress > 0) {
                bar.progress -= 10
            }
        }
        val plus = bottomSheet.findViewById<Button>(R.id.plus)
        plus.setOnClickListener {
            if (bar.progress < 10000) {
                bar.progress += 10
            }
        }
        val clear = bottomSheet.findViewById<Button>(R.id.clear_clusters)
        clear.setOnClickListener {
            clearTestPoints()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        val addOne = bottomSheet.findViewById<Button>(R.id.add_one)
        addOne.setOnClickListener {
            addTestPoint()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        val addSome = bottomSheet.findViewById<Button>(R.id.add_some)
        addSome.setOnClickListener {
            addTestPoints(bar.progress)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        val removeSelected = bottomSheet.findViewById<Button>(R.id.remove_selected)
        removeSelected.setOnClickListener {
            removeTestPoint()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        val removeAdded = bottomSheet.findViewById<Button>(R.id.remove_added)
        removeAdded.setOnClickListener {
            removeTestPoints()
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
        return when (spinner.selectedItem.toString()) {
            "Distance based with cache" -> CacheNonHierarchicalDistanceBasedAlgorithm(provider)
            "View based" -> NonHierarchicalViewBasedAlgorithm(provider)
            "Grid based" -> GridBasedAlgorithm(provider)
            else -> NonHierarchicalDistanceBasedAlgorithm(provider)
        }
    }
}
