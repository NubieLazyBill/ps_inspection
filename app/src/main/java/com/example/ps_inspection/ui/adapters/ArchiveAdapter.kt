package com.example.ps_inspection.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ps_inspection.data.repositories.ArchiveItem
import com.example.ps_inspection.data.utils.FillStatus
import com.example.ps_inspection.R
import com.example.ps_inspection.databinding.ItemArchiveCardBinding

class ArchiveAdapter(
    private var records: List<ArchiveItem>,
    private val onMenuClick: (ArchiveItem, View) -> Unit
) : RecyclerView.Adapter<ArchiveAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemArchiveCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(records[position])
    }

    override fun getItemCount() = records.size

    fun updateData(newRecords: List<ArchiveItem>) {
        records = newRecords
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemArchiveCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: ArchiveItem) {
            binding.tvTitle.text = "Осмотр"

            val parts = record.displayDate.split(" ")
            if (parts.size >= 2) {
                binding.tvDate.text = parts[0]
                binding.tvTime.text = parts[1]
            } else {
                binding.tvDate.text = record.displayDate
                binding.tvTime.text = ""
            }

            // Проценты из ArchiveItem (уже посчитаны)
            updateProgressBadge(binding.tvProgressOru35Archive, record.progressOru35)
            updateProgressBadge(binding.tvProgressOru220Archive, record.progressOru220)
            updateProgressBadge(binding.tvProgressOru500Archive, record.progressOru500)
            updateProgressBadge(binding.tvProgressAtgArchive, record.progressAtg)
            updateProgressBadge(binding.tvProgressBuildingsArchive, record.progressBuildings)

            binding.tvInspector.text = if (record.inspectorName.isNotBlank()) record.inspectorName else ""
            binding.tvSource.text = if (record.fileName.startsWith("server_")) "☁️ Сервер" else "📱 Локально"

            binding.btnMenu.setOnClickListener { view ->
                onMenuClick(record, view)
            }
        }

        private fun updateProgressBadge(textView: TextView, progress: Int) {
            textView.text = "$progress%"
            when {
                progress == 0 -> {
                    textView.setTextColor(0xFF9E9E9E.toInt())
                    textView.setBackgroundResource(R.drawable.bg_progress_empty)
                }
                progress == 100 -> {
                    textView.setTextColor(0xFF4CAF50.toInt())
                    textView.setBackgroundResource(R.drawable.bg_progress_complete)
                }
                else -> {
                    textView.setTextColor(0xFFFF9800.toInt())
                    textView.setBackgroundResource(R.drawable.bg_progress_partial)
                }
            }
        }
    }
}