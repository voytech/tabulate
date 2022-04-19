package io.github.voytech.tabulate.csv.attributes

import io.github.voytech.tabulate.api.builder.CellAttributeBuilder
import io.github.voytech.tabulate.api.builder.dsl.TableLevelAttributesBuilderApi
import io.github.voytech.tabulate.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.model.attributes.CellAttribute

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