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
package com.github.projectsandstone.common.util.extension

/**
 * Format [String] to Sandstone Registry Format.
 *
 * Example: EnderPearl will be formatted as ender_pearl
 */
fun String.formatToSandstoneRegistryId(): String {
    val sb = StringBuilder()
    val chars = this.toCharArray()

    chars.forEachIndexed { i, c ->
        sb.append(c.toLowerCase())

        if (i + 1 < chars.size && chars[i + 1].isUpperCase()){
            sb.append('_')
        }
    }

    return sb.toString()
}

/**
 * Format [String] to Sandstone Registry Name.
 *
 * Example: EnderPearl will be formatted as Ender Pearl
 */
fun String.formatToSandstoneRegistryName(): String {
    val sb = StringBuilder()
    val chars = this.toCharArray()

    chars.forEachIndexed { i, c ->
        sb.append(c)

        if (i + 1 < chars.size && chars[i + 1].isUpperCase()){
            sb.append(' ')
        }
    }

    return sb.toString()
}

/**
 * Format [Enum] to Sandstone Registry Format.
 *
 * Example: ENDER_PEARL will be formatted as ender_pearl
 */
fun <E: Enum<E>> Enum<E>.formatToSandstoneRegistryId(): String = this.name.toLowerCase()

/**
 * Format [Enum] to Sandstone Registry Name.
 *
 * Example: ENDER_PEARL will be formatted as Ender Pearl
 */
fun <E: Enum<E>> Enum<E>.formatToSandstoneRegistryName(): String {
    val sb = StringBuilder()
    val chars = this.name.toCharArray()

    chars.forEachIndexed { i, c ->

        if(i == 0 || i - 1 > 0 && chars[i - 1] == '_')
            sb.append(c.toUpperCase())
        else if(c == '_')
            sb.append(' ')
        else
            sb.append(c.toLowerCase())

    }

    return sb.toString()
}
