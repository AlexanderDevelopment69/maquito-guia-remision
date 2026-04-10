package com.maquitop.guiaremision.ui.nuevaguia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.maquitop.guiaremision.R
import com.maquitop.guiaremision.data.model.Accesorio

class AccesorioAdapter(
    private val onEstadoChanged: (index: Int, estado: String) -> Unit,
    private val onEliminar: (index: Int) -> Unit
) : ListAdapter<Accesorio, AccesorioAdapter.AccesorioViewHolder>(AccesorioDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccesorioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_accesorio, parent, false)
        return AccesorioViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccesorioViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class AccesorioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvAccesorioNombre)
        private val rgEstado: RadioGroup = itemView.findViewById(R.id.rgEstado)
        private val rbBueno: RadioButton = itemView.findViewById(R.id.rbBueno)
        private val rbRegular: RadioButton = itemView.findViewById(R.id.rbRegular)
        private val rbMalo: RadioButton = itemView.findViewById(R.id.rbMalo)
        private val btnEliminar: ImageButton = itemView.findViewById(R.id.btnEliminarAccesorio)

        fun bind(accesorio: Accesorio, position: Int) {
            tvNombre.text = accesorio.nombre

            rgEstado.setOnCheckedChangeListener(null)
            when (accesorio.estado.uppercase()) {
                "BUENO" -> rbBueno.isChecked = true
                "REGULAR" -> rbRegular.isChecked = true
                "MALO" -> rbMalo.isChecked = true
                else -> rbBueno.isChecked = true
            }

            rgEstado.setOnCheckedChangeListener { _, checkedId ->
                val estado = when (checkedId) {
                    R.id.rbBueno -> "BUENO"
                    R.id.rbRegular -> "REGULAR"
                    R.id.rbMalo -> "MALO"
                    else -> "BUENO"
                }
                onEstadoChanged(position, estado)
            }

            btnEliminar.setOnClickListener { onEliminar(position) }
        }
    }

    class AccesorioDiffCallback : DiffUtil.ItemCallback<Accesorio>() {
        override fun areItemsTheSame(oldItem: Accesorio, newItem: Accesorio) =
            oldItem.nombre == newItem.nombre
        override fun areContentsTheSame(oldItem: Accesorio, newItem: Accesorio) =
            oldItem == newItem
    }
}
