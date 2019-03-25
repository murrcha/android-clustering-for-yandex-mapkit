package com.a65apps.clustering.core.log

interface Logger {
    fun logMessage(message: String)
    fun logError(error: Throwable)
}