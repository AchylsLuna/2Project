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
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault()) // 12-hour format with AM/PM
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        if (log.time_in.isNotEmpty()) {
            try {
                val dateTime = dateTimeFormat.parse(log.time_in)
                if (dateTime != null) {
                    val dateIn = dateFormat.format(dateTime)
                    val timeIn = timeFormat.format(dateTime)
                    val timeOut = log.time_out?.let { timeFormat.format(dateTimeFormat.parse(it)) } ?: "N/A"
                    holder.dateText.text = dateIn
                    holder.timeText.text = "In: $timeIn - Out: $timeOut (${log.total_hours ?: 0.0} hrs, ${log.status})"
                    Log.d("ADAPTER_DEBUG", "Parsed: date=$dateIn, time_in=$timeIn, time_out=$timeOut")
                } else {
                    throw IllegalArgumentException("Parsed dateTime is null")
                }
            } catch (e: Exception) {
                Log.e("TIMESTAMP_ERROR", "Failed to parse timestamp: time_in=${log.time_in}, time_out=${log.time_out}", e)
                holder.dateText.text = "Invalid Date"
                holder.timeText.text = "In: N/A - Out: ${log.time_out ?: "N/A"} (${log.total_hours ?: 0.0} hrs, ${log.status})"
            }
        } else {
            Log.e("DATA_ERROR", "Empty time_in for log: $log")
            holder.dateText.text = "Invalid Date"
            holder.timeText.text = "In: N/A - Out: ${log.time_out ?: "N/A"} (${log.total_hours ?: 0.0} hrs, ${log.status})"
        }
    }

    override fun getItemCount(): Int = logs.size
}