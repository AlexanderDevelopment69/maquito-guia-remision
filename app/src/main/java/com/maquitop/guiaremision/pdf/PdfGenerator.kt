package com.maquitop.guiaremision.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.itextpdf.io.image.ImageData
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.*
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment
import com.maquitop.guiaremision.data.model.GuiaRemision
import com.maquitop.guiaremision.data.repository.GuiaRepository
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {

    private val AZUL_PRIMARIO = DeviceRgb(40, 115, 176)
    private val GRIS_CLARO = DeviceRgb(245, 245, 245)
    private val GRIS_MEDIO = DeviceRgb(200, 200, 200)
    private val AZUL_OSCURO = DeviceRgb(30, 30, 80)

    fun generarPdf(context: Context, guia: GuiaRemision): String {
        val repo = GuiaRepository(context)
        val config = repo.getConfigEmpresa()

        val dir = File(context.getExternalFilesDir(null), "GuiasPDF")
        if (!dir.exists()) dir.mkdirs()

        val fileName = "Guia_${guia.numeroGuia.replace("/", "-")}_${System.currentTimeMillis()}.pdf"
        val pdfFile = File(dir, fileName)

        val writer = PdfWriter(pdfFile)
        val pdfDoc = PdfDocument(writer)
        val document = Document(pdfDoc, PageSize.A4)
        document.setMargins(25f, 25f, 25f, 25f)

        // ---- ENCABEZADO ----
        agregarEncabezado(document, context, guia, config)

        // Línea divisora
        document.add(agregarLinea())

        // ---- DATOS CLIENTE ----
        document.add(agregarSeccionTitulo("DATOS DEL CLIENTE"))
        document.add(agregarTablaDobleColumna(
            listOf(
                "Nombre / Razón Social" to guia.clienteNombre,
                "DNI / RUC" to guia.clienteDni,
                "Teléfono" to guia.clienteTelefono,
                "Dirección" to guia.clienteDireccion
            )
        ))

        // ---- DATOS DEL EQUIPO ----
        document.add(Paragraph(" ").setFontSize(4f))
        document.add(agregarSeccionTitulo("DATOS DEL EQUIPO"))
        document.add(agregarTablaDobleColumna(
            listOf(
                "Marca" to guia.equipoMarca,
                "Modelo" to guia.equipoModelo,
                "N° de Serie" to guia.equipoSerie,
                "Tipo de Equipo" to guia.equipoTipo
            )
        ))

        // Estado del equipo
        document.add(Paragraph(" ").setFontSize(4f))
        agregarEstadoEquipo(document, guia.estadoEquipo)

        // ---- ACCESORIOS ----
        if (guia.accesorios.isNotEmpty()) {
            document.add(Paragraph(" ").setFontSize(4f))
            document.add(agregarSeccionTitulo("ACCESORIOS"))
            agregarTablaAccesorios(document, guia)
        }

        // ---- COMENTARIOS ----
        if (guia.comentarios.isNotEmpty()) {
            document.add(Paragraph(" ").setFontSize(4f))
            document.add(agregarSeccionTitulo("OBSERVACIONES / COMENTARIOS"))
            val parComentario = Paragraph(guia.comentarios)
                .setFontSize(10f)
                .setBorder(SolidBorder(GRIS_MEDIO, 0.5f))
                .setPadding(8f)
                .setMinHeight(50f)
            document.add(parComentario)
        }

        // ---- FOTOS ----
        val tieneFotos = guia.foto1Path != null || guia.foto2Path != null || guia.foto3Path != null || guia.foto4Path != null
        if (tieneFotos) {
            document.add(Paragraph(" ").setFontSize(4f))
            document.add(agregarSeccionTitulo("FOTOGRAFÍAS DEL EQUIPO"))
            agregarFotos(document, guia)
        }

        // ---- FIRMAS ----
        document.add(Paragraph(" ").setFontSize(6f))
        agregarFirmas(document, context, guia, config)

        // ---- PIE DE PÁGINA ----
        document.add(Paragraph(" ").setFontSize(6f))
        agregarPiePagina(document, config)

        document.close()
        return pdfFile.absolutePath
    }

    private fun agregarEncabezado(document: Document, context: Context, guia: GuiaRemision, config: com.maquitop.guiaremision.data.model.ConfigEmpresa) {
        val tabla = Table(UnitValue.createPercentArray(floatArrayOf(30f, 45f, 25f)))
            .useAllAvailableWidth()

        // Columna 1: Logo
        val celdaLogo = Cell().setBorder(Border.NO_BORDER).setPadding(4f)
        if (config.logoPath != null) {
            try {
                val imgData = ImageDataFactory.create(config.logoPath)
                val img = Image(imgData).setMaxWidth(80f).setMaxHeight(60f)
                celdaLogo.add(img)
            } catch (e: Exception) {
                celdaLogo.add(Paragraph(config.nombre).setBold().setFontSize(12f))
            }
        } else {
            celdaLogo.add(
                Paragraph(config.nombre)
                    .setBold()
                    .setFontSize(14f)
                    .setFontColor(AZUL_PRIMARIO)
            )
        }
        tabla.addCell(celdaLogo)

        // Columna 2: Info empresa
        val celdaInfo = Cell().setBorder(Border.NO_BORDER).setPadding(4f)
        celdaInfo.add(Paragraph(config.nombre).setBold().setFontSize(13f).setFontColor(AZUL_OSCURO))
        if (config.ruc.isNotEmpty()) celdaInfo.add(Paragraph("RUC: ${config.ruc}").setFontSize(9f))
        if (config.direccion.isNotEmpty()) celdaInfo.add(Paragraph(config.direccion).setFontSize(8f))
        if (config.telefono.isNotEmpty()) celdaInfo.add(Paragraph("Tel: ${config.telefono}").setFontSize(8f))
        if (config.email.isNotEmpty()) celdaInfo.add(Paragraph(config.email).setFontSize(8f))
        tabla.addCell(celdaInfo)

        // Columna 3: Número de guía y fecha
        val celdaGuia = Cell()
            .setBackgroundColor(AZUL_PRIMARIO)
            .setPadding(8f)
            .setBorder(Border.NO_BORDER)
        celdaGuia.add(
            Paragraph("GUÍA DE\nRECEPCIÓN")
                .setBold()
                .setFontSize(12f)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER)
        )
        celdaGuia.add(
            Paragraph(guia.numeroGuia)
                .setBold()
                .setFontSize(11f)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER)
        )
        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(guia.fechaCreacion))
        celdaGuia.add(
            Paragraph(fecha)
                .setFontSize(9f)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER)
        )
        tabla.addCell(celdaGuia)

        document.add(tabla)
    }

    private fun agregarLinea(): Paragraph {
        return Paragraph("\n")
            .setBorderBottom(SolidBorder(AZUL_PRIMARIO, 2f))
            .setMarginBottom(4f)
    }

    private fun agregarSeccionTitulo(titulo: String): Paragraph {
        return Paragraph(titulo)
            .setBold()
            .setFontSize(10f)
            .setFontColor(ColorConstants.WHITE)
            .setBackgroundColor(AZUL_OSCURO)
            .setPadding(5f)
            .setMarginBottom(2f)
    }

    private fun agregarTablaDobleColumna(datos: List<Pair<String, String>>): Table {
        val tabla = Table(UnitValue.createPercentArray(floatArrayOf(25f, 25f, 25f, 25f)))
            .useAllAvailableWidth()

        datos.forEachIndexed { index, (label, valor) ->
            val celdaLabel = Cell()
                .add(Paragraph(label).setFontSize(8f).setBold().setFontColor(DeviceRgb(80, 80, 80)))
                .setBackgroundColor(GRIS_CLARO)
                .setBorder(SolidBorder(GRIS_MEDIO, 0.5f))
                .setPadding(4f)
            tabla.addCell(celdaLabel)

            val celdaValor = Cell()
                .add(Paragraph(valor.ifEmpty { "—" }).setFontSize(9f))
                .setBorder(SolidBorder(GRIS_MEDIO, 0.5f))
                .setPadding(4f)
            tabla.addCell(celdaValor)
        }
        return tabla
    }

    private fun agregarEstadoEquipo(document: Document, estado: String) {
        val tabla = Table(UnitValue.createPercentArray(floatArrayOf(30f, 23f, 23f, 24f)))
            .useAllAvailableWidth()

        val labelCell = Cell()
            .add(Paragraph("ESTADO DEL EQUIPO:").setBold().setFontSize(9f))
            .setBackgroundColor(GRIS_CLARO)
            .setBorder(SolidBorder(GRIS_MEDIO, 0.5f))
            .setPadding(5f)
        tabla.addCell(labelCell)

        listOf("OPERATIVO", "INOPERATIVO", "PARA REVISAR").forEach { opcion ->
            val marcado = estado.uppercase().replace("_", " ") == opcion
            val celda = Cell()
                .setBorder(SolidBorder(GRIS_MEDIO, 0.5f))
                .setPadding(5f)
            val check = if (marcado) "☑ $opcion" else "☐ $opcion"
            celda.add(
                Paragraph(check)
                    .setFontSize(9f)
                    .apply { if (marcado) setBold() }
                    .setFontColor(if (marcado) AZUL_PRIMARIO else DeviceRgb(100, 100, 100))
            )
            tabla.addCell(celda)
        }
        document.add(tabla)
    }

    private fun agregarTablaAccesorios(document: Document, guia: GuiaRemision) {
        val tabla = Table(UnitValue.createPercentArray(floatArrayOf(70f, 30f)))
            .useAllAvailableWidth()

        // Encabezados
        tabla.addHeaderCell(
            Cell().add(
                Paragraph("ACCESORIO").setBold().setFontSize(9f).setFontColor(ColorConstants.WHITE)
            ).setBackgroundColor(AZUL_PRIMARIO).setBorder(Border.NO_BORDER).setPadding(5f)
        )
        tabla.addHeaderCell(
            Cell().add(
                Paragraph("ESTADO").setBold().setFontSize(9f).setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER)
            ).setBackgroundColor(AZUL_PRIMARIO).setBorder(Border.NO_BORDER).setPadding(5f)
        )

        guia.accesorios.forEachIndexed { index, acc ->
            val bg = if (index % 2 == 0) ColorConstants.WHITE else GRIS_CLARO

            tabla.addCell(
                Cell().add(Paragraph(acc.nombre).setFontSize(9f))
                    .setBackgroundColor(bg).setBorder(SolidBorder(GRIS_MEDIO, 0.3f)).setPadding(4f)
            )

            val marcado = acc.estado.uppercase()
            val estadoColor = when (marcado) {
                "BUENO" -> DeviceRgb(0, 150, 50)
                "REGULAR" -> DeviceRgb(200, 140, 0)
                "MALO" -> DeviceRgb(200, 30, 30)
                else -> DeviceRgb(100, 100, 100)
            }

            tabla.addCell(
                Cell().add(
                    Paragraph(marcado).setFontSize(9f).setBold().setFontColor(estadoColor)
                ).setBackgroundColor(bg).setBorder(SolidBorder(GRIS_MEDIO, 0.3f))
                    .setPadding(4f).setTextAlignment(TextAlignment.CENTER)
            )
        }
        document.add(tabla)
    }

    private fun comprimirImagen(path: String, maxWidth: Int = 800): ImageData? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(path, options)

            // Calcular escala para no cargar la imagen gigante en memoria
            var scale = 1
            while (options.outWidth / scale / 2 >= maxWidth) {
                scale *= 2
            }

            val finalOptions = BitmapFactory.Options().apply {
                inSampleSize = scale
            }
            val bitmap = BitmapFactory.decodeFile(path, finalOptions) ?: return null
            
            val stream = ByteArrayOutputStream()
            // Comprimimos al 70% de calidad, suficiente para un PDF
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
            val imageData = ImageDataFactory.create(stream.toByteArray())
            bitmap.recycle() // Liberar memoria
            imageData
        } catch (e: Exception) {
            null
        }
    }

    private fun agregarFotos(document: Document, guia: GuiaRemision) {
        val tabla = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
            .useAllAvailableWidth()

        val paths = listOf(guia.foto1Path, guia.foto2Path, guia.foto3Path, guia.foto4Path)
        
        paths.forEachIndexed { idx, path ->
            val celda = Cell().setBorder(SolidBorder(GRIS_MEDIO, 0.5f)).setPadding(4f)
            celda.add(
                Paragraph("Foto ${idx + 1}").setFontSize(8f).setBold()
                    .setFontColor(DeviceRgb(80, 80, 80))
            )
            if (path != null) {
                val imgData = comprimirImagen(path)
                if (imgData != null) {
                    val img = Image(imgData)
                        .setMaxWidth(220f)
                        .setMaxHeight(160f)
                        .setHorizontalAlignment(HorizontalAlignment.CENTER)
                    celda.add(img)
                } else {
                    celda.add(Paragraph("Error al procesar imagen").setFontSize(8f))
                }
            } else {
                celda.add(
                    Paragraph("Sin foto")
                        .setFontSize(8f)
                        .setFontColor(DeviceRgb(150, 150, 150))
                        .setMinHeight(80f)
                )
            }
            tabla.addCell(celda)
        }
        document.add(tabla)
    }

    private fun agregarFirmas(document: Document, context: Context, guia: GuiaRemision, config: com.maquitop.guiaremision.data.model.ConfigEmpresa) {
        val tabla = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
            .useAllAvailableWidth()

        // Firma del cliente
        val celdaCliente = Cell().setBorder(SolidBorder(GRIS_MEDIO, 0.5f)).setPadding(8f)
        celdaCliente.add(
            Paragraph("FIRMA DEL CLIENTE").setBold().setFontSize(9f)
                .setTextAlignment(TextAlignment.CENTER).setFontColor(AZUL_OSCURO)
        )
        if (guia.firmaClientePath != null) {
            try {
                val imgData = ImageDataFactory.create(guia.firmaClientePath)
                val img = Image(imgData).setMaxWidth(180f).setMaxHeight(70f)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                celdaCliente.add(img)
            } catch (e: Exception) {
                celdaCliente.add(Paragraph("_______________________").setFontSize(10f)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(30f))
            }
        } else {
            celdaCliente.add(Paragraph("_______________________").setFontSize(10f)
                .setTextAlignment(TextAlignment.CENTER).setMarginTop(30f))
        }
        celdaCliente.add(Paragraph(guia.clienteNombre).setFontSize(8f)
            .setTextAlignment(TextAlignment.CENTER))
        
        // Datos de quien entrega
        if (guia.quienEntregaNombre.isNotEmpty()) {
            celdaCliente.add(Paragraph("ENTREGADO POR: ${guia.quienEntregaNombre}")
                .setBold().setFontSize(7f).setTextAlignment(TextAlignment.CENTER).setMarginTop(2f))
        }
        if (guia.quienEntregaDni.isNotEmpty()) {
            celdaCliente.add(Paragraph("DNI: ${guia.quienEntregaDni}")
                .setFontSize(7f).setTextAlignment(TextAlignment.CENTER))
        }

        tabla.addCell(celdaCliente)

        // Firma del responsable
        val celdaResponsable = Cell().setBorder(SolidBorder(GRIS_MEDIO, 0.5f)).setPadding(8f)
        celdaResponsable.add(
            Paragraph("FIRMA DEL RESPONSABLE").setBold().setFontSize(9f)
                .setTextAlignment(TextAlignment.CENTER).setFontColor(AZUL_OSCURO)
        )
        if (config.firmaJefePath != null) {
            try {
                val imgData = ImageDataFactory.create(config.firmaJefePath)
                val img = Image(imgData).setMaxWidth(180f).setMaxHeight(70f)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                celdaResponsable.add(img)
            } catch (e: Exception) {
                celdaResponsable.add(Paragraph("_______________________").setFontSize(10f)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(30f))
            }
        } else {
            celdaResponsable.add(Paragraph("_______________________").setFontSize(10f)
                .setTextAlignment(TextAlignment.CENTER).setMarginTop(30f))
        }
        celdaResponsable.add(Paragraph(config.nombre).setFontSize(8f)
            .setTextAlignment(TextAlignment.CENTER))
        tabla.addCell(celdaResponsable)

        document.add(tabla)
    }

    private fun agregarPiePagina(document: Document, config: com.maquitop.guiaremision.data.model.ConfigEmpresa) {
        val infoList = mutableListOf<String>()
        if (config.telefono.isNotEmpty()) infoList.add("Tel: ${config.telefono}")
        if (config.email.isNotEmpty()) infoList.add(config.email)
        if (config.direccion.isNotEmpty()) infoList.add(config.direccion)

        val textoInfo = infoList.joinToString("  |  ")
        if (textoInfo.isNotEmpty()) {
            document.add(
                Paragraph(textoInfo)
                    .setFontSize(8f)
                    .setFontColor(DeviceRgb(120, 120, 120))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorderTop(SolidBorder(GRIS_MEDIO, 0.5f))
                    .setPaddingTop(5f)
            )
        }

        document.add(
            Paragraph("Documento generado por ${config.nombre} - ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}")
                .setFontSize(7f)
                .setFontColor(DeviceRgb(170, 170, 170))
                .setTextAlignment(TextAlignment.CENTER)
        )
    }
}
