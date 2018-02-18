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
package com.github.projectsandstone.common.service

import com.github.jonathanxd.iutils.kt.classOf
import com.github.jonathanxd.kores.Instruction
import com.github.jonathanxd.kores.base.Access
import com.github.jonathanxd.kores.factory.invokeInterface
import com.github.jonathanxd.kores.factory.invokeStatic
import com.github.jonathanxd.kores.factory.typeSpec
import com.github.jonathanxd.kores.literal.string
import com.github.jonathanxd.kores.literal.type
import com.github.jonathanxd.koresproxy.ProxyData
import com.github.jonathanxd.koresproxy.gen.direct.WrappedInstance
import com.github.jonathanxd.koresproxy.handler.InvocationHandler
import com.github.jonathanxd.koresproxy.info.MethodInfo
import com.github.projectsandstone.api.service.ServiceManager
import java.util.*

class ProxyService<T : Any>(
    val serviceManager: ServiceManager,
    val service: Class<T>
) : WrappedInstance(service) {
    override fun getWrapper(): Any = this.serviceManager

    override fun getWrapperType(): Class<*> = classOf<ServiceManager>()

    override fun evaluate(wrapper: Instruction): Instruction =
        checkValidit(
            invokeInterface(
                localization = classOf<ServiceManager>(),
                target = wrapper,
                name = "provide",
                spec = typeSpec(classOf<Any>(), classOf<Class<*>>()),
                arguments = listOf(type(service))
            )
        )

    private fun checkValidit(ins: Instruction): Instruction =
        invokeStatic(
            localization = classOf<Objects>(),
            target = Access.STATIC,
            name = "requireNonNull",
            spec = typeSpec(classOf<Any>(), classOf<Any>(), classOf<String>()),
            arguments = listOf(
                ins,
                string("No provider of service '${service.canonicalName}' was found!")
            )
        )
}