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
package com.github.projectsandstone.common.test.adapter

import com.github.koresframework.eventsys.event.annotation.Listener
import com.github.projectsandstone.api.event.init.PreInitializationEvent
import com.github.projectsandstone.api.plugin.Plugin
import com.github.projectsandstone.common.adapter.Adapters
import com.github.projectsandstone.common.adapter.registerAllConverters
import org.slf4j.Logger
import javax.inject.Inject

@Plugin(id = "com.github.projectsandstone.common.test.AdapterTestPlugin", name = "Adapter Test Plugin", version = "1.0.0")
class AdapterTestPlugin @Inject constructor(val logger: Logger) {

    @Listener
    fun preInit(event: PreInitializationEvent) {
        Adapters.adapters.registerAllConverters("com.github.projectsandstone.common.test.adapter")

        logger.info("Converter = ${Adapters.adapters.getConverter(String::class.java, Int::class.javaObjectType)}")
        logger.info("Revert Converter = ${Adapters.adapters.getConverter(Int::class.javaObjectType, String::class.java)}")

        throw IllegalArgumentException("AAA")
    }

}