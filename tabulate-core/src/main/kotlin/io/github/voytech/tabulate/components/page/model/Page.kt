package io.github.voytech.tabulate.components.page.model

import io.github.voytech.tabulate.components.commons.operation.newPage
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.layout.Region
import io.github.voytech.tabulate.core.layout.SpaceConstraints

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
        stickyHeaderAndFooterWith { region, footerLeftTop ->
            exportContent(resolveContentMaxRightBottom(footerLeftTop, region))
        }
    }

    private fun ExportApi.renderNewPage() =
        getCustomAttributes().run {
            val execution = ensureExecutionContext { PageExecutionContext() }
            render(newPage(++execution.pageNumber, execution.currentPageTitleWithNumber(), this))
        }

    private fun ExportApi.stickyHeaderAndFooterWith(renderContents: (Region, Position?) -> Unit) {
        exportHeader()
        val footerSize = measureFooterSize()
        val footerLeftTop = currentLayoutSpace().findFooterLeftTop(footerSize)
        renderContents(currentLayoutSpace(), footerLeftTop)
        exportFooter(footerLeftTop + footerSize)
    }

    private fun ExportApi.exportHeader() {
        header?.export(null, true)
    }

    private fun ExportApi.exportContent(spaceConstraints: SpaceConstraints) {
        models.forEach {
            it.export(spaceConstraints)
        }
    }

    private fun ExportApi.measureFooterSize() =
        footer?.measure(null, true)?.let { Size(it.width, it.height) }

    private fun Region.findFooterLeftTop(size: Size?): Position? =
        size?.let { Position(leftTop.x, maxRightBottom.y - it.height) }

    private fun resolveContentMaxRightBottom(footerLeftTop: Position?, region: Region): SpaceConstraints =
        SpaceConstraints(maxRightBottom = footerLeftTop?.let { Position(region.maxRightBottom.x, it.y) })

    private operator fun Position?.plus(size: Size?): SpaceConstraints =
        SpaceConstraints(leftTop = this, maxRightBottom = size?.let { this?.plus(size) })

    private fun ExportApi.exportFooter(spaceConstraints: SpaceConstraints?) {
        footer?.export(spaceConstraints, true)
    }

}
