package io.github.voytech.tabulate.core.model.background

enum class DefaultFillType : FillType {
    WIDE_DOTS,
    LARGE_SPOTS,
    BRICKS,
    DIAMONDS,
    SMALL_DOTS,
    SOLID,
    SQUARES;

    override fun getCellFillId() = name
}