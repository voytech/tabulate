package io.github.voytech.tabulate.core.model

import java.util.concurrent.atomic.AtomicInteger

object NextId {

    private var nextId: AtomicInteger = AtomicInteger(0)

    fun nextId(): Int {
        return nextId.getAndIncrement()
    }

    fun reset() {
        nextId.set(0)
    }
}

