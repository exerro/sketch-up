package com.exerro.sketchup.application

import com.exerro.sketchup.api.*
import com.exerro.sketchup.api.data.*
import com.exerro.sketchup.api.streams.ObservableStream
import com.exerro.sketchup.api.util.onError

class PluginManager(
    private val events: ObservableStream<Event>,
    private val window: Window
) {
    fun load(host: SketchHost, plugin: Plugin<*>) {
        if (plugin in loadedPlugins) { unload(plugin) }
        val loader = createLoader(plugin.name)
        loadedPlugins[plugin] = loader
        plugin.run { loader.load(host) }
    }

    fun unload(plugin: Plugin<*>) {
        // TODO
    }

    fun loadedPlugins(): Set<Plugin<*>> = loadedPlugins.keys

    private fun createLoader(pluginName: String) =
        PluginLoaderImpl(pluginName, events, window)

    private val loadedPlugins: MutableMap<Plugin<*>, PluginLoaderImpl> = mutableMapOf()
}

private class PluginLoaderImpl(
    private val pluginName: String,
    events: ObservableStream<Event>,
    window: Window,
): PluginLoader {
    override val events = events.onError(::printError)

    override val window = object: Window {
        override val events = window.events.onError(::printError)
        override fun close() { /* Plugins can't close the window. */ }
        override fun draw(draw: DrawContext.() -> Unit) = window.draw {
            try { draw() }
            catch (err: Throwable) { printError(err) }
        }

    }

    override fun registerAction(name: String, icon: Unit, defaultKeyBindings: Set<KeyCombination>, action: Unit) {
        // TODO
    }

    override fun registerEntityAction(
        name: String,
        icon: Unit,
        defaultKeyBindings: Set<KeyCombination>,
        action: (Set<Entity>) -> Unit
    ) {
        // TODO
    }

    override fun registerPathTransformer(transform: (Path<WorldSpace>) -> WeightedEntity) {
        // TODO
    }

    override fun registerInputTransformer(transform: (GenericInput, SketchLocation) -> Entity) {
        // TODO
    }

    override fun registerEventStream(events: ObservableStream<Event>) {
        // TODO
    }

    override fun onUnload(fn: () -> Unit) {
        onUnload.add(fn)
    }

    private fun printError(err: Throwable) {
        System.err.println("[ERROR]: In plugin '$pluginName'")
        err.printStackTrace(System.err)
    }

    private val onUnload: MutableList<() -> Unit> = mutableListOf()
}
