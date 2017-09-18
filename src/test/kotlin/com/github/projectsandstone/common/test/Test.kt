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
package com.github.projectsandstone.common.test

import com.github.projectsandstone.api.Sandstone
import com.github.projectsandstone.common.test.platform.SandstoneTestMain
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.util.*

class Test {
    @Test
    fun init() {
        SandstoneTestMain.main(emptyArray())

        val resourceAsStream =
                SandstoneTestMain::class.java.classLoader.getResourceAsStream("plugins.properties")

        val properties = Properties()

        properties.load(resourceAsStream)

        val keys = properties.keys.toString()

        val loadInstant = Instant.now()

        Sandstone.logger.info("Loading plugins: $keys...")

        /*properties.values.forEach {
        Sandstone.pluginManager.loadPlugins(arrayOf(it as String))
        }*/

        val containers =
                Sandstone.pluginManager.createContainers(properties.values.map { it as String }.toTypedArray())
        Sandstone.pluginManager.loadAll(containers)

        Sandstone.logger.info("Plugins loaded in: ${Duration.between(loadInstant, Instant.now()).seconds}s")

        Sandstone.logger.info("Initializing Sandstone Test Environment...")

        SandstoneTestMain.init()
        SandstoneTestMain.stop()

    }
}