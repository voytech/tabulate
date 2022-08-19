package io.github.voytech.tabulate.components.page.operation

import io.github.voytech.tabulate.components.page.template.PageTemplateContext
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.operation.AttributedContext
import io.github.voytech.tabulate.core.template.operation.Operation

class NewPage(val sheetName: String) : AttributedContext()

fun interface RenderPageOperation<CTX : RenderingContext> : Operation<CTX, NewPage>

fun newPage(templateContext: PageTemplateContext): NewPage =
    NewPage(templateContext.model.id).also {
        it.additionalAttributes = templateContext.stateAttributes
    }