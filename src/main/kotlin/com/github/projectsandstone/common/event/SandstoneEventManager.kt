/**
 *      SandstoneCommon - Common implementation of SandstoneAPI
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) Sandstone <https://github.com/ProjectSandstone/>
 *      Copyright (c) contributors
 *
 *
 *      Permission is hereby granted, free of charge, to any person obtaining a copy
 *      of this software and associated documentation files (the "Software"), to deal
 *      in the Software without restriction, including without limitation the rights
 *      to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *      copies of the Software, and to permit persons to whom the Software is
 *      furnished to do so, subject to the following conditions:
 *
 *      The above copyright notice and this permission notice shall be included in
 *      all copies or substantial portions of the Software.
 *
 *      THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *      IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *      FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *      AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *      LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *      OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *      THE SOFTWARE.
 */
package com.github.projectsandstone.common.event

import com.github.jonathanxd.iutils.type.TypeInfo
import com.github.jonathanxd.iutils.type.TypeUtil
import com.github.projectsandstone.api.Sandstone
import com.github.projectsandstone.api.constants.SandstonePlugin
import com.github.projectsandstone.api.event.*
import com.github.projectsandstone.api.event.EventListener
import com.github.projectsandstone.api.plugin.PluginContainer
import com.github.projectsandstone.api.util.internal.gen.event.listener.MethodListenerGen
import com.github.projectsandstone.common.Constants
import com.github.projectsandstone.common.util.event.getEventType
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.Executors

/**
 * Sandstone Event Manager common implementation
 *
 * @param generateDispatchClass True to generate classes that delegates directly to the listener method (faster),
 * false to use Java 7 MethodHandles to delegate to listener methods (slower) (see [MethodDispatcher]).
 */
class SandstoneEventManager @JvmOverloads constructor(val generateDispatchClass: Boolean = true) : EventManager {
    private val listeners: MutableSet<EventListenerContainer<*>> = TreeSet()
    private val executor = Executors.newCachedThreadPool(Constants.daemonThreadFactory)

    override fun <T : Event> dispatch(event: T, pluginContainer: PluginContainer, isBeforeModifications: Boolean) {
        this.dispatch_(event, pluginContainer, isBeforeModifications, isAsync = false)
    }


    private fun <T : Event> dispatch_(event: T, pluginContainer: PluginContainer, isBeforeModifications: Boolean, isAsync: Boolean) {

        val eventType = getEventType(event)

        fun <T : Event> tryDispatch(eventListenerContainer: EventListenerContainer<*>,
                                    event: T,
                                    pluginContainer: PluginContainer,
                                    isBeforeModifications: Boolean) {

            if (isAsync) {
                executor.execute {
                    dispatchDirect(eventListenerContainer, event, eventType, pluginContainer, isBeforeModifications)
                }
            } else {
                dispatchDirect(eventListenerContainer, event, eventType, pluginContainer, isBeforeModifications)
            }
        }

        listeners.filter {
            this.check(container = it, isPlugin = true, eventType = eventType, isBeforeModifications = isBeforeModifications)
        }.forEach {
            tryDispatch(it, event, pluginContainer, isBeforeModifications)
        }

        listeners.filter {
            this.check(container = it, isPlugin = false, eventType = eventType, isBeforeModifications = isBeforeModifications)
        }.forEach {
            tryDispatch(it, event, pluginContainer, isBeforeModifications)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun <T : Event> dispatchDirect(eventListenerContainer: EventListenerContainer<*>,
                                                  event: T,
                                                  eventType: TypeInfo<*>,
                                                  pluginContainer: PluginContainer,
                                                  isBeforeModifications: Boolean) {
        try {
            eventListenerContainer.eventListener.helpOnEvent(event, pluginContainer)
        } catch (throwable: Throwable) {
            Sandstone.logger.exception(RuntimeException("Cannot dispatch event $event (type: $eventType) to listener " +
                    "${eventListenerContainer.eventListener} (of event type: ${eventListenerContainer.eventType}) of plugin " +
                    "${eventListenerContainer.pluginContainer}. " +
                    "(Source Plugin: $pluginContainer, isBeforeModifications: $isBeforeModifications)", throwable), "")

        }
    }

    private fun check(container: EventListenerContainer<*>, isPlugin: Boolean, eventType: TypeInfo<*>, isBeforeModifications: Boolean): Boolean {

        fun checkType(): Boolean {
            return container.eventType.isAssignableFrom(eventType)
                    ||
                    (container.eventType.related.isEmpty()
                            && container.eventType.aClass.isAssignableFrom(eventType.aClass))
        }

        return (container.pluginContainer != SandstonePlugin) == isPlugin
                && checkType()
                && container.eventListener.isBeforeModifications() == isBeforeModifications
    }

    override fun <T : Event> dispatchAsync(event: T, pluginContainer: PluginContainer, isBeforeModifications: Boolean) {
        this.dispatch_(event, pluginContainer, isBeforeModifications, isAsync = true)
    }

    override fun getListeners(): Set<Pair<TypeInfo<*>, EventListener<*>>> {
        return this.listeners
                .map { Pair(it.eventType, it.eventListener) }
                .toSet()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Event> getListeners(eventType: TypeInfo<T>): Set<Pair<TypeInfo<T>, EventListener<T>>> {
        return this.listeners
                .filter { eventType.isAssignableFrom(it.eventType) }
                .map { Pair(it.eventType, it.eventListener) }
                .toSet() as Set<Pair<TypeInfo<T>, EventListener<T>>>
    }

    override fun <T : Event> registerListener(plugin: Any, eventType: TypeInfo<T>, eventListener: EventListener<T>) {
        val find = this.findListener(plugin, eventType, eventListener)

        if (find == null) {
            val pluginContainer = Sandstone.game.pluginManager.getRequiredPlugin(plugin)
            this.listeners.add(EventListenerContainer(pluginContainer, eventType, eventListener))
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Event> registerGenericListener(plugin: Any, eventType: TypeInfo<*>, eventListener: EventListener<*>) {
        this.registerListener(plugin, eventType as TypeInfo<T>, eventListener as EventListener<T>)
    }

    override fun registerListeners(plugin: Any, listener: Any) {
        this.createMethodListeners(plugin, listener).forEach {
            this.registerGenericListener<Event>(plugin, it.eventType, it.eventListener)
        }
    }

    override fun registerMethodListener(plugin: Any, instance: Any?, method: Method, eventPriority: EventPriority, ignoreCancelled: Boolean, isBeforeModifications: Boolean) {
        if (instance != null && plugin == instance) {

        } else {
            this.createPluginMethodListener(
                    plugin = Sandstone.game.pluginManager.getRequiredPlugin(plugin),
                    instance = instance,
                    method = method).let {
                this.registerGenericListener<Event>(plugin, it.eventType, it.eventListener)
            }
        }
    }

    fun <T : Event> findListener(plugin: Any, eventType: TypeInfo<T>, eventListener: EventListener<T>) =
            this.listeners.find { it.pluginContainer.instance == plugin && it.eventType.compareTo(eventType) == 0 && it.eventListener == eventListener }

    @Suppress("UNCHECKED_CAST")
    private fun createPluginMethodListener(plugin: PluginContainer,
                                           instance: Any?,
                                           method: Method): EventListenerContainer<*> {


        return EventListenerContainer(plugin,
                TypeUtil.toReference(method.genericParameterTypes[0]) as TypeInfo<Event>,
                MethodListenerGen.create(plugin, method, instance, ListenerData.fromMethod(method)))
    }


    private fun createMethodListeners(plugin: Any,
                                      instance: Any): List<EventListenerContainer<*>> {

        return instance::class.java.declaredMethods.filter {
            it.getDeclaredAnnotation(Listener::class.java) != null
                    && it.parameterCount > 0
                    && Event::class.java.isAssignableFrom(it.parameterTypes[0])
        }.map {
            if(this.generateDispatchClass) {
                return@map this.createPluginMethodListener(
                        plugin = Sandstone.game.pluginManager.getRequiredPlugin(plugin),
                        method = it,
                        instance = instance)
            } else {
                val data = ListenerData.fromMethod(it)

                @Suppress("UNCHECKED_CAST")
                return@map EventListenerContainer(
                        pluginContainer = Sandstone.game.pluginManager.getRequiredPlugin(plugin),
                        eventType = data.eventType as TypeInfo<Event>,
                        eventListener = MethodDispatcher(instance, data, it))
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Event> EventListener<T>.helpOnEvent(event: Any, pluginContainer: PluginContainer) {
        this.onEvent(event as T, pluginContainer)
    }
}