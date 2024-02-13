package io.github.voytech.tabulate.core.model.attributes

import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.overflow.Overflow

data class HorizontalOverflowAttribute(val overflow: Overflow) : Attribute<HorizontalOverflowAttribute>()  {
}

data class VerticalOverflowAttribute(val overflow: Overflow): Attribute<VerticalOverflowAttribute>() {
}