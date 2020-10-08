package com.exerro.sketchup.api

import com.exerro.sketchup.api.data.*
import com.exerro.sketchup.api.streams.ObservableStream
import com.exerro.sketchup.api.streams.ObservableStreamConnection

interface Plugin<Data: PluginData> {
    val name: String

    fun PluginLoader.load(host: SketchHost)

    fun deserialise(str: String): Data
}

interface PluginData {
    fun serialise(): String
}

interface PluginLoader {
    /** Stream of events coming into the application, including window events. */
    val events: ObservableStream<Event>
    /** Window being used for events/drawing. */
    val window: Window

    /** Register a global action. */
    fun registerAction(
        name: String,
        icon: Unit, // TODO
        defaultKeyBindings: Set<KeyCombination> = emptySet(),
        action: Unit, // TODO
    ) // TODO

    /** Register an action on a set of entities. */
    fun registerEntityAction(
        name: String,
        icon: Unit, // TODO
        defaultKeyBindings: Set<KeyCombination> = emptySet(),
        action: (Set<Entity>) -> Unit, // TODO
    ) // TODO

    /** Register a function to convert an input path to an entity. */
    fun registerPathTransformer(transform: (Path<WorldSpace>) -> WeightedEntity)

    /** Register a function to convert an input event to an entity. */
    fun registerInputTransformer(transform: (GenericInput, SketchLocation) -> Entity)

    /** Register a custom stream of events. */
    fun registerEventStream(events: ObservableStream<Event>)

    /** Specify a function to run when the plugin is unloaded. This may be
     *  called many times. */
    fun onUnload(fn: () -> Unit)

    /** Specify a connection to disconnect when the plugin is unloaded. This may
     *  be called many times. */
    fun onUnload(connection: ObservableStreamConnection) = onUnload {
        connection.disconnect()
    }
}
