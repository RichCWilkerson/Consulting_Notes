# Common JavaScript string methods (for Android/React Native devs)

Strings in JS are immutable, like Kotlin `String`. Methods always return a **new** string.

Use `const` for strings unless you need to reassign.

---
## Creating strings

```js
const s1 = "Hello";
const s2 = 'World';
const name = "Christian";

// Template literals (backticks) — like Kotlin string templates
const message = `Hello, ${name}!`;
```

---
## Length

```js
const str = "React Native";
console.log(str.length); // 12
```

---
## Changing case

```js
const input = "heLLo";
console.log(input.toLowerCase()); // "hello"
console.log(input.toUpperCase()); // "HELLO"
```

---
## Trimming whitespace

```js
const raw = "   padded   ";
console.log(raw.trim());      // "padded"
console.log(raw.trimStart()); // remove only left
console.log(raw.trimEnd());   // remove only right
```

---
## Checking contents

```js
const title = "Senior Android Developer";

console.log(title.includes("Android")); // true
console.log(title.startsWith("Senior")); // true
console.log(title.endsWith("iOS"));      // false
```

---
## Getting substrings

```js
const lang = "JavaScript";

console.log(lang.slice(0, 4));  // "Java" (from index 0 up to, not including, 4)
console.log(lang.slice(4));     // "Script" (from index 4 to end)

// Negative indices count from the end
// counts -6 to 0 
console.log(lang.slice(-6));    // "Script"
```

> Tip: prefer `slice` over the older `substr`/`substring` in new code.

---
## Splitting and joining

```js
const csv = "java,kotlin,swift";

const parts = csv.split(",");
console.log(parts); // ["java", "kotlin", "swift"]

const joined = parts.join(" | ");
console.log(joined); // "java | kotlin | swift"
```

---
## Replacing text

```js
const sentence = "Kotlin is nice. Kotlin is concise.";

console.log(sentence.replace("Kotlin", "Java"));
// "Java is nice. Kotlin is concise." (first match only)

console.log(sentence.replaceAll("Kotlin", "Java"));
// "Java is nice. Java is concise." (all matches)
```

- `replace` changes only the **first** match (unless you use a regex with global flag).
- `replaceAll` (ES2021) replaces **all** occurrences.

---
## Example: sanitizing user input

```js
function normalizeName(rawName) {
  return rawName.trim().replaceAll("  ", "").toLowerCase();
}

console.log(normalizeName("  CHRISTIAN  ")); // "christian"
```

---
As a React Native dev, you’ll commonly use:
- `trim`, `toLowerCase`, `toUpperCase` for input.
- `includes`, `startsWith`, `endsWith` for simple checks.
- `split` / `join` for converting between strings and arrays.
- Template literals (backticks) for building UI strings.
