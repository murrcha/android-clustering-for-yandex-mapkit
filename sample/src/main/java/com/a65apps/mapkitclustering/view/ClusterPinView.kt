package com.a65apps.mapkitclustering.view

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.a65apps.mapkitclustering.R
import kotlinx.android.synthetic.main.pin_cluster.view.*

class ClusterPinView(context: Context) : FrameLayout(context) {
    init {
        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.pin_cluster, this)
    }

    fun setText(text: String) {
        pinTextView?.text = text
    }
}
