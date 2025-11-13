# Kotlin Collections & Data Structures – LeetCode Cheat Sheet

## Quick pick: which data structure?
- Array/IntArray: indexed access O(1), fixed size, primitives (IntArray) avoid boxing.
- `MutableList<T>`: dynamic array list, add/remove at end O(1) amortized; random access O(1).
- `MutableSet<T>` (`HashSet`): membership test O(1) average; no duplicates.
- `MutableMap<K,V>` (`HashMap`): key -> value lookup O(1) average.
- `ArrayDeque<T>`: efficient queue/stack (add/remove at both ends O(1)). Prefer over LinkedList.
- `PriorityQueue<T>` (`java.util.PriorityQueue`): heap; top-k, Dijkstra, etc. Add/poll O(log n).
- `TreeSet`/`TreeMap`: ordered set/map; lookup O(log n).


## Map helpers you’ll actually use

### getOrPut
```kotlin
val groups = mutableMapOf<String, MutableList<String>>()
for (word in listOf("eat", "tea", "tan", "ate", "nat", "bat")) {
    // use sorted() over sort() since sort() sorts in place and returns Unit
    // sort in place means it modifies the original array, which we don't want to do here
    val key = word.toCharArray().sorted().joinToString("")

    // getOrPut returns the existing list for this key if present;
    // otherwise it creates one via the lambda, puts it into the map, and returns it
    val bucket = groups.getOrPut(key) { mutableListOf() }
    bucket.add(word)
}
// groups.values is List<List<String>> of anagram groups
```

### putIfAbsent
```kotlin
val map = mutableMapOf<String, Int>()
// putIfAbsent inserts only if the key is not present; returns previous value or null
map.putIfAbsent("x", 1) // map["x"] becomes 1
map.putIfAbsent("x", 2) // ignored because "x" already exists
```

### getOrElse vs getValue vs getOrDefault
```kotlin
val counts = mutableMapOf<Char, Int>()
val c: Char = 'a'

// getOrElse doesn't modify the map; provides a computed fallback value
val next1 = counts.getOrElse(c) { 0 } + 1 // returns 1 for missing keys

// getValue throws if key missing unless map has a default (see withDefault below)
// counts.getValue(c) // would throw NoSuchElementException if 'c' missing

// getOrDefault returns the value if present, otherwise the provided default
val next2 = counts.getOrDefault(c, 0) + 1
```

### withDefault (safer getValue)
```kotlin
val base = mutableMapOf<Char, Int>()
val counts = base.withDefault { 0 } // only affects getValue/lookups, not iteration contents
val c = 'z'
val next = counts.getValue(c) + 1 // returns 1 instead of throwing
// Note: counts[c] is still null; the default is applied on getValue/contains-like access.
```


## Frequency counting patterns

### Characters in a string
```kotlin
fun freq(s: String): Map<Char, Int> {
    val f = mutableMapOf<Char, Int>()
    for (ch in s) {
        // increment existing value or start at 1
        f[ch] = f[ch]?.plus(1) ?: 1
    }
    return f
}
```

### Integers in an array (and decrement/remove)
```kotlin
fun isAnagram(s: String, t: String): Boolean {
    if (s.length != t.length) return false
    val m = mutableMapOf<Char, Int>()
    for (c in s) m[c] = (m[c] ?: 0) + 1
    for (c in t) {
        val cnt = m[c] ?: return false
        if (cnt == 1) m.remove(c) else m[c] = cnt - 1
    }
    return true
}
```

### Kotlin groupingBy + eachCount (one-liner counts)
```kotlin
val counts: Map<Char, Int> = "banana".groupingBy { it }.eachCount()
```


## Sets for membership and de-dup
```kotlin
fun containsDuplicate(nums: IntArray): Boolean {
    val seen = HashSet<Int>()
    for (n in nums) {
        // O(1) average contains check
        if (!seen.add(n)) return true // add returns false if n already present
    }
    return false
}
```

- HashSet: fastest membership; no order.
- LinkedHashSet: preserves insertion order.
- TreeSet: keeps elements sorted (O(log n) ops).


## Sorting tips
```kotlin
val word = "bca"
// sorted(): returns a new List<Char> leaving original intact
val key1 = word.toCharArray().sorted().joinToString("") // "abc"

// sort(): sorts in place and returns Unit; use only when you intend to mutate
val chars = word.toCharArray()
chars.sort() // modifies chars directly
val key2 = String(chars) // "abc"
```

- Use sorted/sortedBy when you need an immutable sorted view.
- Use sort/sortBy when you own the mutable collection and want in-place speed.


## Stack and Queue with ArrayDeque
```kotlin
val stack = ArrayDeque<Int>()
// Stack (LIFO)
stack.addLast(1) // push
stack.addLast(2)
val top = stack.removeLast() // pop -> 2

val queue = ArrayDeque<Int>()
// Queue (FIFO)
queue.addLast(1) // enqueue
queue.addLast(2)
val head = queue.removeFirst() // dequeue -> 1
```

- ArrayDeque is preferred over LinkedList for both stacks and queues in Kotlin.


## PriorityQueue (heap)
```kotlin
// Min-heap (default)
val minHeap = PriorityQueue<Int>()
minHeap.add(3); minHeap.add(1); minHeap.add(2)
val smallest = minHeap.poll() // 1

// Max-heap via custom comparator
val maxHeap = PriorityQueue<Int>(compareByDescending { it })
maxHeap.add(3); maxHeap.add(1); maxHeap.add(2)
val largest = maxHeap.poll() // 3

// Example: keep k largest elements with a min-heap of size k
fun topK(nums: IntArray, k: Int): List<Int> {
    val heap = PriorityQueue<Int>() // smallest on top
    for (n in nums) {
        heap.add(n)
        if (heap.size > k) heap.poll() // remove smallest to keep only k largest
    }
    return heap.toList()
}
```


## Classic map pattern: Two Sum
```kotlin
fun twoSum(nums: IntArray, target: Int): IntArray {
    val seen = mutableMapOf<Int, Int>() // value -> index
    for (i in nums.indices) {
        val need = target - nums[i]
        val j = seen[need]
        if (j != null) return intArrayOf(j, i)
        seen[nums[i]] = i
    }
    throw IllegalArgumentException("No two sum solution")
}
```


## Grouping values by a computed key
```kotlin
fun groupAnagrams(strs: Array<String>): List<List<String>> {
    val groups = mutableMapOf<String, MutableList<String>>()
    for (word in strs) {
        // joinToString("") converts char list back to a String key
        // use sorted() over sort() since sort() sorts in place and returns Unit
        // sort in place means it modifies the original array, which we don't want to do here
        val key = word.toCharArray().sorted().joinToString("")
        groups.getOrPut(key) { mutableListOf() }.add(word)
    }
    return groups.values.toList()
}
```


## Arrays vs Lists (performance notes)
- Prefer primitive arrays (IntArray, CharArray) in tight loops to avoid boxing.
- Prefer `MutableList<T>` when you need dynamic resizing and generics; random access is O(1).
- Conversions:
```kotlin
val arr = intArrayOf(1, 2, 3)
val list = arr.toMutableList() // boxes Ints
val arr2 = list.toIntArray()   // unboxes back to primitives
```


## String building
```kotlin
// Avoid repeated string concatenation in loops; use StringBuilder or joinToString
val sb = StringBuilder()
for (c in "abcd") sb.append(c)
val s = sb.toString()

val joined = listOf(1,2,3).joinToString(separator = ",", prefix = "[", postfix = "]")
```


## Handy extension tricks
```kotlin
// Safe increment without getOrDefault
val m = mutableMapOf<String, Int>()
val key = "k"
m[key] = m[key]?.plus(1) ?: 1

// Build-and-assign pattern
val byLen: Map<Int, List<String>> = listOf("a", "bb", "ccc").groupBy { it.length }

// In-place sort vs copy-sort on lists
val a = mutableListOf(3, 1, 2)
a.sort()                 // in place -> [1,2,3]
val b = a.sortedDescending() // new list, original 'a' stays sorted from previous step
```


## Big-O quick reference
- HashMap/HashSet: add/contains/remove O(1) average; worst O(n) when heavily collided.
- TreeMap/TreeSet: O(log n) for add/contains/remove; keeps sorted order.
- ArrayDeque: add/remove first/last O(1).
- PriorityQueue: add/poll/peek O(log n) / O(1) for peek.
- MutableList: index access O(1); add/remove at end O(1) amortized.


## When to pick what – quick flowchart

- Do you need key -> value lookups?
  - Yes:
    - Need sorted keys or range queries (floor/ceiling, first/last)?
      - Use `TreeMap<K, V>`
      - Example: val tm = java.util.TreeMap<Int, String>()
    - Need insertion-order iteration or simple LRU?
      - Use `LinkedHashMap<K, V>` (accessOrder=true for LRU)
      - Example: val lhm = LinkedHashMap<Int, Int>(16, 0.75f, true)
    - Otherwise:
      - Use `HashMap<K, V>`
      - Example: val hm = HashMap<Int, String>()
  - No:
    - Do you need membership tests / de-dup?
      - Need sorted order or predecessor/successor (floor/ceiling)?
        - Use `TreeSet<T>`
        - Example: val ts = java.util.TreeSet<Int>()
      - Need insertion-order iteration?
        - Use `LinkedHashSet<T>`
        - Example: val lhs = LinkedHashSet<Int>()
      - Otherwise:
        - Use `HashSet<T>`
        - Example: val hs = HashSet<Int>()

- Do you need to always get/remove the smallest/largest element or keep top-k?
  - Use `PriorityQueue<T>`
  - Examples:
    - Min-heap: val pq = java.util.PriorityQueue<Int>()
    - Max-heap: val maxPQ = java.util.PriorityQueue<Int>(compareByDescending { it })

- Do you need a queue or a stack?
  - Use `ArrayDeque<T>`
  - Examples:
    - Queue (FIFO): val q = ArrayDeque<Int>() // addLast/removeFirst
    - Stack (LIFO): val st = ArrayDeque<Int>() // addLast/removeLast

- Do you need counts/frequencies?
  - Small fixed alphabet/domain: IntArray/LongArray
    - Example: val cnt = IntArray(26)
  - General keys: Map<K, Int>
    - Example: val freq = HashMap<String, Int>()

- Do you need an ordered multiset (elements with counts and min/max by order)?
  - Use `TreeMap<T, Int>`
  - Example: val ms = java.util.TreeMap<Int, Int>()

- Do you need random access with dynamic resizing?
  - Use `MutableList<T>` (ArrayList)
  - Example: val list = mutableListOf<Int>()


## Practical tips & idioms for LeetCode

- Prefer map.getOrPut or map.withDefault + getValue over manual null checks
```kotlin
// Instead of: if (map[key] == null) map[key] = mutableListOf(); map[key]!!.add(x)
val bucket = map.getOrPut(key) { mutableListOf() }
bucket.add(x)
```

- Use IntArray for fixed-alphabet frequency counts (faster than Map)
```kotlin
val cnt = IntArray(26)
for (c in s) cnt[c - 'a']++
```

- When decrementing counts, remove zeroes to keep maps slim
```kotlin
val new = counts[k]!! - 1
if (new == 0) counts.remove(k) else counts[k] = new
```

- Sliding window template with counts
```kotlin
var left = 0
val need = IntArray(128)
var missing = 0 // track how many we still need; set appropriately
for (right in s.indices) {
    // expand window using s[right]
    // update counts and the missing counter here
    while (false /* replace with your shrink condition, e.g., window is invalid */) {
        // shrink from left; also update counts/missing accordingly
        left++
    }
}
```

- BFS/DFS stack-queue using ArrayDeque
```kotlin
val q = ArrayDeque<Int>() // queue
q.addLast(start)
while (q.isNotEmpty()) {
    val u = q.removeFirst()
    // process node u here
}

val stack = ArrayDeque<Int>() // stack
stack.addLast(x)
val top = stack.removeLast()
```

- PriorityQueue: min vs max, and fixed-size heap for kth-largest
```kotlin
val minPQ = PriorityQueue<Int>()
val maxPQ = PriorityQueue<Int>(compareByDescending { it })

val pq = PriorityQueue<Int>() // keep k largest
for (n in nums) {
    pq.add(n)
    if (pq.size > k) pq.poll()
}
```

- Deduplicate while preserving order
```kotlin
val orderedUnique: List<Int> = LinkedHashSet(nums.asList()).toList()
```

- Ordered range queries with TreeSet/TreeMap (floor/ceiling)
```kotlin
val ts = java.util.TreeSet<Int>()
val floor = ts.floor(x)   // greatest <= x
val ceil = ts.ceiling(x)  // smallest >= x
```

- LRU in one class with LinkedHashMap(accessOrder = true)
```kotlin
class LRUCache(private val capacity: Int) : LinkedHashMap<Int, Int>(16, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, Int>?) = size > capacity
}
```

- Keying 2D coordinates efficiently (avoid Pair overhead in hot paths)
```kotlin
fun key(x: Int, y: Int): Long = (x.toLong() shl 32) or (y.toLong() and 0xFFFF_FFFFL)
// Or use data class/Pair for readability when perf is not critical
```

- Use BooleanArray/Array<BooleanArray> for visited instead of HashSet when bounds are known
```kotlin
val visited = Array(rows) { BooleanArray(cols) }
```

- Bucket sort pattern for top-k frequent
```kotlin
val freq = mutableMapOf<Int, Int>()
for (n in nums) freq[n] = (freq[n] ?: 0) + 1
val buckets = Array(nums.size + 1) { mutableListOf<Int>() }
for ((num, f) in freq) buckets[f].add(num)
val ans = mutableListOf<Int>()
for (f in buckets.indices.reversed()) {
    for (num in buckets[f]) {
        ans.add(num)
        if (ans.size == k) break
    }
    if (ans.size == k) break
}
```

- Reuse StringBuilder for backtracking/DFS path construction
```kotlin
val path = StringBuilder()
fun dfs(i: Int) {
    // path.append(someCharOrString)
    // call dfs recursively as needed
    // path.deleteCharAt(path.lastIndex)
}
```

- Graph adjacency list with arrays for 0..n-1 nodes
```kotlin
val adj = Array(n) { mutableListOf<Int>() }
```

- Dijkstra stale-entry check when using PriorityQueue
```kotlin
val dist = IntArray(n) { Int.MAX_VALUE }
val pq = PriorityQueue(compareBy<Pair<Int, Int>> { it.first }) // (dist, node)
while (pq.isNotEmpty()) {
    val (d, u) = pq.poll()
    if (d > dist[u]) continue // stale entry
    // relax neighbors
}
```

- Union-Find (Disjoint Set Union) skeleton
```kotlin
class DSU(n: Int) {
    private val parent = IntArray(n) { it }
    private val rank = IntArray(n)
    fun find(x: Int): Int {
        if (parent[x] != x) parent[x] = find(parent[x])
        return parent[x]
    }
    fun union(a: Int, b: Int): Boolean {
        var x = find(a); var y = find(b)
        if (x == y) return false
        when {
            rank[x] < rank[y] -> parent[x] = y
            rank[x] > rank[y] -> parent[y] = x
            else -> { parent[y] = x; rank[x]++ }
        }
        return true
    }
}
```

- Bitmask for small alphabets (presence/uniqueness checks)
```kotlin
var mask = 0
for (c in word) mask = mask or (1 shl (c - 'a'))
```

- Built-in binary search helpers on arrays/lists
```kotlin
val idx = nums.binarySearch(target) // IntArray extension; >= 0 if found
```

- Ordered multiset with TreeMap (counted set)
```kotlin
val ms = java.util.TreeMap<Int, Int>()
fun add(x: Int) { ms[x] = (ms[x] ?: 0) + 1 }
fun remove(x: Int) { val c = ms[x]!!; if (c == 1) ms.remove(x) else ms[x] = c - 1 }
val min = ms.firstKey()
val max = ms.lastKey()
```


## Visual: When to pick what (flowchart)

Note: Requires a Markdown renderer with Mermaid support (e.g., GitHub, some IDE previews).

```mermaid
flowchart TD
    A[Start: Choose a data structure] --> B{Key -> Value lookups?}
    B -- Yes --> C{Need sorted keys or range queries?}
    C -- Yes --> C1[TreeMap<K, V>]
    C -- No --> C2{Need insertion-order or LRU?}
    C2 -- Yes --> C3[LinkedHashMap<K, V> (accessOrder=true for LRU)]
    C2 -- No --> C4[HashMap<K, V>]

    B -- No --> D{Membership / de-dup?}
    D -- Yes --> E{Need sorted order or floor/ceiling?}
    E -- Yes --> E1[TreeSet<T>]
    E -- No --> E2{Need insertion-order?}
    E2 -- Yes --> E3[LinkedHashSet<T>]
    E2 -- No --> E4[HashSet<T>]

    D -- No --> F{Always need min/max or top-k?}
    F -- Yes --> F1[PriorityQueue<T> (min or max comparator)]
    F -- No --> G{Queue or Stack?}
    G -- Queue --> G1[ArrayDeque<T> (addLast/removeFirst)]
    G -- Stack --> G2[ArrayDeque<T> (addLast/removeLast)]

    G --> H{Counts / frequency?}
    H -- Small fixed alphabet --> H1[IntArray/LongArray]
    H -- General keys --> H2[Map<K, Int>]
    H2 --> H2a[HashMap<K, Int>]
    H2 --> H2b[TreeMap<K, Int>]

    H --> I{Ordered multiset (counts + min/max by order)?}
    I -- Yes --> I1[TreeMap<T, Int>]
    I -- No --> J{Random access + dynamic size?}
    J -- Yes --> J1[MutableList<T>]
    J -- No --> K[Consider arrays or problem-specific structure]

    %% Legend
    subgraph Legend
      L1[Green = O(1) average]:::o1
      L2[Orange = O(log n)]:::olog
      L3[Gray = decision/mixed]:::neutral
    end

    %% Complexity classes
    classDef o1 fill:#dcfce7,stroke:#166534,stroke-width:1px,color:#052e16;
    classDef olog fill:#ffedd5,stroke:#9a3412,stroke-width:1px,color:#7c2d12;
    classDef neutral fill:#e5e7eb,stroke:#374151,stroke-width:1px,color:#111827;

    %% Assign classes
    class C1,E1,I1,F1,H2b olog
    class C4,C3,E4,G1,G2,J1,H1,H2a o1
    class B,C,C2,D,E,E2,F,G,H,H2,I,J,K,L1,L2,L3 neutral
```
