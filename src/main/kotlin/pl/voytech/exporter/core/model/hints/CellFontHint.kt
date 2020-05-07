package pl.voytech.exporter.core.model.hints

data class CellFontHint(
    val fontFamily: String,
    val fontSize: Int,
    val fontStyle: Int,
    val fontColor: String
) : Hint()
