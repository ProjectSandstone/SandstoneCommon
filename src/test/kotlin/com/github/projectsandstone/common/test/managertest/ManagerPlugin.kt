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
package com.github.projectsandstone.common.test.managertest

import com.github.projectsandstone.api.Game
import com.github.projectsandstone.api.event.Listener
import com.github.projectsandstone.api.event.init.InitializationEvent
import com.github.projectsandstone.api.event.init.PostInitializationEvent
import com.github.projectsandstone.api.event.init.ServerStartedEvent
import com.github.projectsandstone.api.event.service.ChangeServiceProviderEvent
import com.github.projectsandstone.api.logging.Logger
import com.github.projectsandstone.api.plugin.Plugin
import javax.inject.Inject

@Plugin(id = "ManagerPlugin", name = "Manager Test Plugin", version = "1.0.0", description = "Event manager test plugin")
class ManagerPlugin @Inject constructor(val game: Game, val logger: Logger) {

    @Listener
    fun init(event: InitializationEvent) {
        logger.info("Init")
    }

    @Listener
    fun post(event: PostInitializationEvent) {
        // Will always call ChangeServiceProviderEvent<? super MyService> listeners because the changed service type is MyService.
        game.serviceManager.setProvider(this, MyService::class.java, MyServiceImpl(9)) // Calls 2 listeners
        game.serviceManager.setProvider(this, MyService::class.java, GMyServiceImpl(9)) // Calls 2 listeners
        game.serviceManager.setProvider(this, MyService::class.java, EMyServiceImpl(9)) // Calls 2 listeners
        game.serviceManager.setProvider(this, MyService::class.java, MyServiceImpl(9))  // Calls 2 listeners
        game.serviceManager.setProvider(this, MyService::class.java, object : MyService {
            override val id: Int = 7
        }) // Calls 2 listeners
    }

    @Listener
    fun started(event: ServerStartedEvent) {

    }

    @Listener
    fun providerChange(event: ChangeServiceProviderEvent<*>) {
        printChange("any", event)
    }

    @Listener
    fun providerChange2(event: ChangeServiceProviderEvent<MyService>) {
        printChange("MyService", event)
    }

    @Listener
    fun providerChange3(event: ChangeServiceProviderEvent<MyServiceImpl>) {
        printChange("MyServiceImpl", event)
        throw IllegalStateException()
    }

    @Listener
    fun providerChange4(event: ChangeServiceProviderEvent<GMyServiceImpl>) {
        printChange("GMyServiceImpl", event)
        throw IllegalStateException()
    }

    @Listener
    fun providerChange5(event: ChangeServiceProviderEvent<EMyServiceImpl>) {
        printChange("EMyServiceImpl", event)
        throw IllegalStateException()
    }

    fun printChange(tag: String, event: ChangeServiceProviderEvent<*>) {
        val oldProviderStr = event.oldProvider?.provider?.javaClass?.toString() ?: "null"
        val newProviderStr = event.newProvider.provider.javaClass.toString()
        println("$tag -> Provider of '${event.service}' changed from '${oldProviderStr}' to '${newProviderStr}'.")
    }

}