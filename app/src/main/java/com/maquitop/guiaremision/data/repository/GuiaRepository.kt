package com.maquitop.guiaremision.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.maquitop.guiaremision.data.database.AppDatabase
import com.maquitop.guiaremision.data.model.ConfigEmpresa
import com.maquitop.guiaremision.data.model.GuiaRemision
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class GuiaRepository(context: Context) {

    private val guiaDao = AppDatabase.getDatabase(context).guiaDao()
    private val prefs: SharedPreferences = context.getSharedPreferences("maquitop_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // ---- Guias CRUD ----
    fun getAllGuias(): Flow<List<GuiaRemision>> = guiaDao.getAllGuias()

    suspend fun getGuiaById(id: Long): GuiaRemision? = guiaDao.getGuiaById(id)

    fun searchGuias(query: String): Flow<List<GuiaRemision>> = guiaDao.searchGuias(query)

    suspend fun saveGuia(guia: GuiaRemision): Long = guiaDao.insertGuia(guia)

    suspend fun updateGuia(guia: GuiaRemision) = guiaDao.updateGuia(guia)

    suspend fun deleteGuia(guia: GuiaRemision) = guiaDao.deleteGuia(guia)

    // ---- Número de guía ----
    suspend fun generarNumeroGuia(): String {
        val config = getConfigEmpresa()
        val contador = config.contadorActual
        val fecha = SimpleDateFormat("yyyyMM", Locale.getDefault()).format(Date())
        val numero = String.format("G-%s-%04d", fecha, contador)
        // Incrementar contador
        saveConfigEmpresa(config.copy(contadorActual = contador + 1))
        return numero
    }

    suspend fun resetearNumeracion(inicio: Int) {
        val config = getConfigEmpresa()
        saveConfigEmpresa(config.copy(numeroInicio = inicio, contadorActual = inicio))
    }

    // ---- Configuración de empresa ----
    fun getConfigEmpresa(): ConfigEmpresa {
        val json = prefs.getString("config_empresa", null)
        return if (json != null) {
            try {
                gson.fromJson(json, ConfigEmpresa::class.java)
            } catch (e: Exception) {
                ConfigEmpresa()
            }
        } else {
            ConfigEmpresa()
        }
    }

    fun saveConfigEmpresa(config: ConfigEmpresa) {
        prefs.edit().putString("config_empresa", gson.toJson(config)).apply()
    }
}
