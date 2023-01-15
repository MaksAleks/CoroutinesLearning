package max.learn

import kotlinx.coroutines.*
import kotlin.coroutines.*

class Coroutine(
//    val scope: CoroutineScope,
    private val block: suspend Accessor.() -> Unit
) {

    private val accessor = AccessorImpl()
    private var continuation: Continuation<Unit>? = null
    private var job: Job? = null
    private var prev: Coroutine? = null

    suspend fun resume() {
        suspendCancellableCoroutine<Unit> {
            prev = current.get()
            current.set(this)
            if (continuation == null) {
                // first resume
                continuation = it
                job = CoroutineScope(it.context).launch {
                    block.invoke(accessor)
                    // at this point main logic of the coroutine is finished
                    // and we should resume our caller
                    continuation?.resume(Unit)
                }
            } else {
                suspendInternal(it)
            }
        }
    }

    fun isCompleted() : Boolean {
        return job != null && job?.isCompleted!!
    }

    private suspend fun suspend() {
        suspendCancellableCoroutine<Unit> {
            current.set(prev)
            suspendInternal(it)
        }
    }

    private fun suspendInternal(cont: CancellableContinuation<Unit>) {
        val suspended = continuation
        continuation = cont
        suspended?.resume(Unit)
    }

    interface Accessor {
        suspend fun suspend()
        // in order to mask kotlinx.coroutines.yield()
        suspend fun yield()
    }

    private inner class AccessorImpl : Accessor {
        override suspend fun suspend() {
            return Coroutine.suspend()
        }

        override suspend fun yield() {
            return Coroutine.suspend()
        }
    }

    companion object {

        private var current: ThreadLocal<Coroutine> = ThreadLocal()
        suspend fun suspend() {
            current.get().suspend()
        }
    }
}

fun main() = runBlocking(newSingleThreadContext("Coroutine")) {

    val co1 = Coroutine {
        println("Step 2")
        Coroutine.suspend()
        println("Step 4")
    }

    val co2 = Coroutine {
        println("Step 1")
        co1.resume()
        println("Step 3")
        co1.resume()
    }
    co2.resume()
}