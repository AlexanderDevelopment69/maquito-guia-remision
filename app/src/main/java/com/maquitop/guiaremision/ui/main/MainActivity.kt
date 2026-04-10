package com.maquitop.guiaremision.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.maquitop.guiaremision.databinding.ActivityMainBinding
import com.maquitop.guiaremision.ui.configuracion.ConfiguracionActivity
import com.maquitop.guiaremision.ui.historial.HistorialActivity
import com.maquitop.guiaremision.ui.nuevaguia.NuevaGuiaActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNuevaGuia.setOnClickListener {
            startActivity(Intent(this, NuevaGuiaActivity::class.java))
        }

        binding.btnHistorial.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }

        binding.btnConfiguracion.setOnClickListener {
            startActivity(Intent(this, ConfiguracionActivity::class.java))
        }
    }
}
