package com.example.scholarly

import API.TimeLogItem
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TimeLogAdapter(private var logs: List<TimeLogItem>) :
    RecyclerView.Adapter<TimeLogAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.log_date)
        val timeIn: TextView = view.findViewById(R.id.log_time_in)
        val timeOut: TextView = view.findViewById(R.id.log_time_out)
        val status: TextView = view.findViewById(R.id.log_status) // Only keeping status
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val log = logs[position]
        holder.date.text = log.log_date
        holder.timeIn.text = log.time_in
        holder.timeOut.text = log.time_out
        holder.status.text = log.status
    }

    override fun getItemCount(): Int = logs.size

    fun updateLogs(newLogs: List<TimeLogItem>) {
        logs = newLogs
        notifyDataSetChanged()
    }
}