package io.github.voytech.tabulate.components.document.template

import io.github.voytech.tabulate.components.document.model.Document
import io.github.voytech.tabulate.core.template.TemplateContext
import java.util.*

class DocumentTemplateContext(
    document: Document
) : TemplateContext<Document>(document, mutableMapOf()) {

    init {
        stateAttributes["_exportTraceId"] = UUID.randomUUID().toString()
    }

}
