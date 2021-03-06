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
package com.github.projectsandstone.common.registry

import com.github.projectsandstone.api.registry.Registry
import com.github.projectsandstone.api.registry.RegistryEntry
import com.github.projectsandstone.common.util.extension.set
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table

class SandstoneRegistry : Registry {

    private val registryTable: Table<String, Class<*>, RegistryEntry> = HashBasedTable.create()

    override fun <T : RegistryEntry> registerEntry(id: String, type: Class<out T>, entry: T) {
        this.registryTable[id, type] = entry
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : RegistryEntry> getEntry(id: String, type: Class<out T>): T? {
        return this.registryTable[id, type] as T?
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : RegistryEntry> getAll(type: Class<out T>): List<T> {
        return this.registryTable.column(type).values.toList() as List<T>
    }

}