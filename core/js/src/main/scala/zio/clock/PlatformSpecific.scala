/*
 * Copyright 2017-2020 John A. De Goes and the ZIO Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zio.clock

import scala.scalajs.js

import zio.duration.Duration
import zio.internal.Scheduler

private[clock] trait PlatformSpecific {
  private[clock] val globalScheduler = new Scheduler {
    import Scheduler.CancelToken

    private[this] val ConstFalse = () => false

    override def schedule(task: Runnable, duration: Duration): CancelToken = duration match {
      case Duration.Infinity => ConstFalse
      case Duration.Finite(_) =>
        var completed = false

        val handle = js.timers.setTimeout(duration.toMillis.toDouble) {
          completed = true

          task.run()
        }
        () => {
          js.timers.clearTimeout(handle)
          !completed
        }
    }
  }
}
