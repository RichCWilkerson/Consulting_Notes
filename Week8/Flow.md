# Flows
- A coroutine that can emit multiple values sequentially.
- Can be thought of as a stream of data that can be observed.
- Supports backpressure, meaning it can handle situations where the producer is faster than the consumer.
- Can be cold (start emitting values only when collected) or hot (emit values regardless of collectors).
  - flow is cold (only runs if there are subscribers/collectors)
  - SharedFlow and StateFlow are hot (emit values regardless of collectors)
- Built on top of coroutines, making them easy to use with suspend functions and other coroutine constructs.
- reactive programming -> react to data changes

## Collect vs CollectLatest
- `collect`: collects all values emitted by the flow, processing each one sequentially.
- `collectLatest`: only processes the latest value emitted by the flow, cancelling any previous processing if a new value is emitted before the previous one is finished.
- when to use which:
  - use `collect` when you need to process every single value emitted by the flow.
  - use `collectLatest` when only the most recent value matters, and you want to avoid processing outdated values.

```kotlin
// ViewModel
class CountViewModel(): ViewModel() {
    val countFlow: flow<Int> = flow {
        // this block is like a coroutine
        val startingValue = 10
        var currentValue = startingValue
        emit(startingValue)
        while(currentValue > 0) {
            delay(1000) // wait for 1 second
            currentValue--
            emit(currentValue)
        }
    }
    
    init {
        countFlow()
    }
    
    // this is how to collect flow data in viewmodel scope
    private fun collectFlow() {
        viewModelScope.launch {
            countFlow.collect { time ->
                // this will collect the emitted values in a sequential manner
                // even if the processing takes time, it will wait for it to finish before collecting the next value
                // so it will count 10, 9, 8, 7, ...
                delay(1500) // simulate processing time
                println("Count: $time")
            }
            countFlow.collectLatest { time ->
                // this will only process the latest emitted value
                // if a new value is emitted before the previous processing is done, it will cancel the previous one
                // so if counting down quickly, it may skip some numbers
                // this will only emit 0, because there is a new value coming in before processing finishes, so it cancels previous ones
                delay(1500) // simulate processing time
                println("Latest Count: $time")
            }
        }
    }
}
// Composable UI
@Composable
fun CountScreen(countViewModel: CountViewModel = viewModel()) {
    // collectAsState collects the flow and converts it to a State object
    val count by countViewModel.countFlow.collectAsState(initial = 10)
    Text(text = "Count: $count")
}
```


