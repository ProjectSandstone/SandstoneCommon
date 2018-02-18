/*
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
import com.github.projectsandstone.api.Sandstone
import com.github.projectsandstone.common.Constants
import com.github.projectsandstone.eventsys.event.Event
import com.github.projectsandstone.eventsys.event.EventDispatcher
import com.github.projectsandstone.eventsys.event.EventListener
import com.github.projectsandstone.eventsys.event.EventManager
import com.github.projectsandstone.eventsys.gen.event.CommonEventGenerator
import com.github.projectsandstone.eventsys.gen.event.EventGenerator
import com.github.projectsandstone.eventsys.impl.CommonEventManager
import com.github.projectsandstone.eventsys.impl.EventListenerContainer
import java.lang.reflect.Method

/**
 * Sandstone Event Manager common implementation
 */
class SandstoneEventManager : EventManager {
    private val logger by lazy(LazyThreadSafetyMode.NONE) { WrapperLoggerInterface(Sandstone.logger) }

    private val commonManager: EventManager by lazy(LazyThreadSafetyMode.NONE) {
        CommonEventManager(
                Constants.listenerSorter,
                Constants.daemonThreadFactory,
                logger,
                CommonEventGenerator(logger)
        )
    }

    override val eventDispatcher: EventDispatcher
        get() = this.commonManager.eventDispatcher

    override val eventGenerator: EventGenerator
        get() = this.commonManager.eventGenerator

    override fun getListeners(): Set<Pair<TypeInfo<*>, EventListener<*>>> =
            this.commonManager.getListeners()

    override fun <T : Event> getListeners(eventType: TypeInfo<T>): Set<Pair<TypeInfo<T>, EventListener<T>>> =
            this.commonManager.getListeners(eventType)

    override fun getListenersContainers(): Set<EventListenerContainer<*>> =
            this.commonManager.getListenersContainers()

    override fun <T : Event> registerListener(owner: Any, eventType: TypeInfo<T>, eventListener: EventListener<T>) =
            this.commonManager.registerListener(owner, eventType, eventListener)

    override fun registerListeners(owner: Any, listener: Any) =
            this.commonManager.registerListeners(owner, listener)

    override fun registerMethodListener(owner: Any, instance: Any?, method: Method) =
            this.commonManager.registerMethodListener(owner, instance, method)
}