package io.github.voytech.tabulate.excel

import io.github.voytech.tabulate.core.model.StateAttributes

class ExcelInputParams {
    companion object {
        const val STREAMING_WINDOW_SIZE = "xlsx-rows-count-in-window"
    }
}

fun Map<String, Any>.setXlsxRowsCountInWindow(size: Int = 100): Map<String, Any> = this + (ExcelInputParams.STREAMING_WINDOW_SIZE to size)

fun StateAttributes.setXlsxRowsCountInWindow(size: Int = 100): StateAttributes =
    apply { set<Int>(ExcelInputParams.STREAMING_WINDOW_SIZE, size) }

fun StateAttributes.getXlsxRowsCountInWindow(): Int = get<Int>(ExcelInputParams.STREAMING_WINDOW_SIZE) ?: 100
