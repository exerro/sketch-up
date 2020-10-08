package com.exerro.sketchup.api.util

import com.exerro.sketchup.api.streams.ConnectedObservable
import com.exerro.sketchup.api.streams.ObservableStream
import com.exerro.sketchup.api.streams.ObservableStreamConnection

/** Handle downstream exceptions using [handler]. */
inline fun <T> ObservableStream<T>.onError(crossinline handler: (Throwable) -> Unit) = ObservableStream<T> { onItem ->
    connect { item -> try { onItem(item) } catch (e: Throwable) { handler(e) } }
}

/** Map items of this stream to new streams and connect to those streams
 *  with the same callback. Sub-streams will be disconnected from when
 *  this stream is disconnected from. */
inline fun <T, R> ObservableStream<T>.flatMap(crossinline fn: (T) -> ObservableStream<R>) = ObservableStream<R> { onItem ->
    val connections = mutableListOf<ObservableStreamConnection>()
    connections.add(connect {
        val conn = fn(it).connect(onItem)
        synchronized(connections) { connections.add(conn) }
    })
    ObservableStreamConnection {
        synchronized(connections) {
            connections.forEach(ObservableStreamConnection::disconnect)
            connections.clear()
        }
    }
}

/** Map items of this stream to new items, potentially of a different type. */
inline fun <T, R> ObservableStream<T>.map(crossinline fn: (T) -> R) = ObservableStream<R> { onItem ->
    connect { onItem(fn(it)) }
}

/** TODO */
fun <T, R> ObservableStream<T>.fold(initial: R, fn: (R, T) -> R): ConnectedObservable<R> {
    var current = initial
    val manager = StreamConnectionManager<R>()
    val connection = connect { item ->
        val newItem = fn(current, item)
        current = newItem
        manager.invoke(newItem)
    }

    return object: ConnectedObservable<R> {
        override val latest: R get() = current

        override fun connect(onItem: (R) -> Unit): ObservableStreamConnection {
            onItem(current)
            return manager.add(onItem)
        }

        override fun disconnect() {
            connection.disconnect()
        }
    }
}

/** Return a ConnectedObservable<T> given this stream and an initial value. */
fun <T> ObservableStream<T>.track(initial: T) = fold(initial) { _, v -> v }

/** Filter items of this stream based on a [predicate] function. */
inline fun <T> ObservableStream<T>.filter(crossinline predicate: (T) -> Boolean) = ObservableStream<T> { onItem ->
    connect { if (predicate(it)) onItem(it) }
}

/** Filter items of this stream to type [T]. */
inline fun <reified T> ObservableStream<*>.filterIsInstance() = ObservableStream<T> { onItem ->
    connect { if (it is T) onItem(it) }
}

/** Combine two streams. Note that results may be interleaved or concatenated
 *  as the streams are theoretically infinite, and potentially finite and fixed. */
operator fun <T> ObservableStream<T>.plus(other: ObservableStream<T>) = ObservableStream<T> { onItem ->
    val c1 = connect(onItem)
    val c2 = other.connect(onItem)
    ObservableStreamConnection {
        c1.disconnect()
        c2.disconnect()
    }
}
