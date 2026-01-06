# Leetcode

[See also: Kotlin Collections & Data Structures Cheat Sheet](Kotlin_Collections_Cheatsheet.md)

## Arrays & Hashing

### Two Sum
```kotlin
fun twoSum(nums: IntArray, target: Int): IntArray {
    val seen = mutableMapOf<Int, Int>()
    for (i in nums.indices) {
        val complement = target - nums[i]
        
        // if complement exists in map, return indices, else return null
        val j = seen[complement]
        if (j != null) return intArrayOf(j, i)
        seen[nums[i]] = i
    }
    throw IllegalArgumentException("No two sum solution")
}
```

### Valid Anagram
```kotlin
fun isAnagram(s: String, t: String): Boolean {
    if (s.length != t.length) return false

    // letter, count
    val letters = mutableMapOf<Char, Int>()    
    for (l in s) {
        if (letters.containsKey(l)) letters[l] = letters[l]!! + 1
        // or letters[l] = letters.getOrDefault(l, 0) + 1
        // or letters[l] = (letters[l]?.plus 1) ?: 1
        else letters[l] = 1
    }    
    for (l in t) {
        val count = letters[l] ?: return false
        if (count == 1) letters.remove(l)
        else letters[l] = count - 1
    }
    return true
}
```

### Contains Duplicate
```kotlin
fun containsDuplicate(nums: IntArray): Boolean {
    val numbers = mutableSetOf<Int>()
    for (n in nums) {
        if (numbers.contains(n)) return true
        numbers.add(n)
    }
    return false
}
```

### Group Anagrams
```kotlin
fun groupAnagrams(strs: Array<String>): List<List<String>> {
    val groups = mutableMapOf<String, MutableList<String>>()
    for (word in strs) {
        // joinToString("") converts char array back to string
        // use sorted() over sort() since sort() sorts in place and returns Unit
        // sort in place means it modifies the original array, which we don't want to do here
        val key = word.toCharArray().sorted().joinToString("")
        // getOrPut returns the value for the given key if it exists, otherwise it puts the result of the lambda expression into the map and returns that value
        // mutableListOf() creates a new mutable list if the key doesn't exist
        val list = groups.getOrPut(key) { mutableListOf() }
        list.add(word)
    }
    return groups.values.toList()
}
```
