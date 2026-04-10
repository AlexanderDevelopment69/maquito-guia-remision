package com.maquitop.guiaremision.ui.configuracion

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.maquitop.guiaremision.databinding.ActivityConfiguracionBinding
import com.maquitop.guiaremision.utils.FileUtils
import java.io.File
import java.io.FileOutputStream

class ConfiguracionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfiguracionBinding
    private val viewModel: ConfiguracionViewModel by viewModels()

    private var logoPickTarget = false // false = logo, true = firma jefe

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (logoPickTarget) guardarFirmaJefeDesdeUri(it)
            else guardarLogoDesdeUri(it)
        }
    }

    private val cameraPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) Snackbar.make(binding.root, "Permiso de cámara denegado", Snackbar.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfiguracionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Configuración"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        observeViewModel()
        setupButtons()
    }

    private var isFirstLoad = true

    private fun observeViewModel() {
        viewModel.config.observe(this) { config ->
            if (isFirstLoad) {
                binding.etEmpresaNombre.setText(config.nombre)
                binding.etEmpresaRuc.setText(config.ruc)
                binding.etEmpresaDireccion.setText(config.direccion)
                binding.etEmpresaTelefono.setText(config.telefono)
                binding.etEmpresaEmail.setText(config.email)
                binding.etNumeroInicio.setText(config.numeroInicio.toString())
                isFirstLoad = false
            }
            
            binding.tvContadorActual.text = "Contador actual: ${config.contadorActual}"

            if (config.logoPath != null) {
                binding.imgLogo.visibility = View.VISIBLE
                Glide.with(this).load(File(config.logoPath)).into(binding.imgLogo)
            } else {
                binding.imgLogo.visibility = View.GONE
            }

            if (config.firmaJefePath != null) {
                binding.imgFirmaJefe.visibility = View.VISIBLE
                Glide.with(this).load(File(config.firmaJefePath)).into(binding.imgFirmaJefe)
            } else {
                binding.imgFirmaJefe.visibility = View.GONE
            }
        }

        viewModel.mensaje.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) {
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
                // Limpiar el mensaje después de mostrarlo para evitar re-ejecución
                viewModel.limpiarMensaje()
            }
        }
    }

    private fun setupButtons() {
        binding.btnGuardarConfig.setOnClickListener {
            val inicio = binding.etNumeroInicio.text.toString().toIntOrNull() ?: 1
            viewModel.guardarConfig(
                nombre = binding.etEmpresaNombre.text.toString().trim(),
                ruc = binding.etEmpresaRuc.text.toString().trim(),
                direccion = binding.etEmpresaDireccion.text.toString().trim(),
                telefono = binding.etEmpresaTelefono.text.toString().trim(),
                email = binding.etEmpresaEmail.text.toString().trim(),
                numeroInicio = inicio
            )
        }

        binding.btnSeleccionarLogo.setOnClickListener {
            logoPickTarget = false
            pickImageLauncher.launch("image/*")
        }

        binding.btnEliminarLogo.setOnClickListener {
            viewModel.eliminarLogo()
        }

        binding.btnDibujarFirmaJefe.setOnClickListener {
            mostrarDialogoFirmaJefe()
        }

        binding.btnSeleccionarFirmaJefe.setOnClickListener {
            logoPickTarget = true
            pickImageLauncher.launch("image/*")
        }

        binding.btnEliminarFirmaJefe.setOnClickListener {
            viewModel.eliminarFirmaJefe()
        }

        binding.btnResetearNumeracion.setOnClickListener {
            confirmarResetearNumeracion()
        }
    }

    private fun guardarLogoDesdeUri(uri: Uri) {
        try {
            val dir = File(filesDir, "empresa")
            if (!dir.exists()) dir.mkdirs()
            val dest = File(dir, "logo.png")
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dest).use { output ->
                    input.copyTo(output)
                }
            }
            viewModel.actualizarLogo(dest.absolutePath)
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Error al cargar imagen", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun guardarFirmaJefeDesdeUri(uri: Uri) {
        try {
            val dir = File(filesDir, "empresa")
            if (!dir.exists()) dir.mkdirs()
            val dest = File(dir, "firma_jefe.png")
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dest).use { output ->
                    input.copyTo(output)
                }
            }
            viewModel.actualizarFirmaJefe(dest.absolutePath)
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Error al cargar imagen", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoFirmaJefe() {
        val dialogView = layoutInflater.inflate(
            com.maquitop.guiaremision.R.layout.dialog_firma, null
        )
        val signatureView = dialogView.findViewById<com.maquitop.guiaremision.utils.SignatureView>(
            com.maquitop.guiaremision.R.id.signatureViewDialog
        )
        val btnLimpiar = dialogView.findViewById<android.widget.Button>(com.maquitop.guiaremision.R.id.btnLimpiarFirmaDialog)
        btnLimpiar.setOnClickListener { signatureView.clear() }

        MaterialAlertDialogBuilder(this)
            .setTitle("Firma del Responsable")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                if (!signatureView.isEmpty()) {
                    val firmaFile = FileUtils.createSignatureFile(this, "FIRMA_JEFE")
                    if (signatureView.saveToFile(firmaFile)) {
                        viewModel.actualizarFirmaJefe(firmaFile.absolutePath)
                    }
                } else {
                    Snackbar.make(binding.root, "Dibuje la firma primero", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarResetearNumeracion() {
        val inicio = binding.etNumeroInicio.text.toString().toIntOrNull() ?: 1
        MaterialAlertDialogBuilder(this)
            .setTitle("Resetear Numeración")
            .setMessage("¿Está seguro? El contador de guías se reiniciará desde $inicio.")
            .setPositiveButton("Resetear") { _, _ ->
                viewModel.resetearNumeracion(inicio)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
