# Common JavaScript operators (for React Native devs)

As a Kotlin/Android dev, most of these will feel familiar, but JS has some quirks (like `==` vs `===`).

---
## Arithmetic operators

```js
const a = 10;
const b = 3;

console.log(a + b); // 13
console.log(a - b); // 7
console.log(a * b); // 30
console.log(a / b); // 3.333...
console.log(a % b); // 1 (remainder)
console.log(-10 % 3); // -1 (remainder with negative)
console.log(a ** b); // 1000 (exponentiation)
```

- Very similar to Kotlin.
- `%` is remainder, not modulo (negative numbers behave like in JS, not math modulo).

---
## Assignment operators

```js
let n = 5;
n += 2; // 7
n *= 3; // 21
n -= 1; // 20
n /= 4; // 5
n %= 2; // 1
```

---
## Comparison operators

```js
console.log(2 == "2");   // true  (loose equality, with coercion)
console.log(2 === "2");  // false (strict equality, no coercion)

console.log(3 > 2);   // true
console.log(3 >= 3);  // true
console.log(2 < 3);   // true
console.log(2 <= 2);  // true

console.log(null == undefined);  // true
console.log(null === undefined); // false
```

- **Rule of thumb:** always use `===` and `!==` in modern JS to avoid weird coercions.

---
## Logical operators

```js
console.log(true && false); // false
console.log(true || false); // true
console.log(!true);         // false
```

- `&&` and `||` return **values**, not just booleans:

```js
"hello" && 42;   // 42
null || "fallback"; // "fallback"
```

This is often used for default values.

---
## Short-circuiting (default values)

```js
const maybeUser = null;
const name = (maybeUser && maybeUser.name) || "Guest";
console.log(name); // "Guest"
```

- If `maybeUser` is `null`, the left side is `null`, so `||` returns "Guest".
- Common in older React code, though nowadays we prefer optional chaining and nullish coalescing.

---
## Nullish coalescing (`??`) and optional chaining (`?.`)

```js
const cfg = { theme: null };
console.log(cfg.theme ?? "light"); // "light" (?? only if null/undefined)

const user = { profile: { email: "a@b.com" } };
console.log(user.profile?.email);  // "a@b.com"
console.log(user.settings?.mode);  // undefined (safe access)
```

- `??` only falls back when the left side is `null` or `undefined` (unlike `||`, which treats `""`, `0`, `false` as falsy).
- `?.` stops the chain if something is `null`/`undefined` instead of throwing.

---
## Ternary operator

```js
const isVIP = true;
const price = isVIP ? 0 : 10; // 0 if VIP, else 10
console.log(price); // 0
```

- Inline `if/else`. Use for simple expressions in JSX:

```jsx
<Text>{isVIP ? "Welcome back, VIP" : "Welcome"}</Text>
```

---
## Spread and rest (`...`)

```js
const arr = [1, 2, 3];
const arr2 = [...arr, 4];         // [1,2,3,4]
const [first, ...rest] = arr2;    // first = 1, rest = [2,3,4]

const base = { a: 1, b: 2 };
const extended = { ...base, b: 3, c: 4 }; // { a:1, b:3, c:4 }
```

- **Spread**: copy contents of arrays/objects.
- **Rest**: collect "the rest" of items/props into an array or object.

---
## typeof and instanceof

```js
console.log(typeof 42);     // "number"
console.log(typeof "hi");  // "string"
console.log(typeof {});     // "object"
console.log([] instanceof Array); // true
```

- `typeof` is for primitives; `instanceof` is for checking prototype chains (e.g., arrays, custom classes).

---
## NaN (Not-a-Number)
- most operations that fail return `NaN` instead of throwing an error.

```js
let result = 10 - "a"; // NaN, not a crash
console.log(result);    // NaN
```

- `NaN` is a special value meaning "invalid number result".
- Note: `NaN === NaN` is `false`; use `Number.isNaN(result)` to check it.
```js
console.log(0 / 0); // NaN
console.log(Number.isNaN(0 / 0)); // true
if (!Number.isNaN(0 / 0)) {
  console.log("It's a valid number");
} else {
  console.log("It's NaN");
}
```

Errors that are thrown:
- SyntaxError `const x = ;` or `JSON.parse("invalid");` not valid JSON
- ReferenceError `console.log(y);` (y not defined)
- TypeError `null.f();` (calling method on null)
- RangeError (e.g., invalid array length)

---

As a React Native dev, the big things to remember:
- Prefer `===` / `!==` over `==` / `!=`.
- `??` / `?.` are your friends for defaults and safe access.
- `...` (spread/rest) is everywhere in React code (props, state updates).
