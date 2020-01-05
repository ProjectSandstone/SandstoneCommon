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
package com.github.projectsandstone.common.test.invocationtest

import com.github.projectsandstone.api.Game
import com.github.projectsandstone.api.event.init.PostInitializationEvent
import com.github.projectsandstone.api.plugin.Plugin
import com.github.projectsandstone.api.plugin.PluginContainer
import com.github.projectsandstone.common.util.extension.typeInfo
import com.github.koresframework.eventsys.event.ListenerSpec
import com.github.koresframework.eventsys.event.annotation.Listener
import com.github.koresframework.eventsys.gen.event.EventClassSpecification
import com.github.koresframework.eventsys.reflect.PropertiesSort
import org.slf4j.Logger
import javax.inject.Inject

@Plugin(id = "projectsandstone.invocationtest", name = "InvocationTestPlugin", version = "1.0")
class InvocationTestPlugin @Inject constructor(val logger: Logger, val game: Game, val container: PluginContainer) {

    val evListener = EvListener(logger)

    @Listener
    fun post(event: PostInitializationEvent) {

        val spec = EventClassSpecification(
                MyEvent::class.typeInfo,
                emptyList(),
                emptyList()
        )

        val evClass = game.eventGenerator
                .createEventClass(MyEvent::class.java)
                .resolve()

        game.eventGenerator.registerEventImplementation(spec, evClass)

        val arg = PropertiesSort.sort(evClass.constructors.first(),
                arrayOf("message"),
                arrayOf("Test 123")) // A factory interface is better

        val myEvent = evClass.constructors.first().newInstance(*arg) as MyEvent

        val toInvoke = EvListener::class.java.getDeclaredMethod("toInvoke", MyEvent::class.java)

        val generated = game.eventGenerator.
                createMethodListener(EvListener::class.java, toInvoke, evListener, ListenerSpec.fromMethod(toInvoke))
                .resolve()

        generated.onEvent(myEvent, container)
    }
}

class EvListener(logger: Logger) {
    @Listener
    fun toInvoke(event: MyEvent) {
        println("Message: ${event.message}")
    }
}