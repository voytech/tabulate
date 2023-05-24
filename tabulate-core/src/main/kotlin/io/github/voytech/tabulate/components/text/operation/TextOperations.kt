package io.github.voytech.tabulate.components.text.operation

import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.model.asXPosition
import io.github.voytech.tabulate.core.model.asYPosition
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.layout.LayoutSpace
import io.github.voytech.tabulate.core.layout.RenderableBoundingBox
import io.github.voytech.tabulate.core.layout.policy.SimpleLayout
import io.github.voytech.tabulate.core.operation.HasText
import io.github.voytech.tabulate.core.operation.Operation
import io.github.voytech.tabulate.core.operation.Renderable

class TextRenderable internal constructor(
    val text: String,
    override val attributes: Attributes?,
    stateAttributes: StateAttributes,
) : Renderable<SimpleLayout>(), HasText {

    init {
        additionalAttributes = stateAttributes.data
    }

    override fun LayoutSpace.defineBoundingBox(policy: SimpleLayout): RenderableBoundingBox = with(policy) {
        elementBoundingBox(x = getX(START_X, uom), y = getY(START_Y, uom))
    }

    override val value: String
        get() = text

    companion object {
        private val START_X = 0.asXPosition()
        private val START_Y = 0.asYPosition()
    }
}

fun interface TextOperation<CTX : RenderingContext> : Operation<CTX, TextRenderable>
