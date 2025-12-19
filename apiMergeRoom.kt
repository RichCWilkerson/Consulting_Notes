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







