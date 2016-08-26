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
import com.github.projectsandstone.api.event.Event
import com.github.projectsandstone.api.event.EventPriority
import com.github.projectsandstone.api.event.MethodEventListener
import com.github.projectsandstone.api.plugin.PluginContainer
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method

/**
 * Created by jonathan on 18/08/16.
 */
class SandstoneMethodListener(override val eventType: TypeInfo<Event>,
                              override val instance: Any?,
                              val ignoreCancelled: Boolean,
                              val isBeforeMods: Boolean,
                              val method_: Method,
                              val priority_: EventPriority) : MethodEventListener {

    override val method: MethodHandle = lookup.unreflect(this.method_).bindTo(this.instance)
    override val parameters: Array<TypeInfo<*>> = this.method_.genericParameterTypes.map { TypeUtil.toReference(it) }.toTypedArray()

    override fun onEvent(event: Event, pluginContainer: PluginContainer) {
        // Process [parameters]
        method.invokeWithArguments(event)
    }

    override fun getPriority(): EventPriority {
        return this.priority_
    }

    override fun isBeforeModifications(): Boolean {
        return this.isBeforeMods
    }

    override fun ignoreCancelled(): Boolean {
        return this.ignoreCancelled
    }

    companion object {
        val lookup = MethodHandles.publicLookup()
    }
}