# Task
- Create and submit GIT repository of Kotlin Learning
    - Notes I assume from a variety of sources
- Focused learning:
    - Lambda Function →
    - Higher Order Functions
    - Coroutines

# Things to Study
- Interpolation
    - var msg = “Welcome $name, you have ${getBalance(loan, dues)} balance remaining.”
    - can use any function or variable inside ${}

- remember access modifiers means (public, private, etc.)
    - kotlin is public and final by default
    - Internal → available in current module

## Language Specific Features
- WHEN → you can multiple conditions handled in one block of code
```kotlin
// Enum and several `when` examples
enum class Weather {
    SUNNY, RAINY, CLOUDY, WINDY, STORMY
}

// when with an enum subject
fun respondToWeather(weather: Weather) {
    when (weather) {
        Weather.SUNNY -> println("Wear sunglasses")
        Weather.RAINY -> println("Take an umbrella")
        Weather.CLOUDY, Weather.WINDY -> println("Light jacket recommended")
        Weather.STORMY -> println("Stay indoors")
    }
}

// when with ranges (subject = Int)
fun checkNumber(n: Int) {
    when (n) {
        in 1..10 -> println("Between 1 and 10")
        !in 11..20 -> println("Not between 11 and 20")
        else -> println("Other")
    }
}

// when with type checks (subject = Any)
fun handleInput(x: Any?) {
    when (x) {
        is String -> println("String of length ${'$'}{x.length}")
        is Int -> println("Integer: ${'$'}x")
        null -> println("Null value")
        else -> println("Unknown type")
    }
}

// subjectless when (useful for boolean conditions)
fun subjectlessExample(value: Int) {
    when {
        value < 0 -> println("Negative")
        value == 0 -> println("Zero")
        value > 0 -> println("Positive")
    }
}
```

- data class → similar to POJO (plain old java object) class, but we don’t have to define getters and setters
    - provides default functions for equals(), hashCode(), toString(), copy(), and componentN() functions
- sealed class → similar to ENUMs, but we can use any data type or object as choice
    - used to represent internal states of an application or feature
    - don't use them in place of ENUMs
    - use them when u have complex states with multiple data types
```kotlin
// Sealed class example
sealed class Result {
    data class Success(val data: String) : Result()
    data class Error(val exception: Exception) : Result()
    object Loading : Result()
}
fun handleResult(result: Result) {
    when (result) {
        is Result.Success -> println("Data: ${'$'}{result.data}")
        is Result.Error -> println("Error: ${'$'}{result.exception.message}")
        Result.Loading -> println("Loading...")
    }
}

```

- Coroutine → enables you to handle with suspension (virtual threads)
    - https://kotlinlang.org/docs/coroutines-basics.html
    - Suspend Function
    - SCOPE → how long coroutine should live
        - GlobalScope
        - LifecycleScope
        - ViewModelScope
        - CoroutineScope
    - DISPATCHER → where to execute the coroutine task
        - Main → display, ui thread, application. . .
        - IO (input/output) → updating recording, data, CRUD, DB, API . . .
        - Default → system related tasks, algorithm, sorting, filtering. . .
        - Unconfined → no priority tasks, free threads
    - BUILDER → style of execution of the task
        - Launch → synchronous execution of the tasks (linear execution)
        - Async → asynchronous execution of the tasks (parallel execution)
        - RunBlocking → block the current thread, until the task is complete
    - Steps:
        - A suspending function.
        - A coroutine scope in which it can run, for example inside the withContext() function.
        - A coroutine builder like CoroutineScope.launch() to start it.
        - A dispatcher to control which threads it uses.

```kotlin
// Coroutine example
import kotlinx.coroutines.*
fun main() = runBlocking {
    // Launch a coroutine in the IO dispatcher
    val job = launch(Dispatchers.IO) {
        // Simulate a long-running task
        delay(1000L)
        println("Task completed on thread: ${'$'}{Thread.currentThread().name}")
    }
    println("Waiting for the task to complete...")
    job.join() // Wait for the coroutine to finish
    println("Main program ends on thread: ${'$'}{Thread.currentThread().name}")
}
```

- Data class → gives us default functions to handle objects better
- Sealed class → gives us strict options to define UseCases
- Object → Creates Singleton in Kotlin
    - Companion Object → Creates a public static reference for a CLASS

- Extension Function → allows you to add additional functionality to a class without using inheritance/implementation
    - Syntax → fun ClassName.FunctionName(PARAMETERS): RETURN_TYPE { // can use all features/values of the class here }

### Scope Functions
- Ref: https://kotlinlang.org/docs/scope-functions.html#functions
    - Let → gives you a block of code, in case a condition is met → mostly used for null safety (user?.let {//only run when the user is not null})
    - Apply → this enables you to modify an object directly, (user.name = “new” → user.apply {name = “new”, email = “something”} )
    - Run → gives you a block of code that can execute tasks (just want to execute a separate task)
    - With → gives you a block of code with reference to current instance (you don’t have to specify name of the instance again and directly use all the values)
    - Also → gives you a block of code to run additional tasks (logging, analytics, reporting. . .)

### GIT
- Rebase → combine the two branches without commit history (just changes)
- Conflict → when same line or file has been changed in two versions
- Branch
    - master → retains a version of code that is LIVE
    - staging → retains the version that is getting tested with LIVE environment
    - qa → retains the version that is current tested (usually dev environment)
    - development → retains the most recent changed version (up to date version of code)
    - feature → individual version of the code
- Cherry Picking → Each commit has a unique address,
    - the act of picking a specific commit address from another/same branch and making it HEAD is called cherry picking
    - `git cherry-pick`: apply the changes introduced by an existing commit (identified by its hash) onto your current branch as a new commit.
        - Useful for copying a bugfix or single change without merging an entire branch.
- Stash → hold the code on side until u make changes on another branch/version
    - `git stash`: temporarily save (stash) changes that are not yet ready to be committed.
        - Useful for switching branches without committing incomplete work.
        - You can later reapply the stashed changes with `git stash apply` or `git stash pop`.

- Have an existing branch and need to merge ->
```zsh
git add .
git commit -m “write commit message”
git pull 
# resolve any conflicts, if conflicted start from top again
git push    

# Rebase 
git checkout development
git pull origin development
git checkout feature_branch
git rebase development
# resolve any conflicts, if conflicted start from top again
```

# Full Notes Following [Kotlin Guide](https://www.programiz.com/kotlin-programming/variable-types)

```kotlin
package com.example.kotlinnotes
import kotlinx.coroutines.*


class KotlinNotes {

    // I'm focusing on topics that were either new to me or those I've had trouble with in the past.

    // WHEN EXPRESSION IN KOTLIN
    // Enum and several `when` examples

    // Char
    // Unlike Java, Char types cannot be treated as numbers.
    fun charExample(c: Char) {
        when (c) {
            'a', 'e', 'i', 'o', 'u' -> println("Vowel")
            // cannot do this in Kotlin - number comparisons not allowed with Char
            // in 65..90 -> println("Uppercase letter")
            // in 97..122 -> println("Lowercase letter")
            in 'b'..'z' -> println("Consonant")
            else -> println("Not a letter")
        }
    }

    // Type Conversion
    // In Kotlin, a numeric value of one type is not automatically converted to another type even when the other type is larger.
    fun typeConversionExample() {
        val number1: Int = 55
        // val number2: Long = number1   // Error: type mismatch.
        val number2: Long = number1.toLong() // Explicit conversion
    }

    // Operators
    fun operatorExample() {
        // Assignment operators: =, +=, -=, *=, /=, %=
        val a: Int = 5
        val b: Int = 2
        // under the hood calls a.plus(b)
        var sum: Int = a + b           // Addition
        sum += 3             // equals sum.plusAssign(3)
        // strings override .plus() which concatenates them
        val name: String = "John"
        // "Hello, ".plus(name)
        val greeting: String = "Hello, " + name  // String concatenation
    }


    // Unary operators
    fun unaryExample() {
        var x: Int = 10
        var y: Int = -2
        var result : Int

        result = -y          // Unary minus, calls y.unaryMinus() = 2
        ++x                 // Increment, calls x.inc() = 11
    }

    // "in" operator = a.contains(b)
    fun inOperatorExample() {
        val range = 1..10
        val num = 5
        if (num in range) {
            println("$num is in the range")
        } else {
            println("$num is not in the range")
        }
    }

    // Index accessors
    fun indexAccessorExample() {
        // Use primitive int array when you want array copy helpers like copyInto
        val array = intArrayOf(1, 2, 3, 4, 5)
        val valueAtIndex2 = array[2] // Calls array.get(2), valueAtIndex2 = 3
        array[3] = 10                // Calls array.set(3, 10), array is now [1, 2, 3, 10, 5]

        // Explicit unpacking of first three elements (arrays don't provide automatic componentN for destructuring)
        val first = array[0]
        val second = array[1]
        val third = array[2]

        // Copying a contiguous block into the array (source must be same primitive type)
        val source = intArrayOf(100, 101, 102)
        source.copyInto(array, destinationOffset = 1, startIndex = 0, endIndex = source.size)

        // If you need to replace a contiguous sub-list for MutableList, use subList
        val list = mutableListOf(1, 2, 3, 4, 5)
        val replacement = listOf(10, 11, 12)
        // Replace elements at indices 1..3 (end exclusive)
        list.subList(1, 4).clear()
        list.addAll(1, replacement)
        // output: [1, 10, 11, 12, 5]
    }

    // Invoke operator
    class Invokable {
        // operator keyword allows you to define custom behavior for operators
        // here we define what happens when an instance of Invokable is called like a function
        operator fun invoke(message: String) {
            println("Invoked with message: $message")
        }
    }

    // "IF"
    // if is an expression in Kotlin, meaning it returns a value
    // in java it is a statement and does not return a value
    fun max(a: Int, b: Int): Int {
        return if (a > b) a else b
    }

    fun ifExample() {
        val x = 10
        val y = 20
        // due to if being an expression, we can assign its result to a variable
        // else is mandatory when using if as an expression
        // no ternary operator in Kotlin so this is how you do it
        val maxVal = if (x > y) {
            println("x is greater")
            x // last expression is the value
        } else {
            println("y is greater or equal")
            y // last expression is the value
        }
        println("Max value is $maxVal")
    }

    // Input / Print
    // println points to Java System.out.println()
    fun inputPrintExample() {
        print("Enter your name: ")
        val name = readLine() // readLine() reads a line of input from the standard input (console)
        println("Hello, $name!")

        // if you want other input types, you need to use Scanner or similar
        // here we read an integer from input
        val reader = java.util.Scanner(System.`in`)
        print("Enter your age: ")
        val age = reader.nextInt() // nextLong(), nextDouble(), etc.
        println("You are $age years old.")
    }

    enum class Weather {
        SUNNY, RAINY, CLOUDY, WINDY, STORMY
    }

    // when can be used as an expression or a statement
    // when with an enum subject (Weather)
    // when is a statement here
    fun respondToWeather(weather: Weather) {
        when (weather) {
            Weather.SUNNY -> println("Wear sunglasses")
            Weather.RAINY -> println("Take an umbrella")
            Weather.CLOUDY, Weather.WINDY -> println("Light jacket recommended")
            Weather.STORMY -> println("Stay indoors")
        }
    }

    // when as an expression. We set result to the value returned by when
    fun getOperator() {
        println("Enter operator either +, -, * or /")
        val operator = readLine()

        val result = when (operator) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> a / b
            else -> "$operator operator is invalid operator."
        }

        println("result = $result")
    }

    // when with ranges (subject = Int)
    fun checkNumber(n: Int) {
        when (n) {
            // ".." is the range operator and will count inclusively to the end value
            in 1..10 -> println("Between 1 and 10")
            // use "," to separate multiple values
            !in listOf(11, 12, 13, 14) -> println("Not between 11 and 14")
            else -> println("Other")
        }
    }

    // when with type checks (subject = Any)
    fun handleInput(x: Any?) {
        when (x) {
            is String -> println("String of length ${'$'}{x.length}")
            is Int -> println("Integer: ${'$'}x")
            null -> println("Null value")
            else -> println("Unknown type")
        }
    }

    // subjectless when (useful for boolean conditions)
    fun subjectlessExample(value: Int) {
        when {
            value < 0 -> println("Negative")
            value == 0 -> println("Zero")
            value > 0 -> println("Positive")
        }
    }

    // STRING INTERPOLATION
    // being able to embed a function call inside a string template was new to me
    fun interpolationExample(n: Int) {
        val message = "I'm counting to ${n}: ${checkNumber(n)}"
        println(message)
    }

    // DATA CLASS
    data class Person(val name: String, val age: Int) {
        fun greet() = "Hello, my name is ${name} and I'm ${age} years old."
    }

    // do while
    fun doWhileExample() {
        var count = 1
        do {
            println("Count is $count")
            count++
        } while (count <= 5)
    }

    // for loop
    // no traditional for loop in Kotlin
    // can use ranges, arrays, collections, or anything that provides an iterator
    fun forLoopExample() {
        val items = listOf("apple", "banana", "kiwi")
        val word = "Hello"
        for (item in items) {
            println(item)
        }

        // loop with index
        for (index in items.indices) {
            println("Item at index $index is ${items[index]}")
        }

        // loop through characters in a string
        for (index in word.indices) {
            println(word[index])
        }

        // loop with step and downTo keywords
        // can also use until (excludes the end value)
        for (i in 1..5 step 2) {
            println(i)
        }
        for (i in 5 downTo 1) {
            println(i)
        }
    }

    // Break, Continue, and Return
    // continue skips the current iteration of the nearest enclosing loop
    // return exits the nearest enclosing function or anonymous function
    // break exits the nearest enclosing loop or when expression
    fun breakExample() {
        for (i in 1..10) {
            if (i == 5) {
                println("Breaking at $i")
                break // exit the loop when i is 5
            } else if (i % 2 == 0) {
                println("Skipping even number $i")
                continue // skip even numbers
            }
            println(i)
        }
    }

    // break with label
    fun breakLabelExample() {
        outer@ for (i in 1..5) {
            inner@ for (j in 1..5) {
                if (i * j > 6) {
                    println("Breaking out of outer loop at i=$i, j=$j")
                    break@outer // exit the outer loop
                }
                if (i == j) {
                    println("Continuing inner loop at i=$i, j=$j")
                    continue@inner // skip to next iteration of inner loop
                }
                println("i=$i, j=$j")
            }
            // jumps to here after break@inner
        }
        // jumps to here after break@outer
    }

    // Functions
    // return type is optional if it is Unit (like void in Java)
    // You can omit the curly braces { } of the function body and specify the body after = symbol
    //      if the function returns a single expression.
    //      return type is inferred from the expression.
    fun getName(firstName: String, lastName: String) = "$firstName $lastName"
    fun getNameExplicit(firstName: String, lastName: String): String {
        return "$firstName $lastName"
    }

    // Infix Notation
    // And / Or
    fun infixExample() {
        val a = true
        val b = false
        var result: Boolean

        // just checking if either a or b is true
        result = a or b // a.or(b)
        println("result = $result")
        // true

        // checking if both a and b are true
        result = a and b // a.and(b)
        println("result = $result")
        // false
        // can also use && and || operators
    }
    // Rules for infix functions:
    // 1. They must be member functions or extension functions.
    // 2. They must have a single parameter.
    // 3. Must be marked with the infix keyword.
    infix fun createPyramid(rows: Int) {
        var k = 0
        for (i in 1..rows) {
            k = 0
            for (space in 1..rows-i) {
                print("  ")
            }
            while (k != 2*i-1) {
                print("* ")
                ++k
            }
            println()
        }
    }

    // Default and Named Arguments
    fun displayBorder(character: Char = '=', length: Int = 15) {
        for (i in 1..length) {
            print(character)
        }
    }
    // if we call displayBoarder(5) it will think we are passing the character
    // so we need to use named arguments to specify which parameter we are passing
    // displayBorder(length = 5)


    // Recursion and Tail Recursion
    // recursion = perform all recursive calls first, and calculate the result from return values at last
    // tail = calculations are performed first, then recursive calls are executed (the recursive call passes the result of your current step to the next recursive call).
    //      This makes the recursive call equivalent to looping, and avoids the risk of stack overflow.
    //      A recursive function is eligible for tail recursion if the function call to itself is the last operation it performs.

    // Not Eligible for Tail Recursion
    // n*factorial(n-1) is not the last operation
    // return n.toLong() is performed after the recursive call returns
    fun factorial(n: Int): Long {

        if (n == 1) {
            return n.toLong()
        } else {
            return n*factorial(n - 1)
        }
    }
    // Would need to rewrite like this to be tail recursive
    // place the base case first and use a helper parameter to accumulate the result
    tailrec fun factorial(n: Int, run: Int = 1): Long {
        return if (n == 1) run.toLong() else factorial(n-1, run*n)
    }
    println("${factorial(5)}")

    // Eligible for Tail Recursion
    // must mark with "tailrec" keyword to enable tail call optimization if eligible
    tailrec fun fibonacci(n: Int, a: Long, b: Long): Long {
        return if (n == 0) b else fibonacci(n-1, a+b, a)
    }


    // Objects and Classes
    // kotlin supports functional programming and object oriented programming
    // functional = higher order functions, lambdas, inline functions, tail recursion
    // oop = classes, objects, inheritance, polymorphism, encapsulation, abstraction

    // primary constructor is part of the class header
    class Car(val make: String, val model: String, var speed: Int = 0) {}

    // initializer block
    // used to initialize the class or perform some setup tasks

    // use "_" prefix to avoid conflict with property name
    class Bike(val make: String, val model: String, var _name: String) {
        var speed: Int = 0
        var name: String

        // initializer block runs when the class is instantiated
        init {
            name = _name.uppercase()
            println("Bike initialized: $name")
        }

        // secondary constructor - must delegate to primary constructor
        // can have multiple secondary constructors
        // secondary constructors are not commonly used in Kotlin
        constructor (make: String, model: String) : this(make, model, "Unknown") {
            println("Secondary constructor called")
        }
        // can also call the parent class constructor using super keyword
        constructor (make: String) : super(make, "Unknown", "Unknown") {
            println("Secondary constructor with one parameter called")
        }

        fun accelerate(increment: Int) {
            speed += increment
            println("Bike speed: $speed")
        }

        fun brake(decrement: Int) {
            speed -= decrement
            if (speed < 0) speed = 0
            println("Bike speed: $speed")
        }
    }


    // GETTERS AND SETTERS
    // optional, Kotlin provides default getters and setters for properties
    class Rectangle(var width: Double, var height: Double) {
        var color: String = "Red"
            get() = field.uppercase() // custom getter
            set(value) {               // custom setter
                field = value.lowercase()
            }

        var area: Double
            get() = width * height // custom getter
            set(value) {          // custom setter
                val ratio = width / height
                height = Math.sqrt(value / ratio)
                width = ratio * height
            }
    }

    // Inheritance
    // notice the "open" keyword which allows the class to be inherited
    // by default, classes in Kotlin are final (cannot be inherited)
    open class Animal(val name: String) {
        open fun sound() {
            println("Animal sound")
        }
    }
    // we declare inheritance using ":" symbol
    // any parameters required by the parent class constructor must be passed from the child class constructor
    class Dog(name: String, val breed: String) : Animal(name) {
        // override keyword is mandatory when overriding a method
        // the method in the parent class must be marked as open
        override fun sound() {
            println("Woof")
        }
    }
    val dog = Dog("Buddy", "Golden Retriever")
    dog.sound() // Woof

    // In case of no primary constructor, each base class has to initialize the base (using super keyword),
    // or delegate to another constructor which does that. For example,
    open class Base {
        constructor(name: String) {
            println("Base class initialized with name: $name")
        }
    }
    class Derived : Base {
        constructor() : super("Default") {
            println("Derived class initialized with name: $name")
        }
    }

    // visibility modifiers
    // internal = visible within the same module (multipe files)
    // primary constructor is public by default ->
        // explicitly use constructor keyword to change visibility
    class Person1 private constructor(name: String) {}


    // Abstract Classes and Interfaces
    // The members (properties and methods) of an abstract class are non-abstract unless you explictly use abstract keyword to make them abstract.
    // abstract classes are open by default - do not need to use open keyword explicitly
    abstract class Shape {
        abstract fun area(): Double // abstract method
        // we must mark display as open if we want to override it in a subclass
        open fun display() {         // non-abstract method
            println("Displaying shape")
        }
    }

    // Kotlin interfaces are similar to abstract classes.
    // However, interfaces cannot store state whereas abstract classes can.
    //      Meaning, interface may have property but it needs to be abstract or has to provide accessor implementations.
    //      Whereas, it's not mandatory for property of an abstract class to be abstract.
    interface Drawable {
        var color: String // abstract property

        // accessors with default implementations makes this valid
        var borderWidth: Int
            get() = 1 // non-abstract property with default implementation
            set(value) {} // non-abstract property with default implementation
        fun draw() // abstract method
        fun resize() { // non-abstract method with default implementation
            println("Resizing drawable")
        }
    }
    // A class can inherit from one superclass and implement multiple interfaces.
    // use "," to separate multiple interfaces after the superclass
    class Circle(val radius: Double) : Shape(), Drawable {
        override var color: String = "Red" // must override abstract property

        override fun area(): Double { // must override abstract method
            return Math.PI * radius * radius
        }

        override fun draw() { // must override abstract method
            println("Drawing circle with radius $radius and color $color")
        }

        override fun display() { // optional to override non-abstract method
            println("Displaying circle")
        }
    }

    // Inherit two methods with the same signature from different interfaces
    interface A {

        fun callMe() {
            println("From interface A")
        }
    }

    interface B  {
        fun callMe() {
            println("From interface B")
        }
    }

    // MUST override callMe() to resolve the conflict.
    // If you want both behaviors, you can call them using super with the interface name.
    class C: A, B {
        override fun callMe() {
            super<A>.callMe()
            super<B>.callMe()
        }
    }

    fun main(args: Array<String>) {
        val obj = C()

        obj.callMe()
    }

    // Nested and Inner Classes
    // Nested class = static by default, cannot access members of the outer class
    // Inner class = non-static, can access members of the outer class
    // In Java, when you declare a class inside another class, it becomes an inner class by default.
    //      However in Kotlin, you need to use inner modifier to create an inner class
    class Outer {
        val a = "Outside Nested class."
        class Nested {
            val b = "Inside Nested class."
            fun callMe() = "Function call from inside Nested class."

            // cannot access 'a' from Outer class
            // to access 'a', Nested class would need to be marked as inner
            val c = a + " " + b
        }

        inner class Inner {
            val d = "Inside Inner class."
            fun callMe() = "Function call from inside Inner class."

            // can access 'a' from Outer class
            val e = a + " " + d
        }
    }
    // use Outer.Nested to access the nested class
    val nestedObjB = Outer.Nested().b


    // Data Classes
    // Rules:
        //  The primary constructor must have at least one parameter.
        //  The parameters of the primary constructor must be marked as either val (read-only) or var (read-write).
        //  The class cannot be open, abstract, inner or sealed.
        //  The class may extend other classes or implement interfaces. If you are using Kotlin version before 1.1, the class can only implement interfaces.
    // Provides:
        //  equals()/hashCode() pair
            // to check structural equality based on property values and not reference equality
        //  toString() of the form "User(name=John, age=42)"
        //  componentN() functions corresponding to the properties in their order of declaration.
            // used for destructuring declarations
        //  copy() function
            // to create a copy of the object with some properties modified
    data class User(val name: String, val age: Int) {}
    val user = User("Alice", 30)
    val olderUser = user.copy(age = 31) // copy with modified age
    val (userName, userAge) = user // destructuring declaration


    // Sealed Classes
    // Sealed classes are used when a value can have only one of the types from a limited set (restricted hierarchies).
    // NOTE:
        // All subclasses of a sealed class must be declared in the same file where sealed class is declared.
        // A sealed class is abstract by itself, and you cannot instantiate objects from it.
        // You cannot create non-private constructors of a sealed class; their constructors are private by default.
    // add sealed keyword before class to make it sealed
    class Expr
    class Const(val value: Int) : Expr
    class Sum(val left: Expr, val right: Expr) : Expr

    fun eval(e: Expr): Int =
        when (e) {
            is Const -> e.value
            is Sum -> eval(e.right) + eval(e.left)
            else ->
                throw IllegalArgumentException("Unknown expression")
        }
    // else is mandatory if the when expression is not exhaustive (i.e., does not cover all possible subclasses of the sealed class).
    // If you cover all possible subclasses, the else branch is not required.
    // if we make Expr a sealed class, we don't need the else branch
    // because the compiler knows all possible subclasses of Expr at compile time and can verify that all cases are covered.

    // ENUM vs SEALED CLASS
    // Enum class and sealed class are pretty similar. The set of values for an enum type is also restricted like a sealed class.
    // The only difference is that, enum can have just a single instance, whereas a subclass of a sealed class can have multiple instances.

    // OBJECT DECLARATIONS
    // object keyword is used to declare a singleton class
    // not allowed to have constructors (primary or secondary)
    // can have properties and methods
    // Remember it's hard to unit test singletons
        // dependency injection is a better approach for managing shared resources
    // Singleton pattern = restricts the instantiation of a class to a single instance
        // useful for things like database connections, logging, configuration settings
    object DatabaseConnection {
        val url: String = "jdbc:mysql://localhost:3306/mydb"
        val user: String = "root"
        val password: String = "password"
        fun connect() {
            println("Connecting to database at $url with user $user")
            // connection logic here
        }
    }

    // object keyword can also be used to create objects of an anonymous class known as anonymous objects.
    // They are used if you need to create an object of a slight modification of some class or interface without declaring a subclass for it

    // Example: create an anonymous object that overrides 2 methods of MouseAdapter
    window.addMouseListener(object : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            // handle mouse click
        }
        override fun mouseEntered(e: MouseEvent) {
            // handle mouse enter
        }
    })

    // Companion Object
    // n Kotlin, you can also call callMe() method by using the class name, i.e, Person in this case.
    // For that, you need to create a companion object by marking object declaration with companion keyword.
    class Person1 {
        // companion keyword
        companion object Test {
            fun callMe() = println("I'm called.")
        }
    }

    fun main(args: Array<String>) {
        // allows you to call a method without creating an instance of the class
        // Test can be omitted (optional) - Person1.Test.callMe() is also valid
        Person1.callMe()
        // instead of:
        // val obj = Person1()
        // obj.callMe()
    }

    // Extension Functions
    // Suppose, you need to extend a class with new functionalities.
    // In most programming languages, you either derive a new class or use some kind of design pattern to do this.
    // an extension function is a member function of a class that is defined outside the class.
        // This means you can add new functions to a class without modifying its source code, inheriting from it, or using any design patterns like Decorator.
    fun String.removeFirstLastChar(): String =  this.substring(1, this.length - 1)
    // this refers to the String instance on which the function is called

    fun extenstionFunctionExample() {
        val myString= "Hello Everyone"
        val result = myString.removeFirstLastChar()
        println("First character is: $result")
    }
    // above adds removeFirstLastChar() function to the String class

    // If you need to integrate Kotlin on the top of Java project, you do not need to modify the whole code to Koltin.
    // Just use extension functions to add functionalities.
    // NOTE: Do not abuse extension functions.


    // Operator Overloading

    // You can also define how operator works for objects by overloading its corresponding function.
    // For example, you need define how + operator works for objects by overloading plus() function.
    // NOTE: not all operators can be overloaded. Refer to docs
    class Point(val x: Int = 0, val y: Int = 10) {
        // overload + operator
        // operator keyword is mandatory to enable operator overloading
        // we are telling the compiler that we are overloading an operator
        operator fun plus(other: Point): Point {
            return Point(x + other.x, y + other.y)
        }
        operator fun dec() = Point(--x, --y)
        // equivalent to:
        // operator fun dec(): Point {
        //    return Point(--x, --y)
        // }

    }
    fun operatorOverloadingExample() {
        val p1 = Point(10, 20)
        val p2 = Point(30, 40)
        val p3 = p1 + p2 // calls p1.plus(p2)
        println("p3 = (${p3.x}, ${p3.y})") // p3 = (40, 60)
        // adding both x and y coordinates together

        var p4 = Point(5, 5)
        p4-- // calls p4.dec()
        println("p4 = (${p4.x}, ${p4.y})") // p4 = (4, 4)
    }

    // Keywords
    // hard keywords = if, else, when, try, catch, finally, for, do, while, return, break, continue, object, class, interface, this, super, val, var, fun, in, is, !is, null, true, false, typealias, typeof
    // soft keywords = by, get, set, import, where, constructor, init, companion, override, public, private, protected, internal, open, final, abstract, enum,
    //                sealed, const, external, inner, lateinit, tailrec, vararg, suspend, data, inline, noinline, crossinline, reified, out, field, it
    // soft keywords have special meaning only in certain contexts, otherwise can be used as identifiers (variable names, function names, etc.)


    // NOTE strings are immutable in Kotlin
    // must use StringBuilder for mutable strings or create a new string
    // you can reassign a complete new string to the same variable, just not modify characters in the existing string
    fun stringBuilderExample() {
        val sb = StringBuilder("Hello")
        sb.append(" World")
        println(sb.toString()) // Hello World

        // not allowed
        val str = "Hello"
        str[0] = 'h' // Error: Val cannot be reassigned
        // instead do this
        val newStr = 'h' + str.substring(1)
        println(newStr) // hello
    }

    // LAMBDAS
    // is an anonymous function; a function without name.
    // These functions are passed immediately as an expression without declaration
    fun lambdaExample() {
        // lambda syntax
        val sum: (Int, Int) -> Int = { a: Int, b: Int -> a + b }
        // can also omit parameter types if they can be inferred
        val multiply: (Int, Int) -> Int = { a, b -> a * b }

        val result1 = sum(3, 4) // 7
        val result2 = multiply(3, 4) // 12

        println("Sum: $result1, Multiply: $result2")

        // lambda with no parameters
        val greet: () -> Unit = { println("Hello!") }
        // variable: (parameters) -> returnType = { function body }
        // if the lambda has no parameters, use empty parentheses ()
        // OR:
        val greetShort = { println("Hello!") }
        greet() // Hello!

        // lambda with single parameter - "it" keyword can be used to refer to the single parameter
        val square: (Int) -> Int = { it * it }
        val result3 = square(5) // 25
        println("Square: $result3")
    }

    // Higher-Order Functions
    // higher-order function = a function that takes another function as a parameter or returns a function
    fun callMe(greeting: () -> Unit) {
        greeting()
    }
    fun callMeExample() {
        callMe({ println("Hello from lambda!") })
        // if the lambda is the last parameter, it can be placed outside the parentheses
        callMe() { println("Hello from lambda outside parentheses!") }
        // if the lambda is the only parameter, the parentheses can be omitted entirely
        callMe { println("Hello from lambda with no parentheses!") }
    }

    // often used with collections
    // The maxBy() function returns the first element yielding the largest value of the given function or null if there are no elements.
    fun higherOrderExample() {
        val people = listOf(
            Person("Jack", 34),
            Person("Shelly", 19),
            Person("Patrick", 13),
            Person("Jill", 12),
            Person("Shane", 22),
            Person("Joe", 18)
        )

        val selectedPerson = people.maxBy({ person ->  person.age })
        // OR
        val selectedPerson2 = people.maxBy({ it.age })

        println(selectedPerson)
        println("name: ${selectedPerson?.name}" )
        println("age: ${selectedPerson?.age}" )
    }
    // In the above program, the lambda expression accepts only one parameter (a list of Person objects).
    // In such cases, you can refer the argument by using keyword it.


    // CO-ROUTINES
    // not actually in parallel, but give a feel for it
    // Basic coroutine example using kotlinx.coroutines library
    /*
    - SCOPE → how long coroutine should live
        - GlobalScope - application wide, live as long as the app is alive
        - LifecycleScope - tied to lifecycle of an activity or fragment
        - ViewModelScope - tied to lifecycle of a ViewModel
        - CoroutineScope - custom scope, can define your own scope
    - DISPATCHER → where to execute the coroutine task
        - Main → display, ui thread, application. . .
        - IO (input/output) → updating recording, data, CRUD, DB, API . . .
        - Default → system related tasks, algorithm, sorting, filtering. . .
        - Unconfined → no priority tasks, free threads

            - You don't have to specify a dispatcher for every coroutine.
            - By default, coroutines inherit the dispatcher from their parent scope.
            - You can specify a dispatcher to run a coroutine in a different context.

If the coroutine context doesn't include a dispatcher, coroutine builders use Dispatchers.Default.
    - BUILDER → style of execution of the task
        - Launch → synchronous execution of the tasks (linear execution)
        - Async → asynchronous execution of the tasks (parallel execution)
        - RunBlocking → block the current thread, until the task is complete
            - Use runBlocking() only when there is no other option to call suspending code from non-suspending code:
            - Never call runBlocking on the Android main/UI thread — it will freeze the UI.
     */
    // this will block the main thread until the coroutine is done
    // runBlocking is setting the scope for the coroutine
    // runBlocking is not recommended to use "suspend" keyword
    fun coroutineExample() = runBlocking {
        // launch allows us to start a new coroutine without blocking the current thread
        // this is not required, but it is here to show that launch is run inside a coroutine scope
        this.launch {
            delay(1000L)
            println("World!")
        }
        println("Hello,")
    }
    suspend fun nonBlockingExample() = coroutineScope {
        launch {
            delay(1000L)
            println("World!")
        }
        println("Hello,")
    }

    suspend fun fetchData(): String {
        // The suspending withContext() function is typically used for context switching,
        // but in this example, it also defines a non-blocking entry point for concurrent code.

        // Dispatchers.Default dispatcher to run code on a shared thread pool for multithreaded execution.
        // IO is for CRUD / API / DB operations

        // The coroutines launched inside the withContext() block share the same coroutine scope, which ensures structured concurrency.
        return withContext(Dispatchers.IO) {
            // add builders here
            val userData = async { getUserFromNetwork() }
            userData.await()
        }
    }

    suspend fun getUserFromNetwork(): String {
        delay(1000L) // Simulate network delay
        return "User data"
    }

    suspend fun anotherCoroutineExample() = withContext(Dispatchers.Default) {
        val job1 = async {
            // Simulate a long-running task
            delay(1000L)
            "Result from Task 1"
        }

        val job2 = async {
            // Simulate another long-running task
            delay(1500L)
            "Result from Task 2"
        }

        // Await results from both tasks
        val result1 = job1.await()
        val result2 = job2.await()

        println("Results: ${result1}, ${result2}")
    }



}
```