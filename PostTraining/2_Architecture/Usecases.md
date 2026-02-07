# Resources:
- [Result Class - Medium](https://proandroiddev.com/resilient-use-cases-with-kotlin-result-coroutines-and-annotations-511df10e2e16)

# Use Cases - Overview
TODO: explain Invoke and Execute and any thing else like them used in use cases.


## Try-Catch vs Result Class

When implementing use cases in your application, handling success and failure scenarios is crucial. Two common approaches are using try-catch blocks and utilizing a Result class.

### Try-Catch Approach
Writing plain try-catches around every use case you call works fine, but has some disadvantages:
- No obligation to catch exceptions, easy to forget. ❌
- Inconsistent API if some use cases throw exceptions while others don’t require any try-catch. ❌
- Difficult to find where try-catch is missing. Airplane mode could prevent code that actually throws exceptions from ever being reached. ❌

### Result Class Approach
The Result class is a simple data structure to wrap data using Result.success(myData) or an exception using Result.failure(ex).

With the runCatching extension you do not have to manually instantiate the Result class:
```kotlin
class PostCommentUseCase() {
    
    // TODO: could you explain this better. runCatching is part of the Result class? like its an extension function of it? or it's an import/dependency?
    // TODO: runCatching has functions like validate and upload? are there others? can you define each of them and when to use them?
    fun execute(comment: Comment) = runCatching {
        // If any of this code throws an exception,
        // this block wrap it in a Result.
        validate(comment)
        // Else, the return value of upload() will be wrapped in a Result.
        upload(comment)
    }
    
    // validate() & upload() methods left out
}

// TODO: this would typically be done in a view model where we are calling the use case class?
// executed:
PostCommentUseCase().execute()
    .onSuccess {
        // Success!
        showCommentPosted()
    }
    .onFailure { e->
        // Log the exception and inform the user.
        showError(e)
    }
```

PROS:
This is a very clear API and it fixes the issues listed above:
- No obligation to catch: ✅ Fixed! You either write .onSuccess { } or .onFailure { }. Or both, or none!
- Inconsistent API: ✅ Fixed! Every use case returns the Result wrapper. No more surprises! If you use Flow, you could make use cases return either Result or Flow directly. That way, you have only two solid return types to deal with.
  - TODO: need an example of a flow. are there other things that exist like flow that need to be handled differently?
- Difficult to locate missing try-catches: ✅ Fixed! You can focus on writing success/error handling when needed only without having to worry about exceptions popping up unexpectedly. 
  - TODO: i'm not sure how this solves this issues, now we are just looking for runCatching instead? 

CONS:
- runCatching catches Throwable. This also means it catches Errors like OutOfMemoryError
  - typically you would catch Exception instead of Throwable, but runCatching does not allow that customization.
  - TODO: so is it best practice to use runCatching if this is the case? How do i know when to implement my own custom try, catch block vs runCatching with Result as it comes to use cases? or should i always use runCatching and just know that it also catches throwable?
- runCatching does not rethrow cancellation exceptions. This makes it a bad choice for coroutines as it breaks structured concurrency.
  - so we should use try / catch with coroutines? should i use a custom Result class instead of the API?
- It is not possible to specify a non-Exception error type or a custom base exception class. 

- TODO: i feel like the cons outweigh a custom result class that I can then use to handle the cons. is it not possible to create a generic sealed Result class to allow for extension when needed and also handling exceptions vs throwable separately?

Alternatives:



## Operator Keywords (`in`, `out`, `reified`, `invoke`)