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
package com.github.projectsandstone.common.test.platform

import com.github.projectsandstone.api.SandstoneObjectHelper

object TestObjectHelper : SandstoneObjectHelper {
    override fun <U, T> createLiveCollection(from: Collection<U>, mapper: (U) -> T): Collection<T> =
            object : Collection<T> {
                override val size: Int
                    get() = from.size

                override fun contains(element: T): Boolean =
                        from.any { mapper(it) == element }

                override fun containsAll(elements: Collection<T>): Boolean =
                        elements.all { this.contains(it) }

                override fun isEmpty(): Boolean = from.isEmpty()

                override fun iterator(): Iterator<T> = object : Iterator<T> {

                    val iter = from.iterator()

                    override fun next(): T = mapper(iter.next())
                    override fun hasNext(): Boolean = iter.hasNext()
                }
            }

    override fun <U, T> createLiveList(from: List<U>, mapper: (U) -> T): List<T> =
            object : List<T> {
                override val size: Int
                    get() = from.size

                override fun contains(element: T): Boolean =
                        from.any { mapper(it) == element }

                override fun containsAll(elements: Collection<T>): Boolean =
                        elements.all { this.contains(it) }

                override fun get(index: Int): T = mapper(from[index])

                override fun indexOf(element: T): Int =
                        from.indexOfFirst { mapper(it) == element }

                override fun isEmpty(): Boolean = from.isEmpty()

                override fun iterator(): Iterator<T> = object : Iterator<T> {

                    val iter = from.iterator()

                    override fun next(): T = mapper(iter.next())
                    override fun hasNext(): Boolean = iter.hasNext()
                }

                override fun lastIndexOf(element: T): Int =
                        from.indexOfLast { mapper(it) == element }

                override fun listIterator(): ListIterator<T> = this.listIterator(0)

                override fun listIterator(index: Int): ListIterator<T> = object : ListIterator<T> {
                    val iter = from.listIterator(index)

                    override fun hasNext(): Boolean = iter.hasNext()

                    override fun hasPrevious(): Boolean = iter.hasPrevious()

                    override fun next(): T = mapper(iter.next())

                    override fun nextIndex(): Int = iter.nextIndex()

                    override fun previous(): T = mapper(iter.previous())

                    override fun previousIndex(): Int = iter.previousIndex()
                }

                override fun subList(fromIndex: Int, toIndex: Int): List<T> =
                        createLiveList(from.subList(fromIndex, toIndex), mapper)

            }

}