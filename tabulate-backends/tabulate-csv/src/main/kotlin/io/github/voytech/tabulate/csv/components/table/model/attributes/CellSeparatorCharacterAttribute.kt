package io.github.voytech.tabulate.csv.components.table.model.attributes

import io.github.voytech.tabulate.components.table.api.builder.dsl.TableLevelAttributesBuilderApi
import io.github.voytech.tabulate.components.table.rendering.CellRenderableEntity
import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.api.builder.dsl.TabulateMarker
import io.github.voytech.tabulate.core.model.Attribute

/**
 * Attribute that tells what cell separator will be used in CSV file.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
data class CellSeparatorCharacterAttribute(val separator: String = ","):
    Attribute<CellSeparatorCharacterAttribute>() {
    @TabulateMarker
    class Builder : AttributeBuilder<CellSeparatorCharacterAttribute>(CellRenderableEntity::class.java) {
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
fun <T: Any> TableLevelAttributesBuilderApi<T>.separator(block: CellSeparatorCharacterAttribute.Builder.() -> Unit) =
    attribute(CellSeparatorCharacterAttribute.Builder().apply(block))