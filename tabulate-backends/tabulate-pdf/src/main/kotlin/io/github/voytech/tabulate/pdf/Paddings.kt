package io.github.voytech.tabulate.pdf

import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.attributes.BordersAttribute
import io.github.voytech.tabulate.core.operation.AttributedEntity

data class Paddings(private val context: AttributedEntity) {
    private val bordersAttribute: BordersAttribute? by lazy { context.getModelAttribute<BordersAttribute>() }
    val left: Float by lazy { bordersAttribute?.leftBorderWidth?.switchUnitOfMeasure(UnitsOfMeasure.PT)?.value ?: 0F }
    val top: Float by lazy { bordersAttribute?.topBorderHeight?.switchUnitOfMeasure(UnitsOfMeasure.PT)?.value ?: 0F }
    val right: Float by lazy { bordersAttribute?.rightBorderWidth?.switchUnitOfMeasure(UnitsOfMeasure.PT)?.value ?: 0F }
    val bottom: Float by lazy { bordersAttribute?.bottomBorderHeight?.switchUnitOfMeasure(UnitsOfMeasure.PT)?.value ?: 0F }
    val width: Float = left + right
    val height: Float = top + bottom
}

fun AttributedEntity.paddings(): Paddings = Paddings(this)