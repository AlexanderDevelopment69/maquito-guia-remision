package com.maquitop.guiaremision.ui.historial

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.maquitop.guiaremision.R
import com.maquitop.guiaremision.data.model.GuiaRemision
import java.text.SimpleDateFormat
import java.util.*

class GuiaAdapter(
    private val onVerDetalle: (GuiaRemision) -> Unit
) : ListAdapter<GuiaRemision, GuiaAdapter.GuiaViewHolder>(GuiaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuiaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_guia, parent, false)
        return GuiaViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuiaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GuiaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNumero: TextView = itemView.findViewById(R.id.tvGuiaNumero)
        private val tvCliente: TextView = itemView.findViewById(R.id.tvGuiaCliente)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvGuiaFecha)
        private val tvEquipo: TextView = itemView.findViewById(R.id.tvGuiaEquipo)
        private val ivPdf: ImageView = itemView.findViewById(R.id.ivPdfIndicator)

        fun bind(guia: GuiaRemision) {
            tvNumero.text = guia.numeroGuia
            tvCliente.text = guia.clienteNombre.ifEmpty { "Sin nombre" }
            tvEquipo.text = "${guia.equipoMarca} ${guia.equipoModelo}".trim().ifEmpty { "Sin equipo" }

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            tvFecha.text = sdf.format(Date(guia.fechaCreacion))

            ivPdf.visibility = if (guia.pdfGenerado) View.VISIBLE else View.INVISIBLE

            itemView.setOnClickListener { onVerDetalle(guia) }
        }
    }

    class GuiaDiffCallback : DiffUtil.ItemCallback<GuiaRemision>() {
        override fun areItemsTheSame(oldItem: GuiaRemision, newItem: GuiaRemision) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: GuiaRemision, newItem: GuiaRemision) =
            oldItem == newItem
    }
}
