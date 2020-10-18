package pl.voytech.exporter.impl.template.excel

import pl.voytech.exporter.core.api.builder.CellAttributeBuilder
import pl.voytech.exporter.core.model.attributes.CellAttribute

data class CellExcelDataFormatAttribute(
    val dataFormat: String
) : CellAttribute() {
    class Builder : CellAttributeBuilder {
        var value: String = "general"
        override fun build(): CellAttribute = CellExcelDataFormatAttribute(value)
    }
    override fun mergeWith(other: CellAttribute): CellAttribute  = CellExcelDataFormatAttribute(
        dataFormat = if (other is CellExcelDataFormatAttribute) other.dataFormat else this.dataFormat
    )
}

fun dataFormat(block: CellExcelDataFormatAttribute.Builder.() -> Unit): CellAttribute = CellExcelDataFormatAttribute.Builder().apply(block).build()
