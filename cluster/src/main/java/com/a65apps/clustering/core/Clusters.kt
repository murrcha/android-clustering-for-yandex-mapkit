package com.a65apps.clustering.core

data class Clusters(val actualMarkers: Set<Marker>,
                    val newMarkers: Set<Marker> = emptySet(),
                    val transitions: Map<Marker, Set<Marker>> = emptyMap(),
                    val isCollapsed: Boolean = false)
