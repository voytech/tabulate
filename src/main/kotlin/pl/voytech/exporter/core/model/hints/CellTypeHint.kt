package pl.voytech.exporter.core.model.hints

enum class CellType {
    NUMERIC,
    STRING,
    NATIVE_FORMULA,
    FORMULA,
    ERROR,
    BOOLEAN,
    DATE
}

data class CellTypeHint(
   val type: CellType
): Hint()
