package com.a65apps.clustering.core.log

import android.util.Log
import com.a65apps.clustering.BuildConfig

object CMLogger : Logger {
    private const val TAG = "clusterManager"

    override fun logMessage(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }

    override fun logError(error: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, error.message, error)
        }
    }
}