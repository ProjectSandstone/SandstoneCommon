/**
 *      SandstoneCommon - Common implementation of SandstoneAPI
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2016 Sandstone <https://github.com/ProjectSandstone/>
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

import com.github.jonathanxd.iutils.`object`.TypeInfo
import com.github.jonathanxd.iutils.`object`.TypeUtil
import com.github.projectsandstone.api.event.*
import com.github.projectsandstone.api.event.EventListener
import com.github.projectsandstone.api.plugin.PluginContainer
import java.lang.reflect.Method
import java.util.*

/**
 * Created by jonathan on 18/08/16.
 */
class SandstoneEventManager : EventManager {

    private val listeners: MutableSet<EventListenerContainer<*>> = TreeSet()


    // First dispatch plugins, then dispatch to normal event listeners. (plugin specific registration need to be created).
    override fun <T : Event> dispatch(event: T, pluginContainer: PluginContainer, isBeforeModifications: Boolean) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // TODO Change to Set<Pair<TypeInfo<*>, EventListener<*>>
    override fun getListeners(): Set<Pair<TypeInfo<*>, EventListener<*>>> {
        return this.listeners
                .map { Pair(it.eventType, it.eventListener) }
                .toSet()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Event> getListeners(eventType: TypeInfo<T>): Set<Pair<TypeInfo<T>, EventListener<T>>> {
        return this.listeners
                .filter { it.eventType.compareToAssignable(eventType) == 0 }
                .map { Pair(it.eventType, it.eventListener) }
                .toSet() as Set<Pair<TypeInfo<T>, EventListener<T>>>
    }

    override fun <T : Event> registerListener(plugin: Any, eventType: TypeInfo<T>, eventListener: EventListener<T>) {
        val find = this.findListener(plugin, eventType, eventListener)

        if (find == null) {
            this.listeners.add(EventListenerContainer(plugin, eventType, eventListener))
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Event> registerGenericListener(plugin: Any, eventType: TypeInfo<*>, eventListener: EventListener<*>) {
        this.registerListener(plugin, eventType as TypeInfo<T>, eventListener as EventListener<T>)
    }

    override fun registerListeners(plugin: Any, listener: Any) {
        this.createMethodListeners(plugin, listener).forEach {
            this.registerGenericListener<Event>(plugin, it.eventType, it)
        }
    }

    override fun registerMethodListener(plugin: Any, instance: Any?, method: Method, eventPriority: EventPriority, ignoreCancelled: Boolean, isBeforeModifications: Boolean) {
        this.createMethodListener(instance = instance,
                method = method,
                eventPriority = eventPriority,
                ignoreCancelled = ignoreCancelled,
                isBeforeModifications = isBeforeModifications).let {
            this.registerGenericListener<Event>(plugin, it.eventType, it)
        }
    }

    fun <T : Event> findListener(plugin: Any, eventType: TypeInfo<T>, eventListener: EventListener<T>) =
            this.listeners.find { it.plugin == plugin && it.eventType.compareTo(eventType) == 0 && it.eventListener == eventListener }

    @Suppress("UNCHECKED_CAST")
    private fun createMethodListener(instance: Any?,
                                     method: Method,
                                     isBeforeModifications: Boolean = false,
                                     eventPriority: EventPriority = EventPriority.NORMAL,
                                     ignoreCancelled: Boolean = false): MethodEventListener {

        return SandstoneMethodListener(eventType = TypeUtil.toReference(method.genericParameterTypes[0]) as TypeInfo<Event>,
                instance = instance,
                isBeforeMods = isBeforeModifications,
                method_ = method,
                priority_ = eventPriority,
                ignoreCancelled = ignoreCancelled)
    }


    private fun createMethodListeners(plugin: Any,
                                     instance: Any,
                                     isBeforeModifications: Boolean = false,
                                     eventPriority: EventPriority = EventPriority.NORMAL): List<MethodEventListener> {

        return instance.javaClass.declaredMethods.filter {
            it.getDeclaredAnnotation(Listener::class.java) != null
                    && it.parameterCount > 0
                    && Event::class.java.isAssignableFrom(it.parameterTypes[0])
        }.map {
            val listenerAnnotation = it.getDeclaredAnnotation(Listener::class.java)

            this.createMethodListener(instance = instance,
                    isBeforeModifications = listenerAnnotation.beforeModifications,
                    method = it,
                    eventPriority = listenerAnnotation.priority,
                    ignoreCancelled = listenerAnnotation.ignoreCancelled)
        }
    }
}