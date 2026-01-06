# Common JavaScript array methods (for React Native devs)

Arrays in JS are like `MutableList` in Kotlin, but used **everywhere** in React.

The most important higher-order methods are: `forEach`, `map`, `filter`, `find`, and `reduce`.

> Higher-order function = a function that takes a function as an argument or returns a function.

---
## forEach — do something for each element (no return)

```js
const nums = [1, 2, 3];

nums.forEach((n) => {
  console.log(n);
});
// 1
// 2
// 3
```

Use when you want side effects (logging, updating something), not new arrays.

---
## map — transform each element (returns new array)

```js
const nums = [1, 2, 3];
const doubled = nums.map((n) => n * 2);

console.log(doubled); // [2, 4, 6]
```

- Very common when mapping API data into UI models.

---
## filter — keep elements that match a condition

```js
const nums = [1, 2, 3, 4, 5];
const evens = nums.filter((n) => n % 2 === 0);

console.log(evens); // [2, 4]
```

- Great for building subsets: visible items, completed tasks, etc.

---
## find — return the **FIRST** matching element (or undefined)

```js
const users = [
  { id: 1, name: "Alice" },
  { id: 2, name: "Bob" },
];

const user = users.find((u) => u.id === 2);
console.log(user); // { id: 2, name: "Bob" }
```

---
## some and every

```js
const nums = [1, 2, 3, 4];

console.log(nums.some((n) => n > 3));  // true (at least one)
console.log(nums.every((n) => n > 0)); // true (all)
```

---
## reduce — combine elements into a single value

```js
const nums = [1, 2, 3, 4];

// accumulator `acc` is what is being built up
// `n` is the current element being processed
// 0 is the initial value of the accumulator
const sum = nums.reduce((acc, n) => acc + n, 0);
console.log(sum); // 10
```

- Think of `reduce` as folding the array into one result: sum, product, object, map, etc.

---
## Adding/removing items (mutable)

```js
const arr = [1, 2, 3];

// push adds to end
arr.push(4);      // [1, 2, 3, 4]
// pop removes from end
arr.pop();        // [1, 2, 3]

// unshift adds to start
arr.unshift(0);   // [0, 1, 2, 3]
// shift removes from start
arr.shift();      // [1, 2, 3]
```

These mutate the original array. In React, we **prefer immutable updates**:
- original array stays unchanged
- create a new array with the changes

```js
const arr = [1, 2, 3];

const added = [...arr, 4];    // [1, 2, 3, 4]
const removed = arr.filter(n => n !== 2); // [1, 3]
const getBeginning = arr.slice(0, 2); // [1, 2]
```

---
## Destructuring arrays

```js
const tuple = [100, 200];
const [top, bottom] = tuple;

console.log(top, bottom); // 100 200
```

- Useful in hooks:

```js
const [count, setCount] = useState(0);
```

---
## Sorting

```js
const nums = [3, 1, 10, 2];

nums.sort();
console.log(nums); // [1, 10, 2, 3] — string comparison by default

nums.sort((a, b) => a - b);
console.log(nums); // [1, 2, 3, 10] — numeric sort
```

- `sort` mutates the array; clone first if needed:

```js
const sorted = [...nums].sort((a, b) => a - b);
```

---
As a React Native dev, practice:
- `map` + `filter` for rendering lists and derived state.
- `find` / `some` / `every` for conditions.
- Immutable updates with `...` and non-mutating methods instead of `push`/`splice` in state.
