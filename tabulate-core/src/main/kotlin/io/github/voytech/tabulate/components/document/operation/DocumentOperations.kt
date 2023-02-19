package io.github.voytech.tabulate.components.document.operation

import io.github.voytech.tabulate.components.document.model.Document
import io.github.voytech.tabulate.core.model.ModelExportContext
import io.github.voytech.tabulate.core.template.operation.AttributedContext

sealed class DocumentContext(templateContext: ModelExportContext) : AttributedContext() {
    init {
        additionalAttributes = templateContext.customStateAttributes.data
    }
}

class DocumentStart(templateContext: ModelExportContext) : DocumentContext(templateContext)

class DocumentEnd(templateContext: ModelExportContext) : DocumentContext(templateContext)