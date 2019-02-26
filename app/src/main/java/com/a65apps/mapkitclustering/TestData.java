package com.a65apps.mapkitclustering;

import com.a65apps.mapkitcluster.ClusterAnimator;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;

public class TestData {

    public static final int POINTS_COUNT = 100;
    @NonNull
    public static final Point POINT = new Point(56.863069, 53.219774);
    @NonNull
    public static final List<Point> POINTS_LIST = new ArrayList<>(1000);
    @NonNull
    public static final Point CLUSTER_POINT_0;
    @NonNull
    public static final CameraPosition CAMERA_POSITION;
    private static final float MIN_LAT = 56.837725f;
    private static final float MAX_LAT = 56.866513f;
    private static final float MIN_LON = 53.205989f;
    private static final float MAX_LON = 53.233493f;

    static {
        for (int i = 0; i < POINTS_COUNT; i++) {
            Random r = new Random();
            float lat = MIN_LAT + r.nextFloat() * (MAX_LAT - MIN_LAT);
            float lon = MIN_LON + r.nextFloat() * (MAX_LON - MIN_LON);
            POINTS_LIST.add(new Point(lat, lon));
        }

        CLUSTER_POINT_0 = ClusterAnimator.calcCenter(POINTS_LIST);
        CAMERA_POSITION = new CameraPosition(CLUSTER_POINT_0, 14.0f, 0.0f, 0.0f);
    }
}
