package br.edu.ifsuldeminas.mch.pdm.alarmrem

data class HistoricoItem(
    val nomeRemedio: String,
    val descricao: String,
    val dataHoraConclusao: String // ex: "22/06/2026 21:50"
)