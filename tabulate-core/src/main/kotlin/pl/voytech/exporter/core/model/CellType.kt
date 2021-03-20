package pl.voytech.exporter.core.model

import java.time.LocalDate
import java.time.LocalDateTime

enum class CellType {
    NUMERIC,
    STRING,
    BOOLEAN,
    DATE,
    IMAGE_DATA,
    IMAGE_URL,
    FUNCTION,
    ERROR,
}

/**
 * Only basic cell types can be resolved, for the rest - type must be provided explicitly.
 */
internal fun CellType?.orProbe(cellValue: Any?): CellType? {
    return this ?: when(cellValue) {
        is Number -> CellType.NUMERIC
        is Boolean -> CellType.BOOLEAN
        is java.util.Date -> CellType.DATE
        is java.sql.Date -> CellType.DATE
        is LocalDate -> CellType.DATE
        is LocalDateTime -> CellType.DATE
        is String -> CellType.STRING
        else -> null
    }
}

