package io.github.voytech.tabulate.components.commons.operation

import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.model.StateAttributes
import io.github.voytech.tabulate.core.operation.AttributedEntity
import io.github.voytech.tabulate.core.operation.VoidOperation

class NewPage(val pageNumber: Int, val pageName: String, val customAttributes: StateAttributes) : AttributedEntity() {
    override fun toString(): String = "NewPage[$pageNumber]"
}

fun interface PageOperation<CTX : RenderingContext> : VoidOperation<CTX, NewPage>

fun newPage(pageNumber: Int, pageName: String, customAttributes: StateAttributes): NewPage =
    NewPage(pageNumber, pageName, customAttributes)