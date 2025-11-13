# Task
- only focus on database implementation - add DI if you have time
- create a todo app that allows users to add, view, update, and delete tasks

# Databases
- Android uses SQLite as its default database engine.
- SQLite is a lightweight, embedded relational database management system that is included with Android.


## Concepts
- Tables: Data is organized into tables, which consist of rows and columns. Each table has a unique name and contains related data.
- Rows: Each row in a table represents a single record or entry.
- Columns: Each column in a table represents a specific attribute or field of the data.
- Primary Key: A unique identifier for each row in a table. It ensures that each record can be uniquely identified.
- Foreign Key: A field in one table that refers to the primary key of another table, establishing a relationship between the two tables.
- Secondary Key: An index that improves the speed of data retrieval operations on a table.

- NoSQL - each entry/document contains all the data for that entry 
  - use case -> you made an order -> all data about that order is stored in one document
  - less expensive to store information that we don't need to query a non-relational way
  - typically only indexed on primary key and you find it to display the data

### CRUD
- Create: Inserting new records into a table using the INSERT statement.
- Read: Retrieving data from a table using the SELECT statement.
- Update: Modifying existing records in a table using the UPDATE statement.
- Delete: Removing records from a table using the DELETE statement.


# Room
- Room is a persistence library that provides an abstraction layer over SQLite to allow fluent database access while harnessing the full power of SQLite.
- It simplifies database management by reducing boilerplate code and providing compile-time checks of SQL queries.

- Implementation (Gradle Import)
  - Gradle import for Room and KAPT compiler
    - implementation("androidx.room:room-runtime:2.5.2")

- Annotations
  - @Entity: Marks a (data) class as a database entity (table). Schema for a table.
  - @PrimaryKey: Specifies the primary key of an entity.
  - @ColumnInfo: Customizes the column name in the database.
  - @Dao: Marks an interface or abstract class as a Data Access Object (DAO).
    - entry point to access the database operations, actions, and modifications
    - all CRUD operations are defined here
  - @Insert, @Update, @Delete: Annotate methods in a DAO for inserting, updating, and deleting records.
  - @Query: Annotates methods in a DAO to define SQL queries for data retrieval.
  - @Database: Marks a class as a Room database and defines the entities and version.
    - merges the entities (tables) and the DAOs (data access objects) together
    - define DB name, DAOs used, Entities used, version, converter, migration strategy
      - version is for keeping track of schema changes (migrations)
      - converter -> handling complex objects, DB can only store primitive types
      - migration strategy -> what to do when schema changes
        - fallbackToDestructiveMigration() -> wipes the DB and starts fresh (data loss)
        - allowMainThreadQueries() -> allows DB operations on main thread (not recommended for production)
  - @TypeConverter: Annotates methods that convert custom types to and from types that Room can store in the database.

- Query Key Words
  - IN: Used in a WHERE clause to specify multiple values for a column.
  - IS: Used in a WHERE clause to check for NULL values.
  - LIKE: Used in a WHERE clause for pattern matching.
  - AND, OR: Logical operators used in WHERE clauses to combine multiple conditions.
  - ORDER BY: Sorts the result set of a query by one or more columns.
  - GROUP BY: Groups rows that have the same values in specified columns into summary rows.
  - HAVING: Used to filter groups created by the GROUP BY clause.
  - SELECT: Retrieves data from one or more tables.
  - FROM: Specifies the table(s) from which to retrieve data.
  - WHERE: Filters records based on specified conditions.
  - INSERT INTO: Adds new records to a table.
  - LIKE: Used in a WHERE clause for pattern matching.
  - UPDATE: Modifies existing records in a table.
  - NOT EXHAUSTIVE LIST

### Setup Room
1. Add Room dependencies to your build.gradle file.
- BASE: `implementation("androidx.room:room-runtime:2.5.2")`
- KAPT COMPILER: `kapt("androidx.room:room-compiler:2.5.2")`
  - add to plugins (module and project level)
  - plugin needs to match kotlin version (libs.versions.toml)
- he went through creating libs.versions.toml file to manage versions
2. add /data/local to file structure
3. create Entity (data class with @Entity annotation) - Person
- @PrimaryKey for unique identifier
- @ColumnInfo for custom column names
- notice we are adding these as constructor parameters
```kotlin
// PersonEntity.kt
@Entity(tableName = "person_table")
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "first_name") val firstName: String,
    @ColumnInfo(name = "last_name") val lastName: String
)
```
4. create DAO (interface with @Dao annotation) - PersonDao
- define CRUD operations here
- use @Insert, @Update, @Delete for respective operations
- use @Query for custom SQL queries
- can define "if" statements for conflict resolution inside annotations
  - ex: @Insert(onConflict = OnConflictStrategy.REPLACE)
- NOTICE: using interface and suspend functions for coroutines
```kotlin
// PersonDao.kt
@Dao
interface PersonDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPerson(person: Person)
    @Update
    suspend fun updatePerson(person: Person)
    @Delete
    suspend fun deletePerson(person: Person)
    // the person_table is defined by the @Entity annotation in PersonEntity.kt
    // this is true for property names as well
    @Query("SELECT * FROM person_table")
    fun getAllPersons(): LiveData<List<PersonEntity>>
    
    // we use ":" to reference method parameters in the query
    // LiveData is being used to observe data changes
        // so when the query results come back, the UI can automatically update
    // id comes from the PersonEntity primary key name "id"
    // :id comes from the method parameter name "id"
    @Query("SELECT * FROM person_table WHERE id = :id")
    fun getPersonById(id: Int): LiveData<PersonEntity?>
}
```
5. create Database (abstract class with @Database annotation) - PersonDatabase
- define entities (tables) and DAOs (data access objects) - required
- define version (for migrations) - required
- define type converters - optional
- define migration strategy - optional
- NOTICE: using abstract class and abstract method for DAO
  - TODO: why abstract?
- NOTICE: entities takes an array of entities (tables)
```kotlin
// PersonDatabase.kt
@Database(entities = [PersonEntity::class], version = 1, exportSchema = false)
abstract class PersonDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    
    // everything below is OPTIONALLY in this class - singleton pattern (companion object) for DB instance
    // it needs to be instantiated, but it can be in its own file if preferred
    
    companion object {
        // Volatile ensures that the instance is always up-to-date and the same to all execution threads
        // it means that changes made by one thread to INSTANCE are visible to all other threads immediately
        // without volatile, it's possible that one thread could see a stale or cached version of INSTANCE
        // this is important for singleton pattern to prevent multiple instances being created in a multi-threaded
        // ACID compliance - Atomicity, Consistency, Isolation, Durability
        @Volatile
        private var INSTANCE: PersonDatabase? = null

        fun getDatabase(context: Context): PersonDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    // applicationContext is used to avoid leaking an Activity or BroadcastReceiver
                    context.applicationContext,
                    PersonDatabase::class.java,
                    // name of the database file on the device where SQLite stores the data
                    "person_database"
                )
                .fallbackToDestructiveMigration() // handle migrations by wiping and starting fresh
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```
6. Database instance (Adams way)
- typically a singleton pattern to ensure only one instance of the database is created
- use a companion object with a volatile INSTANCE variable
```kotlin
// DBInstance.kt

object DBInstance {
    lateinit var appContext: Context
    
    // make the database instance accessible
    fun getRoomDB(): PersonDatabase {
        
        // using the PersonDatabase? lets us set a default, but not setting it every time it is called
        // it's logic: if roomInstance is null, set roomInstance = null 
        var roomInstance: PersonDatabase? = null
        
        // ensures the database is only created once 
        if (roomInstance == null) {
            roomInstance = Room.databaseBuilder(
                // these three parameters are required
                // appContext is needed to avoid leaking an Activity or BroadcastReceiver as a Singleton
                appContext,
                // the database class created above
                PersonDatabase::class.java,
                // name of the database file on the device where SQLite stores the data
                "person_database"
            ).build()
        }

        return database
    }
}

```
7. Fragment/Activity usage
- get the database instance
```kotlin
// need this to use viewModel scope for coroutines 
private lateinit var personViewModel: PersonViewModel

// ... other code ...

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    
    binding.apply {
        btnSubmit.setOnClickListener {
            // should use viewModelScope.launch when using ViewModel
            // should use lifecycleScope.launch when using Fragment/Activity
            personViewModel.addPerson(
                PersonEntity(
                    firstName = etFirstName.text.toString(),
                    lastName = etLastName.text.toString(),
                    email = etEmail.text.toString()
                )
            )
            clearInputFields()
        }
    }
}
private fun clearInputFields() {
    binding.apply {
        etFirstName.text.clear()
        etLastName.text.clear()
        etEmail.text.clear()
    }
}
// PersonViewModel.kt
class PersonViewModel: ViewModel() {

    fun addPerson(person: PersonEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            DBInstance.getRoomDB().personDao().insertPerson(person)
        }
    }
}

```

--- might not need this ---

7. Start DB in Application class
- create /app package
- create PersonApplication class that extends Application
- define a lazy-initialized database instance
- define a lazy-initialized DAO instance
- register PersonApplication in AndroidManifest.xml
```kotlin
// PersonApplication.kt
class PersonApplication : Application() {
    val database: PersonDatabase by lazy { PersonDatabase.getDatabase(this) }
    val personDao: PersonDao by lazy { database.personDao() }
}
```



# IMPORTANT NOTES: 
- if you block the main thread for 5 seconds the app will ANR (Application Not Responding) and crash the app
- to avoid blocking the main thread, use coroutines (suspend functions) or AsyncTask


- tool -> app inspection -> databases allows you to view the database and its contents while the app is running
- tool -> device explorer allows you to view the file structure of the device/emulator
  - can pull the database file from here to view in a SQLite viewer 
  - emulator has root access (if you attach your own device)
  - go to your build.gradle.kts file and check your android.namespace
    - this will help you find the database file in the device explorer
    - database file is located in /data/data/your.package.name/databases/your_database_name

