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
package com.github.projectsandstone.common.service

import com.github.jonathanxd.iutils.map.WeakValueHashMap
import com.github.jonathanxd.iutils.type.TypeInfo
import com.github.jonathanxd.koresproxy.KoresProxy
import com.github.jonathanxd.koresproxy.handler.InvocationHandler
import com.github.projectsandstone.api.Sandstone
import com.github.projectsandstone.api.event.SandstoneEventFactoryCache
import com.github.projectsandstone.api.event.service.ChangeServiceProviderEvent
import com.github.projectsandstone.api.service.RegisteredProvider
import com.github.projectsandstone.api.service.ServiceManager
import java.lang.reflect.Modifier

abstract class SandstoneServiceManager : ServiceManager {

    private val services = mutableMapOf<Class<*>, RegisteredProvider<*>>()
    private val serviceRegListeners = ServiceRegListeners()
    private val proxyCache = WeakValueHashMap<Class<*>, Any>()

    /**
     * Dispatch is disabled by default, if enabled, make sure to prevent the event to be fired twice
     * when the platform dispatch your own ChangeServiceProvider event.
     */
    open val dispatch: Boolean = false

    protected abstract fun <T : Any> internalSetProvider(service: Class<T>, instance: T)

    protected abstract fun <T : Any> internalProvide(service: Class<T>): T?

    override fun <T : Any> setProvider(plugin: Any, service: Class<T>, instance: T) {

        val oldProvider = this.getRegisteredProvider(service)

        /////////////////////////////////////////////////////

        val pluginContainer = Sandstone.game.pluginManager.getRequiredPlugin(plugin)

        val registeredProvider: RegisteredProvider<T> =
            SandstoneRegisteredProvider(pluginContainer, instance, service)

        /////////////////////////////////////////////////////

        if (this.dispatch) {
            val event = SandstoneEventFactoryCache.getInstance()
                .createChangeServiceProviderEvent(
                    TypeInfo.builderOf(ChangeServiceProviderEvent::class.java)
                        .of(service)
                        .buildGeneric(),
                    TypeInfo.of(service), oldProvider, registeredProvider
                )

            Sandstone.eventManager.dispatch(event, pluginContainer)
        }

        /////////////////////////////////////////////////////

        this.serviceRegListeners.onRegister(
            registeredProvider.plugin,
            registeredProvider.service,
            registeredProvider.provider,
            registeredProvider
        )

        this.services.put(service, registeredProvider)

        this.internalSetProvider(service, instance)
    }

    override fun <T : Any> provide(service: Class<T>): T? {

        val reg = this.getRegisteredProvider(service)

        return if (reg != null) {
            reg.provider
        } else {
            this.internalProvide(service)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> provideProxy(service: Class<T>): T {

        if (Modifier.isFinal(service.modifiers))
            throw IllegalArgumentException("Cannot provide final class as Proxy, use provideLazy() instead.")

        if (Modifier.isPrivate(service.modifiers))
            throw IllegalArgumentException("Cannot provide private class as Proxy, use provideLazy() instead.")

        if (this.proxyCache.containsKey(service))
            return this.proxyCache[service] as T

        val superClass = if (!service.isInterface) service else Any::class.java
        val interfaces = if (service.isInterface) listOf(service) else emptyList()

        val instance = KoresProxy.newProxyInstance(
            arrayOf(),
            arrayOf()) {
            it.classLoader(service.classLoader)
                .superClass(superClass)
                .interfaces(interfaces)
                .invocationHandler(InvocationHandler.NULL)
                .addCustom(ProxyService(this, service))
        } as T

        this.proxyCache[service] = instance

        return instance
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getRegisteredProvider(service: Class<T>): RegisteredProvider<T>? {
        return if (this.services.containsKey(service)) this.services[service] as RegisteredProvider<T> else null
    }

    override fun <T : Any> watch(function: (RegisteredProvider<T>) -> Boolean) {
        this.serviceRegListeners.add({ plugin, klass, instance -> true }, function)
    }

    override fun <T : Any> watch(
        predicate: (Any, Class<T>, T) -> Boolean,
        function: (RegisteredProvider<T>) -> Boolean
    ) {
        this.serviceRegListeners.add(predicate, function)
    }

    class ServiceRegListeners {
        private val listeners = mutableListOf<RegistryConsumer>()

        fun <T : Any> add(
            predicate: (Any, Class<T>, T) -> Boolean,
            function: (RegisteredProvider<T>) -> Boolean
        ) {
            this.listeners.add(object : RegistryConsumer {
                @Suppress("UNCHECKED_CAST")
                override fun consume(
                    plugin: Any,
                    service: Class<*>,
                    instance: Any,
                    registeredProvider: RegisteredProvider<*>
                ): Boolean {
                    if (!predicate.invoke(plugin, service as Class<T>, instance as T)) {
                        return true
                    }

                    return function(registeredProvider as RegisteredProvider<T>)
                }
            })
        }

        fun onRegister(
            plugin: Any,
            service: Class<*>,
            instance: Any,
            registeredProvider: RegisteredProvider<*>
        ) {

            val iterator = this.listeners.iterator()

            while (iterator.hasNext()) {
                val registryConsumer = iterator.next()
                if (!registryConsumer.consume(plugin, service, instance, registeredProvider)) {
                    iterator.remove()
                }
            }
        }
    }

    private interface RegistryConsumer {
        fun consume(
            plugin: Any,
            service: Class<*>,
            instance: Any,
            registeredProvider: RegisteredProvider<*>
        ): Boolean
    }
}
//CodeProxy.newProxyInstance(service.classLoader, )