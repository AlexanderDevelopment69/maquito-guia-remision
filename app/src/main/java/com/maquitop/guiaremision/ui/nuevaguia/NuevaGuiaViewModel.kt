package com.maquitop.guiaremision.ui.nuevaguia

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.maquitop.guiaremision.data.model.Accesorio
import com.maquitop.guiaremision.data.model.GuiaRemision
import com.maquitop.guiaremision.data.repository.GuiaRepository
import com.maquitop.guiaremision.pdf.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NuevaGuiaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GuiaRepository(application)

    val numeroGuia = MutableLiveData<String>("")
    val accesorios = MutableLiveData<MutableList<Accesorio>>(mutableListOf())
    val foto1Path = MutableLiveData<String?>(null)
    val foto2Path = MutableLiveData<String?>(null)
    val foto3Path = MutableLiveData<String?>(null)
    val foto4Path = MutableLiveData<String?>(null)
    val firmaClientePath = MutableLiveData<String?>(null)
    val guardadoExito = MutableLiveData<Long?>(null)
    val pdfPath = MutableLiveData<String?>(null)
    val error = MutableLiveData<String?>(null)
    val isLoading = MutableLiveData(false)

    init {
        generarNumero()
        cargarAccesoriosPredeterminados()
    }

    private fun generarNumero() {
        viewModelScope.launch {
            val numero = repository.generarNumeroGuia()
            numeroGuia.postValue(numero)
        }
    }

    private fun cargarAccesoriosPredeterminados() {
        val lista = mutableListOf(
            Accesorio("Cargador", "BUENO"),
            Accesorio("Transformador", "BUENO"),
            Accesorio("Bateria", "BUENO"),
            Accesorio("Mini Prisma", "BUENO"),
            Accesorio("Prisma Circular", "BUENO"),
            Accesorio("Estuche", "BUENO"),
            Accesorio("USB", "BUENO"),
            Accesorio("SD", "BUENO"),
            Accesorio("Maletin de Estuche", "BUENO"),
            Accesorio("Base Nivelante", "BUENO")
        )
        accesorios.value = lista
    }

    fun actualizarEstadoAccesorio(index: Int, estado: String) {
        val lista = accesorios.value ?: return
        if (index < lista.size) {
            lista[index] = lista[index].copy(estado = estado)
            accesorios.postValue(lista)
        }
    }

    fun agregarAccesorioPersonalizado(nombre: String) {
        val lista = accesorios.value ?: mutableListOf()
        lista.add(Accesorio(nombre, "BUENO"))
        accesorios.postValue(lista)
    }

    fun eliminarAccesorio(index: Int) {
        val lista = accesorios.value ?: return
        if (index < lista.size) {
            lista.removeAt(index)
            accesorios.postValue(lista)
        }
    }

    fun setFoto(slot: Int, path: String) {
        when (slot) {
            1 -> foto1Path.postValue(path)
            2 -> foto2Path.postValue(path)
            3 -> foto3Path.postValue(path)
            4 -> foto4Path.postValue(path)
        }
    }

    fun setFirmaCliente(path: String) {
        firmaClientePath.postValue(path)
    }

    private var currentGuiaId: Long = 0

    fun guardarGuia(
        clienteNombre: String, clienteTelefono: String, clienteDireccion: String, clienteDni: String,
        quienEntregaNombre: String, quienEntregaDni: String,
        equipoMarca: String, equipoModelo: String, equipoSerie: String, equipoTipo: String,
        estadoEquipo: String, comentarios: String
    ) {
        viewModelScope.launch {
            isLoading.postValue(true)
            try {
                val guia = GuiaRemision(
                    id = currentGuiaId,
                    numeroGuia = numeroGuia.value ?: "",
                    clienteNombre = clienteNombre,
                    clienteTelefono = clienteTelefono,
                    clienteDireccion = clienteDireccion,
                    clienteDni = clienteDni,
                    quienEntregaNombre = quienEntregaNombre,
                    quienEntregaDni = quienEntregaDni,
                    equipoMarca = equipoMarca,
                    equipoModelo = equipoModelo,
                    equipoSerie = equipoSerie,
                    equipoTipo = equipoTipo,
                    estadoEquipo = estadoEquipo,
                    accesorios = accesorios.value ?: emptyList(),
                    comentarios = comentarios,
                    foto1Path = foto1Path.value,
                    foto2Path = foto2Path.value,
                    foto3Path = foto3Path.value,
                    foto4Path = foto4Path.value,
                    firmaClientePath = firmaClientePath.value
                )
                
                if (currentGuiaId == 0L) {
                    currentGuiaId = repository.saveGuia(guia)
                } else {
                    repository.updateGuia(guia)
                }
                
                guardadoExito.postValue(currentGuiaId)
            } catch (e: Exception) {
                error.postValue("Error al guardar: ${e.message}")
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun generarPdf(
        clienteNombre: String, clienteTelefono: String, clienteDireccion: String, clienteDni: String,
        quienEntregaNombre: String, quienEntregaDni: String,
        equipoMarca: String, equipoModelo: String, equipoSerie: String, equipoTipo: String,
        estadoEquipo: String, comentarios: String
    ) {
        viewModelScope.launch {
            isLoading.postValue(true)
            try {
                val guiaBase = GuiaRemision(
                    id = currentGuiaId,
                    numeroGuia = numeroGuia.value ?: "",
                    clienteNombre = clienteNombre,
                    clienteTelefono = clienteTelefono,
                    clienteDireccion = clienteDireccion,
                    clienteDni = clienteDni,
                    quienEntregaNombre = quienEntregaNombre,
                    quienEntregaDni = quienEntregaDni,
                    equipoMarca = equipoMarca,
                    equipoModelo = equipoModelo,
                    equipoSerie = equipoSerie,
                    equipoTipo = equipoTipo,
                    estadoEquipo = estadoEquipo,
                    accesorios = accesorios.value ?: emptyList(),
                    comentarios = comentarios,
                    foto1Path = foto1Path.value,
                    foto2Path = foto2Path.value,
                    foto3Path = foto3Path.value,
                    foto4Path = foto4Path.value,
                    firmaClientePath = firmaClientePath.value
                )

                // Generar el PDF
                val path = withContext(Dispatchers.IO) {
                    PdfGenerator.generarPdf(getApplication(), guiaBase)
                }
                
                val guiaConPdf = guiaBase.copy(pdfPath = path, pdfGenerado = true)
                
                if (currentGuiaId == 0L) {
                    currentGuiaId = repository.saveGuia(guiaConPdf)
                } else {
                    repository.updateGuia(guiaConPdf)
                }
                
                pdfPath.postValue(path)
                guardadoExito.postValue(currentGuiaId)
            } catch (e: Exception) {
                error.postValue("Error al generar PDF: ${e.message}")
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun limpiarFormulario() {
        foto1Path.postValue(null)
        foto2Path.postValue(null)
        foto3Path.postValue(null)
        foto4Path.postValue(null)
        firmaClientePath.postValue(null)
        pdfPath.postValue(null)
        guardadoExito.postValue(null)
        error.postValue(null)
        cargarAccesoriosPredeterminados()
        generarNumero()
    }
}
