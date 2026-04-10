package com.maquitop.guiaremision.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// ---- Accesorio ----
data class Accesorio(
    val nombre: String,
    val estado: String = "BUENO" // BUENO, REGULAR, MALO
)

// ---- GuiaRemision ----
@Entity(tableName = "guias")
@TypeConverters(Converters::class)
data class GuiaRemision(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val numeroGuia: String = "",
    val fechaCreacion: Long = System.currentTimeMillis(),

    // Datos del cliente
    val clienteNombre: String = "",
    val clienteTelefono: String = "",
    val clienteDireccion: String = "",
    val clienteDni: String = "",

    // Quien entrega el equipo
    val quienEntregaNombre: String = "",
    val quienEntregaDni: String = "",

    // Datos del equipo
    val equipoMarca: String = "",
    val equipoModelo: String = "",
    val equipoSerie: String = "",
    val equipoTipo: String = "",
    val estadoEquipo: String = "", // OPERATIVO, INOPERATIVO, PARA_REVISAR

    // Accesorios
    val accesorios: List<Accesorio> = emptyList(),

    // Observaciones / comentarios
    val comentarios: String = "",

    // Fotos (rutas de archivos)
    val foto1Path: String? = null,
    val foto2Path: String? = null,
    val foto3Path: String? = null,
    val foto4Path: String? = null,

    // Firma del cliente (ruta de archivo)
    val firmaClientePath: String? = null,

    // Estado
    val pdfGenerado: Boolean = false,
    val pdfPath: String? = null
)

// ---- Configuracion de empresa ----
data class ConfigEmpresa(
    val nombre: String = "MAQUITOP S.A.",
    val ruc: String = "",
    val direccion: String = "",
    val telefono: String = "",
    val email: String = "",
    val logoPath: String? = null,
    val firmaJefePath: String? = null,
    val numeroInicio: Int = 1,
    val contadorActual: Int = 1
)

// ---- Converters para Room ----
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromAccesorioList(value: List<Accesorio>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toAccesorioList(value: String): List<Accesorio> {
        return try {
            val type = object : TypeToken<List<Accesorio>>() {}.type
            gson.fromJson(value, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
