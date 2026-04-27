package com.example.ps_inspection

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ArchiveAdapter(
    private var records: List<ArchiveItem>,
    private val onItemClick: (ArchiveItem) -> Unit
) : RecyclerView.Adapter<ArchiveAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_archive_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(records[position])
    }

    override fun getItemCount() = records.size

    fun updateData(newRecords: List<ArchiveItem>) {
        records = newRecords
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val tvIcon: TextView = itemView.findViewById(R.id.tvIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvDefects: TextView = itemView.findViewById(R.id.tvDefects)

        fun bind(record: ArchiveItem) {
            val equipmentType = record.equipmentType

            tvIcon.text = when {
                equipmentType.contains("ОРУ-35") -> "⚡"
                equipmentType.contains("ОРУ-220") -> "🔌"
                equipmentType.contains("ОРУ-500") -> "⚡"
                equipmentType.contains("АТГ") -> "🏗️"
                equipmentType.contains("Здания") -> "🏢"
                else -> "📋"
            }

            tvTitle.text = equipmentType
            tvDate.text = record.displayDate
            tvDefects.text = "✅ Без замечаний"
            tvDefects.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))

            itemView.setOnClickListener { onItemClick(record) }
        }
    }
}