package io.github.voytech.tabulate.components.commons.operation

import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.operation.AttributedContext
import io.github.voytech.tabulate.core.operation.Operation
import io.github.voytech.tabulate.core.operation.VoidOperation

class NewPage(val pageNumber: Int, val pageName: String) : AttributedContext()

fun interface PageOperation<CTX : RenderingContext> : VoidOperation<CTX, NewPage>

fun newPage(pageNumber: Int,pageName: String): NewPage = NewPage(pageNumber, pageName)