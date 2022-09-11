package io.github.voytech.tabulate.components.document.operation

import io.github.voytech.tabulate.components.document.template.DocumentTemplateContext
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.operation.AttributedContext
import io.github.voytech.tabulate.core.template.operation.Operation

sealed class DocumentContext(templateContext: DocumentTemplateContext) : AttributedContext() {
    init {
        additionalAttributes = templateContext.stateAttributes
    }
}

class DocumentStart(templateContext: DocumentTemplateContext) : DocumentContext(templateContext)

class DocumentEnd(templateContext: DocumentTemplateContext) : DocumentContext(templateContext)