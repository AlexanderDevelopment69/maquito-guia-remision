# 📱 Maquitop — Sistema de Gestión Digital de Guías de Recepción

Aplicación Android profesional diseñada para la empresa **MAQUITOP S.A.** que permite digitalizar el proceso de recepción de equipos técnicos. El sistema automatiza la creación de documentos, el registro fotográfico y la gestión de firmas, generando un archivo PDF optimizado para su envío y archivo.

---

## 📋 Funcionalidades Implementadas

| N° | Categoría | Funcionalidad | Estado |
|----|-----------|---------------|--------|
| 1 | **Navegación** | Pantalla principal con acceso rápido a módulos | ✅ |
| 2 | **Clientes** | Registro de datos: Nombre/Razón Social, DNI/RUC, Teléfono, Dirección | ✅ |
| 3 | **Equipo** | Registro de: Marca, Modelo, Serie, Tipo y Estado (Operativo/Inoperativo/Revisar) | ✅ |
| 4 | **Accesorios** | Inventario de 10 accesorios base con selección de estado (Bueno, Regular, Malo) | ✅ |
| 5 | **Accesorios** | Posibilidad de agregar accesorios personalizados al formulario | ✅ |
| 6 | **Multimedia** | Captura de hasta 4 fotografías del equipo con previsualización | ✅ |
| 7 | **Firmas** | Captura de firma del cliente mediante diálogo emergente profesional | ✅ |
| 8 | **Persistencia** | Guardado de guías en base de datos local (Room) para evitar pérdida de datos | ✅ |
| 9 | **PDF** | Generación de PDF profesional con tablas, fotos, firmas y logo de empresa | ✅ |
| 10 | **PDF** | Algoritmo de compresión de imágenes (de 30MB a <1MB por archivo) | ✅ |
| 11 | **Historial** | Listado completo de guías generadas con búsqueda por cliente o número | ✅ |
| 12 | **Gestión** | Reexportación de PDFs desde el historial y compartición directa | ✅ |
| 13 | **Configuración** | Gestión de datos de la empresa (RUC, Contacto, Dirección) | ✅ |
| 14 | **Marca** | Carga y persistencia del logo empresarial para el encabezado del PDF | ✅ |
| 15 | **Autoridad** | Registro y guardado de la firma del responsable/jefe de taller | ✅ |
| 16 | **Control** | Gestión de numeración correlativa (inicio y reset de contador) | ✅ |
| 17 | **UX** | Persistencia de scroll inteligente tras capturar fotografías | ✅ |
| 18 | **Seguridad** | Prevención de registros duplicados al generar múltiples PDFs | ✅ |

---

## 🏗 Arquitectura del Proyecto

El proyecto está construido bajo el patrón **MVVM (Model-View-ViewModel)** garantizando un código modular y fácil de mantener:

```
MaquitopApp/
├── app/src/main/
│   ├── java/com/maquitop/guiaremision/
│   │   ├── data/
│   │   │   ├── database/AppDatabase.kt     ← Gestión de Room DB y DAOs
│   │   │   ├── model/Models.kt             ← Entidades (GuiaRemision, Accesorio, Config)
│   │   │   └── repository/GuiaRepository.kt← Origen único de verdad de los datos
│   │   ├── pdf/
│   │   │   └── PdfGenerator.kt             ← Motor de iText7 con compresión JPEG
│   │   ├── ui/
│   │   │   ├── main/MainActivity.kt        ← Dashboard principal
│   │   │   ├── nuevaguia/
│   │   │   │   ├── NuevaGuiaActivity.kt    ← Formulario dinámico con scroll estable
│   │   │   │   └── NuevaGuiaViewModel.kt   ← Lógica de negocio y estados de UI
│   │   │   ├── historial/
│   │   │   │   ├── HistorialActivity.kt    ← Lista con filtros de búsqueda
│   │   │   │   └── DetalleGuiaActivity.kt  ← Vista previa y reexportación
│   │   │   └── configuracion/
│   │   │       └── ConfiguracionActivity.kt← Ajustes globales de la empresa
│   │   └── utils/
│   │       ├── SignatureView.kt            ← Canvas optimizado para firmas táctiles
│   │       └── FileUtils.kt                ← Manejo de URIs y almacenamiento seguro
│   └── res/
│       ├── layout/                         ← Definición de interfaces en XML
│       ├── values/colors.xml               ← Identidad visual azul corporativo
│       └── values/strings.xml              ← Textos centralizados y traducibles
```

---

## 🎨 Identidad Visual (MAQUITOP S.A.)

- **Color Primario (Azul Marca):** `#2873B0` (Utilizado en Toolbars, Botones y Secciones).
- **Color Secundario (Azul Profundo):** `#1E1E50` (Utilizado en Textos de cabecera y énfasis).
- **Indicadores de Estado:**
  - **Bueno:** `#00AA44` (Verde)
  - **Regular:** `#CC8800` (Ámbar)
  - **Malo:** `#CC0000` (Rojo)

---

## 🚀 Guía de Instalación y Desarrollo

### Requisitos
- **Android Studio** Hedgehog (2023.1.1) o superior.
- **Java JDK 17**.
- **Dispositivo/Emulador:** Android 7.0 (API 24) o superior.

### Pasos para Empezar
1. Clonar o descargar el código fuente.
2. Abrir el proyecto en Android Studio.
3. Sincronizar Gradle para instalar automáticamente las dependencias (**iText7**, **Room**, **Glide**).
4. Configurar el archivo `local.properties` con el path de tu Android SDK.
5. Ejecutar en un dispositivo para realizar el primer registro en **Configuración**.

---

## 📦 Tecnologías Principales
- **Kotlin:** Lenguaje moderno y seguro.
- **Room:** Almacenamiento local persistente.
- **iText7:** Generación de documentos PDF de alta fidelidad.
- **Glide:** Gestión eficiente de memoria para imágenes de alta resolución.
- **ViewBinding:** Comunicación segura entre lógica e interfaz.

---

## ⚙️ Configuración Inicial Obligatoria
Para que los documentos PDF se generen correctamente, el usuario debe configurar por única vez:
1. **Datos Legales:** Nombre, RUC y Dirección de Maquitop.
2. **Logo:** Cargar imagen de alta calidad para el encabezado.
3. **Firma del Jefe:** Registrar la firma del responsable que validará los ingresos.
4. **Contador:** Definir el número inicial de las guías (ej. 0001).

---

*Desarrollado por Alexander para MAQUITOP S.A. — Versión 1.1*
