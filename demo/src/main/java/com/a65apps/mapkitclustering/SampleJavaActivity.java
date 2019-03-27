package com.a65apps.mapkitclustering;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.a65apps.clustering.core.Cluster;
import com.a65apps.clustering.core.DefaultCluster;
import com.a65apps.clustering.core.VisibleRect;
import com.a65apps.clustering.core.algorithm.Algorithm;
import com.a65apps.clustering.core.algorithm.CacheNonHierarchicalDistanceBasedAlgorithm;
import com.a65apps.clustering.core.algorithm.DefaultAlgorithmParameter;
import com.a65apps.clustering.core.algorithm.GridBasedAlgorithm;
import com.a65apps.clustering.core.algorithm.NonHierarchicalDistanceBasedAlgorithm;
import com.a65apps.clustering.core.algorithm.NonHierarchicalViewBasedAlgorithm;
import com.a65apps.clustering.core.view.ClusterRenderer;
import com.a65apps.clustering.yandex.YandexClusterManager;
import com.a65apps.clustering.yandex.extention.PointExtKt;
import com.a65apps.clustering.yandex.view.ClusterPinProvider;
import com.a65apps.clustering.yandex.view.TapListener;
import com.a65apps.clustering.yandex.view.YandexClusterRenderer;
import com.a65apps.clustering.yandex.view.YandexRenderConfig;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class SampleJavaActivity extends AppCompatActivity {

    private BottomSheetBehavior bottomSheetBehavior;
    private TextView amount;
    private Spinner spinner;

    private Set<Cluster> testMarkers = new HashSet<>();
    private YandexClusterManager clusterManager;
    private ClusterRenderer<YandexRenderConfig> clusterRenderer;
    private DefaultAlgorithmParameter parameter;
    private Toast toast;
    private Map map;
    private final InputListener inputListener = new InputListener() {
        @Override
        public void onMapTap(@NonNull Map map,
                             @NonNull Point point) {
            showToast(point.toString());
        }

        @Override
        public void onMapLongTap(@NonNull Map map,
                                 @NonNull Point point) {
            testMarkers.add(new DefaultCluster(PointExtKt.toLatLng(point), null));
            clusterManager.addItem(new DefaultCluster(PointExtKt.toLatLng(point), null));
            showToast(point.toString());
        }
    };
    private Cluster selectedCluster;
    private final TapListener tapListener = new TapListener() {
        @Override
        public void clusterTapped(@NotNull Cluster cluster,
                                  @NotNull PlacemarkMapObject mapObject) {
            showToast(cluster.toString());
            selectedCluster = (cluster.isCluster()) ? cluster.items().iterator().next() : cluster;
        }
    };

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MapKitFactory.setApiKey(BuildConfig.YANDEX_MAP_KIT_KEY);
        MapKitFactory.initialize(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Java version");
        initViews();

        ClusterPinProvider clusterPinProvider = new CustomPinProvider(this);
        mapView = findViewById(R.id.mapView);
        map = mapView.getMap();
        YandexRenderConfig renderConfig = new YandexRenderConfig();
        clusterRenderer =
                new YandexClusterRenderer(map, clusterPinProvider, renderConfig, tapListener,
                        "RENDER_LAYER");
        parameter = new DefaultAlgorithmParameter(
                new VisibleRect(
                        PointExtKt.toLatLng(map.getVisibleRegion().getTopLeft()),
                        PointExtKt.toLatLng(map.getVisibleRegion().getBottomRight())),
                (int) map.getCameraPosition().getZoom()
        );
        initClusterManager(new NonHierarchicalDistanceBasedAlgorithm(new CustomClusterProvider()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        MapKitFactory.getInstance().onStop();
        mapView.onStop();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_activity: {
                switchActivity();
                break;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
        return true;
    }

    private void switchActivity() {
        Intent intent = new Intent(this, SampleKotlinActivity.class);
        startActivity(intent);
        finish();
    }

    private void setTestPoints(int amount) {
        Set<Cluster> markers = new HashSet<>();
        clusterManager.clearItems();
        for (int i = 0; i < amount; i++) {
            Point point = TestData.INSTANCE.randomPoint();
            markers.add(new DefaultCluster(PointExtKt.toLatLng(point), null));
        }
        clusterManager.setItems(markers);
    }

    private void clearTestPoints() {
        clusterManager.clearItems();
    }

    private void addTestPoint() {
        Point point = TestData.INSTANCE.randomPoint();
        Cluster marker = new DefaultCluster(PointExtKt.toLatLng(point), null);
        testMarkers.add(marker);
        clusterManager.addItem(marker);
    }

    private void removeTestPoint() {
        if (selectedCluster != null) {
            clusterManager.removeItem(selectedCluster);
        }
    }

    private void addTestPoints(int count) {
        for (int i = 0; i < count; i++) {
            Cluster marker =
                    new DefaultCluster(PointExtKt.toLatLng(TestData.INSTANCE.randomPoint()), null);
            testMarkers.add(marker);
        }
        clusterManager.addItems(testMarkers);
    }

    private void removeTestPoints() {
        clusterManager.removeItems(testMarkers);
        testMarkers.clear();
    }

    private void showToast(@NonNull String text) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void initViews() {
        ConstraintLayout bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
        final SeekBar bar = bottomSheet.findViewById(R.id.clusters_amount);
        amount = bottomSheet.findViewById(R.id.amount);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar,
                                          int progress,
                                          boolean fromUser) {
                int value = progress / 10 * 10;
                amount.setText(String.valueOf(value));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        Button setParams = bottomSheet.findViewById(R.id.set_params);
        setParams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clusterManager.clearItems();
                initClusterManager(setAlgorithm());
                setTestPoints(Integer.valueOf(amount.getText().toString()));
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        spinner = bottomSheet.findViewById(R.id.algorithm_spinner);
        Button minus = bottomSheet.findViewById(R.id.minus);
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int progress = bar.getProgress();
                if (progress > 0) {
                    bar.setProgress(progress - 10);
                }
            }
        });
        Button plus = bottomSheet.findViewById(R.id.plus);
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int progress = bar.getProgress();
                if (progress < 10000) {
                    bar.setProgress(progress + 10);
                }
            }
        });
        Button clear = bottomSheet.findViewById(R.id.clear_clusters);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearTestPoints();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        Button addOne = bottomSheet.findViewById(R.id.add_one);
        addOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTestPoint();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        Button addSome = bottomSheet.findViewById(R.id.add_some);
        addSome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTestPoints(bar.getProgress());
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        Button removeSelected = bottomSheet.findViewById(R.id.remove_selected);
        removeSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeTestPoint();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        Button removeAdded = bottomSheet.findViewById(R.id.remove_added);
        removeAdded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeTestPoints();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
    }

    private void initClusterManager(Algorithm<DefaultAlgorithmParameter> algorithm) {
        clusterManager = new YandexClusterManager(clusterRenderer, algorithm, parameter);
        map.addCameraListener(clusterManager);
        map.addInputListener(inputListener);
        map.move(TestData.INSTANCE.getCAMERA_POSITION());
    }

    private Algorithm<DefaultAlgorithmParameter> setAlgorithm() {
        CustomClusterProvider provider = new CustomClusterProvider();
        switch (spinner.getSelectedItem().toString()) {
            case "Distance based with cache":
                return new CacheNonHierarchicalDistanceBasedAlgorithm(provider);
            case "View based":
                return new NonHierarchicalViewBasedAlgorithm(provider);
            case "Grid based":
                return new GridBasedAlgorithm(provider);
            default:
                return new NonHierarchicalDistanceBasedAlgorithm(provider);
        }
    }
}
