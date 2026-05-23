package br.edu.ifsuldeminas.mch.pdm.alarmrem

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AlarmeAdapter(
    private val listaAlarmes: MutableList<Alarme>,
    private val onEditar: (Alarme, Int) -> Unit,
    private val onExcluir: (Alarme, Int) -> Unit
) : RecyclerView.Adapter<AlarmeAdapter.AlarmeViewHolder>() {

    class AlarmeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgMedicamentoItem: ImageView = itemView.findViewById(R.id.imgMedicamentoItem)
        val tvNomeRemedioItem: TextView = itemView.findViewById(R.id.tvNomeRemedioItem)
        val tvDescricaoItem: TextView = itemView.findViewById(R.id.tvDescricaoItem)
        val tvHoraItem: TextView = itemView.findViewById(R.id.tvHoraItem)
        val tvDiasItem: TextView = itemView.findViewById(R.id.tvDiasItem)
        val btnEditarItem: Button = itemView.findViewById(R.id.btnEditarItem)
        val btnExcluirItem: Button = itemView.findViewById(R.id.btnExcluirItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarme, parent, false)
        return AlarmeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmeViewHolder, position: Int) {
        val alarme = listaAlarmes[position]

        holder.tvNomeRemedioItem.text = alarme.nomeRemedio
        holder.tvDescricaoItem.text = alarme.descricao
        holder.tvHoraItem.text = alarme.hora
        holder.tvDiasItem.text = alarme.diasSelecionados

        if (!alarme.fotoUri.isNullOrEmpty()) {
            holder.imgMedicamentoItem.setImageURI(null)
            holder.imgMedicamentoItem.setImageURI(Uri.parse(alarme.fotoUri))
        } else {
            holder.imgMedicamentoItem.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.btnEditarItem.setOnClickListener {
            val posicaoAtual = holder.adapterPosition
            if (posicaoAtual != RecyclerView.NO_POSITION) {
                onEditar(listaAlarmes[posicaoAtual], posicaoAtual)
            }
        }

        holder.btnExcluirItem.setOnClickListener {
            val posicaoAtual = holder.adapterPosition
            if (posicaoAtual != RecyclerView.NO_POSITION) {
                onExcluir(listaAlarmes[posicaoAtual], posicaoAtual)
            }
        }
    }

    override fun getItemCount(): Int = listaAlarmes.size

    fun adicionarAlarme() {
        notifyItemInserted(listaAlarmes.size - 1)
    }

    fun atualizarAlarme(position: Int, alarmeAtualizado: Alarme) {
        listaAlarmes[position] = alarmeAtualizado
        notifyItemChanged(position)
    }

    fun removerAlarme(position: Int) {
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listaAlarmes.size - position)
    }
}