package com.example.bledemo.bledevices

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.bledemo.R

class DevicesListAdapter(
    private val dataSet: List<BluetoothDevice>,
    private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<DevicesListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.gatt_server_name_text_view)
        val button: Button = view.findViewById<Button>(R.id.connect_gatt_server_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_gatt_server, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = dataSet[position].alias + "\n" + dataSet[position].address
        holder.button.setOnClickListener {
            listener.onItemClick(dataSet[position])
        }
    }

    override fun getItemCount() = dataSet.size

    interface OnItemClickListener {
        fun onItemClick(item: BluetoothDevice)
    }

}
