package com.maquitop.guiaremision.ui.nuevaguia

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.maquitop.guiaremision.R
import com.maquitop.guiaremision.databinding.ActivityNuevaGuiaBinding
import com.maquitop.guiaremision.utils.FileUtils
import com.maquitop.guiaremision.utils.SignatureView
import java.io.File

class NuevaGuiaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNuevaGuiaBinding
    private val viewModel: NuevaGuiaViewModel by viewModels()
    private lateinit var accesorioAdapter: AccesorioAdapter

    private var currentPhotoFile: File? = null
    private var currentPhotoSlot = 1
    private var lastScrollY = 0

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("lastScrollY", binding.nuevaGuiaScrollView.scrollY)
        outState.putInt("currentPhotoSlot", currentPhotoSlot)
        currentPhotoFile?.let { outState.putString("currentPhotoPath", it.absolutePath) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuevaGuiaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {
            lastScrollY = savedInstanceState.getInt("lastScrollY")
            currentPhotoSlot = savedInstanceState.getInt("currentPhotoSlot")
            savedInstanceState.getString("currentPhotoPath")?.let { currentPhotoFile = File(it) }
            
            // Restaurar scroll inicial si hubo recreación
            binding.nuevaGuiaScrollView.post {
                binding.nuevaGuiaScrollView.scrollTo(0, lastScrollY)
            }
        }

        setupToolbar()
        setupAccesoriosRecycler()
        setupButtons()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Nueva Guía de Recepción"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupAccesoriosRecycler() {
        accesorioAdapter = AccesorioAdapter(
            onEstadoChanged = { index, estado ->
                viewModel.actualizarEstadoAccesorio(index, estado)
            },
            onEliminar = { index -> viewModel.eliminarAccesorio(index) }
        )
        binding.rvAccesorios.layoutManager = LinearLayoutManager(this)
        binding.rvAccesorios.adapter = accesorioAdapter
        binding.rvAccesorios.isNestedScrollingEnabled = false
    }

    private fun setupButtons() {
        // Fotos
        binding.btnFoto1.setOnClickListener {
            currentPhotoSlot = 1
            checkCameraAndLaunch()
        }
        binding.btnFoto2.setOnClickListener {
            currentPhotoSlot = 2
            checkCameraAndLaunch()
        }
        binding.btnFoto3.setOnClickListener {
            currentPhotoSlot = 3
            checkCameraAndLaunch()
        }
        binding.btnFoto4.setOnClickListener {
            currentPhotoSlot = 4
            checkCameraAndLaunch()
        }

        // Agregar accesorio personalizado
        binding.btnAgregarAccesorio.setOnClickListener {
            mostrarDialogoAccesorioPersonalizado()
        }

        // Firma del cliente (vía Diálogo)
        binding.btnDibujarFirma.setOnClickListener {
            mostrarDialogoFirma()
        }

        // Estado del equipo
        setupEstadoEquipo()

        // Guardar guía
        binding.btnGuardarGuia.setOnClickListener {
            confirmarGuardar()
        }

        // Generar PDF
        binding.btnGenerarPdf.setOnClickListener {
            confirmarGenerarPdf()
        }
    }

    private fun setupEstadoEquipo() {
        val labels = arrayOf("Operativo", "Inoperativo", "Para Revisar")
        binding.spinnerEstadoEquipo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, labels).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun observeViewModel() {
        viewModel.numeroGuia.observe(this) { numero ->
            binding.tvNumeroGuia.text = "N° $numero"
        }

        viewModel.accesorios.observe(this) { lista ->
            accesorioAdapter.submitList(lista.toList())
        }

        viewModel.foto1Path.observe(this) { path ->
            if (path != null) {
                binding.imgFoto1.visibility = View.VISIBLE
                Glide.with(this).load(File(path)).into(binding.imgFoto1)
                binding.btnFoto1.text = "Foto 1"
            } else {
                binding.imgFoto1.visibility = View.GONE
                binding.btnFoto1.text = "Foto 1"
            }
        }

        viewModel.foto2Path.observe(this) { path ->
            if (path != null) {
                binding.imgFoto2.visibility = View.VISIBLE
                Glide.with(this).load(File(path)).into(binding.imgFoto2)
                binding.btnFoto2.text = "Foto 2"
            } else {
                binding.imgFoto2.visibility = View.GONE
                binding.btnFoto2.text = "Foto 2"
            }
        }

        viewModel.foto3Path.observe(this) { path ->
            if (path != null) {
                binding.imgFoto3.visibility = View.VISIBLE
                Glide.with(this).load(File(path)).into(binding.imgFoto3)
                binding.btnFoto3.text = "Foto 3"
            } else {
                binding.imgFoto3.visibility = View.GONE
                binding.btnFoto3.text = "Foto 3"
            }
        }

        viewModel.foto4Path.observe(this) { path ->
            if (path != null) {
                binding.imgFoto4.visibility = View.VISIBLE
                Glide.with(this).load(File(path)).into(binding.imgFoto4)
                binding.btnFoto4.text = "Foto 4"
            } else {
                binding.imgFoto4.visibility = View.GONE
                binding.btnFoto4.text = "Foto 4"
            }
        }

        viewModel.firmaClientePath.observe(this) { path ->
            if (path != null) {
                binding.tvFirmaGuardada.visibility = View.VISIBLE
                binding.imgFirmaPreview.visibility = View.VISIBLE
                Glide.with(this).load(File(path)).into(binding.imgFirmaPreview)
                binding.tvFirmaGuardada.text = "✓ Firma guardada"
            } else {
                binding.tvFirmaGuardada.visibility = View.GONE
                binding.imgFirmaPreview.visibility = View.GONE
            }
        }

        viewModel.guardadoExito.observe(this) { id ->
            if (id != null) {
                Snackbar.make(binding.root, "Guía guardada exitosamente", Snackbar.LENGTH_SHORT).show()
                // Permanecemos en la misma vista. 
                // Opcionalmente podrías llamar a limpiarFormulario() si quieres dejarla lista para otra guía.
                // viewModel.limpiarFormulario()
            }
        }

        viewModel.pdfPath.observe(this) { path ->
            if (path != null) {
                mostrarDialogoPdfGenerado(path)
            }
        }

        viewModel.error.observe(this) { msg ->
            if (msg != null) {
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnGenerarPdf.isEnabled = !loading
            binding.btnGuardarGuia.isEnabled = !loading
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
        else Snackbar.make(binding.root, "Se necesita permiso de cámara", Snackbar.LENGTH_SHORT).show()
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoFile?.let { file ->
                viewModel.setFoto(currentPhotoSlot, file.absolutePath)
                // Usamos un delay ligeramente mayor y smoothScroll para mayor estabilidad
                binding.nuevaGuiaScrollView.postDelayed({
                    binding.nuevaGuiaScrollView.smoothScrollTo(0, lastScrollY)
                }, 400)
            }
        }
    }

    private fun checkCameraAndLaunch() {
        // Guardamos la posición del scroll antes de abrir la cámara
        lastScrollY = binding.nuevaGuiaScrollView.scrollY
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> launchCamera()
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val photoFile = FileUtils.createImageFile(this, "FOTO_${currentPhotoSlot}")
        currentPhotoFile = photoFile
        val photoUri = FileUtils.getUriForFile(this, photoFile)
        takePictureLauncher.launch(photoUri)
    }

    private fun mostrarDialogoAccesorioPersonalizado() {
        val input = EditText(this).apply {
            hint = "Nombre del accesorio"
            setPadding(40, 20, 40, 20)
        }
        MaterialAlertDialogBuilder(this)
            .setTitle("Agregar accesorio")
            .setView(input)
            .setPositiveButton("Agregar") { _, _ ->
                val nombre = input.text.toString().trim()
                if (nombre.isNotEmpty()) {
                    viewModel.agregarAccesorioPersonalizado(nombre)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoFirma() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_firma, null)
        val signatureView = dialogView.findViewById<SignatureView>(R.id.signatureViewDialog)
        val btnLimpiar = dialogView.findViewById<Button>(R.id.btnLimpiarFirmaDialog)

        btnLimpiar.setOnClickListener { signatureView.clear() }

        MaterialAlertDialogBuilder(this)
            .setTitle("Firma del Cliente")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                if (signatureView.isEmpty()) {
                    Snackbar.make(binding.root, "No se ha dibujado ninguna firma", Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val firmaFile = FileUtils.createSignatureFile(this, "FIRMA_CLIENTE")
                if (signatureView.saveToFile(firmaFile)) {
                    viewModel.setFirmaCliente(firmaFile.absolutePath)
                    Snackbar.make(binding.root, "Firma guardada correctamente", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(binding.root, "Error al guardar firma", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun recopilarDatos(): DatosGuia {
        val estadosEquipo = arrayOf("OPERATIVO", "INOPERATIVO", "PARA_REVISAR")
        return DatosGuia(
            clienteNombre = binding.etClienteNombre.text.toString().trim(),
            clienteTelefono = binding.etClienteTelefono.text.toString().trim(),
            clienteDireccion = binding.etClienteDireccion.text.toString().trim(),
            clienteDni = binding.etClienteDni.text.toString().trim(),
            quienEntregaNombre = binding.etQuienEntregaNombre.text.toString().trim(),
            quienEntregaDni = binding.etQuienEntregaDni.text.toString().trim(),
            equipoMarca = binding.etEquipoMarca.text.toString().trim(),
            equipoModelo = binding.etEquipoModelo.text.toString().trim(),
            equipoSerie = binding.etEquipoSerie.text.toString().trim(),
            equipoTipo = binding.etEquipoTipo.text.toString().trim(),
            estadoEquipo = estadosEquipo.getOrElse(binding.spinnerEstadoEquipo.selectedItemPosition) { "OPERATIVO" },
            comentarios = binding.etComentarios.text.toString().trim()
        )
    }

    private fun validarDatos(datos: DatosGuia): Boolean {
        if (datos.clienteNombre.isEmpty()) {
            binding.etClienteNombre.error = "Requerido"
            binding.etClienteNombre.requestFocus()
            return false
        }
        if (datos.quienEntregaNombre.isEmpty()) {
            binding.etQuienEntregaNombre.error = "Requerido"
            binding.etQuienEntregaNombre.requestFocus()
            return false
        }
        if (datos.equipoMarca.isEmpty()) {
            binding.etEquipoMarca.error = "Requerido"
            binding.etEquipoMarca.requestFocus()
            return false
        }
        return true
    }

    private fun confirmarGuardar() {
        val datos = recopilarDatos()
        if (!validarDatos(datos)) return

        MaterialAlertDialogBuilder(this)
            .setTitle("Guardar Guía")
            .setMessage("¿Está seguro de guardar la guía de recepción?\nSe guardará en el historial.")
            .setPositiveButton("Guardar") { _, _ ->
                viewModel.guardarGuia(
                    datos.clienteNombre, datos.clienteTelefono, datos.clienteDireccion, datos.clienteDni,
                    datos.quienEntregaNombre, datos.quienEntregaDni,
                    datos.equipoMarca, datos.equipoModelo, datos.equipoSerie, datos.equipoTipo,
                    datos.estadoEquipo, datos.comentarios
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarGenerarPdf() {
        val datos = recopilarDatos()
        if (!validarDatos(datos)) return

        MaterialAlertDialogBuilder(this)
            .setTitle("Generar PDF")
            .setMessage("¿Seguro de generar PDF?\nAl generar el PDF, esta guía se guardará en el historial y se limpiarán todos los datos del formulario.")
            .setPositiveButton("Generar") { _, _ ->
                viewModel.generarPdf(
                    datos.clienteNombre, datos.clienteTelefono, datos.clienteDireccion, datos.clienteDni,
                    datos.quienEntregaNombre, datos.quienEntregaDni,
                    datos.equipoMarca, datos.equipoModelo, datos.equipoSerie, datos.equipoTipo,
                    datos.estadoEquipo, datos.comentarios
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoPdfGenerado(path: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("✓ PDF Generado")
            .setMessage("La guía fue guardada y el PDF fue generado exitosamente.")
            .setPositiveButton("Abrir PDF") { _, _ ->
                FileUtils.openPdf(this, path)
                finish()
            }
            .setNeutralButton("Compartir") { _, _ ->
                FileUtils.sharePdf(this, path, viewModel.numeroGuia.value ?: "")
                finish()
            }
            .setNegativeButton("Cerrar") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun limpiarFormulario() {
        binding.etClienteNombre.text?.clear()
        binding.etClienteTelefono.text?.clear()
        binding.etClienteDireccion.text?.clear()
        binding.etClienteDni.text?.clear()
        binding.etQuienEntregaNombre.text?.clear()
        binding.etQuienEntregaDni.text?.clear()
        binding.etEquipoMarca.text?.clear()
        binding.etEquipoModelo.text?.clear()
        binding.etEquipoSerie.text?.clear()
        binding.etEquipoTipo.text?.clear()
        binding.etComentarios.text?.clear()
        binding.spinnerEstadoEquipo.setSelection(0)
        binding.imgFirmaPreview.visibility = View.GONE
        viewModel.limpiarFormulario()
    }

    data class DatosGuia(
        val clienteNombre: String, val clienteTelefono: String,
        val clienteDireccion: String, val clienteDni: String,
        val quienEntregaNombre: String, val quienEntregaDni: String,
        val equipoMarca: String, val equipoModelo: String,
        val equipoSerie: String, val equipoTipo: String,
        val estadoEquipo: String, val comentarios: String
    )
}
