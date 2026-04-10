package com.maquitop.guiaremision.ui.historial

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.maquitop.guiaremision.databinding.ActivityHistorialBinding

class HistorialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistorialBinding
    private val viewModel: HistorialViewModel by viewModels()
    private lateinit var guiaAdapter: GuiaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Historial de Guías"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        setupRecycler()
        setupSearch()
        observeViewModel()
    }

    private fun setupRecycler() {
        guiaAdapter = GuiaAdapter(
            onVerDetalle = { guia ->
                val intent = Intent(this, DetalleGuiaActivity::class.java)
                intent.putExtra(DetalleGuiaActivity.EXTRA_GUIA_ID, guia.id)
                startActivity(intent)
            }
        )
        binding.rvGuias.layoutManager = LinearLayoutManager(this)
        binding.rvGuias.adapter = guiaAdapter
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.buscar(newText ?: "")
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewModel.guias.observe(this) { lista ->
            guiaAdapter.submitList(lista)
            binding.tvEmpty.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
            binding.rvGuias.visibility = if (lista.isEmpty()) View.GONE else View.VISIBLE
        }
    }
}
