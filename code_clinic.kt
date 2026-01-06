/*
You are given two sources of notifications in an Android app:

Local notifications (Room DB)

Remote notifications (API)
The rules:

Merge both lists.

2.If a notification has the same id in both lists → keep the one with newer timestamp.

Sort the final list

First by priority (1 → high first) -> this is id

Then by timestamp (newest first)

6.Return the final sorted list.
 */

data class Product(
    val id: Int,
    val timestamp: Instant, // Instant does what?
    val name: String
)

fun main() {
    val api = listOf<Product>(
        // eventually do this
    )
    val room = listOf<Product>(
        // eventually do this
    )
    val currentState = sortApiRoomList(api, room)
}

fun sortApiRoomList(api: List<Product>, room: List<Product>): List<Product> {

    // list to be returned
    val finalList = mutableListOf<Product>() // empty list not needed

    // is it faster to sort by id here or run the loop? sorting still takes compute and time
    val sortedApi = api.sortedBy { it.id }
    val sortedRoom = room.sortedBy { it.id }

    // is it possible to have different ids? api has 1,2,3 and room 1,4,5?
    // if it doesn't exist in one it should be added to list still

    // since it's sorted we can use an index tracker to see the last time we were at an id
    // we looked at index 2, start from index 2 not 0
    var currentRoomIndex = 0 // dont think i need this.. idk

    // need to have a break to this loop when equal or apiP is less than roomP
    // already sorted
    // if roomP is less, then we need to continue iterating through it until they are equal
    // currently this will always run the whole inner loop, not sure how to avoid this except with keeping track of it's last value looked at

    outer@ sortedApi.forEach { apiP ->
        while (currentRoomIndex < sortedRoom.size) {
            val roomP = sortedRoom[currentRoomIndex]
            if (apiP.id < roomP.id) {
                finalList.add(apiP)
                continue@outer
            }
            // if same need to compare timestamp
            else if  (apiP.id == roomP.id) {
                // greater timestamp value means more current (most updated)
                if (apiP.timestamp >= roomP.timestamp) {
                    finalList.add(apiP)
                }
                // timestamp is more current for room
                else {
                    finalList.add(roomP)
                }
                currentRoomIndex++
                continue@outer
            }
            else { // (apiP.id > roomP.id)
                finalList.add(roomP)
                currentRoomIndex++
                // don't break out of inner loop,
            }
        }
        // add remaining apiP values
        finalList.add(apiP)
    }

    // add remaining roomP values
    while (currentRoomIndex < sortedRoom.size) {
        finalList.add(sortedRoom[currentRoomIndex])
        currentRoomIndex++
    }

    return finalList
}

/*
Task 1: Create Data Models

User
RemoteUser
FinalUser (id, name?, totalScore)
*/

// can any of these values be null??? or is it just that the user wont exist?
// local
data class User(
    val id: Int
    val name: String,
    val score: Int,
)

// remote
data class RemoteUser(
    @SerializedName("id")
    val id: Int
    @SerializedName("name") // treating this as unique
    val name: String,
    @SerializedName("score")
    val score: Int
)

data class FinalUser(
    val id: Int,
    val name: String, // not sure to put default here or in my function
    val totalScore: Int
)

val localUsers = listOf(
    User(id = 1, name = "Aisha", localScore = 40),
    User(id = 2, name = "Rahul", localScore = 10),
    User(id = 3, name = "Mei", localScore = 20),
    User(id = 4, name = "James", localScore = 80),
    )

val remoteUsers = listOf(
    RemoteUser(id = 1, name = "Aisha", localScore = 10),
    RemoteUser(id = 2, name = "Rahul", localScore = 40),
    RemoteUser(id = 3, name = "Mei", localScore = 40),
    RemoteUser(id = 5, name = "Adam", localScore = 60),
    )

/*
Task 2: Merge Local + Remote Data

Rules:
- If a user exists in both sources, totalScore = localScore + remoteScore
- If a user exists only in remote, include them (name = "Unknown").
- If a user exists only in local, totalScore = localScore.

Ignore entries with null/invalid data.
*/

fun mergeRules(localUsers: List<User>, remoteUsers: List<RemoteUser>) : List<FinalUser> {
// sort by id first
    val sortedLocal = localUsers.sortedBy { it.id }
    val sortedRemote = remoteUsers.sortedBy { it.id }

    val remoteIndex = 0
    val finalUsers = MutableList<FinalUser>()

    outer@ sortedLocal.forEach { local ->
        while (remoteIndex < sortedRemote.size) {
            val remote = sortedRemote[remoteIndex]
            if (local == remote) {
                // means id is found both places
                val combineFinalUser = FinalUser(
                    id = local.id,
                    name = local.name,
                    totalScore = local.score + remote.score
                )
                finalUsers.add(combineFinalUser)
                remoteIndex++ // consumed remote -> move forward in list
                continue@outer // consumed local -> break to outer foreach
            }
            else if (local < remote) {
                // means id is found only on local
                val localUser = FinalUser(
                    id = local.id,
                    name = local.name,
                    totalScore = local.score
                )
                finalUsers.add(localUser)
                continue@outer // consumed local -> break to outer foreach
            }
            // remote is greater than local
            else {
                // means id is found only on remote
                val remoteUser = FinalUser(
                    id = remote.id,
                    name = "Unknown",
                    totalScore = remote.score
                )
                finalUsers.add(remoteUser)
                remoteIndex++ // consumed remote -> move forward in list
            }
            // need to handle the end of the remote list (if remote list is longer)
            val remoteUser = FinalUser(
                id = remote.id,
                name = "Unknown",
                totalScore = remote.score
            )
            finalUsers.add(remoteUser)

        }
        // need to handle the end of the local list (if local list is longer)
        val localUser = FinalUser(
            id = local.id,
            name = local.name,
            totalScore = local.score
        )
        finalUsers.add(localUser)

    }
    return finalUsers
}


/*
Task 3: Sort and Return the Top 3 Users

Return a List<FinalUser> sorted by:
highest totalScore
if tie → alphabetical name

*/
fun sortTopThreeScorers(finalUsers: List<FinalUser>) : List<FinalUser> {
    // TODO: what if there are less than 3 users?? do we care? or just return up to 3?

    if (finalUsers.isEmpty()) return listOf<FinalUser>() // TODO: not sure if this is correct

    val sortedOnScore = finalUsers.sortedBy { it.totalScore } // TODO: is this ascending?

    val topScores = MutableList<FinalUser>()

    // need to stop adding once top 3 are determined -> while (topScores < 3 in size)

    // check for ties
    // this wont work for more than 2 users tying -> this is a bubble sort, but only one pass by
    // create a map of all users at a specific score?
    val previousUser = null

    // can set the value and iterate through the forEach to add those users to a list to then do the name sort

    outer@ sortedOnScore.forEach { user ->
        if (previousUser == null) {
            previousUser = user
            continue@outer
        }


        if (user.totalScore == previousUser.totalScore) {
            // TIE -> check name
            val names = listOf(
                user.name,
                previousUser.name
            )
            val sortedNames = names.sortedBy { it } // TODO: i believe this will be ascending alph
        }
        previousUser = user
    }
}


/*
Task 4 (Bonus, optional): Use Coroutines

Simulate:

fetchLocalUsers() → returns after 200ms

fetchRemoteUsers() → returns after 400ms
Use async/await to run them concurrently.
 */



// ---------------------------------------------------

/*
Given a string S, return the first character that does not repeat anywhere else in the string.
input - "Hello World"
output - H
hHello - e
 */

fun firstNonRepeatChar(myString: String): Char {
    val myChars = mutableMap<Char, Int>()

    val value = myString.lowercase()

    value.forEach{ char ->
        myChars.getOrDefault(char, 0) + 1
    }


    value.forEach { char ->
        if (myChars[char] == 1) return char
    }
    return ""
}

/*
You are given an unsorted list of integers (positive, negative, and duplicates allowed).
Your task is to find the length of the longest consecutive number chain.
input : [100, 4, 200, 1, 3, 2]
output - 4
[1,2,3,4] - 4
100 -1
200 -1

// WE can sort this?

 */

fun main() {

    var thisList = listOf(
        100, 4, 200, 1, 3, 2
    )
    var result = longestConsecNumbers(thisList)

    println(result)
}

fun longestConsecNumbers(numberList: List<Int>): Int {

    if (numberList.isEmpty()) return 0

    var sortedNum = numberList.sorted()

    var previousNum = sortedNum[0]
    var count = 0
    var maxCount = 0

    sortedNum.forEachIndexed { index, num ->
        if (index == 0) count++
        else if (num == previousNum + 1) {
            count++
            previousNum = num
        } else {
            count = 0
        }

        if (count > maxCount) maxCount = count

    }

    return maxCount
}


// -----------------
/*
You are given a string that may contain:

Letters (a–z, A–Z)

Digits (0–9)

Special characters (!@#$%^&*)

Multiple spaces

Your task

Reverse only alphabetic characters (a–z, A–Z) inside each word

Do NOT move digits or special characters

Preserve all spaces exactly as they appear

Words are separated by spaces
"a1b-c  De!f2" - input
"b1a-c  fe!D2"- output
 */


fun reverseCharsInWord(sent: String): String {

    // start adding chars to a map<char, index>
    // spaces stop this
    // add special chars to map with current index
    // add word to final return string (concat)

    // final string
    var returnString: String = ""

    //  index
    val specialChars = mutableMapOf<Int, Char>()

    // letters should be a stack ->
    val letters = mutableListOf<Char>() // i dont think i need this...

    // Is length “number of characters seen in this word so far”?
    // Or “last index of this word (0-based)”?
    var length = -1

    // need to handle last word when a space does not trigger. so an or based on index?
    sent.forEachIndexed { index, char ->
        if (char.isLetter()) {
            letters.add(char)
            length++
        } else if (char == ' ') {
            // handle reversing the word
            var newWord: String = "" // i don't need to clear this once it leaves scope? or does it need to be inside the while...

            while (length >= 0) {
                // if the map contains that index -> add the value for the char (symbol/num)
                if (specialChars.contains(length)) {
                    newWord = newWord + currentWord[length] // add the char at that position
                } else {
                    newWord = newWord + letters.removeLast() // get the last added or top of the stack of letters
                }
                length--
            }

            // this correctly handles a double space - newWord will be ""
            returnString = returnString + newWord + " "
            specialChars.clear() // need to set it to null?
            letters.clear() // set letters to null
        } else {
            // this should be any other special chars and numbers
            specialChars.put(index) = char
            length++
        }
    }
    // handle the last word here?
    if (!letters.isEmpty()) {
        // handle reversing the word
        vav newWord: String = "" // i don't need to clear this once it leaves scope? or does it need to be inside the while...

        while (length >= 0) {
            // if the map contains that index -> add the value for the char (symbol/num)
            if (specialChars.contains(length)) {
                newWord = newWord + "" // add the char at that position
            } else {
                newWord = newWord + letters.removeLast() // get the last added or top of the stack of letters
            }
            length--
        }

        returnString = returnString + newWord + " "
    }
}





// ---------------------------------------------------------------
/*
Given a list of integers, write a function that returns the top K most frequent numbers in the list.

If two numbers have the same frequency, return the smaller number first.
nums = [1,1,1,2,2,3,3,3,4]
k = 2
Frequency: 1 → 3 times, 3 → 3 times, 2 → 2 times, 4 → 1 time

Top 2 frequent: 1 and 3

Tie broken by smaller number
[1, 3]
 */


fun getTopKNumbers(nums: List<Int>, K: Int) : List<Int> {

    val frequencies = mutableMapOf<Int, Int>()

    nums.forEach { num ->
        frequencies[num] = frequencies.getOrDefault(num, 0) + 1
    }

    // frequencies.entries returns a Set<Map.Entry<Int, Int>> (key/value pairs).
    return frequencies.entries
        // sortedWith is different than sortedBy as it can take multiple sortings by using .thenBy. use .thenByDescending to sort the other direction
        // the order of sorting prioritizes the first sortings
        // here value is the primary sort -> then the key
        .sortedWith(compareByDescending<Map.Entry<Int, Int> { it.value }
            .thenBy { it.key }) // this will furthur sort any tied values by the key, by default the lower value
        .map { it.key } // converts the list of Map.Entry objects to a List<Int> of keys (you need this because the function should return the numbers, not entries). .map always creates a new list of the values passed (here it.key)
        .take(K) // returns up to K items from the start of the list. If K is larger than the list size you get fewer items; consider returning early for K <= 0 if you want to handle that case explicitly.
}



/*
You are given a string s consisting of lowercase English letters.

Your task is to rearrange the characters of the string according to the following rules:

Characters with lower frequency should appear first.

If two characters have the same frequency, the character that appears later in the alphabet should come first.

If there is still a tie, preserve the order of first appearance in the original string.

Each character must appear exactly the same number of times as in the input.

Return the final rearranged string.

s = "bananaapple"
b → 1
n → 2
a → 4
p → 2
l → 1
e → 1

lebppnnaaaa - output
 */


fun getCharFrequencyOrder(word: String): String {

    // step 1 add all chars to the map with frequency in word
    val charFrequency = mutableMapOf<Char, Int>()

    word.forEach { char ->
        charFrequency[char] = charFrequency.getOrDefault(char, 0) + 1
    }

    // step 2 sort the map with lowest frequency first, then by later char in alphabet if tied
    val sortedChars = charFrequency.entries // entries returns a map?
        .sortedWith(
            compareBy<Map.Entry<Char, Int>> { it.value } // frequency ascending
                .thenByDescending { it.key } // char descending
        )

    val stringBuilder = StringBuilder()
    sortedChars.forEach { entry ->
        val char = entry.key
        val value = entry.value
        repeat(value) { stringBuilder.append(char) }
    }
    return stringBuilder.toString()
}


/*
You are given a list of integers and a number k.

Write a function that returns the top k most frequent numbers in the list based on the rules below:

Numbers with higher frequency should come first.

If two numbers have the same frequency, the number with the smaller absolute value should come first.

If two numbers have the same frequency and the same absolute value (for example -3 and 3), return the negative number first.

The final result should be ordered according to the above rules.
nums = [1, 1, -1, -1, 2, 2, 2, 3, 3, -3, -3, -3, 4]
k = 3
[2, -3, -1] - output
 */

fun topKNumbers(nums: List<Int>, K: Int): List<Int> {

    // step 1: get frequencies of numbers
    val numberFrequency = mutableMapOf<Int, Int>()

    nums.forEach { num ->
        numberFrequency[num] = numberFrequency.getOrDefault(num, 0) + 1
    }

    // step 2: sort
    val sortedNums = numberFrequency.entries
        .sortedWith(
            compareByDescending<Map.Entry<Int, Int>> { it.value } // frequency descending
                .thenBy {
                    if (it.key < 0) it.key * -1
                    else it.key } // number ascending by absolute value... ensure it is a positive...
        )
        .map { it.key } // return list of keys
        .take(K) // only get the top K values

    return sortedNums
}


// ----------------------------------
/*
Calculate the total time spent in valid sessions for each user.

Return the result in a map or dictionary format.
You are given a list of application log entries.

Each log entry contains:

user ID

session ID

timestamp

event type (START or END)

A Session Is Valid Only If

It has a START event before an END event.

The session duration is at least 5 minutes.

The session does not overlap with another session of the same user.
Rules to Handle

Log entries may be out of order.

Some sessions may have:

Missing END

END before START

Duration less than 5 minutes

Ignore all invalid sessions completely.
[
  ("u1", "s1", 1, "START"),
  ("u1", "s1", 10, "END"),
  ("u1", "s2", 8, "START"),
  ("u1", "s2", 20, "END"),
  ("u2", "s3", 3, "START"),
  ("u2", "s3", 6, "END")
]
output - {
  "u1": 9,
  "u2": 0
}
 */

enum class EventType(){
    START(),
    END()
}

data class UserSession(
    val userId: String?,
    val sessionId: String?,
    val timestamp: Int?,
    val eventType: EventType?
)

data class ValidSession(
    val userId: String?,
    val sessionId: String?
    val totalTime: Int?,
    val startTime: Int?,
    val endTime: Int?,
)

fun calcTotalTimeSpent(sessions: List<UserSession>): Map<String, Int> {
    // returns userId and time for valid sessions
    // valid session:
        // START comes before END
        // always has START, might miss END, only valid if both
        // timestamp difference between START and END of at least 5 mins. >=
        // user sessions cannot overlap for the same user

    // create a map of all userIds as the key and valid time as the value
    val validUserSessionTime = mutableMapOf<String, Int>()


    val sortedSessionsBySession = sessions.sortedBy { it.sessionId }

    val allSessions = mutableListOf<ValidSession>()

    var previousSession = UserSession()
    var currentSession = ValidSession()

    sortedSessionsBySession.forEach { session ->
        // check if we need to move to next sessionId (ordered)
        if (session.sessionId != previousSession.sessionId) {
            if (currentSession.endTime != null)

            previousSession = session
            continue // move to next session
        }



    }





    return validUserSessionTime
}


// -----------------------------------------------------------------
/*
ou are given a list of tasks in a system.
Each task may depend on one or more other tasks being completed first.

Your goal is to find a valid order to complete all tasks while respecting these dependencies.

If no valid order exists, return "Invalid".
input - [
  ("A", "C"),
  ("B", "C"),
  ("C", "D"),
  ("D", "E")
]
["A", "B", "C", "D", "E"] - output
example - Wash → Cut

THIS is just an example - not an input
Cut → Cook
Cook → Serve
Wash → Cut → Cook → Serve
invalid output example - Wash → Cut
Cut → Cook
Cook → Wash


invalid input - A → B
B → C
C → A
B - A - invalid
A,B,C,D.......
B,D,C,A - INVALID

A must come before C

B must come before C

C must come before D

D must come before E
 */
val order: List<Pair<String, String>> = listOf(
    "A" to "C",
    "B" to "C",
    "C" to "D",
    "D" to "E"
)
val testCaseWithViolation = listOf(
    "C" to "A", // will cause: "A must come before C"
    "B" to "C",
    "D" to "B",
    "A" to "C"
)



// or an array of pairs
//val orderArray: Array<Pair<String, String>> = arrayOf(
//    Pair("A", "C"),
//    Pair("B", "C"),
//    Pair("C", "D"),
//    Pair("D", "E")
//)


fun validOrder(order: List<Pair<String, String>>): List<String> {


    val finalSet = mutableSetOf<String>()
    var isValid = true

    // can use (from/to), pair with .first/.second, or pair with .component1()/.component2()
    order.forEach { (from, to) ->
        // larger number means further in alphabet
        if (from > to) {
            println("${to} must come before ${from}")
            isValid = false
        } else {
            // keep track of valid with a set? sets maintain order?
            finalSet.add(from)
            finalSet.add(to)
        }
    }

    // sorted by default is a -> z, use sortedBy if you need more control
    if (isValid) return finalSet.toList().sorted()
    else return "Invalid"
}