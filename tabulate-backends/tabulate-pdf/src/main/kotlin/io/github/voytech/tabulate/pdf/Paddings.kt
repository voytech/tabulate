package io.github.voytech.tabulate.pdf

import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.attributes.BordersAttribute
import io.github.voytech.tabulate.core.operation.AttributedContext

data class Paddings(private val context: AttributedContext) {
    private val bordersAttribute: BordersAttribute? by lazy { context.getModelAttribute<BordersAttribute>() }
    val left: Float by lazy { bordersAttribute?.leftBorderWidth?.switchUnitOfMeasure(UnitsOfMeasure.PT)?.value ?: 0F }
    val top: Float by lazy { bordersAttribute?.topBorderWidth?.switchUnitOfMeasure(UnitsOfMeasure.PT)?.value ?: 0F }
    val right: Float by lazy { bordersAttribute?.rightBorderWidth?.switchUnitOfMeasure(UnitsOfMeasure.PT)?.value ?: 0F }
    val bottom: Float by lazy { bordersAttribute?.bottomBorderWidth?.switchUnitOfMeasure(UnitsOfMeasure.PT)?.value ?: 0F }
    val width: Float = left + right
    val height: Float = top + bottom
}

fun AttributedContext.paddings(): Paddings = Paddings(this)