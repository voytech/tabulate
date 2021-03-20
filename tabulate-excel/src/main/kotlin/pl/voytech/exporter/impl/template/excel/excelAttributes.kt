package pl.voytech.exporter.impl.template.excel

import pl.voytech.exporter.core.api.builder.CellAttributeBuilder
import pl.voytech.exporter.core.model.attributes.CellAttribute

data class CellExcelDataFormatAttribute(
    val dataFormat: String
) : CellAttribute<CellExcelDataFormatAttribute>() {
    class Builder : CellAttributeBuilder<CellExcelDataFormatAttribute> {
        var value: String = "general"
        override fun build(): CellExcelDataFormatAttribute = CellExcelDataFormatAttribute(value)
    }
    override fun mergeWith(other: CellExcelDataFormatAttribute): CellExcelDataFormatAttribute  = other
}

fun dataFormat(block: CellExcelDataFormatAttribute.Builder.() -> Unit): CellExcelDataFormatAttribute = CellExcelDataFormatAttribute.Builder().apply(block).build()
