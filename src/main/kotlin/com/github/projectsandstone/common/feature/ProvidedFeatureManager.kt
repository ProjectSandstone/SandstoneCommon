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
package com.github.projectsandstone.common.feature

import com.github.jonathanxd.kores.Instruction
import com.github.jonathanxd.kores.factory.invokeVirtual
import com.github.jonathanxd.kores.factory.typeSpec
import com.github.jonathanxd.kores.literal.Literals
import com.github.jonathanxd.koresproxy.KoresProxy
import com.github.jonathanxd.koresproxy.gen.direct.DummyCustom
import com.github.jonathanxd.koresproxy.gen.direct.WrappedInstance
import com.github.projectsandstone.api.feature.Feature
import com.github.projectsandstone.api.feature.FeatureManager

/**
 * A simple feature manager backed to API Proposed logic
 */
open class ProvidedFeatureManager : FeatureManager {
    private val cachedDummy = mutableMapOf<Class<*>, Feature<*>>()
    private val cachedFeature = mutableMapOf<Class<*>, Feature<*>>()
    private val featureProvideMap = mutableMapOf<Class<*>, Any>()

    override fun <T : Feature<T>> provide(type: Class<T>, feature: T): T? {
        @Suppress("UNCHECKED_CAST")
        return this.featureProvideMap.put(type, feature) as? T?
    }

    override fun <T : Feature<T>> drop(type: Class<T>): T? {
        this.cachedFeature.remove(type)
        this.cachedDummy.remove(type)
        @Suppress("UNCHECKED_CAST")
        return this.featureProvideMap.remove(type) as? T?
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Feature<T>> get(type: Class<T>): Feature<T> {
        this.cachedDummy.computeIfAbsent(type) { provideDummyFeature(it as Class<T>) }
        return this.cachedFeature.computeIfAbsent(type) {
            provideFeature(it as Class<T>,
                    { type -> this.featureProvideMap[type] as? T? },
                    { type -> this.cachedDummy[type] as T }
            )
        } as Feature<T>
    }
}

fun <T : Feature<T>> provideDummyFeature(type: Class<T>): Feature<T> =
        KoresProxy.newProxyInstance {
            it.classLoader(type.classLoader)
            if (type.isInterface)
                it.interfaces(type)
            else
                it.superClass(type)
            it.addCustom(DummyCustom.create { method -> type == method.declaringClass })
        }


fun <T : Feature<T>> provideFeature(type: Class<T>,
                                    provider: (Class<T>) -> T?,
                                    dummyProvider: (Class<T>) -> T): Feature<T> =
        KoresProxy.newProxyInstance {
            it.classLoader(type.classLoader)
            if (type.isInterface)
                it.interfaces(type)
            else
                it.superClass(type)
            it.addCustom(ProvidedSwitchInstance(type) { type ->
                provider(type) ?: dummyProvider(type)
            })
        }


class ProvidedSwitchInstance<T : Feature<T>>(private val type: Class<T>,
                                             private val provider: (Class<T>) -> T) : WrappedInstance(type) {

    override fun evaluate(provider: Instruction): Instruction {
        return invokeVirtual(Function1::class.java,
                provider,
                "invoke",
                typeSpec(Any::class.java, Class::class.java), listOf(Literals.CLASS(this.type)))
    }

    override fun getWrapperType(): Class<*> {
        return Function1::class.java
    }

    override fun getWrapper(): Any {
        return this.provider
    }

}