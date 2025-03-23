package com.example.scholarly

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import API.PastLogEntry
import java.text.SimpleDateFormat
import java.util.*

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
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val apiDateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        // Log raw values for debugging
        Log.d("ADAPTER_RAW", "Position $position - time_in: '${log.time_in}', time_out: '${log.time_out}'")

        // Parse time_in to get date and time
        if (log.time_in.isNotEmpty()) {
            try {
                val timeInDate = apiDateTimeFormat.parse(log.time_in)
                if (timeInDate != null) {
                    val date = dateFormat.format(timeInDate)
                    val timeIn = timeFormat.format(timeInDate)

                    // Parse time_out using the same format
                    val timeOut = log.time_out?.let { timeOutStr ->
                        try {
                            val timeOutDate = apiDateTimeFormat.parse(timeOutStr)
                            timeFormat.format(timeOutDate)
                        } catch (e: Exception) {
                            Log.e("ADAPTER_ERROR", "Failed to parse time_out: $timeOutStr", e)
                            "N/A"
                        }
                    } ?: "N/A"

                    holder.dateText.text = date
                    holder.timeText.text = "In: $timeIn - Out: $timeOut (${log.total_hours ?: 0.0} hrs, ${log.status})"
                    Log.d("ADAPTER_SUCCESS", "Position $position - Date: $date, Time In: $timeIn, Time Out: $timeOut")
                } else {
                    throw IllegalArgumentException("Parsed time_in is null")
                }
            } catch (e: Exception) {
                Log.e("ADAPTER_ERROR", "Failed to parse time_in: ${log.time_in}", e)
                holder.dateText.text = "Unknown Date"
                holder.timeText.text = "In: Unknown - Out: ${log.time_out ?: "N/A"} (${log.total_hours ?: 0.0} hrs, ${log.status})"
            }
        } else {
            Log.w("ADAPTER_EMPTY", "time_in is empty for log: $log")
            holder.dateText.text = "No Date"
            holder.timeText.text = "In: N/A - Out: ${log.time_out ?: "N/A"} (${log.total_hours ?: 0.0} hrs, ${log.status})"
        }
    }

    override fun getItemCount(): Int = logs.size
}