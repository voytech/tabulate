package pl.voytech.exporter.core.template

data class FileData<T>(
    val content: T,
    val fileName: String? = "data-export",
    val fileType: String? = null
)