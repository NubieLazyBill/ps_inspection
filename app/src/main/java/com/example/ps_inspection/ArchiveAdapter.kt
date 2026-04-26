package com.example.ps_inspection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.ps_inspection.databinding.ItemArchiveBinding

class ArchiveAdapter(
    private val items: List<ArchiveItem>,
    private val onItemClick: (ArchiveItem) -> Unit
) : RecyclerView.Adapter<ArchiveAdapter.ViewHolder>() {

    // Ресурсы рамок
    private val frameBackgrounds = mapOf(
        FillStatus.EMPTY to R.drawable.bg_status_frame_empty,
        FillStatus.PARTIAL to R.drawable.bg_status_frame_partial,
        FillStatus.FULL to R.drawable.bg_status_frame_full
    )

    inner class ViewHolder(private val binding: ItemArchiveBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ArchiveItem) {
            binding.tvArchiveDate.text = item.displayDate

            // Применяем фон к каждому фрейму
            binding.frameOru35.setBackgroundResource(frameBackgrounds[item.statusORU35]!!)
            binding.frameOru220.setBackgroundResource(frameBackgrounds[item.statusORU220]!!)
            binding.frameOru500.setBackgroundResource(frameBackgrounds[item.statusORU500]!!)
            binding.frameAtg.setBackgroundResource(frameBackgrounds[item.statusATG]!!)
            binding.frameBuildings.setBackgroundResource(frameBackgrounds[item.statusBuildings]!!)

            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemArchiveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size
}