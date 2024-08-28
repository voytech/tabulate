package io.github.voytech.tabulate.components.page.model

import io.github.voytech.tabulate.components.commons.operation.newPage
import io.github.voytech.tabulate.core.layout.Layout
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.layout.RegionConstraints

class Page internal constructor(
    @get:JvmSynthetic
    internal val name: String = "untitled",
    internal val header: AbstractModel? = null,
    internal val footer: AbstractModel? = null,
    override val models: List<AbstractModel>,
) : AbstractContainerModel() {

    override fun exportContextCreated(api: ExportApi) = api {
        getCustomAttributes()["_pageName"] = name
        getCustomAttributes()["_sheetName"] = name
        getCustomAttributes().ensureExecutionContext { PageExecutionContext() }.pageTitle = name
    }

    override fun doExport(api: ExportApi) = api {
        renderNewPage()
        stickyHeaderAndFooterWith { layout, footerLeftTop ->
            exportContent(resolveContentMaxRightBottom(footerLeftTop, layout))
        }
    }

    private fun ExportApi.renderNewPage() =
        getCustomAttributes().run {
            val execution = ensureExecutionContext { PageExecutionContext() }
            render(newPage(++execution.pageNumber, execution.currentPageTitleWithNumber(), this))
        }

    private fun ExportApi.stickyHeaderAndFooterWith(renderContents: (Layout, Position?) -> Unit) {
        exportHeader()
        val footerSize = measureFooterSize()
        val currentLayout = currentLayout()
        val footerLeftTop = currentLayout.findFooterLeftTop(footerSize)
        renderContents(currentLayout, footerLeftTop)
        exportFooter(footerLeftTop + footerSize)
    }

    private fun ExportApi.exportHeader() {
        header?.export(force = true)
    }

    private fun ExportApi.exportContent(regionConstraints: RegionConstraints) {
        models.forEach {
            it.export(regionConstraints)
        }
    }

    private fun ExportApi.measureFooterSize() =
        footer?.measure(force = true)?.let { Size(it.width, it.height) }

    private fun Layout.findFooterLeftTop(size: Size?): Position? = size?.let {
        getContentRectangle().let { (leftTop, rightBottom) ->
            Position(leftTop.x, rightBottom.y - it.height)
        }
    }


    private fun resolveContentMaxRightBottom(footerLeftTop: Position?, layout: Layout): RegionConstraints {
        val rightBottom = layout.getContentRectangle().rightBottom
        return RegionConstraints(maxRightBottom = footerLeftTop?.let { Position(rightBottom.x, it.y) })
    }

    private operator fun Position?.plus(size: Size?): RegionConstraints =
        RegionConstraints(leftTop = this, maxRightBottom = size?.let { this?.plus(size) })

    private fun ExportApi.exportFooter(regionConstraints: RegionConstraints?) {
        footer?.export(regionConstraints, true)
    }

    override fun toString(): String = "Page[$name]"

}
