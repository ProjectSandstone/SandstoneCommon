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
package com.github.projectsandstone.common.test.platform

import com.github.jonathanxd.iutils.containers.MutableContainer
import com.github.jonathanxd.iutils.containers.primitivecontainers.BooleanContainer
import com.github.projectsandstone.api.scheduler.SubmittedTask
import com.github.projectsandstone.api.scheduler.Task
import com.github.projectsandstone.common.Constants
import com.github.projectsandstone.common.scheduler.SandstoneScheduler
import com.github.projectsandstone.common.scheduler.SandstoneSubmittedTask
import java.util.concurrent.Executors

class TestScheduler : SandstoneScheduler() {

    override fun submit(task: Task): SubmittedTask {
        val container: MutableContainer<Runnable?> = MutableContainer()
        val submitted: BooleanContainer = BooleanContainer(false)
        val running: BooleanContainer = BooleanContainer(false)

        val cancelled: BooleanContainer = BooleanContainer(false)

        val start = Runnable {
            submitted.set(true)

            Thread.sleep(task.delay.toMillis())

            executor.submit(container.value!!)
        }

        val r: Runnable = Runnable {

            if (cancelled.get())
                return@Runnable

            submitted.set(false)

            running.set(true)
            task.runnable.run()
            running.set(false)

            if (cancelled.get())
                return@Runnable

            if (!task.interval.isZero) {
                submitted.set(true)

                Thread.sleep(task.interval.toMillis())

                if (cancelled.get())
                    return@Runnable

                executor.submit(container.value!!)
            } else {
                container.set(null as Runnable?)
            }
        }

        container.set(r)

        executor.submit(start)

        val submittedTask = SandstoneSubmittedTask(task = task, canceller = {
            cancelled.set(true)
        }, submittedFetcher = {
            submitted.get()
        }, runningFetcher = {
            running.get()
        }, waitFinish = {
            while (it.isAlive())
                Thread.sleep(100)
        })


        return submittedTask
    }

    companion object {
        val executor = Executors.newCachedThreadPool(Constants.daemonThreadFactory)
    }

}