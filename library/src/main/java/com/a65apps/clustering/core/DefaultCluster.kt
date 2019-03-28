package com.a65apps.clustering.core

open class DefaultCluster(protected val geoCoor: LatLng,
                          protected val payload: Any? = null) : Cluster {

    private val self = setOf(this)

    private val items: MutableSet<Cluster> = mutableSetOf()

    override fun items(): Set<Cluster> = if (items.isNotEmpty()) {
        items
    } else {
        self
    }

    override fun geoCoor(): LatLng = geoCoor

    override fun payload(): Any? = payload

    override fun isCluster(): Boolean = items().size > 3

    override fun size(): Int = items().size

    override fun addItem(cluster: Cluster) = items.add(cluster)

    override fun removeItem(cluster: Cluster): Boolean = items.remove(cluster)

    override fun equals(other: Any?): Boolean {
        when {
            this === other -> return true
            javaClass != other?.javaClass -> return false
        }

        other as DefaultCluster

        when {
            geoCoor != other.geoCoor() -> return false
            payload != other.payload -> return false
            items != other.items -> return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = geoCoor.hashCode()
        result = 31 * result + (payload?.hashCode() ?: 0)
        result = 31 * result + items.hashCode()
        return result
    }

    override fun toString(): String {
        return if (isCluster()) {
            "CLUSTER \ncoords: ${geoCoor()} \nsize: ${size()} \npayload: ${payload()}"
        } else {
            "PIN \ncoords: ${geoCoor()} \npayload: ${payload()}}"
        }
    }
}
