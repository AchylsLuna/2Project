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

        val date = log.duty_date ?: "N/A"
        val timeIn = log.time_in ?: "N/A"
        val timeOut = log.time_out ?: "N/A"

        val hours = log.total_hours?.toDouble() ?: 0.0
        val formattedHours = String.format("%.1f hrs", hours)

        holder.dateText.text = date
        holder.timeText.text = "In: $timeIn - Out: $timeOut ($formattedHours, ${log.status})"
    }

    override fun getItemCount(): Int = logs.size
}