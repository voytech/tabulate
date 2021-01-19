package pl.voytech.exporter.core.template.iterators

import pl.voytech.exporter.core.template.OperationContext

abstract class TableDataIterator<E, T : OperationContext<E>> : Iterator<T> {

}
