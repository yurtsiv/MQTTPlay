package com.example.mqttplay.adapter

import android.content.Context
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mqttplay.R
import com.example.mqttplay.repo.RecurringTileTime
import com.example.mqttplay.repo.Tile
import com.example.mqttplay.repo.TileType

class TileItemAdapter(
    private val context: Context,
    private val dataSet: List<Tile>,
    private val onItemClick: (tile: Tile) -> Unit
) :
    RecyclerView.Adapter<TileItemAdapter.ItemViewHolder>() {
    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnCreateContextMenuListener {

        val titleView: TextView = view.findViewById(R.id.tile_list_item_title)
        val iconView: ImageView = view.findViewById(R.id.tile_list_item_icon)

        init {
            view.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu?.add(
                adapterPosition,
                R.id.edit_tile_menu_item,
                Menu.NONE,
                R.string.tile_menu_edit
            )
            menu?.add(
                adapterPosition,
                R.id.remove_tile_menu_item,
                Menu.NONE,
                R.string.tile_menu_remove
            )
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.tile_list_item, parent, false)

        return ItemViewHolder(adapterLayout)
    }

    private fun getTitleText(tile: Tile): String {
        return when (tile.type) {
            TileType.RECURRING -> {
                // TODO: DRY
                val time = tile.recurringTime as RecurringTileTime
                val h = time.hour.toString().padStart(2, '0')
                val m = time.minute.toString().padStart(2, '0');

                return "$h:$m"
            }
            TileType.BUTTON ->
                 tile.topic
        }
    }

    private fun getIcon(tile: Tile): Int {
        return when (tile.type) {
            TileType.RECURRING -> R.drawable.time
            TileType.BUTTON -> R.drawable.button
        }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val tile = dataSet[position]

        holder.titleView.text = getTitleText(tile)
        holder.iconView.setImageResource(getIcon(tile))
        holder.itemView.setOnClickListener {
            onItemClick(tile)
        };
    }

    override fun getItemCount() = dataSet.size
}