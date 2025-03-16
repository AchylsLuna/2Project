package com.example.scholarly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import API.PastLogEntry
import java.text.SimpleDateFormat
import java.util.Locale
import android.util.Log

class LogsAdapter(private val logs: List<PastLogEntry>) : RecyclerView.Adapter<LogsAdapter.LogViewHolder>() {

    class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateText: TextView = itemView.findViewById(android.R.id.text1)
        val timeText: TextView = itemView.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logs[position]
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) // Adjust based on Logcat
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        try {
            val dateTime = inputFormat.parse(log.time_in)
            val dateIn = dateFormat.format(dateTime)
            val timeIn = timeFormat.format(dateTime)
            val timeOut = log.time_out?.let { timeFormat.format(inputFormat.parse(it)) } ?: "N/A"
            holder.dateText.text = dateIn
            holder.timeText.text = "In: $timeIn - Out: $timeOut (${log.total_hours} hrs, ${log.status})"
        } catch (e: Exception) {
            Log.e("TIMESTAMP_ERROR", "Failed to parse timestamp: time_in=${log.time_in}, time_out=${log.time_out}", e)
            holder.dateText.text = "Invalid Date"
            holder.timeText.text = "In: N/A - Out: ${log.time_out ?: "N/A"} (${log.total_hours} hrs, ${log.status})"
        }
    }

    override fun getItemCount(): Int = logs.size
}