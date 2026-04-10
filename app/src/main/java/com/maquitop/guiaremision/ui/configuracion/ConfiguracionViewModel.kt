package com.maquitop.guiaremision.ui.configuracion

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.maquitop.guiaremision.data.model.ConfigEmpresa
import com.maquitop.guiaremision.data.repository.GuiaRepository
import kotlinx.coroutines.launch

class ConfiguracionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GuiaRepository(application)

    val config = MutableLiveData<ConfigEmpresa>()
    val mensaje = MutableLiveData<String?>()

    init {
        cargarConfig()
    }

    private fun cargarConfig() {
        config.value = repository.getConfigEmpresa()
    }

    fun guardarConfig(
        nombre: String, ruc: String, direccion: String,
        telefono: String, email: String, numeroInicio: Int
    ) {
        val actual = config.value ?: ConfigEmpresa()
        val nueva = actual.copy(
            nombre = nombre,
            ruc = ruc,
            direccion = direccion,
            telefono = telefono,
            email = email,
            numeroInicio = numeroInicio
        )
        repository.saveConfigEmpresa(nueva)
        config.value = nueva
        mensaje.value = "Configuración guardada correctamente"
    }

    fun actualizarLogo(path: String) {
        val actual = config.value ?: ConfigEmpresa()
        val nueva = actual.copy(logoPath = path)
        repository.saveConfigEmpresa(nueva)
        config.value = nueva
        mensaje.value = "Logo actualizado"
    }

    fun eliminarLogo() {
        val actual = config.value ?: ConfigEmpresa()
        val nueva = actual.copy(logoPath = null)
        repository.saveConfigEmpresa(nueva)
        config.value = nueva
        mensaje.value = "Logo eliminado"
    }

    fun actualizarFirmaJefe(path: String) {
        val actual = config.value ?: ConfigEmpresa()
        val nueva = actual.copy(firmaJefePath = path)
        repository.saveConfigEmpresa(nueva)
        config.value = nueva
        mensaje.value = "Firma del responsable actualizada"
    }

    fun eliminarFirmaJefe() {
        val actual = config.value ?: ConfigEmpresa()
        val nueva = actual.copy(firmaJefePath = null)
        repository.saveConfigEmpresa(nueva)
        config.value = nueva
        mensaje.value = "Firma eliminada"
    }

    fun resetearNumeracion(inicio: Int) {
        viewModelScope.launch {
            repository.resetearNumeracion(inicio)
            cargarConfig()
            mensaje.postValue("Numeración reiniciada desde $inicio")
        }
    }

    fun limpiarMensaje() {
        mensaje.value = null
    }
}
