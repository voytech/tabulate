package pl.voytech.exporter.core.template

data class Coordinates(
    val tableName: String,
    val rowIndex: Int = 0,
    val columnIndex: Int = 0
)