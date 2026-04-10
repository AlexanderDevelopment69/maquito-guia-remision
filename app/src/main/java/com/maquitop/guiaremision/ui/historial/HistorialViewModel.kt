package com.maquitop.guiaremision.ui.historial

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.maquitop.guiaremision.data.model.GuiaRemision
import com.maquitop.guiaremision.data.repository.GuiaRepository

class HistorialViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GuiaRepository(application)
    private val queryLiveData = MutableLiveData("")

    val guias: LiveData<List<GuiaRemision>> = queryLiveData.switchMap { query ->
        if (query.isNullOrBlank()) {
            repository.getAllGuias().asLiveData()
        } else {
            repository.searchGuias(query).asLiveData()
        }
    }

    fun buscar(query: String) {
        queryLiveData.value = query
    }
}
