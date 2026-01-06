# Divesh Agile Training 
## Task
- get a list of all jobs I have applied to in the last year
- need to start weekly reporting (every friday - between 4 and 5pm, what was delivered in the training, what tasks were completed, target coming up -> what are our goals)
    - send to Divesh via email ()

# Agile
- environment (dev, qa, prod)
- SDLC (software development life cycle)
    - steps: analysis, design, development, testing, deployment, maintenance
- Agile/scrum - always
    - waterfall exists, but wont do it
    - contractors - being done before end date is biggest priority
    - Spring Review Meeting (UAT) - user acceptance testing
        - demo to stakeholders, get feedback, make changes
    - definition of done - criteria to consider a task complete
        - code complete, tested, reviewed, documented, deployed to staging
    - code freeze phase - no new features, only bug fixes
    - this week we need to report on what a burndown chart it
        - we need to be above the line
        - how accurately we estimate and complete tasks
        - if we need to work overtime to complete tasks - get approval from the CDM in writing 
- Scrum roles
    - Product Owner - defines the product vision, prioritizes the backlog, and ensures the team is delivering value to the customer
    - Scrum Master - facilitates the scrum process, removes impediments, and ensures the team is following agile principles
    - Development Team - self-organizing team that delivers the product increment
- Scrum events
    - Sprint Planning - plan the work for the sprint, define the sprint goal, and create the sprint backlog
    - Daily Standup - daily meeting to discuss progress, impediments, and plans for the day
    - Sprint Review - review the work completed in the sprint, demo to stakeholders, and get feedback
    - Sprint Retrospective - reflect on the sprint, discuss what went well, what didn't, and how to improve
- Scrum artifacts
    - Product Backlog - prioritized list of features, enhancements, and bug fixes
    - Sprint Backlog - list of tasks to be completed in the sprint
    - Increment - the sum of all the product backlog items completed during a sprint and all previous sprints


# Architectures
## TASK
SOLID
Design Pattern
Architecture (MVVM -> CLEAN)
Code - Conditional Navigation (using NavGraph)
- Login Screen
- On Successful Login -> Next Screen (calculator screen)
- Calculator screen should NOT go back to login screen with the back button -> logout to get back to login
    - fancy UI is priority, but needs to also be functional
- One Activity with multiple fragments (NavGraph)
    - NavGraph gives the whole flow of the app
    - When you move to successful login -> we don't want the stack to include the login screen
        - popUpTo = R.id.loginFragment
        - popUpToInclusive = true

### Idea
- Girl Math Calculator
  - on login you add your bank balance
  - Product Cost, Discount %, Tax %, Final Price, Amount Saved -> you can buy Amount Saved worth of stuff

- Dribble -> Mobile -> calculator to get a concept of what needs to be done UI

## Architectures Timeline
- timeline (solving problems that leads to better scalability, maintainability, testability)
    - MVC -> MVP -> MVVM -> MVI -> Clean
    - MVC - too tightly coupled, hard to test
    - MVP - better separation of concerns, but still tightly coupled
    - MVVM - allowed for data binding, live data, flow, coroutines
    - MVI - unidirectional data flow, state management
    - Clean - separates code into layers, easier to test, maintain, and scale

- MVC - model view controller - different from CLEAN because it is tightly coupled
    - model - data layer (repository, network, database)
    - view - UI layer (activity, fragment)
    - controller - connects model and view (callbacks, interfaces)
    - Key Points:
        - controller handles all communication and is the main middle man and focus.
        - view can communicate directly with the model when required - difference with MVP

- MVP - model view presenter
    - model - data layer (repository, network, database)
    - view - UI layer (activity, fragment)
    - presenter - connects model and view (callbacks, interfaces).
        - only one responsible for communicating with data layer (model).
- Key Points:
    - presenter handles all communication and is the main character
    - presenter controls/holds the UI actions and behaviors
    - presenter and view are tightly coupled
        - each view needs its own presenter
    - like JSX in React, it handles the UI logic and updates the view directly.
        - view can never directly communicate with the model.

- MVVM - model view view model
    - model - data layer (repository, network, database)
        - a little more responsibility than MVP
            - can handle some business logic (data transformations, formatting)
    - view - UI layer (activity, fragment)
    - view model - lifecyle aware, holds reusable business logic (live data, flow, coroutines)
    - Key Points:
        - VM doesn't communicate directly with the view (like presenter does in MVP)
          - View gets data from the view model via observable components (live data, flow)
          - View is requesting data from the view model based on observable changes
        - View model are lifecycle aware (survives configuration changes) - presenter does not
        - view model has all the same responsibilities as presenter, but also holds UI state (memory)
        - view model has observable components (live data, flow) that the view can observe and react to changes
        - view model and view are loosly coupled
            - one view model can be shared between multiple views (fragments) - one to many relationship
        - view model does not hold a reference to the view (no memory leaks)
        - when you close the app, netflix isn't still pushing updates to your view model
          - it is not longer observable

- MVI - model view intent
    - model - data layer (repository, network, database)
    - view - UI layer (activity, fragment)
    - intent - unidirectional data flow (state management)
        - user actions -> intents -> view model -> state -> view
        - acts as a messenger
    - Key Points:
        - nothing is tightly coupled
        - unidirectional data flow - easier to reason about
            - Model -> View or View -> Model
            - reactive approach (declarative programming)
        - easier to test and debug
            - less crashes in the app, because state is always consistent
        - can use libraries like Redux, Mobius, Orbit MVI

- Clean Architecture - separates code into layers (presentation, domain, data)
    - can have other layers, but these are the main ones
      - other layers can be added for specific use cases (e.g., caching, logging, analytics, specific features)
    - presentation - UI layer (activity, fragment, view model)
    - domain - business logic layer (use cases, interactors), if X -> Y, then do Z
      - use cases - single responsibility, reusable, testable
      - interactors - orchestrate multiple use cases, handle complex business logic
    - data - data layer (repository, network, database)
    - Key Points:
      - Clean adds layering, boundaries, and dependency inversion
      - can easily swap out implementations (e.g., change database, network library)
      - different from MVVM because of the domain layer
        - domain layer is the main focus and contains all business logic
        - presentation layer is only responsible for UI logic and state
        - data layer is only responsible for data access and storage
        - essentially each layer has a single responsibility
        - can replace a layer without affecting the others (PLUG AND PLAY)
        - heavily influenced by SOLID principles
          - IOC - inversion of control -> Dependency Injection/Inversion

- Microservervices - separates code into small, independent services (each service has its own database, can be deployed independently)
    - service - small, independent service (user service, order service, payment service)
    - database - each service has its own database (user database, order database, payment database)
    - communication - services communicate via APIs (REST, gRPC, messaging)
- Monolithic - all code in one application (single database, single deployment)
    - application - all code in one application (web app, mobile app)
    - database - single database (shared database)
    - deployment - single deployment (single server, single container)
    - APK - android application package (file format for android apps)
        - contains all the code, resources, assets, and manifest file for the app
        - can be signed and aligned for release
        - can be installed on an android device or emulator
        - can be uploaded to the google play store or other app stores
    - Bundles - android app bundle (file format for android apps)
        - contains all the code, resources, assets, and manifest file for the app
        - can be signed and aligned for release
        - can be installed on an android device or emulator
        - can be uploaded to the google play store or other app stores
        - allows for dynamic delivery of features and resources (download on demand)
        - reduces the size of the app by only including the necessary resources for the device
    - Viper - view interactor presenter entity router
        - view - UI layer (activity, fragment)
        - interactor - business logic layer (use cases, interactors)
        - presenter - connects view and interactor (callbacks, interfaces)
        - entity - data layer (repository, network, database)
        - router - navigation layer (navigation controller, navigation graph)

## Design Patterns
- How architecture is implemented (like architecture is the blueprint, design patterns are the building blocks)
- Structural : how classes and objects are composed to form larger structures
    - Adapter - converts the interface of a class into another interface that the client expects.
        - example: recycler view adapter, list adapter, paging adapter
    - Decorator - adds behavior to an object dynamically without affecting other objects of the same class.
        - example: logging decorator, caching decorator, authentication decorator
    - Facade - provides a simplified interface to a complex subsystem.
        - example: api facade, database facade, network facade
- Behavioral : how classes and objects interact and communicate with each other
    - Observer - defines a one-to-many dependency between objects so that when one object changes state, all its dependents are notified and updated automatically.
        - example: live data, flow, event bus
    - Strategy - defines a family of algorithms, encapsulates each one, and makes them interchangeable.
        - example: sorting strategy, filtering strategy, searching strategy
    - Command - encapsulates a request as an object, thereby letting you parameterize clients with different requests, queue or log requests, and support undoable operations.
        - example: button click command, menu item command, toolbar command
    - Chain of Responsibility - passes a request along a chain of handlers until one of them handles the request.
        - example: error handling chain, logging chain, authentication chain
    - State - allows an object to alter its behavior when its internal state changes. The object will appear to change its class.
        - example: login state, network state, ui state
- Creational : how objects are created
    - Builder - separates the construction of a complex object from its representation so that the same construction process can create different representations.
        - example: alert dialog builder, retrofit builder, okHttp client builder
    - Singleton - ensures a class has only one instance and provides a global point of access to it.
        - example: database connection, network client, shared preferences
          - thread safe, if someone changes a list (add, remove) while you are trying to read it, get will always be consistent (synchronized marks things so we dont have conflicts of adding/removing while reading)
          - when you create an instance it should be global, should reference the same instance every time
          - dont create a singleton for a view -> when you rotate the screen, the view is destroyed and recreated -> memory leak
          - singletons are things you do not want to destroyed and recreated
    - Prototype - creates new objects by copying an existing object, known as the prototype.
        - example: cloning objects, copying objects, duplicating objects
    - Abstract Factory - provides an interface for creating families of related or dependent objects without specifying their concrete classes.
        - example: ui component factory, theme factory, style factory
    - Factory - creates objects without exposing the instantiation logic to the client.
        - example: view model factory, repository factory, service factory


- Dependency Injection - a technique where an object receives other objects that it depends on, rather than creating them itself.
        - example: hilt, dagger, koin

## Guides
- KISS (Keep It Simple Stupid) - avoid unnecessary complexity, keep code simple and easy to understand
- DRY (Don't Repeat Yourself) - avoid code duplication, extract common code into functions or classes
- SOLID - five principles of object-oriented design
    - Single Responsibility Principle - a class should have only one reason to change
    - Open/Closed Principle - a class should be open for extension, but closed for modification
    - Liskov Substitution Principle - subclasses should be substitutable for their base classes
    - Interface Segregation Principle - clients should not be forced to depend on interfaces they do not use
    - Dependency Inversion Principle - high-level modules should not depend on low-level modules. Both should depend on abstractions.

## NOTE:
- Architectures are not mutually exclusive. You can combine them to fit your needs.
    - e.g., MVVM + Clean Architecture, MVP + Clean Architecture
      - sports betting -> price changes -> need real-time updates on UI -> WebSockets (data layer talks directly to UI layer)
    - They are just guidelines to help you structure your code better.
    - domain should only be language agnostic code (kotlin) - not android specific code
      - library modules - domain module should not depend on android libraries
- purpose of knowing these architectures is to communicate how we are going to build a certain feature of app
  - secondary knowing the pros and cons of each architecture and use cases

- can use builder and singleton patterns together
  - e.g., retrofit builder + singleton instance

- DI is like having an action figure with interchangeable parts
  - an arm with a shield, an arm with a sword, a leg with a jetpack
  - you can swap out the parts without changing the action figure itself

- Dependency Inversion is a guideline principle (SOLID), 
- Dependency Injection is an application of that principle (design pattern)
  - DI is a way to implement DIP
  - Constructor Injection -> User(canFly)
  - Field Injection -> Car(lateinit var engine: Engine)
    - we know we want a car, but will decide what engine later
    - manual dependency injection - not recommended, requires us to manage the lifecycle of the dependencies
  - Method Injection -> fun drive(engine: Engine)
    - we know we want to drive, but will decide what engine to use each time


- when you have multiple onClick listeners you can add onClickListener to the class declaration
```kotlin
class CalculatorFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentCalculatorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalculatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set click listeners
        binding.button1.setOnClickListener(this)
        binding.button2.setOnClickListener(this)
        // ...
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button1 -> {
                // handle button 1 click
            }
            R.id.button2 -> {
                // handle button 2 click
            }
            // ...
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

- to handle not going back to login screen you can use popUpTo and popUpToInclusive in the navigation action
    - you want to popUpTo the bottom of the stack you're ok going back to
    - Here we do not want to go back to the login screen -> we want to exclude it from the back stack
      - popUpTo = R.id.PageAfterSuccessfulLogin
      - popUpToInclusive = false
        - setting this true will remove the destination specified in popUpTo from the back stack
        - since we are popping up to the page after login, we want to keep it in the back stack, keep it false


- calculator buttons can be generated from a ENUM + xml for base layout 
  - enum contains colors, symbols, ids
  - xml contains size, shape, position
  - parent xml contains the grid layout that will populate the buttons
- can use a RecyclerView with a GridLayoutManager to create the calculator buttons dynamically
- can use cardview for the buttons to get the shadow and ripple effect

- OnCreateView -> initialize views 
- onViewCreated -> business logic

- SharedViewModel - share data between fragments
  - essentially a singleton / global variable for the activity
  - can use to share data between fragments without using interfaces or callbacks
  - prefer to use a shared view model over interfaces or callbacks because it is easier to manage and test and is more scalable
- activityViewModels() - scope is the activity, shared between all fragments in the activity
- viewModels() - scope is the fragment, not shared between fragments

- User login auth you can use a sealed class to represent the different states of the login process
  - Success, Error
  - can use a when statement to handle the different states in the UI
  - can use LiveData or StateFlow to observe the state changes in the UI
```kotlin
sealed class LoginState {
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

// viewModel 
viewModel.loginState.observe(viewLifecycleOwner) { state ->
    when (state) {
        is LoginState.Success -> {
            // navigate to next screen
        }
        is LoginState.Error -> {
            // show error message
            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
        }
    }
}
```

- observe when LoginState changes in the ViewModel and update the UI accordingly
    