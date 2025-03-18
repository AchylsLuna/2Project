package com.example.scholarly

import API.PastLogEntry
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TimeLogAdapter(private val logs: List<PastLogEntry>) :
    RecyclerView.Adapter<TimeLogAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val logDate: TextView = view.findViewById(R.id.log_date)
        val logTimeIn: TextView = view.findViewById(R.id.log_time_in)
        val logTimeOut: TextView = view.findViewById(R.id.log_time_out)
        val logStatus: TextView = view.findViewById(R.id.log_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.log_entry_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val log = logs[position]
        holder.logTimeIn.text = log.time_in
        holder.logTimeOut.text = log.time_out ?: "N/A"
        holder.logStatus.text = log.status
    }

    override fun getItemCount(): Int = logs.size
}
