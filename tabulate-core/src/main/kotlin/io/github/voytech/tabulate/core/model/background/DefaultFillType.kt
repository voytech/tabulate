package io.github.voytech.tabulate.core.model.background

import io.github.voytech.tabulate.core.api.builder.dsl.DSLCommand

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

interface DefaultFillStyleWords {
    var fill: FillType
    val wideDots : DSLCommand
        get() {
            fill = DefaultFillType.WIDE_DOTS; return DSLCommand
        }
    val largeSpots : DSLCommand
        get() {
            fill = DefaultFillType.LARGE_SPOTS; return DSLCommand
        }
    val bricks : DSLCommand
        get() {
            fill = DefaultFillType.BRICKS; return DSLCommand
        }

    val diamonds : DSLCommand
        get() {
            fill = DefaultFillType.DIAMONDS; return DSLCommand
        }

    val smallDots : DSLCommand
        get() {
            fill = DefaultFillType.SMALL_DOTS; return DSLCommand
        }

    val solid : DSLCommand
        get() {
            fill = DefaultFillType.SOLID; return DSLCommand
        }

    val squares : DSLCommand
        get() {
            fill = DefaultFillType.SQUARES; return DSLCommand
        }
}