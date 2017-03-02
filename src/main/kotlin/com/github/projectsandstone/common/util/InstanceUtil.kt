/**
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
package com.github.projectsandstone.common.util

import java.lang.invoke.MethodHandles


internal val LOOKUP = MethodHandles.publicLookup()

/**
 * Tries to resolve instance of Kotlin and Scala objects.
 *
 * For Kotlin Objects, the method will use [kotlin.reflect.KClass.objectInstance] method to get `object` instance.
 *
 * For Scala, the method will use [MethodHandles.publicLookup] to lookup for a static `MODULE$` field.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any> getInstance(klass: Class<T>): T? {

    val objectInstance = klass.kotlin.objectInstance

    // Kotlin
    if (objectInstance != null)
        return objectInstance

    val find = try {
        LOOKUP.findStaticGetter(klass, "MODULE\$", klass)
    } catch(t: NoSuchFieldException) {
        null
    }

    return find?.invokeExact() as? T

}
