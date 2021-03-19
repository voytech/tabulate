package pl.voytech.exporter.core.model.attributes.style

import pl.voytech.exporter.core.model.attributes.RowAttribute

data class RowHeightAttribute(val height: Int) : RowAttribute<RowHeightAttribute>() {
    override fun mergeWith(other: RowHeightAttribute): RowHeightAttribute = other
}