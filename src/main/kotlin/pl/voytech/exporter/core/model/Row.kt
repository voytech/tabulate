package pl.voytech.exporter.core.model


class Row {
    enum class Position {
        END,
        START
    }

    val index: Int?
    val type: Position?

    constructor(index: Int) {
        this.index = index
        this.type = null
    }

    constructor(type: Position) {
        this.index = null
        this.type = type
    }

}