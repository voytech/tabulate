package io.github.voytech.tabulate.components.commons.operation

import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.operation.AttributedContext
import io.github.voytech.tabulate.core.template.operation.Operation

class NewPage(val pageNumber: Int, val pageName: String) : AttributedContext()

fun interface PageOperation<CTX : RenderingContext> : Operation<CTX, NewPage>

fun newPage(pageNumber: Int,pageName: String): NewPage = NewPage(pageNumber, pageName)