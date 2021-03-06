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
package com.github.projectsandstone.common.test.serviceproxy

import com.github.projectsandstone.api.Game
import com.github.projectsandstone.api.event.init.InitializationEvent
import com.github.projectsandstone.api.event.init.PreInitializationEvent
import com.github.projectsandstone.api.event.init.ServerStartedEvent
import com.github.projectsandstone.api.plugin.Dependency
import com.github.projectsandstone.api.plugin.Plugin
import com.github.koresframework.eventsys.event.annotation.Listener
import org.slf4j.Logger
import javax.inject.Inject

@Plugin(
    id = "projectsandstone.serviceproxyplugin",
    name = "ServiceProxy",
    version = "1.0.0",
    dependencies = [Dependency("com.github.projectsandstone.common.test.eventlistenerplugin")]
)
class ServiceProxyPlugin @Inject constructor(val game: Game, val logger: Logger) {

    lateinit var service: TestService
    lateinit var unavailable: UnavailableService

    @Listener
    fun onInit(event: InitializationEvent) {

        service = game.serviceManager.provideProxy(TestService::class.java)
        unavailable = game.serviceManager.provideProxy(UnavailableService::class.java)

        game.serviceManager.setProvider(this, TestService::class.java, TestServiceImpl("Primeiro"))
    }

    @Listener
    fun server(event: ServerStartedEvent) {
        service.speak(logger)

        game.serviceManager.setProvider(this, TestService::class.java, TestServiceImpl("Segundo"))

        service.speak(logger)

        unavailable.x()
    }
}