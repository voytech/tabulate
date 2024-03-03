package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.core.HavingViewportSize
import io.github.voytech.tabulate.core.RenderingContext
import io.github.voytech.tabulate.core.model.Height
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.Width
import io.github.voytech.tabulate.support.mock.Spy

class TestRenderingContext : RenderingContext, HavingViewportSize {

    override fun getWidth(): Width = Spy.spy.documentWidth ?: Width(600F,UnitsOfMeasure.PT)

    override fun getHeight(): Height = Spy.spy.documentHeight ?: Height(800F,UnitsOfMeasure.PT)

}