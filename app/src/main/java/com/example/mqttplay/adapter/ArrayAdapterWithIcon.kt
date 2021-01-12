package com.example.mqttplay.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.mqttplay.R

public class ArrayAdapterWithIcon(
    context: Context,
    val items: List<String>,
    val images: List<Int>
) : ArrayAdapter<String>(context, R.layout.list_with_icon_item, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)

        val textView = view.findViewById<TextView>(R.id.list_with_icon_item_text)
        textView?.setCompoundDrawablesWithIntrinsicBounds(images[position], 0, 0, 0)
        textView?.compoundDrawablePadding = 70

        return textView;
    }
}