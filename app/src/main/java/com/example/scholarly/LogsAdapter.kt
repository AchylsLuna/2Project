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
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val apiDateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) // Matches API format

        // Log raw values for debugging
        Log.d("ADAPTER_RAW", "Position $position - duty_date: '${log.duty_date}', time_in: '${log.time_in}', time_out: '${log.time_out}'")

        // Set the date from duty_date
        holder.dateText.text = log.duty_date

        // Parse and display time_in and time_out
        if (log.time_in.isNotEmpty()) {
            try {
                val timeInDate = apiDateTimeFormat.parse(log.time_in)
                if (timeInDate != null) {
                    val timeIn = timeFormat.format(timeInDate)

                    // Parse time_out if it exists
                    val timeOut = log.time_out?.let { timeOutStr ->
                        try {
                            val timeOutDate = apiDateTimeFormat.parse(timeOutStr)
                            timeFormat.format(timeOutDate)
                        } catch (e: Exception) {
                            Log.e("ADAPTER_ERROR", "Failed to parse time_out: $timeOutStr", e)
                            "N/A"
                        }
                    } ?: "N/A"

                    holder.timeText.text = "In: $timeIn - Out: $timeOut (${log.total_hours ?: 0.0} hrs, ${log.status})"
                    Log.d("ADAPTER_SUCCESS", "Position $position - Time In: $timeIn, Time Out: $timeOut")
                } else {
                    throw IllegalArgumentException("Parsed time_in is null")
                }
            } catch (e: Exception) {
                Log.e("ADAPTER_ERROR", "Failed to parse time_in: ${log.time_in}", e)
                holder.timeText.text = "In: Unknown - Out: ${log.time_out ?: "N/A"} (${log.total_hours ?: 0.0} hrs, ${log.status})"
            }
        } else {
            Log.w("ADAPTER_EMPTY", "time_in is empty for log: $log")
            holder.timeText.text = "In: N/A - Out: ${log.time_out ?: "N/A"} (${log.total_hours ?: 0.0} hrs, ${log.status})"
        }
    }

    override fun getItemCount(): Int = logs.size
}