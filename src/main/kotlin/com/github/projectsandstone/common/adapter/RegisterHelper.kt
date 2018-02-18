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
package com.github.projectsandstone.common.adapter

import com.github.jonathanxd.adapterhelper.Adapter
import com.github.jonathanxd.adapterhelper.AdapterManager
import com.github.jonathanxd.adapterhelper.AdapterSpecification
import com.github.jonathanxd.adapterhelper.Converter
import com.github.jonathanxd.iutils.type.TypeUtil
import com.github.projectsandstone.api.Sandstone
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier

@Suppress("UNCHECKED_CAST")
fun AdapterManager.registerAllConverters(`package`: String) {
    val scan = FastClasspathScanner(`package`).strictWhitelist().scan()

    scan.getNamesOfClassesImplementing(Converter::class.java)
            .map {
                this.javaClass.classLoader.loadClass(it) as Class<Converter<Any, Any>>
            }
            .filter { Modifier.isPublic(it.modifiers) }
            .forEach {
                try {
                    val type = TypeUtil.resolve(it, Converter::class.java)

                    val instance: Converter<Any, Any> =
                            try {
                                it.getDeclaredField("INSTANCE").let {
                                    it.isAccessible = true
                                    it
                                }.get(null) as Converter<Any, Any>
                            } catch (t: Exception) {
                                val ctr = it.getConstructor()
                                ctr.isAccessible = true
                                ctr.newInstance()
                            }

                    this.registerConverter(
                            type.getTypeParameter(0).typeClass as Class<Any>,
                            type.getTypeParameter(1).typeClass as Class<Any>,
                            instance
                    )
                } catch (e: Exception) {
                    Sandstone.logger.error("Can't register converter class '$it'!", e)
                }

            }

}

@Suppress("UNCHECKED_CAST")
fun AdapterManager.registerAllAdapters(`package`: String) {
    val scan = FastClasspathScanner(`package`).strictWhitelist().scan()

    scan.getNamesOfClassesImplementing(Adapter::class.java)
            .map {
                this.javaClass.classLoader.loadClass(it) as Class<Adapter<Any>>
            }
            .filter { Modifier.isPublic(it.modifiers) }
            .forEach {
                try {

                    val type = TypeUtil.resolve(it, Adapter::class.java)

                    val constructor: Constructor<Adapter<Any>>? = it.declaredConstructors.find {
                        when (it.parameterCount) {
                            0 -> true
                            1 -> it.parameterTypes[0] == type.getTypeParameter(0).typeClass
                            2 -> it.parameterTypes[0] == type.getTypeParameter(0).typeClass
                                    && it.parameterTypes[1] == AdapterManager::class.java
                            else -> false
                        }
                    } as Constructor<Adapter<Any>>?

                    if (constructor == null) {
                        Sandstone.logger.error("""Can't register adapter class '$it'. The adapter class must have at least:
                                | - A empty constructor.
                                | - A constructor with only parameter of type '${type.getTypeParameter(0).typeClass.canonicalName}'.
                                | - A constructor with two parameters of type '${type.getTypeParameter(0).typeClass.canonicalName}' and '${AdapterManager::class.java.canonicalName}'!!!""".trimMargin())
                    } else {

                        val factory: (Any, AdapterManager) -> Adapter<Any> = { a, m ->
                            when (constructor.parameterCount) {
                                0 -> constructor.newInstance()
                                1 -> constructor.newInstance(a)
                                else -> constructor.newInstance(a, m)
                            }

                        }

                        val spec = AdapterSpecification.create(factory, it, type.getTypeParameter(0).typeClass as Class<Any>)

                        this.register(spec)
                    }
                } catch (e: Exception) {
                    Sandstone.logger.error("Can't register adapter class '$it'!", e)
                }

            }

}