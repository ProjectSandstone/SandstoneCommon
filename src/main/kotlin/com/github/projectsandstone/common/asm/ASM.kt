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
package com.github.projectsandstone.common.asm

import com.github.projectsandstone.api.plugin.Plugin
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import java.io.InputStream

/**
 * Created by jonathan on 15/08/16.
 */
object ASM {

    private val annotationType = Type.getDescriptor(Plugin::class.java)

    @Suppress("UNCHECKED_CAST")
    fun findPluginAnnotation(inputStream: InputStream): SimpleDesc? {
        try {
            val reader = ClassReader(inputStream)

            val node: ClassNode = ClassNode()

            reader.accept(node, 0)

            val visibleAnnotations: List<AnnotationNode> = node.visibleAnnotations as List<AnnotationNode>

            for (annotation in visibleAnnotations) {
                if (annotation.desc == ASM.annotationType) {

                    val values = annotation.values;


                    for (x in 0..(values.size-1)) {
                        val value = values[x]

                        if (value == "usePlatformInternals") {
                            return SimpleDesc(values[x + 1] as Boolean)
                        }
                    }

                    return SimpleDesc(false)

                }
            }

            return null
        }catch(e: Exception) {
            return null
        }
    }

}