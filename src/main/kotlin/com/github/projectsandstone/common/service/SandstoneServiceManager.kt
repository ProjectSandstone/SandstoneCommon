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
package com.github.projectsandstone.common.service

import com.github.jonathanxd.codeproxy.CodeProxy
import com.github.jonathanxd.iutils.map.WeakValueHashMap
import com.github.projectsandstone.api.Sandstone
import com.github.projectsandstone.api.event.SandstoneEventFactory
import com.github.projectsandstone.api.service.RegisteredProvider
import com.github.projectsandstone.api.service.ServiceManager

/**
 * Created by jonathan on 18/08/16.
 */
abstract class SandstoneServiceManager : ServiceManager {

    private val services = mutableMapOf<Class<*>, RegisteredProvider<*>>()
    private val serviceRegListeners = ServiceRegListeners()
    private val proxyCache = WeakValueHashMap<Class<*>, Any>()

    protected abstract fun <T : Any> internalSetProvider(service: Class<T>, instance: T)

    protected abstract fun <T : Any> internalProvide(service: Class<T>): T?

    override fun <T : Any> setProvider(plugin: Any, service: Class<T>, instance: T) {

        val oldProvider = this.getRegisteredProvider(service)

        /////////////////////////////////////////////////////

        val pluginContainer = Sandstone.game.pluginManager.getRequiredPlugin(plugin)

        val registeredProvider = SandstoneRegisteredProvider(pluginContainer, instance, service)

        /////////////////////////////////////////////////////
        val event = SandstoneEventFactory.createChangeServiceProviderEvent(oldProvider = oldProvider, newProvider = registeredProvider, service = service)

        Sandstone.eventManager.dispatch(event, pluginContainer)
        /////////////////////////////////////////////////////

        this.serviceRegListeners.onRegister(plugin, service, instance, registeredProvider)

        this.services.put(service, registeredProvider)

        this.internalSetProvider(service, instance)
    }

    override fun <T : Any> provide(service: Class<T>): T? {

        val reg = this.getRegisteredProvider(service)

        if (reg != null) {
            return reg.provider
        } else {
            return this.internalProvide(service)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> provideProxy(service: Class<T>): T {

        if(this.proxyCache.containsKey(service))
            return this.proxyCache[service] as T

        val superClass = if(!service.isInterface) service else Any::class.java
        val interfaces = if(service.isInterface) arrayOf(service) else emptyArray()

        val instance = CodeProxy.newProxyInstance(service.classLoader, superClass, interfaces, ProxyServiceIHandler(this, service)) as T

        this.proxyCache.put(service, instance)

        return instance
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getRegisteredProvider(service: Class<T>): RegisteredProvider<T>? {
        return if (this.services.containsKey(service)) this.services[service] as RegisteredProvider<T> else null
    }

    override fun <T : Any> watch(function: (RegisteredProvider<T>) -> Boolean) {
        this.serviceRegListeners.add({ plugin, klass, instance -> true }, function)
    }

    override fun <T : Any> watch(predicate: (Any, Class<T>, T) -> Boolean, function: (RegisteredProvider<T>) -> Boolean) {
        this.serviceRegListeners.add(predicate, function)
    }

    class ServiceRegListeners {
        private val listeners = mutableListOf<RegistryConsumer>()

        fun <T : Any> add(predicate: (Any, Class<T>, T) -> Boolean, function: (RegisteredProvider<T>) -> Boolean) {
            this.listeners.add(object : RegistryConsumer {
                @Suppress("UNCHECKED_CAST")
                override fun consume(plugin: Any, service: Class<*>, instance: Any, registeredProvider: RegisteredProvider<*>): Boolean {
                    if (!predicate.invoke(plugin, service as Class<T>, instance as T)) {
                        return true
                    }

                    return function(registeredProvider as RegisteredProvider<T>)
                }
            })
        }

        fun onRegister(plugin: Any, service: Class<*>, instance: Any, registeredProvider: RegisteredProvider<*>) {

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
        fun consume(plugin: Any, service: Class<*>, instance: Any, registeredProvider: RegisteredProvider<*>): Boolean
    }
}
//CodeProxy.newProxyInstance(service.classLoader, )