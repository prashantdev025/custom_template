package com.editor.template

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TemplateAdapter(
    private val items: List<TemplateItem>,
    private val onItemClick: (TemplateItem) -> Unit
) : RecyclerView.Adapter<TemplateAdapter.TemplateViewHolder>() {

    data class TemplateItem(val frameNumber: Int, val layoutResId: Int)

    inner class TemplateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLabel: TextView = view.findViewById(R.id.tvFrameLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_template, parent, false)
        return TemplateViewHolder(view)
    }

    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
        val item = items[position]
        holder.tvLabel.text = "Frame ${item.frameNumber}"
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size
}