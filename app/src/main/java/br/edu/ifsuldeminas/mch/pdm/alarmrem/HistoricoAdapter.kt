package br.edu.ifsuldeminas.mch.pdm.alarmrem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoricoAdapter(
    private val itens: MutableList<HistoricoItem>,
    private val onExcluirItem: (HistoricoItem, Int) -> Unit
) : RecyclerView.Adapter<HistoricoAdapter.HistoricoViewHolder>() {

    class HistoricoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNome: TextView = itemView.findViewById(R.id.tvNomeHistoricoItem)
        val tvDescricao: TextView = itemView.findViewById(R.id.tvDescricaoHistoricoItem)
        val tvDataHora: TextView = itemView.findViewById(R.id.tvDataHoraHistoricoItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoricoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historico, parent, false)
        return HistoricoViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoricoViewHolder, position: Int) {
        val item = itens[position]
        holder.tvNome.text = item.nomeRemedio
        holder.tvDescricao.text = item.descricao
        holder.tvDataHora.text = item.dataHoraConclusao

        // Clique longo para abrir menu de exclusão
        holder.itemView.setOnLongClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.menu_historico, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                if (menuItem.itemId == R.id.menu_deletar) {
                    val posAtual = holder.adapterPosition
                    if (posAtual != RecyclerView.NO_POSITION) {
                        onExcluirItem(itens[posAtual], posAtual)
                    }
                    true
                } else {
                    false
                }
            }
            popup.show()
            true
        }
    }

    override fun getItemCount(): Int = itens.size

    fun removerNaPosicao(position: Int) {
        itens.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itens.size - position)
    }
}