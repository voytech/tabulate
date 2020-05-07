package pl.voytech.exporter.core.model

data class Table<T>(
   val columns : List<Column<T>> = emptyList(),
   val showHeader: Boolean? = false,
   val showFooter: Boolean? = false,
   val name: String? = "",
   val headerText: String? = "",
   val footerText: String? =""
)
