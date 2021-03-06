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
package com.github.projectsandstone.common.test.platform

import com.github.koresframework.eventsys.event.EventListenerRegistry
import com.github.projectsandstone.api.*
import com.github.projectsandstone.api.command.CommandManager
import com.github.projectsandstone.api.plugin.PluginManager
import com.github.projectsandstone.api.registry.Registry
import com.github.projectsandstone.api.scheduler.Scheduler
import com.github.projectsandstone.api.service.ServiceManager
import com.github.projectsandstone.api.util.edition.GameEdition
import com.github.projectsandstone.common.command.SandstoneCommandManager
import com.github.koresframework.eventsys.event.EventManager
import com.github.koresframework.eventsys.gen.event.CommonEventGenerator
import com.github.koresframework.eventsys.gen.event.EventGenerator
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject

class TestGame @Inject constructor(
        override val eventManager: EventManager,
        override val eventGenerator: EventGenerator,
        override val eventListenerRegistry: EventListenerRegistry,
        override val pluginManager: PluginManager,
        override val registry: Registry
) : Game {
    override val gamePath: Path = Paths.get("/")

    override val platform: Platform = TestPlatform()

    override val savePath: Path = Paths.get("/")

    override val scheduler: Scheduler = TestScheduler()

    override val server: Server = TestServer()

    override val serviceManager: ServiceManager = TestServiceManager()

    override val edition: GameEdition = TestGameEdition

    override val objectFactory: SandstoneObjectFactory = TestObjectFactory
    override val objectHelper: SandstoneObjectHelper = TestObjectHelper
    override val commandManager: CommandManager = SandstoneCommandManager(this)
}