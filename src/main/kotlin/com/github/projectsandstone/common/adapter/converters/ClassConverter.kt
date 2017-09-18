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
package com.github.projectsandstone.common.adapter.converters

import com.github.jonathanxd.adapterhelper.Adapter
import com.github.jonathanxd.adapterhelper.AdapterManager
import com.github.jonathanxd.adapterhelper.Converter
import com.github.projectsandstone.common.util.extension.biMapOf

object ClassConverter : Converter<Class<*>, Class<*>> {

    private val converters = biMapOf<Class<*>, Class<*>>()

    fun addClass(platform: Class<*>, sandstone: Class<*>) {
        this.converters.put(platform, sandstone)
    }

    fun hasSandstoneEquivalent(klass: Class<*>) = converters.containsKey(klass)
    fun hasPlatformEquivalent(klass: Class<*>) = converters.inverse().containsKey(klass)
    fun hasEquivalent(klass: Class<*>) = this.hasSandstoneEquivalent(klass) || this.hasPlatformEquivalent(klass)

    fun getEquivalent(klass: Class<*>): Class<*> =
            this.converters.getOrElse(klass, { this.converters.inverse()[klass] }) ?: throw IllegalArgumentException("Cannot convert input class: '$klass'!")

    fun getSandstoneEquivalent(klass: Class<*>): Class<*> =
            this.converters[klass] ?: throw IllegalArgumentException("Cannot get Sandstone equivalent class of: '$klass'!")

    fun getPlatformEquivalent(klass: Class<*>): Class<*> =
            this.converters.inverse()[klass] ?: throw IllegalArgumentException("Cannot get Platform equivalent class of: '$klass'!")

    override fun convert(input: Class<*>, adapter: Adapter<*>?, manager: AdapterManager): Class<*> = this.getEquivalent(input)

}