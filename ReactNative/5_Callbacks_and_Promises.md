# Callbacks and Promises (async JavaScript for React Native devs)

Async code is everywhere in React Native: network calls, file I/O, timers, etc. Modern code usually prefers **Promises + async/await**, but you’ll still see callbacks.

---
## 1. Callbacks (older style)

> A callback is a function passed into another function, to be called later.

```js
function fetchDataCallback(url, callback) {
  // Simulate async operation
  setTimeout(() => {
    const data = { message: "ok" };
    callback(null, data); // (error, result) pattern
  }, 1000);
}

fetchDataCallback("/api", (err, result) => {
  if (err) {
    console.error("Error:", err);
    return;
  }
  console.log("Result:", result);
});
```

**Problems:**
- Nested callbacks become "callback hell".
- Error handling is manual and inconsistent.

---
## 2. Promises

A Promise represents a value that will be available **now or in the future**.

```js
function fetchDataPromise(url) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const success = true;
      if (success) {
        resolve({ message: "ok" });
      } else {
        reject(new Error("Network error"));
      }
    }, 1000);
  });
}

fetchDataPromise("/api")
  .then((result) => {
    console.log("Result:", result);
  })
  .catch((error) => {
    console.error("Error:", error);
  });
```

- `then` handles success, `catch` handles errors.
- Promises can be chained.

---
## 3. async/await (modern, easiest to read)

`async/await` is syntax sugar on top of Promises and feels like Kotlin coroutines with `suspend`.

```js
async function loadData() {
  try {
    const result = await fetchDataPromise("/api");
    console.log("Result:", result);
  } catch (error) {
    console.error("Error:", error);
  }
}

loadData();
```

- `async` before a function means it returns a Promise.
- `await` pauses inside that function until the Promise settles.


# let, var, const (JavaScript basics for React Native)
---
## 4. Using Promises in React Native (fetch example)

```js
async function fetchUser(userId) {
  const response = await fetch(`https://api.example.com/users/${userId}`);

  if (!response.ok) {
    throw new Error("Network response was not ok");
  }

  const json = await response.json();
  return json;
}

// In a component or hook:

useEffect(() => {
  let isActive = true;

  (async () => {
    try {
      const user = await fetchUser(123);
      if (isActive) {
        setUser(user);
      }
    } catch (e) {
      if (isActive) {
        setError(e.message);
      }
    }
  })();

  return () => {
    isActive = false; // avoid setting state on unmounted component
    // unmounted component is like cancelling a coroutine
    // isActive flag is like a Job's isActive property
  };
}, []);
```

---
## 5. Mental model vs Kotlin coroutines

- **Promise** ≈ a single async result (`Deferred` in coroutines).
- **async/await** ≈ `suspend` + `withContext` in Kotlin.
- Error handling: use `try/catch` around `await`, similar to coroutines.

---
As a cross-platform Android dev, focus on:
- Recognizing older **callback-style** APIs and how to wrap them into Promises.
- Using **async/await** for clarity in React Native components and hooks.
- Remembering that async functions return Promises and that you should handle both success and error paths.

These are the three main ways to declare variables in modern JavaScript. As an Android dev, think of them as different *scopes* and *mutability* rules (similar to `val`/`var` in Kotlin, but with an extra legacy one: `var`).

---
## `var` (function-scoped) — legacy, usually avoid

- **Scope:** function-scoped (ignores block boundaries like `if`, `for`).
- **Redeclaration:** you can redeclare the same name in the same scope.
- **Modern use:** generally **avoid** in new code; use `let`/`const` instead.

```js
function demoVar() {
  for (var i = 0; i < 3; i++) {
    // ...
  }
  console.log(i); // 3 — var escapes block scope

  var x = 1;
  var x = 2; // redeclaration allowed
  console.log(x); // 2
}
```

**Key idea:** `var` behaves in surprising ways because it doesn’t respect block scope. This can lead to bugs in larger apps.

---
## `let` (block-scoped) — use for reassignable variables

- **Scope:** block-scoped (like Kotlin `var` inside `{ }`).
- **Redeclaration:** cannot redeclare in the same scope.
- **Use when:** value needs to change over time.

```js
function demoLet() {
  for (let i = 0; i < 3; i++) {
    // i is only visible in the loop block
  }
  // console.log(i); // ReferenceError — i is block-scoped

  let y = 1;
  // let y = 2; // SyntaxError — cannot redeclare in same scope
  y = 2;         // OK — reassignment allowed
  console.log(y); // 2
}
```

**Think:** `let` ≈ Kotlin `var` (mutable, block-scoped).

---
## `const` (block-scoped, no reassignment) — default choice

- **Scope:** block-scoped.
- **Reassignment:** **not allowed**.
- **Use when:** the *binding* should never change (similar to Kotlin `val`).

```js
function demoConst() {
  const z = 1;
  // z = 2; // TypeError — cannot reassign const

  const obj = { a: 1 };
  obj.a = 2;       // OK — mutating a property
  // obj = { a: 3 }; // TypeError — cannot rebind const

  const arr = [1, 2, 3];
  arr.push(4);     // OK — mutating array contents
  // arr = [];     // TypeError — cannot reassign the variable
}
```

**Important:** `const` prevents reassignment of the variable, **NOT** mutation of the object/array it points to.

---
## Summary (Android dev mental model)

- Prefer **`const`** by default (like `val`).
- Use **`let`** when you really need to reassign (like a loop counter or accumulator).
- Avoid **`var`** in new code; it’s mostly for legacy compatibility.

In React / React Native codebases, you’ll see `const` used almost everywhere (components, hooks, config), with `let` reserved for rare mutable values.
