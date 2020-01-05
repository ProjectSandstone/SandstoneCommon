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

import com.github.jonathanxd.iutils.data.TypedData
import com.github.jonathanxd.iutils.option.Options
import com.github.jonathanxd.kores.base.MethodDeclaration
import com.github.koresframework.eventsys.context.EnvironmentContext
import com.github.koresframework.eventsys.event.*
import com.github.koresframework.eventsys.extension.ExtensionSpecification
import com.github.koresframework.eventsys.gen.GenerationEnvironment
import com.github.koresframework.eventsys.gen.ResolvableDeclaration
import com.github.koresframework.eventsys.gen.check.CheckHandler
import com.github.koresframework.eventsys.gen.event.EventClassSpecification
import com.github.koresframework.eventsys.gen.event.EventGenerator
import com.github.koresframework.eventsys.gen.event.PropertyInfo
import com.github.koresframework.eventsys.impl.EventListenerContainer
import com.github.koresframework.eventsys.logging.LoggerInterface
import com.github.koresframework.eventsys.result.DispatchResult
import com.github.projectsandstone.common.plugin.SandstonePluginManager
import org.slf4j.Logger
import java.lang.reflect.Method
import java.lang.reflect.Type
import java.util.concurrent.CompletableFuture

class SandstoneEventDispatcher(private val backed: EventDispatcher,
                               private val sandstonePluginManager: SandstonePluginManager) : EventDispatcher {

    override fun <T : Event> dispatch(event: T,
                                      eventType: Type,
                                      dispatcher: Any,
                                      channel: String,
                                      isAsync: Boolean,
                                      ctx: EnvironmentContext): DispatchResult<T> {
        return this.backed.dispatch(
                event,
                eventType,
                dispatcher,
                channel,
                isAsync,
                newContext(dispatcher, ctx, sandstonePluginManager)
        )
    }
}

class SandstoneEventGenerator(private val backed: EventGenerator,
                              private val sandstonePluginManager: SandstonePluginManager) : EventGenerator {
    override var checkHandler: CheckHandler
        get() = this.backed.checkHandler
        set(value) {
            this.backed.checkHandler = value
        }

    override val generationEnvironment: GenerationEnvironment
        get() = this.backed.generationEnvironment

    override val logger: LoggerInterface
        get() = this.backed.logger

    override val options: Options
        get() = this.backed.options

    override fun <T : Event> createEventClass(type: Type,
                                              additionalProperties: List<PropertyInfo>,
                                              extensions: List<ExtensionSpecification>,
                                              ctx: EnvironmentContext): ResolvableDeclaration<Class<out T>> =
            this.backed.createEventClass(type, additionalProperties, extensions,
                    newContext(type, ctx, this.sandstonePluginManager))


    override fun <T : Event> createEventClassAsync(type: Type,
                                                   additionalProperties: List<PropertyInfo>,
                                                   extensions: List<ExtensionSpecification>,
                                                   ctx: EnvironmentContext): CompletableFuture<ResolvableDeclaration<Class<out T>>> =
            this.backed.createEventClassAsync(type, additionalProperties, extensions,
                    newContext(type, ctx, this.sandstonePluginManager))


    override fun <T : Any> createFactory(factoryType: Type): ResolvableDeclaration<T> =
            this.createFactory(factoryType, EnvironmentContext())


    override fun <T : Any> createFactory(factoryType: Type,
                                         ctx: EnvironmentContext): ResolvableDeclaration<T> =
            this.backed.createFactory(factoryType,
                    newContext(factoryType, ctx, this.sandstonePluginManager))


    override fun <T : Any> createFactoryAsync(factoryType: Type): CompletableFuture<out ResolvableDeclaration<T>> =
            this.createFactoryAsync(factoryType, EnvironmentContext())

    override fun <T : Any> createFactoryAsync(factoryType: Type,
                                              ctx: EnvironmentContext): CompletableFuture<out ResolvableDeclaration<T>> =
            this.backed.createFactoryAsync(factoryType,
                    newContext(factoryType, ctx, this.sandstonePluginManager))

    override fun createListenerSpecFromMethod(method: MethodDeclaration): ListenerSpec =
            this.backed.createListenerSpecFromMethod(method)

    override fun createListenerSpecFromMethod(method: Method): ListenerSpec =
            this.backed.createListenerSpecFromMethod(method)

    override fun createMethodListener(listenerClass: Type,
                                      method: MethodDeclaration,
                                      listenerSpec: ListenerSpec,
                                      ctx: EnvironmentContext): ResolvableDeclaration<Class<out EventListener<Event>>> =
            this.backed.createMethodListener(listenerClass, method, listenerSpec,
                    newContext(listenerClass, ctx, this.sandstonePluginManager))

    override fun createMethodListener(listenerClass: Type,
                                      method: MethodDeclaration,
                                      instance: Any?,
                                      listenerSpec: ListenerSpec,
                                      ctx: EnvironmentContext): ResolvableDeclaration<EventListener<Event>> =
            this.backed.createMethodListener(listenerClass, method, instance, listenerSpec,
                    newContext(listenerClass, ctx, this.sandstonePluginManager))

    override fun createMethodListener(listenerClass: Type,
                                      method: Method,
                                      instance: Any?,
                                      listenerSpec: ListenerSpec,
                                      ctx: EnvironmentContext): ResolvableDeclaration<EventListener<Event>> =
            this.backed.createMethodListener(listenerClass, method, instance, listenerSpec,
                    newContext(listenerClass, ctx, this.sandstonePluginManager))

    override fun createMethodListenerAsync(listenerClass: Type,
                                           method: MethodDeclaration,
                                           listenerSpec: ListenerSpec,
                                           ctx: EnvironmentContext): CompletableFuture<ResolvableDeclaration<Class<out EventListener<Event>>>> =
            this.backed.createMethodListenerAsync(listenerClass, method, listenerSpec,
                    newContext(listenerClass, ctx, this.sandstonePluginManager))

    override fun createMethodListenerAsync(listenerClass: Type,
                                           method: MethodDeclaration,
                                           instance: Any?,
                                           listenerSpec: ListenerSpec,
                                           ctx: EnvironmentContext): CompletableFuture<ResolvableDeclaration<EventListener<Event>>> =
            this.backed.createMethodListenerAsync(listenerClass, method, instance, listenerSpec,
                    newContext(listenerClass, ctx, this.sandstonePluginManager))

    override fun createMethodListenerAsync(listenerClass: Type,
                                           method: Method,
                                           instance: Any?,
                                           listenerSpec: ListenerSpec,
                                           ctx: EnvironmentContext): CompletableFuture<ResolvableDeclaration<EventListener<Event>>> =
            this.backed.createMethodListenerAsync(listenerClass, method, instance, listenerSpec,
                    newContext(listenerClass, ctx, this.sandstonePluginManager))

    override fun <T : Event> registerEventImplementation(eventClassSpecification: EventClassSpecification,
                                                         implementation: Class<out T>) =
            this.backed.registerEventImplementation(eventClassSpecification, implementation)

    override fun registerExtension(base: Type, extensionSpecification: ExtensionSpecification) =
            this.backed.registerExtension(base, extensionSpecification)


}

class SandstoneListenerRegistry(private val backed: EventListenerRegistry,
                                private val sandstonePluginManager: SandstonePluginManager) : EventListenerRegistry {

    override fun <T : Event> getListeners(eventType: Type): Set<Pair<Type, EventListener<T>>> =
            this.backed.getListeners(eventType)

    override fun getListenersAsPair(): Set<Pair<Type, EventListener<*>>> =
            this.backed.getListenersAsPair()

    override fun getListenersContainers(): Set<EventListenerContainer<*>> =
            this.backed.getListenersContainers()

    override fun <T : Event> getListenersContainers(event: T,
                                                    eventType: Type,
                                                    channel: String): Iterable<EventListenerContainer<*>> =
            this.backed.getListenersContainers(event, eventType, channel)

    override fun <T : Event> getListenersContainers(eventType: Type): Set<EventListenerContainer<*>> =
            this.backed.getListenersContainers<T>(eventType)

    override fun <T : Event> registerListener(owner: Any,
                                              eventType: Type,
                                              eventListener: EventListener<T>): ListenerRegistryResults =
            this.backed.registerListener(owner, eventType, eventListener)


    override fun registerListeners(owner: Any,
                                   listener: Any,
                                   ctx: EnvironmentContext): ListenerRegistryResults =
            this.backed.registerListeners(owner, listener,
                    newContext(owner, ctx, this.sandstonePluginManager))

    override fun registerListeners(owner: Any, listener: Any): ListenerRegistryResults =
            this.registerListeners(owner, listener, EnvironmentContext())

    override fun registerMethodListener(owner: Any,
                                        eventClass: Type,
                                        instance: Any?,
                                        method: Method): ListenerRegistryResults =
            this.registerMethodListener(owner, eventClass, instance, method, EnvironmentContext())

    override fun registerMethodListener(owner: Any,
                                        eventClass: Type,
                                        instance: Any?,
                                        method: Method,
                                        ctx: EnvironmentContext): ListenerRegistryResults =
            this.backed.registerMethodListener(owner, eventClass, instance, method,
                    newContext(owner, ctx, this.sandstonePluginManager))
}

private fun findLogger(instance: Any, sandstonePluginManager: SandstonePluginManager): Logger? {
    return sandstonePluginManager.findPlugins(instance).firstOrNull()?.logger
}

private fun newContext(dispatcher: Any,
                       ctx: EnvironmentContext,
                       sandstonePluginManager: SandstonePluginManager): EnvironmentContext {
    val newCtx = EnvironmentContext(TypedData(ctx.data))

    findLogger(dispatcher, sandstonePluginManager)?.let {
        loggerKey.set(newCtx, it)
    }

    return newCtx
}

private fun findLogger(dispatcherType: Type, sandstonePluginManager: SandstonePluginManager): Logger? {
    return sandstonePluginManager.findPlugins(dispatcherType).firstOrNull()?.logger
}

private fun newContext(dispatcherType: Type,
                       ctx: EnvironmentContext,
                       sandstonePluginManager: SandstonePluginManager): EnvironmentContext {
    val newCtx = EnvironmentContext(TypedData(ctx.data))

    findLogger(dispatcherType, sandstonePluginManager)?.let {
        loggerKey.set(newCtx, it)
    }

    return newCtx
}