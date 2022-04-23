package io.github.voytech.tabulate.csv.components.table.model.attributes

import io.github.voytech.tabulate.components.table.api.builder.CellAttributeBuilder
import io.github.voytech.tabulate.components.table.api.builder.dsl.TableLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.model.attributes.CellAttribute
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker

/**
 * Attribute that tells what cell separator will be used in CSV file.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class CellSeparatorCharacterAttribute(val separator: String = ","):
    CellAttribute<CellSeparatorCharacterAttribute>() {
    @TabulateMarker
    class Builder : CellAttributeBuilder<CellSeparatorCharacterAttribute>() {
        var value: String by observable(",")
        override fun provide(): CellSeparatorCharacterAttribute = CellSeparatorCharacterAttribute(value)
    }

    override fun overrideWith(other: CellSeparatorCharacterAttribute): CellSeparatorCharacterAttribute = other
}

/**
 * Extension method supplying 'separator' attribute builder.
 * Can be installed only on global table level.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
fun <T> TableLevelAttributesBuilderApi<T>.separator(block: CellSeparatorCharacterAttribute.Builder.() -> Unit) =
    attribute(CellSeparatorCharacterAttribute.Builder().apply(block))