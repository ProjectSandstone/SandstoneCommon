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
package com.github.projectsandstone.common.adapter

import com.github.jonathanxd.adapter.AdaptedClassInfo
import com.github.jonathanxd.adapter.AdapterEnvironment
import com.github.jonathanxd.adapter.AdapterFactory
import com.github.jonathanxd.adapter.adapter.AdapterSpecificationSpec
import com.github.jonathanxd.adapter.spec.Specification
import com.github.jonathanxd.iutils.container.MutableContainer
import com.github.projectsandstone.common.adapter.annotation.SingletonField
import com.github.projectsandstone.common.util.extension.registerSpecificationHelper
import java.lang.reflect.Modifier
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

/**
 * Created by jonathan on 28/08/16.
 */
class SandstoneAdapters {

    val adapterEnvironment: AdapterEnvironment = AdapterEnvironment()
    val adapterFactory = AdapterFactory(adapterEnvironment)

    /**
     * Retain data of [AdapterEnvironment.cachedInstances].
     *
     * Cached adapters is good because avoid class generation.
     */
    private val map: MutableMap<Pair<Class<*>, Any>, Pair<AdaptedClassInfo, Any>> = mutableMapOf()

    fun registerAdapterSpecification(adapterSpecificationSpec: AdapterSpecificationSpec) {
        this.adapterEnvironment.registerAdapterSpecification(adapterSpecificationSpec)
        this.adapterEnvironment.adaptToInfo(adapterSpecificationSpec.specificationProviders)
    }

    /**
     * Register all specifications in [resourcePath] of [classLoader]
     */
    fun registerAll(classLoader: ClassLoader, resourcePath: String) {
        val resource = classLoader.getResourceAsStream(resourcePath)
                ?: throw IllegalArgumentException("Cannot find resource $resourcePath in classloader $classLoader.")

        val bytes = resource.readBytes()
        val content = bytes.toString(Charsets.UTF_8)

        content.split("\n").forEach {
            val class_ = classLoader.loadClass(it)

            val fieldName =
                    if (class_.isAnnotationPresent(SingletonField::class.java))
                        class_.getDeclaredAnnotation(SingletonField::class.java)!!.value
                    else
                        "INSTANCE"

            val field = try {
                class_.getDeclaredField(fieldName)
            } catch (e: Exception) {
                null
            }
                    ?: throw IllegalArgumentException("Provided class: $it has no $fieldName field!")

            if (!Modifier.isStatic(field.modifiers))
                throw IllegalArgumentException("The field $fieldName of class $it must be static.")

            val instance = field.get(null) as? RegistryCandidate<*> ?:
                    throw IllegalArgumentException("Class $it is not a RegistryCandidate<*>")

            val id = instance.id
            val spec = instance.spec
            val type = instance.registryType

            this.adapterEnvironment.registerSpecificationHelper<Specification>(id, type.type, spec)
        }
    }

    fun adapt(type: Class<*>, instance: Any): Any? {

        val bi = Pair(type, instance)

        if (this.map.containsKey(bi)) {
            return this.map[bi]!!.second
        }

        val container = MutableContainer<AdaptedClassInfo>()

        val opt = this.adapterEnvironment.adapt<Any>(type, instance, { info ->
            container.set(info)
        })

        if (opt.isPresent) {
            val get = opt.get()

            this.store(type, instance, container.value, get)

            if (isDebug)
                save(adaptersDir)

            return get
        } else {
            return null
        }
    }

    fun store(type: Class<*>, instance: Any, info: AdaptedClassInfo, generated: Any) = this.map.put(Pair(type, instance), Pair(info, generated))

    fun remove(type: Class<*>, instance: Any) = this.map.remove(Pair(type, instance))

    /**
     * Save adapters
     */
    fun save(path: Path) {
        this.adapterEnvironment.adaptedClasses.forEach {

            val adapterClass = it.adapterClass
            val generatedClass = it.generatedClass

            val class_ = generatedClass.bytes
            val source_ = generatedClass.source.toByteArray(charset = Charset.forName("UTF-8"))


            val saveClass = "class/${adapterClass.canonicalName.replace('.', '/')}.class"
            val saveJava = "java/${adapterClass.canonicalName.replace('.', '/')}.java"

            val resolvedClass = path.resolve(saveClass)
            val resolvedJava = path.resolve(saveJava)

            try {
                Files.deleteIfExists(resolvedClass)
                Files.deleteIfExists(resolvedJava)
            } catch (ignored: Exception) {
            }

            Files.createDirectories(resolvedClass.parent)
            Files.createDirectories(resolvedJava.parent)

            Files.write(resolvedClass, class_, StandardOpenOption.CREATE)
            Files.write(resolvedJava, source_, StandardOpenOption.CREATE)

        }
    }

    companion object {
        val adaptersDir = Paths.get(".", "Sandstone", "Adapters")
        val isDebug = getBooleanProperty("sandstone.adapters.debug")

        fun getBooleanProperty(name: String): Boolean {
            val prop = System.getProperties()[name]

            return if (prop == null) false else prop == "true"
        }
    }
}