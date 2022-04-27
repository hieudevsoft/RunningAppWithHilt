package com.devapp.runningapp.ui.fragments

import android.content.Context
import android.widget.TextView
import com.devapp.runningapp.R
import com.devapp.runningapp.model.Run
import com.devapp.runningapp.util.TrackingUtils
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView(
    val runs:List<Run>,
    c:Context,
    val layoutID:Int
):MarkerView(c,layoutID) {

    override fun getOffset(): MPPointF {
        return MPPointF(-width/2f,-height.toFloat())
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if(e==null) return
        val currentIndex = e.x.toInt()
        val run = runs[currentIndex]
        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timeStamp!!
        }
        val dateFormat = SimpleDateFormat("dd-MM-yy", Locale.getDefault())
        findViewById<TextView>(R.id.tvDate).text = dateFormat.format(calendar.time)

        val avgSpeed = "${run.avgSpeedInKMH}km/h"
        findViewById<TextView>(R.id.tvAvgSpeed).text = avgSpeed

        val distanceInKm = "${run.distanceInMeters?.div(1000f)}km"
        findViewById<TextView>(R.id.tvDistance).text = distanceInKm

        findViewById<TextView>(R.id.tvDuration).text = TrackingUtils.getFormattedStopWatchTime(run.timeInRun!!)

        val caloriesBurned = "${run.caloriesBurned}kcal"
        findViewById<TextView>(R.id.tvCaloriesBurned).text = caloriesBurned

    }
}