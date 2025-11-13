# Phillip Lackner
[Youtube](https://www.youtube.com/watch?v=0Hv5LTxAutw)

- Thread pool
    - create multiple threads to run blocking code in parallel
    - each thread can only do one thing at a time
    - if a thread is blocked (e.g., waiting for network), it cannot do other work
    - creating too many threads can lead to overhead and context switching

- Dispatchers are just a group of thread pools
    - only difference between Default and IO is the size of the thread pool
    - Default: for CPU-intensive tasks
      - has number of threads equal to number of CPU cores 
        - no point in having more threads than cores for computation-heavy tasks, 
          - because only one thread can use a core at a time and cpu tasks don't have idle time
      - good for computation-heavy tasks (bitmaps, sorting, etc.)
    - IO: for network or disk operations
      - IO has 64 different threads
      - good for long-running tasks that may block (network calls, database operations)
      - these are tasks that typically have idle time waiting for responses (like delay below)
    - Main: for UI updates
    - Unconfined: starts in the caller thread, but only until the first suspension point


```kotlin
fun main() {
    println("Start")
    thread {
        println("Thread started")
        blockingCode()
        println("Thread Done")
    }
    CouroutineScope(Dispatchers.Default).launch {
        // allows us to run suspending functions
        println("Coroutine started")
        cookRice()
        cookChicken()
        // this will take 7 seconds total
        println("Coroutine Done")
    }
    println("End")
}
fun mainCoroutine() = runBlocking {
    CoroutineScope(Dispatchers.Default).launch {
        // allows us to run suspending functions
        println("Coroutine started")
        cookRice()
        cookChicken()
        // this will take 7 seconds total
        println("Coroutine Done")
        
        // ----------------------------------------
        launch {
            cookRice()
        }
        launch {
            cookChicken()
        }
        // this will take 4 seconds total as both run in an illusion of parallelism
        // since delay is non-blocking, the dispatcher can switch between the two tasks 
        // the dispatcher will look at cookRice, start the delay, then switch to cookChicken, start that delay,
        // then when the delays are over, it will resume both tasks
        // the cpu core is not busy doing computation during the delays
            // 1 core can do only one thread of computation at a time
            // coroutines can switch between tasks during non-blocking delays (if CPU is free)
    }.join() // wait for coroutine to finish before exiting main
    
    ViewModelScope(Dispatchers.IO).launch {
        // for long-running tasks like network or database operations
    }
    LifecycleScope(Dispatchers.Main).launch {
        // for updating the UI
    }
    GlobalScope(Dispatchers.Default).launch {
        // avoid using this as it lives for the entire app lifetime
    }
    SupervisorScope {
        // similar to coroutine scope, but if one child fails, it doesn't cancel the others
    }
    
    val job = CoroutineScope(Dispatchers.Default).launch {
        // allows us to run suspending functions
        println("Coroutine started")
        cookRice()
        cookChicken()
        // this will take 7 seconds total
        println("Coroutine Done")
    }
    job.cancel() // cancels the coroutine if needed
    job.join() // wait for coroutine to finish before exiting main
    job.invokeOnCompletion {
        // called when the coroutine completes or is cancelled
    }
    job.isActive // check if the coroutine is still active
    job.isCompleted // check if the coroutine has completed
    job.isCancelled // check if the coroutine was cancelled
    job.start() // start the coroutine if it was created in a lazy state
    job.children // get the child coroutines of this job
    job.getCancellationException() // get the exception that caused the cancellation, if any
    job.plus(anotherJob) // combine two jobs into one
}

fun blockingCode() {
    (1..50_000_000).map { it * it }
    println("Blocking code finished")
}

suspend fun suspendingCode() = withContext(Dispatchers.Default) {
    (1..50_000_000).map { it * it }
    println("Suspending code finished")
}

suspend fun cookRice() {
    delay(3000) // simulates a long-running task without blocking the thread
    println("Rice is cooked")
}
suspend fun cookChicken() {
    delay(4000)
    println("Chicken is cooked")
}
```

## Suspend
- 