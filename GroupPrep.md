## Amazon Group Prep

### Interviews
R2 - Cherlan -> http://s3-storage-explorer.s3-website.ap-south-1.amazonaws.com/?region=ap-south-1&bucketlist=&bucket=storage-solution&county=auth&accesskey=AKIAXQREBX3WM2AAHXHV&secretkey=RG7ERERB%2FVqeNwPfyYXjpgfY2RjJaCUNXI6pvl%2BC
R2 - Xavier -> http://s3-storage-explorer.s3-website.ap-south-1.amazonaws.com/?region=ap-south-1&bucketlist=&bucket=storage-solution&county=auth&accesskey=AKIAXQREBX3WM2AAHXHV&secretkey=RG7ERERB%2FVqeNwPfyYXjpgfY2RjJaCUNXI6pvl%2BC

### Job Description
Client – Accenture/Amazon

Title - Android Developer

Location - Redmond, WA

Job Description/ Roles & Responsibilities

Role: Android Developer

Must have -

Android Mobile Application

Kotlin and Java

Java, C#, C++

Mission - Looking for a passionate and talented Android Software Development Engineer who has experience building world-class mobile apps. You will be responsible for developing mobile applications that interact seamlessly with our devices and delight our customers.

Key job responsibilities:
Design, develop and maintain our Android mobile applications
Work with team members to investigate design approaches, prototype technology and evaluate technical feasibility
Lead architecture and design of features, from conception to launch
Help to improve engineering processes and tools to increase team effectiveness
Be part of an agile development process to deliver high-quality software

Basic Qualification:
Experience as a mentor, tech lead or leading an engineering team
Experience leading the architecture and design (architecture, design patterns, reliability and scaling) of new and current systems
Experience in professional, non-internship software development
Experience programming with at least one modern language such as Java, C++, or C# including object-oriented design
Android mobile application development experience in Kotlin and Java

Preferred Qualification:
Bachelor's degree in computer science or equivalent
Experience with full software development life cycle, including coding standards, code reviews, source control management, build processes, testing, and operations
Proficient understanding of code versioning tools such as Git
Experience with Reactive programming frameworks
Experience optimizing apps for performance
Experience writing testable code and automated tests
Experience with deployments to the Play Store
Experience with REST architecture for web services


### Code Example:
```kotlin
interface MyStockApi {
// ??? should be replaced with the HTTP method annotation, the endpoint path,
// and the expected data type.

// Using a list of Stock data objects for a basic ticker.
@GET("stocks/current")
suspend fun getStocks(): List<StockData>
}

// Data class to represent a single stock item.
data class StockData(
    val symbol: String, // e.g., "AMZN"
    val price: Double,
    val change: Double,
    val changePercent: Double
)
// request should be GET
// Reason: Retrieving existing data (the current stock quotes) without modifying any state on the server is the fundamental purpose of a GET (idempotent, read-only) request in REST architecture.
```


### Potential Questions
1. you have an API for cart
- walk through network, domain, ui layers
- if someone press + on an item in the cart, how do you handle that?
- when they mention API -> means what endpoints you would use and what you would expect to get back
  - they are not defining it, we get to define it and build around it
- when they mention what tools would you use -> retrofit, okHttp, moshi/gson, coroutines/flow

- Optimistic loading is fine because we typically are using loading screens and shimmer placeholders if we didn't get the data
- use the retry logic

![img.png](img.png)
![img_1.png](img_1.png)

- can use something like exceladraw to draw the architecture