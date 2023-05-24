package io.github.voytech.tabulate.core.model.text

import io.github.voytech.tabulate.core.api.builder.dsl.DSLCommand

enum class DefaultWeightStyle: WeightStyle {
    NORMAL,
    BOLD;

    override fun getWeightStyleId(): String = name
}



interface WeightStyleBuilder {
    var weight: WeightStyle?
}


interface WeightStyleWords : WeightStyleBuilder {
    val bold : DSLCommand
        get() {
            weight = DefaultWeightStyle.BOLD; return DSLCommand
        }

    val normal : DSLCommand
        get() {
            weight = DefaultWeightStyle.NORMAL; return DSLCommand
        }
}