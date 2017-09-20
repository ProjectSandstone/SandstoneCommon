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
package com.github.projectsandstone.common.asm

import com.github.projectsandstone.api.plugin.Dependency
import com.github.projectsandstone.api.plugin.DependencyContainer
import com.github.projectsandstone.api.plugin.Plugin
import com.github.projectsandstone.api.plugin.PluginContainer
import com.github.projectsandstone.api.util.version.Version
import com.github.projectsandstone.api.util.version.VersionScheme
import com.github.projectsandstone.common.plugin.SandstoneDependencyContainer
import com.github.projectsandstone.common.plugin.SandstonePluginContainer
import com.github.projectsandstone.common.util.CommonVersionScheme
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import java.io.InputStream
import java.util.*

object ASM {

    private val annotationType = Type.getDescriptor(Plugin::class.java)
    private val depAnnotationType = Type.getDescriptor(Dependency::class.java)

    @Suppress("UNCHECKED_CAST")
    fun findPluginAnnotation(inputStream: InputStream): SandstonePluginContainer? {
        try {
            val reader = ClassReader(inputStream)

            val node: ClassNode = ClassNode()

            reader.accept(node, 0)

            val visibleAnnotations: List<AnnotationNode> = node.visibleAnnotations as List<AnnotationNode>

            for (annotation in visibleAnnotations) {
                if (annotation.desc == ASM.annotationType) {

                    var id: String? = null
                    var name: String? = null
                    var version: String? = null
                    var description: String? = null
                    var authors: List<String> = emptyList()
                    var usePlatformInternals = false
                    var dependencies: List<DependencyContainer> = emptyList()
                    var optional = false
                    var targetPlatformNames: List<String> = emptyList()

                    val values = annotation.values

                    if(values != null) {

                        for (x in 0..(values.size - 1) step 2) {
                            val key = values[x]
                            val value = values[x + 1]

                            when (key) {
                                "id" -> id = value as String
                                "name" -> name = value as String
                                "version" -> version = value as String
                                "description" -> description = (value as String).let { if (it.isEmpty()) null else it }
                                "authors" -> authors = (value as List<String>)
                                "usePlatformInternals" -> usePlatformInternals = value as Boolean
                                "dependencies" -> dependencies = createDependenciesList(value as List<AnnotationNode>)
                                "optional" -> optional = value as Boolean
                                "targetPlatformNames" -> targetPlatformNames = (value as List<String>)
                            }

                        }
                    }

                    if (id == null)
                        throw IllegalArgumentException("Id cannot be null.")

                    if (version == null)
                        throw IllegalArgumentException("Version cannot be null.")

                    return SandstonePluginContainer(
                            id_ = id,
                            name_ = name ?: id,
                            authors_ = Collections.unmodifiableList(authors),
                            description_ = description,
                            version_ = Version(version, CommonVersionScheme),
                            usePlatformInternals_ = usePlatformInternals,
                            dependencies = Collections.unmodifiableList(dependencies),
                            mainClass = Type.getType(node.name).className ?: Type.getType(node.name).internalName.replace('/', '.'),
                            optional = optional,
                            targetPlatformNames = Collections.unmodifiableList(targetPlatformNames)
                    )
                }
            }

            return null
        } catch(e: Exception) {
            return null
        }
    }

    private fun createDependenciesList(list: List<AnnotationNode>): List<DependencyContainer> {
        return list.map {

            if (it.desc != ASM.depAnnotationType)
                throw IllegalArgumentException("Annotation ${it.desc} is not a @Dependency annotation!")

            val values = it.values

            val end = values.size - 1

            var id: String? = null
            var version: String = ".*"
            var incompatibleVersions: String = ""
            var isRequired = true

            for(x in 0..end step 2) {
                val key = values[x]
                val value = values[x + 1]

                when(key) {
                    "id" -> id = value as String
                    "version" -> version = value as String
                    "incompatibleVersions" -> incompatibleVersions = value as String
                    "isRequired" -> isRequired = value as Boolean
                }
            }

            id ?: throw IllegalArgumentException("'id' cannot be null in '@${it.desc}' annotation.")

            return@map SandstoneDependencyContainer(
                    id = id,
                    version = version,
                    incompatibleVersions = incompatibleVersions,
                    isRequired = isRequired
            )
        }
    }

}