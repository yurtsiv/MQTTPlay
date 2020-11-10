package com.example.mqttplay.adapter

import android.content.Context
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mqttplay.R
import com.example.mqttplay.model.Broker

class BrokerItemAdapter(private val context: Context, private val dataSet: List<Broker>) :
    RecyclerView.Adapter<BrokerItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnCreateContextMenuListener {
        val titleView: TextView = view.findViewById(R.id.item_title)
        val subTitleView: TextView = view.findViewById(R.id.item_subtitle)

        init {
            view.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu?.add(adapterPosition, R.id.edit_broker_menu_item, Menu.NONE, R.string.broker_menu_edit)
            menu?.add(
                adapterPosition,
                R.id.remove_broker_menu_item,
                Menu.NONE,
                R.string.broker_menu_remove
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.broker_list_item, parent, false)

        return ItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = dataSet[position]
        holder.titleView.text = item.label
        holder.subTitleView.text = item.address
    }

    override fun getItemCount() = dataSet.size
}