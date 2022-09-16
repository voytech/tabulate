package io.github.voytech.tabulate.components.page.model

import io.github.voytech.tabulate.components.page.template.PageTemplate
import io.github.voytech.tabulate.components.page.template.PageTemplateContext
import io.github.voytech.tabulate.core.model.AbstractModel

class Page internal constructor(
    @get:JvmSynthetic
    internal val name: String = "untitled",
    @get:JvmSynthetic
    internal val nodes: List<AbstractModel<*, *, *>>? = null,
    internal val header: AbstractModel<*, *, *>? = null,
    internal val footer: AbstractModel<*, *, *>? = null,
) : AbstractModel<PageTemplate, Page, PageTemplateContext>() {
    override fun getExportTemplate() = PageTemplate()
}
