package io.github.voytech.tabulate.components.text.operation

import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.model.asXPosition
import io.github.voytech.tabulate.core.model.asYPosition
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.layout.DefaultLayoutPolicy
import io.github.voytech.tabulate.core.template.layout.Layout
import io.github.voytech.tabulate.core.template.layout.LayoutElementApply
import io.github.voytech.tabulate.core.template.layout.LayoutElementBoundingBox
import io.github.voytech.tabulate.core.template.operation.HasText
import io.github.voytech.tabulate.core.template.operation.Operation
import io.github.voytech.tabulate.core.template.operation.RenderableContext

class TextRenderable internal constructor(
    val text: String,
    override val attributes: Attributes?,
    stateAttributes: StateAttributes,
) : RenderableContext<DefaultLayoutPolicy>(), HasText, LayoutElementApply<DefaultLayoutPolicy> {

    init {
        additionalAttributes = stateAttributes.data
    }

    override fun Layout.computeBoundingBox(policy: DefaultLayoutPolicy): LayoutElementBoundingBox = with(policy) {
        elementBoundingBox(
            x = getX(START_X, uom),
            y = getY(START_Y, uom),
            //width = maxBoundingRectangle?.getWidth(),
        )
    }

    override val value: String
        get() = text

    override fun Layout.applyBoundingBox(context: LayoutElementBoundingBox, policy: DefaultLayoutPolicy) {
        //TODO("Not yet implemented")
    }

    companion object {
        private val START_X = 0.asXPosition()
        private val START_Y = 0.asYPosition()
    }
}

fun interface TextOperation<CTX : RenderingContext> : Operation<CTX, TextRenderable>
