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
package com.github.projectsandstone.common.test.invocationtest

import com.github.projectsandstone.api.Game
import com.github.projectsandstone.api.event.Listener
import com.github.projectsandstone.api.event.ListenerData
import com.github.projectsandstone.api.event.SandstoneEventFactory
import com.github.projectsandstone.api.event.init.PostInitializationEvent
import com.github.projectsandstone.api.logging.Logger
import com.github.projectsandstone.api.plugin.Plugin
import com.github.projectsandstone.api.plugin.PluginContainer
import com.github.projectsandstone.api.util.internal.gen.event.listener.MethodListenerGen
import com.github.projectsandstone.common.util.extension.typeInfo
import javax.inject.Inject

@Plugin(id = "projectsandstone.invocationtest", name = "InvocationTestPlugin", version = "1.0")
class InvocationTestPlugin @Inject constructor(val logger: Logger, val game: Game, val container: PluginContainer) {

    val evListener = EvListener(logger)

    @Listener
    fun post(event: PostInitializationEvent) {

        val event = SandstoneEventFactory.createEvent(MyEvent::class.typeInfo, mapOf("message" to "Test 123"))

        val toInvoke = EvListener::class.java.getDeclaredMethod("toInvoke", MyEvent::class.java)

        val generated = MethodListenerGen.create(container, toInvoke, evListener, ListenerData.fromMethod(toInvoke))

        generated.onEvent(event, container)


    }
}

class EvListener(logger: Logger) {
    @Listener
    fun toInvoke(event: MyEvent) {
        println("Message: ${event.message}")
    }
}