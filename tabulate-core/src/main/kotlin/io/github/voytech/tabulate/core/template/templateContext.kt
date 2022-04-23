package io.github.voytech.tabulate.core.template

import io.github.voytech.tabulate.core.model.CategorizedAttributes
import io.github.voytech.tabulate.core.model.Model

open class TemplateContext<ARM: Model>(
    val model: ARM,
    val stateAttributes: MutableMap<String, Any>,
    val modelAttributes: CategorizedAttributes? = null
) {
    fun getCustomAttributes(): MutableMap<String, Any> = stateAttributes

}