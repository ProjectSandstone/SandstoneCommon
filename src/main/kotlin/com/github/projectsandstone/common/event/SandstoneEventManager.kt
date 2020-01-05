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
import com.github.koresframework.eventsys.channel.ChannelSet
import com.github.koresframework.eventsys.event.*
import com.github.koresframework.eventsys.gen.event.CommonEventGenerator
import com.github.koresframework.eventsys.gen.event.EventGenerator
import com.github.koresframework.eventsys.impl.*
import com.github.koresframework.eventsys.logging.LoggerInterface
import com.github.projectsandstone.api.Sandstone
import com.github.projectsandstone.common.Constants
import java.lang.reflect.Method
import javax.inject.Inject

/**
 * Sandstone Event Manager common implementation
 */
class SandstoneEventManager @Inject constructor(
        val eventListenerRegistry: EventListenerRegistry,
        val eventGenerator: EventGenerator,
        override val eventDispatcher: EventDispatcher
) : EventManager {
    /*private val logger by lazy(LazyThreadSafetyMode.NONE) { WrapperLoggerInterface(Sandstone.logger) }

    internal val commonGenerator: EventGenerator by lazy(LazyThreadSafetyMode.NONE) {
        CommonEventGenerator(logger)
    }

    private val commonRegistry: EventListenerRegistry by lazy(LazyThreadSafetyMode.NONE) {
        CommonChannelEventListenerRegistry(
                ChannelSet.ALL,
                Constants.listenerSorter,
                logger,
                commonGenerator
        )
    }


    internal val commonDispatcher: EventDispatcher by lazy(LazyThreadSafetyMode.NONE) {
        CommonEventDispatcher(
                Constants.daemonThreadFactory,
                commonGenerator,
                logger,
                commonRegistry
        )
    }*/

    internal val commonManager: EventManager by lazy(LazyThreadSafetyMode.NONE) {
        CommonEventManager(
                this.eventGenerator,
                this.eventDispatcher,
                this.eventListenerRegistry
        )
    }
}