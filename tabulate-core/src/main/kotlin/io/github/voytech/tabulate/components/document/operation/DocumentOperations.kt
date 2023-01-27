package io.github.voytech.tabulate.components.document.operation

import io.github.voytech.tabulate.components.document.model.Document
import io.github.voytech.tabulate.core.model.ModelExportContext
import io.github.voytech.tabulate.core.template.operation.AttributedContext

sealed class DocumentContext(templateContext: ModelExportContext<Document>) : AttributedContext() {
    init {
        additionalAttributes = templateContext.stateAttributes.data
    }
}

class DocumentStart(templateContext: ModelExportContext<Document>) : DocumentContext(templateContext)

class DocumentEnd(templateContext: ModelExportContext<Document>) : DocumentContext(templateContext)