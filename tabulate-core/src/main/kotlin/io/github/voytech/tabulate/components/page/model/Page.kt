package io.github.voytech.tabulate.components.page.model

import io.github.voytech.tabulate.components.commons.operation.newPage
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.layout.LayoutSpace
import io.github.voytech.tabulate.core.layout.SpaceConstraints

class Page internal constructor(
    @get:JvmSynthetic
    internal val name: String = "untitled",
    @get:JvmSynthetic
    internal val nodes: List<AbstractModel>? = null,
    internal val header: AbstractModel? = null,
    internal val footer: AbstractModel? = null,
) : AbstractModel() {

    override fun initialize(api: ExportApi) = api {
        getCustomAttributes()["_pageName"] = name
        getCustomAttributes()["_sheetName"] = name
    }

    override fun prepareExport(api: ExportApi): Unit = api {
        clearLayouts()
    }

    override fun doExport(api: ExportApi) = api {
        render(newPage(nextPageNumber(), name))
        stickyHeaderAndFooterWith { layout, footerLeftTop ->
            exportContent(footerLeftTop.asConstraints(layout))
        }
    }

    private fun ExportApi.nextPageNumber(): Int =
        getCustomAttributes().run {
            ++ensureExecutionContext { PageExecutionContext() }.pageNumber
        }

    private fun ExportApi.stickyHeaderAndFooterWith(renderContents: (LayoutSpace, Position?) -> Unit) {
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
        nodes?.forEach {
            it.export(spaceConstraints)
        }
    }

    private fun ExportApi.measureFooterSize() =
        footer?.measure(null, true)?.let { Size(it.width, it.height) }

    private fun LayoutSpace.findFooterLeftTop(size: Size?): Position? = maxRightBottom?.let { maxRightBottom ->
        size?.let { Position(leftTop.x, maxRightBottom.y - it.height) }
    }

    private fun Position?.asConstraints(layoutSpace: LayoutSpace): SpaceConstraints =
        SpaceConstraints(maxRightBottom = this?.let { Position(layoutSpace.maxRightBottom!!.x, it.y) })

    private operator fun Position?.plus(size: Size?): SpaceConstraints =
        SpaceConstraints(leftTop = this, maxRightBottom = size?.let { this?.plus(size) })

    private fun ExportApi.exportFooter(spaceConstraints: SpaceConstraints?) {
        footer?.export(spaceConstraints, true)
    }

}
