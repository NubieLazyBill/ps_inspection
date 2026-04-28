package com.example.ps_inspection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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
            // Фиксированный заголовок "Осмотр"
            binding.tvTitle.text = "Осмотр"

            // Разбиваем дату и время
            val parts = record.displayDate.split(" ")
            if (parts.size >= 2) {
                binding.tvDate.text = parts[0]  // дата
                binding.tvTime.text = parts[1]  // время
            } else {
                binding.tvDate.text = record.displayDate
                binding.tvTime.text = ""
            }

            // Устанавливаем индикаторы
            setIndicator(binding.indicatorOru35Dot, record.statusORU35)
            setIndicator(binding.indicatorOru220Dot, record.statusORU220)
            setIndicator(binding.indicatorOru500Dot, record.statusORU500)
            setIndicator(binding.indicatorAtgDot, record.statusATG)
            setIndicator(binding.indicatorBuildingsDot, record.statusBuildings)

            // Меню (три точки)
            binding.btnMenu.setOnClickListener { view ->
                onMenuClick(record, view)
            }
        }

        private fun setIndicator(view: View, status: FillStatus) {
            val drawableRes = when (status) {
                FillStatus.EMPTY -> R.drawable.indicator_empty
                FillStatus.PARTIAL -> R.drawable.indicator_partial
                FillStatus.FULL -> R.drawable.indicator_full
            }
            view.setBackgroundResource(drawableRes)
        }
    }
}