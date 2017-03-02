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
package com.github.projectsandstone.common.scheduler

import com.github.projectsandstone.api.scheduler.ScheduledFutureTask
import com.github.projectsandstone.api.scheduler.SubmittedTask
import com.github.projectsandstone.api.scheduler.Task
import com.github.projectsandstone.common.scheduler.CallableTask
import java.time.Duration
import java.util.concurrent.Delayed
import java.util.concurrent.TimeUnit

class ScheduledFutureTaskImpl<V>(override val task: Task,
                                 override val submittedTask: SubmittedTask,
                                 val callableTask: CallableTask<V>) : ScheduledFutureTask<V> {

    override fun isCancelled(): Boolean = submittedTask.isCancelled

    override fun get(): V {
        return this.callableTask.get()
    }

    override fun get(timeout: Long, unit: TimeUnit): V {
        return this.callableTask.get(timeout, unit)
    }

    override fun run() {
        this.callableTask.run()
    }

    override fun isDone(): Boolean {
        return this.callableTask.isDone
    }

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        this.submittedTask.cancel()
        return this.callableTask.cancel(mayInterruptIfRunning)
    }

    override fun getDelay(unit: TimeUnit): Long {
        return unit.convert(this.task.delay.toNanos(), TimeUnit.NANOSECONDS)
    }

    override fun isPeriodic(): Boolean {
        return task.interval > Duration.ZERO
    }

    override fun compareTo(other: Delayed?): Int {
        if (other == null)
            return -1

        return getDelay(TimeUnit.NANOSECONDS).compareTo(other.getDelay(TimeUnit.NANOSECONDS))
    }

}