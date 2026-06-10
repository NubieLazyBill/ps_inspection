package com.example.ps_inspection.ui.fragments.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ps_inspection.R
import com.example.ps_inspection.data.utils.VoiceParsedResult

class VoiceConfirmDialog : DialogFragment() {

    private var results: List<VoiceParsedResult> = emptyList()
    private var onConfirm: ((List<VoiceParsedResult>) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        results = arguments?.getParcelableArrayList("results") ?: emptyList()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_voice_confirm, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val btnConfirm = view.findViewById<TextView>(R.id.btnConfirm)
        val btnCancel = view.findViewById<TextView>(R.id.btnCancel)

        val adapter = VoiceConfirmAdapter(results)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        btnConfirm.setOnClickListener {
            val selected = adapter.getSelectedItems()
            if (selected.isNotEmpty()) {
                onConfirm?.invoke(selected)
                dismiss()
            } else {
                Toast.makeText(context, "Выберите хотя бы одно значение", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("🎤 Подтвердите ввод")
            .create()
    }

    fun setOnConfirmListener(listener: (List<VoiceParsedResult>) -> Unit) {
        onConfirm = listener
    }

    companion object {
        fun newInstance(results: List<VoiceParsedResult>): VoiceConfirmDialog {
            val fragment = VoiceConfirmDialog()
            val args = Bundle()
            args.putParcelableArrayList("results", ArrayList(results))
            fragment.arguments = args
            return fragment
        }
    }
}

class VoiceConfirmAdapter(private val items: List<VoiceParsedResult>) :
    RecyclerView.Adapter<VoiceConfirmAdapter.ViewHolder>() {

    private val selectedItems = BooleanArray(items.size) { true } // По умолчанию всё выбрано

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_voice_confirm, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, selectedItems[position]) { isChecked ->
            selectedItems[position] = isChecked
        }
    }

    override fun getItemCount(): Int = items.size

    fun getSelectedItems(): List<VoiceParsedResult> {
        return items.filterIndexed { index, _ -> selectedItems[index] }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        private val textField: TextView = itemView.findViewById(R.id.textField)

        fun bind(item: VoiceParsedResult, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
            val prettyName = formatFieldName(item.fieldName)
            textField.text = "$prettyName → ${item.value}"
            checkBox.isChecked = isChecked
            checkBox.setOnCheckedChangeListener { _, checked ->
                onCheckedChange(checked)
            }
        }

        private fun formatFieldName(fieldName: String): String {
            return when (fieldName) {
                "tsn2" -> "2ТСН"
                "tsn3" -> "3ТСН"
                "tsn4" -> "4ТСН"
                "tt352tsnA" -> "ТТ-35 2ТСН А"
                "tt352tsnB" -> "ТТ-35 2ТСН В"
                "tt352tsnC" -> "ТТ-35 2ТСН С"
                "tt353tsnA" -> "ТТ-35 3ТСН А"
                "tt353tsnB" -> "ТТ-35 3ТСН В"
                "tt353tsnC" -> "ТТ-35 3ТСН С"
                "v352tsnA" -> "В-35 2ТСН А"
                "v352tsnB" -> "В-35 2ТСН В"
                "v352tsnC" -> "В-35 2ТСН С"
                "v353tsnA" -> "В-35 3ТСН А"
                "v353tsnB" -> "В-35 3ТСН В"
                "v353tsnC" -> "В-35 3ТСН С"
                else -> fieldName
            }
        }
    }
}