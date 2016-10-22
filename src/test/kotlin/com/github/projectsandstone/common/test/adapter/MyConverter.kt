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
package com.github.projectsandstone.common.test.adapter

import com.github.jonathanxd.adapter.info.CallInfo
import com.github.jonathanxd.adapter.spec.ConverterSpec
import com.github.projectsandstone.common.adapter.RegistryCandidate
import com.github.projectsandstone.common.adapter.annotation.RegistryType

object MyConverter : RegistryCandidate<ConverterSpec> {
    override val id: String = "CONVERTER_9"

    override val spec: ConverterSpec = ConverterSpec(
            String::class.java, Int::class.javaPrimitiveType!!,
            MyConverter::class.java,
            "convert",
            Int::class.javaPrimitiveType,
            arrayOf(String::class.java))

    override val registryType: RegistryType = RegistryType(ConverterSpec::class.java)

    @JvmStatic
    fun convert(callInfo: CallInfo, input: String): Int {
        return input.toInt()
    }
}