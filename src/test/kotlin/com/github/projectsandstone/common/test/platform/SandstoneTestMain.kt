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

import com.github.projectsandstone.api.Sandstone
import com.github.projectsandstone.api.constants.SandstonePlugin
import com.github.projectsandstone.api.event.SandstoneEventFactoryCache
import com.github.projectsandstone.common.SandstoneInit
import java.nio.file.Paths

object SandstoneTestMain {
    fun main(args: Array<String>) {
        System.err.println(" Starting Sandstone test environment...")

        start()

        System.err.println(" Sandstone test environment started!")

        //SandstoneInit.loadPlugins(Paths.get("./plugins/"))
    }

    fun start() {
        SandstoneInit.initConsts()
        SandstoneInit.initGame(TestGame())
        SandstoneInit.initLogger(TestLogger(null))
        SandstoneInit.initLoggerFactory(TestLoggerFactory())
        SandstoneInit.initPath(Paths.get("testenv"))
        SandstoneEventFactoryCache.getAsync()
    }

    fun init() {
        Sandstone.eventManager.dispatchAsync(Sandstone.eventFactory.createPreInitializationEvent(), SandstonePlugin)
        Sandstone.eventManager.dispatch(Sandstone.eventFactory.createInitializationEvent(), SandstonePlugin)
        Sandstone.eventManager.dispatch(Sandstone.eventFactory.createPostInitializationEvent(), SandstonePlugin)
        Sandstone.eventManager.dispatch(Sandstone.eventFactory.createServerStartingEvent(), SandstonePlugin)
        Sandstone.eventManager.dispatch(Sandstone.eventFactory.createServerStartedEvent(emptyList()), SandstonePlugin)
    }

    fun stop() {
        Sandstone.eventManager.dispatch(Sandstone.eventFactory.createServerStoppingEvent(), SandstonePlugin)
        Sandstone.eventManager.dispatch(Sandstone.eventFactory.createServerStoppedEvent(), SandstonePlugin)
    }
}