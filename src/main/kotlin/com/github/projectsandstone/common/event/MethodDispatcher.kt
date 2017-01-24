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

import com.github.jonathanxd.iutils.annotation.Named
import com.github.jonathanxd.iutils.type.TypeInfo
import com.github.jonathanxd.iutils.type.TypeUtil
import com.github.projectsandstone.api.event.Event
import com.github.projectsandstone.api.event.EventPriority
import com.github.projectsandstone.api.event.ListenerData
import com.github.projectsandstone.api.event.MethodEventListener
import com.github.projectsandstone.api.plugin.PluginContainer
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method

/**
 * Class used to dispatch method listener events.
 *
 * This dispatcher uses Java 7 [MethodHandle] to dispatch the event, the [MethodHandle] is faster
 * than Reflection but is slower than direct invocation.
 *
 * This dispatcher is a bit optimized, but we recommend to use direct invocation.
 */
open class MethodDispatcher(override val instance: Any?,
                            val listenerData: ListenerData,
                            val method_: Method) : MethodEventListener {

    @Suppress("UNCHECKED_CAST")
    override val eventType: TypeInfo<Event> = this.listenerData.eventType as TypeInfo<Event>

    private val backingMethod_: MethodHandle by lazy {
        lookup.unreflect(this.method_).bindTo(this.instance)
    }

    override val method: MethodHandle
        get() = backingMethod_

    override val parameters: Array<TypeInfo<*>> = this.method_.genericParameterTypes.map { TypeUtil.toReference(it) }.toTypedArray()

    internal val namedParameters: Array<com.github.jonathanxd.iutils.`object`.Named<TypeInfo<*>>> =
            this.method_.parameters.map {
                val typeInfo = TypeUtil.toReference(it.parameterizedType)

                val name: String? = it.getDeclaredAnnotation(Named::class.java)?.value
                        ?: it.getDeclaredAnnotation(javax.inject.Named::class.java)?.value
                        ?: it.getDeclaredAnnotation(com.google.inject.name.Named::class.java)?.value

                return@map com.github.jonathanxd.iutils.`object`.Named(name, typeInfo)

            }.toTypedArray()

    override fun onEvent(event: Event, pluginContainer: PluginContainer) {


        // Process [parameters]
        if (parameters.size == 1) {
            method.invokeWithArguments(event)
        } else if (parameters.size > 1) {
            val args: MutableList<Any?> = mutableListOf(event)

            this.namedParameters.forEachIndexed { i, named ->
                if (i > 0) {
                    val name = named.name
                    val typeInfo = named.value

                    args += event.getProperty(typeInfo.aClass, name)
                }
            }

            method.invokeWithArguments(args)
        } else {
            throw IllegalStateException("Invalid Method: $method_. (No Parameters)")
        }
    }

    override fun getPriority(): EventPriority {
        return this.listenerData.priority
    }

    override fun isBeforeModifications(): Boolean {
        return this.listenerData.beforeModifications
    }

    override fun ignoreCancelled(): Boolean {
        return this.listenerData.ignoreCancelled
    }

    companion object {
        val lookup = MethodHandles.publicLookup()
    }
}