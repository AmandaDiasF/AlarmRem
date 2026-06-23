package br.edu.ifsuldeminas.mch.pdm.alarmrem

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoricoRemediosActivity : AppCompatActivity() {

    private lateinit var rvHistorico: RecyclerView
    private lateinit var tvMensagemVazio: TextView
    private lateinit var adapter: HistoricoAdapter
    private var listaHistorico: MutableList<HistoricoItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico_remedios)

        rvHistorico = findViewById(R.id.rvHistorico)
        tvMensagemVazio = findViewById(R.id.tvMensagemVazio)
        val btnVoltar = findViewById<Button>(R.id.btnVoltarHistorico)

        rvHistorico.layoutManager = LinearLayoutManager(this)

        listaHistorico = HistoricoStorage.carregarHistorico(this)
        adapter = HistoricoAdapter(listaHistorico) { item, position ->
            excluirRegistro(item, position)
        }
        rvHistorico.adapter = adapter

        atualizarVisibilidadeMensagemVazio(listaHistorico)

        btnVoltar.setOnClickListener {
            finish()
        }
    }

    private fun excluirRegistro(item: HistoricoItem, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Excluir registro")
            .setMessage("Deseja excluir o registro do remédio \"${item.nomeRemedio}\"?")
            .setPositiveButton("Excluir") { _, _ ->
                // Remove da lista em memória
                adapter.removerNaPosicao(position)

                // Salva a lista atualizada no SharedPreferences
                HistoricoStorage.salvarHistorico(this, listaHistorico)

                atualizarVisibilidadeMensagemVazio(listaHistorico)

                Toast.makeText(this, "Registro excluído", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun atualizarVisibilidadeMensagemVazio(lista: List<HistoricoItem>) {
        if (lista.isEmpty()) {
            tvMensagemVazio.visibility = View.VISIBLE
            rvHistorico.visibility = View.GONE
        } else {
            tvMensagemVazio.visibility = View.GONE
            rvHistorico.visibility = View.VISIBLE
        }
    }
}