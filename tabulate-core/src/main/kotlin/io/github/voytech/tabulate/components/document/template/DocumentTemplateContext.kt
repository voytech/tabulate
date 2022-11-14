package io.github.voytech.tabulate.components.document.template

import io.github.voytech.tabulate.components.document.model.Document
import io.github.voytech.tabulate.core.template.ExportInstance
import io.github.voytech.tabulate.core.template.TemplateContext
import java.util.*

class DocumentTemplateContext(
    document: Document,
    instance: ExportInstance
) : TemplateContext<DocumentTemplateContext,Document>(document, mutableMapOf(),instance) {
    init {
        stateAttributes["_exportTraceId"] = UUID.randomUUID().toString()
    }
}
