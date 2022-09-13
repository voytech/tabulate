package io.github.voytech.tabulate.components.commons.operation

import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.operation.AttributedContext
import io.github.voytech.tabulate.core.template.operation.Operation

class NewPage(val pageName: String) : AttributedContext()

fun interface RenderPageOperation<CTX : RenderingContext> : Operation<CTX, NewPage>

fun newPage(pageName: String): NewPage = NewPage(pageName)