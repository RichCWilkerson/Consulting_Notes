# Task
- unit testing
- figure out what parts of your project can be unit tested without instrumentation tests
- grab a project with api, database or both -> write unit tests for it
- unit tests for:
  - repository
  - use cases
  - view model
  - API/DB should be mocked/faked
- submit new project to github

# Terry - Paypal



# Arth - Google Maps
- Google Maps API key setup
- Can modify map view (zoom, type, etc) 
- can modify default location (default is somewhere in the pacific ocean)
- user needs to accept location permission
- can turn on and off certain SDK features (e.g. 3D buildings, traffic layer, etc) as devs


# Pradyumn - Unit Testing
- Junit 4 for android (not Junit 5)
- Google Truth for assertions
- Robolectric for mocking android SDK
- Mockito for mocking classes
- Hilt test runner -> test 



## Junit
- AAA
    - Arrange -> setup test data and mock dependencies
      - @Before annotation for setup method
      - initialize mocks with MockitoAnnotations.openMocks(this)
      - hiltRule.inject() for Hilt
      - create test data (e.g. list of items, variables)
    - Act -> call the method under test
      - val result = classUnderTest.methodUnderTest(params)
      - any computations needed to get the result
    - Assert -> verify the result
      - assertThat and assertEqual are common assertions
- right click the class or method -> generate test -> select methods to test
    - ensure the project structure stays the same for test to source
- two naming conventions
    - methodNameStateUnderTestExpectedBehavior (camelCase)
    - `descriptive test name with spaces` (backticks)
      - backticks allow for spaces and special characters in method names
- clean up after yourself -> close mocks, clear database, etc
  - have a teardown method with @After annotation

## Annotations
- @Test -> marks a method as a test method
- @Before -> runs before each test method (like an initializer or setup method)
  - use @BeforeClass
- @After -> runs after each test method
- @BeforeClass -> runs once before all test methods (must be static)
- @AfterClass -> runs once after all test methods (must be static)
- @Ignore -> ignores a test method
- @RunWith -> specifies a test runner (e.g. MockitoJUnitRunner::class)
- @Mock -> creates a mock object
  - mock() -> whenever(methodCall()).returns(something)
  - whenever() -> mock the return value of a method call
  - returns() -> specify the return value of a mocked method call
  - cannot mock final classes or methods unless using inline mocking (mock-maker-inline)
    - use `doResponse(response).when(mock).methodCall()` syntax instead of `whenever()`
- @Stub -> creates a stub object (like a mock but with default behavior), but fake
  - 
- @InjectMocks -> creates an instance of the class and injects the mocks into it
- @Captor -> creates an argument captor
- @Spy -> creates a spy object
- @Rule -> creates a test rule (e.g. HiltAndroidRule(this))

```kotlin
@Before
fun setup() {
    // Arrange
    MockitoAnnotations.openMocks(this) // if you say you're going to mock something, you need to mock the Act part too
    hiltRule.inject()
    mockObject = mock() // this creates a mock and will grab all the methods in the class, need to stub only the methods you care about
}

@Test
fun `Mid Element Test`() {
    
    // Act -> findMidElement is an extension function we created for List<Int>
    // Act is the action we are testing
    val result = listOf<Int>(1, 2, 3, 4, 5).findMidElement()

    // Mockito.verify(mockObject).methodCall() -> verify that a method was called on a mock object
    // Mockito.verify(mockObject, times(1)).methodCall() -> verify that a method was called a specific number of times
    // Mockito.verifyNoMoreInteractions(mockObject) -> verify that no other methods were called on the mock object
    
    // below is us stubbing a method call on a mock object
    // whenever(mockObject.methodCall()).thenReturn(something) -> stub a method call on
    
    // Assert -> verify the result is as expected
    assertThat(result).isEqualTo(3)
}

@Test
fun `Spy Test`() {
    // use spy when you want to use the real object, but you want to stub some methods of it
    val list = listOf(1, 2, 3)
    val elementFinder = ElementFinder()
    val mock = spy(elementFinder) // spy on the real object, but we can stub methods if we want to
    assertThat(mock.findMidElement(list)).isEqualTo(2) // real method is called
    doReturn(3).`when`(mock).findMidElement(list) // stub the method to return 3 instead of 2
    assertThat(mock.findMidElement(list)).isEqualTo(3) // stubbed method is called
}
```