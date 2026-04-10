package com.maquitop.guiaremision.ui.historial

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.maquitop.guiaremision.data.model.GuiaRemision
import com.maquitop.guiaremision.data.repository.GuiaRepository
import com.maquitop.guiaremision.databinding.ActivityDetalleGuiaBinding
import com.maquitop.guiaremision.databinding.IncludeDetalleSeccionClienteBinding
import com.maquitop.guiaremision.databinding.IncludeDetalleSeccionEquipoBinding
import com.maquitop.guiaremision.pdf.PdfGenerator
import com.maquitop.guiaremision.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// ---- ViewModel inline ----
class DetalleGuiaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GuiaRepository(application)
    val guia = MutableLiveData<GuiaRemision?>()
    val pdfPath = MutableLiveData<String?>()
    val error = MutableLiveData<String?>()
    val isLoading = MutableLiveData(false)

    fun cargarGuia(id: Long) {
        viewModelScope.launch {
            guia.postValue(repository.getGuiaById(id))
        }
    }

    fun reexportarPdf() {
        val g = guia.value ?: return
        viewModelScope.launch {
            isLoading.postValue(true)
            try {
                val path = withContext(Dispatchers.IO) {
                    PdfGenerator.generarPdf(getApplication(), g)
                }
                repository.updateGuia(g.copy(pdfPath = path, pdfGenerado = true))
                pdfPath.postValue(path)
            } catch (e: Exception) {
                error.postValue("Error al generar PDF: ${e.message}")
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun eliminarGuia() {
        val g = guia.value ?: return
        viewModelScope.launch {
            repository.deleteGuia(g)
        }
    }
}

// ---- Activity ----
class DetalleGuiaActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_GUIA_ID = "extra_guia_id"
    }

    private lateinit var binding: ActivityDetalleGuiaBinding
    private val viewModel: DetalleGuiaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleGuiaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detalle de Guía"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val id = intent.getLongExtra(EXTRA_GUIA_ID, -1L)
        if (id == -1L) { finish(); return }

        viewModel.cargarGuia(id)
        observeViewModel()

        binding.btnReexportarPdf.setOnClickListener { viewModel.reexportarPdf() }
        binding.btnEliminarGuia.setOnClickListener { confirmarEliminar() }
    }

    private fun observeViewModel() {
        viewModel.guia.observe(this) { guia ->
            guia ?: return@observe
            mostrarDatos(guia)
        }

        viewModel.pdfPath.observe(this) { path ->
            if (path != null) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("PDF Generado")
                    .setMessage("El PDF fue generado exitosamente.")
                    .setPositiveButton("Abrir") { _, _ -> FileUtils.openPdf(this, path) }
                    .setNeutralButton("Compartir") { _, _ ->
                        FileUtils.sharePdf(this, path, viewModel.guia.value?.numeroGuia ?: "")
                    }
                    .setNegativeButton("Cerrar", null)
                    .show()
            }
        }

        viewModel.error.observe(this) { msg ->
            if (msg != null) Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
        }

        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnReexportarPdf.isEnabled = !loading
        }
    }

    private fun mostrarDatos(guia: GuiaRemision) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        binding.tvNumeroGuia.text = guia.numeroGuia
        binding.tvFecha.text = sdf.format(Date(guia.fechaCreacion))

        // Bind the included layouts to access their internal views
        val bindingCliente = IncludeDetalleSeccionClienteBinding.bind(binding.layoutCliente.root)
        val bindingEquipo = IncludeDetalleSeccionEquipoBinding.bind(binding.layoutEquipo.root)

        bindingCliente.tvClienteNombre.text = guia.clienteNombre.ifEmpty { "—" }
        bindingCliente.tvClienteTelefono.text = guia.clienteTelefono.ifEmpty { "—" }
        bindingCliente.tvClienteDireccion.text = guia.clienteDireccion.ifEmpty { "—" }
        bindingCliente.tvClienteDni.text = guia.clienteDni.ifEmpty { "—" }

        // Quien entrega
        binding.tvQuienEntregaNombre.text = guia.quienEntregaNombre.ifEmpty { "—" }
        binding.tvQuienEntregaDni.text = if (guia.quienEntregaDni.isEmpty()) "DNI: —" else "DNI: ${guia.quienEntregaDni}"

        bindingEquipo.tvEquipoMarca.text = guia.equipoMarca.ifEmpty { "—" }
        bindingEquipo.tvEquipoModelo.text = guia.equipoModelo.ifEmpty { "—" }
        bindingEquipo.tvEquipoSerie.text = guia.equipoSerie.ifEmpty { "—" }
        bindingEquipo.tvEquipoTipo.text = guia.equipoTipo.ifEmpty { "—" }
        bindingEquipo.tvEstadoEquipo.text = guia.estadoEquipo.replace("_", " ")

        binding.tvComentarios.text = guia.comentarios.ifEmpty { "Sin comentarios" }

        // Accesorios
        val accesoriosText = if (guia.accesorios.isEmpty()) "Ninguno"
        else guia.accesorios.joinToString("\n") { "• ${it.nombre}: ${it.estado}" }
        binding.tvAccesorios.text = accesoriosText

        // Fotos
        if (guia.foto1Path != null) {
            binding.imgFoto1.visibility = View.VISIBLE
            Glide.with(this).load(File(guia.foto1Path)).into(binding.imgFoto1)
        } else {
            binding.imgFoto1.visibility = View.GONE
        }
        if (guia.foto2Path != null) {
            binding.imgFoto2.visibility = View.VISIBLE
            Glide.with(this).load(File(guia.foto2Path)).into(binding.imgFoto2)
        } else {
            binding.imgFoto2.visibility = View.GONE
        }
        if (guia.foto3Path != null) {
            binding.imgFoto3.visibility = View.VISIBLE
            Glide.with(this).load(File(guia.foto3Path)).into(binding.imgFoto3)
        } else {
            binding.imgFoto3.visibility = View.GONE
        }
        if (guia.foto4Path != null) {
            binding.imgFoto4.visibility = View.VISIBLE
            Glide.with(this).load(File(guia.foto4Path)).into(binding.imgFoto4)
        } else {
            binding.imgFoto4.visibility = View.GONE
        }

        // Firma
        if (guia.firmaClientePath != null) {
            binding.imgFirmaCliente.visibility = View.VISIBLE
            Glide.with(this).load(File(guia.firmaClientePath)).into(binding.imgFirmaCliente)
        } else {
            binding.imgFirmaCliente.visibility = View.GONE
        }
    }

    private fun confirmarEliminar() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar Guía")
            .setMessage("¿Está seguro de eliminar esta guía? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarGuia()
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
