package br.edu.ifsuldeminas.mch.pdm.alarmrem

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object HistoricoStorage {

    private const val PREFS_NAME = "alarmrem_prefs"
    private const val KEY_HISTORICO = "key_historico_remedios"

    private val gson = Gson()

    fun carregarHistorico(context: Context): MutableList<HistoricoItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORICO, null) ?: return mutableListOf()

        return try {
            val type = object : TypeToken<MutableList<HistoricoItem>>() {}.type
            gson.fromJson<MutableList<HistoricoItem>>(json, type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun salvarHistorico(context: Context, lista: List<HistoricoItem>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(lista)
        prefs.edit().putString(KEY_HISTORICO, json).apply()
    }

    fun adicionarRegistro(context: Context, item: HistoricoItem) {
        val lista = carregarHistorico(context)
        lista.add(0, item) // mais recente primeiro
        salvarHistorico(context, lista)
    }
}