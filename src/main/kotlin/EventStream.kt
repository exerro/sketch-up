
fun interface EventStreamConnection {
    fun disconnect()
}

fun interface EventStream<T> {
    fun connect(onEvent: (T) -> Unit): EventStreamConnection
}

////////////////////////////////////////////////////////////////////////////////

inline fun <T, R> EventStream<T>.map(crossinline fn: (T) -> R) = EventStream<R> { onEvent ->
    connect { onEvent(fn(it)) }
}

inline fun <T> EventStream<T>.filter(crossinline predicate: (T) -> Boolean) = EventStream<T> { onEvent ->
    connect { if (predicate(it)) onEvent(it) }
}

inline fun <reified T> EventStream<*>.filterIsInstance() = EventStream<T> { onEvent ->
    connect { if (it is T) onEvent(it) }
}
