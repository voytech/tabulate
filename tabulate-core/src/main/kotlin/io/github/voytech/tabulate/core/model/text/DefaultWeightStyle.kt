package io.github.voytech.tabulate.core.model.text

enum class DefaultWeightStyle: WeightStyle {
    NORMAL,
    BOLD;

    override fun getWeightStyleId(): String = name
}