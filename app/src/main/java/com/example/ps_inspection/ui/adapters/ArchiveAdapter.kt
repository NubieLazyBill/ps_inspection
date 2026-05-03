package com.example.ps_inspection.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

            setIndicator(binding.indicatorOru35Dot, record.statusORU35)
            setIndicator(binding.indicatorOru220Dot, record.statusORU220)
            setIndicator(binding.indicatorOru500Dot, record.statusORU500)
            setIndicator(binding.indicatorAtgDot, record.statusATG)
            setIndicator(binding.indicatorBuildingsDot, record.statusBuildings)

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