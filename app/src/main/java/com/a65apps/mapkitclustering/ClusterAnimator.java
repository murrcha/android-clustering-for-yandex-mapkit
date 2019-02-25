package com.a65apps.mapkitclustering;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;

import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.PlacemarkMapObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

public class ClusterAnimator {
    @NonNull
    public static ValueAnimator move(final @NonNull List<PlacemarkMapObject> objects, @NonNull final List<Point> to) {
        if (objects.size() <= 0 || to.size() != objects.size()) {
            throw new IllegalArgumentException("Wrong objects or points count");
        }
        Point clusterPoint = calcCenter(to);
        return move(objects, to, clusterPoint);
    }

    @NonNull
    public static ValueAnimator move(final @NonNull List<PlacemarkMapObject> objects, @NonNull final List<Point> to,
            @NonNull Point clusterPoint) {
        if (objects.size() <= 0 || to.size() != objects.size()) {
            throw new IllegalArgumentException("Wrong objects or points count");
        }
        final Map<PlacemarkMapObject, Double> deltaX = new HashMap<>(to.size());
        final Map<PlacemarkMapObject, Double> deltaY = new HashMap<>(to.size());

        final double currentX = clusterPoint.getLatitude();
        final double currentY = clusterPoint.getLongitude();

        for (int i = 0; i < objects.size(); i++) {
            Point point = to.get(i);
            PlacemarkMapObject object = objects.get(i);
            deltaX.put(object, point.getLatitude() - currentX);
            deltaY.put(object, point.getLongitude() - currentY);
        }

        final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float factor = (float) animation.getAnimatedValue();
                for (PlacemarkMapObject object : objects) {
                    Double kx = deltaX.get(object);
                    Double ky = deltaY.get(object);
                    if (kx != null && ky != null) {
                        double lat = currentX + (factor * kx);
                        double lon = currentY + (factor * ky);
                        object.setGeometry(new Point(lat, lon));
                    }
                }
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animator.removeAllUpdateListeners();
                animation.removeListener(this);
            }
        });
        return animator;
    }

    @NonNull
    public static ValueAnimator move(@NonNull final List<PlacemarkMapObject> objects, @NonNull Point to) {
        if (objects.isEmpty()) {
            throw new IllegalArgumentException("Empty objects");
        }
        final Map<PlacemarkMapObject, Point> startPoint = new HashMap<>(objects.size());
        final Map<PlacemarkMapObject, Double> deltaX = new HashMap<>(objects.size());
        final Map<PlacemarkMapObject, Double> deltaY = new HashMap<>(objects.size());

        final double targetX = to.getLatitude();
        final double targetY = to.getLongitude();

        for (int i = 0; i < objects.size(); i++) {
            PlacemarkMapObject object = objects.get(i);
            startPoint.put(object, object.getGeometry());
            deltaX.put(object, targetX - object.getGeometry().getLatitude());
            deltaY.put(object, targetY - object.getGeometry().getLongitude());
        }

        final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float factor = (float) animation.getAnimatedValue();
                for (PlacemarkMapObject object : objects) {
                    Point start = startPoint.get(object);
                    Double kx = deltaX.get(object);
                    Double ky = deltaY.get(object);
                    if (kx != null && ky != null && start != null) {
                        double lat = start.getLatitude() + (factor * kx);
                        double lon = start.getLongitude() + (factor * ky);
                        object.setGeometry(new Point(lat, lon));
                    }
                }
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animator.removeAllUpdateListeners();
                animation.removeListener(this);
            }
        });
        return animator;
    }

    @NonNull
    public static ValueAnimator move(@NonNull Point from, @NonNull Point to,
            @NonNull final PlacemarkMapObject placemarkMapObject) {
        final double dX = to.getLatitude() - from.getLatitude();
        final double dY = to.getLongitude() - from.getLongitude();

        final double startX = from.getLatitude();
        final double startY = from.getLongitude();

        final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float factor = (float) animation.getAnimatedValue();
                double lat = startX + (factor * dX);
                double lon = startY + (factor * dY);
                placemarkMapObject.setGeometry(new Point(lat, lon));
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animator.removeAllUpdateListeners();
                animation.removeListener(this);
            }
        });
        return animator;
    }

    public static Point calcCenter(@NonNull List<Point> points) {
        int length = points.size();
        double x = 0, y = 0, z = 0;
        for (Point point : points) {
            double lat = point.getLatitude();
            double lng = point.getLongitude();
            lat = Math.toRadians(lat);
            lng = Math.toRadians(lng);
            x += Math.cos(lat) * Math.cos(lng);
            y += Math.cos(lat) * Math.sin(lng);
            z += Math.sin(lat);
        }

        x /= length;
        y /= length;
        z /= length;

        double longitude = Math.atan2(y, x);
        double hypot = Math.hypot(x, y);
        double latitude = Math.atan2(z, hypot);

        latitude = Math.toDegrees(latitude);
        longitude = Math.toDegrees(longitude);
        return new Point(latitude, longitude);
    }
}
