package io.github.voytech.tabulate.components.document.operation

import io.github.voytech.tabulate.components.document.model.DocumentAttribute
import io.github.voytech.tabulate.components.document.template.DocumentTemplateContext
import io.github.voytech.tabulate.core.template.operation.AttributedContext

sealed class DocumentContext(templateContext: DocumentTemplateContext) : AttributedContext<DocumentAttribute<*>>() {
    init {
        additionalAttributes = templateContext.stateAttributes
    }
}

class DocumentOpeningContext(templateContext: DocumentTemplateContext) : DocumentContext(templateContext)

class DocumentClosingContext(templateContext: DocumentTemplateContext) : DocumentContext(templateContext)