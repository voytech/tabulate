package pl.voytech.exporter.core.model

object NextId {

    private var nextId: ThreadLocal<Int> = ThreadLocal()

    fun nextId(): Int {
        val value = nextId.get() ?: 0
        nextId.set(value + 1)
        return nextId.get()
    }

    fun reset() {
        nextId.set(0)
    }
}

