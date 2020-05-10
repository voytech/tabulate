package pl.voytech.exporter.core.template

import pl.voytech.exporter.core.model.Table

open class ExportOperationsTemplate<T> {

    fun initialize(table: Table<T>, collection: Collection<T>) {
        
    }

    fun createColumnTitlesRow() {

    }

    fun export(table: Table<T>, collection: Collection<T>) {
        collection.forEach {
            this.exportRow(table, it)
        }
    }

    private fun exportRow(table: Table<T>, row: T) {

    }

    fun collect() {

    }
}