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
package com.github.projectsandstone.common.test.specializedevent

import com.github.jonathanxd.iutils.`object`.LateInit
import com.github.jonathanxd.kores.type.Generic
import com.github.jonathanxd.kores.type.genericTypeOf
import com.github.jonathanxd.redin.Late
import com.github.koresframework.eventsys.event.EventListenerRegistry
import com.github.koresframework.eventsys.event.EventManager
import com.github.koresframework.eventsys.event.annotation.Listener
import com.github.koresframework.eventsys.gen.event.EventGenerator
import com.github.koresframework.eventsys.util.create
import com.github.projectsandstone.api.event.init.PostInitializationEvent
import com.github.projectsandstone.api.plugin.Plugin
import org.slf4j.Logger
import javax.inject.Inject

@Plugin(id = "com.github.projectsandstone.common.test.specializedevent", name = "Specialization test plugin", version = "1.0")
class SpecializedEventPlugin @Inject constructor(val logger: Logger,
                                                 val eventGenerator: EventGenerator,
                                                 val eventManager: EventManager,
                                                 @Late val plugin: LateInit.Ref<SpecializedEventPlugin>) {

    @Listener
    fun postInit(init: PostInitializationEvent) {
        plugin.value
        val stringMessageEventType = genericTypeOf<MessageEvent<String>>()
        val messageEventClass = this.eventGenerator.createEventClass<MessageEvent<String>>(
                stringMessageEventType
        ).resolve()

        val messageEvent = create(messageEventClass, mapOf(
                "message" to "Hello"
        ))

        this.eventManager.dispatch(messageEvent, stringMessageEventType, this)
    }
}