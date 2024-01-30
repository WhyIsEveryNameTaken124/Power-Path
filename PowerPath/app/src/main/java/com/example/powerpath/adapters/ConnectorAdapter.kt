package com.example.powerpath.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.powerpath.DataManager
import com.example.powerpath.R
import com.example.powerpath.classes.ConnectorItem

class ConnectorAdapter (private val items: List<ConnectorItem>, private val itemClickListener: (Int) -> Unit) :
    RecyclerView.Adapter<ConnectorAdapter.ViewHolder>() {
    var selectedItemText: String? = DataManager.connectorType

    class ViewHolder(itemView: View, clickListener: (Int) -> Unit) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textView: TextView = itemView.findViewById(R.id.tvPinTitle)
        val llConnector: LinearLayout = itemView.findViewById(R.id.llConnector)

        init {
            llConnector.setOnClickListener {
                clickListener(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_connector, parent, false)
        return ViewHolder(view) { position ->
            itemClickListener(position)
            selectedItemText = items[position].text
            notifyDataSetChanged()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        if (item.text == selectedItemText) {
            holder.llConnector.setBackgroundResource(R.drawable.blue_border)
        } else {
            holder.llConnector.setBackgroundResource(0)
        }
        holder.imageView.setImageResource(item.imageResId)
        holder.textView.text = item.text
    }

    override fun getItemCount(): Int {
        return items.size
    }
}