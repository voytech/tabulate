package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.attributes.Attribute

sealed class AttributedModel<A: Attribute<*>>(open val attributes: Set<A>?) : ContextData()
