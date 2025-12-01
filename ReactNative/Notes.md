
// Operators cheat‑sheet with runnable examples

// Arithmetic:
const a = 10, b = 3;
console.log(a + b, a - b, a * b, a / b, a % b); // 13 7 30 3.333... 1
console.log(a ** b); // 1000 (exponentiation)

// Assignment:
let n = 5;
n += 2;  // 7
n *= 3;  // 21
n -= 1;  // 20
n /= 4;  // 5
n %= 2;  // 1

// Comparison:
console.log(2 == "2");   // true  (coercion)
console.log(2 === "2");  // false (strict)
console.log(3 > 2, 3 >= 3, 2 < 3, 2 <= 2); // true true true true
console.log(null == undefined);  // true
console.log(null === undefined); // false

// Logical:
console.log(true && false); // false
console.log(true || false); // true
console.log(!true);         // false

// Short‑circuiting
const maybeUser = null;
const name = (maybeUser && maybeUser.name) || "Guest"; // "Guest"

// Nullish coalescing and optional chaining:
const cfg = { theme: null };
console.log(cfg.theme ?? "light"); // "light" (?? only if null/undefined)

const user = { profile: { email: "a@b.com" } };
console.log(user.profile?.email);  // "a@b.com"
console.log(user.settings?.mode);  // undefined (safe access)

// Ternary (conditional):
const isVIP = true;
const price = isVIP ? 0 : 10;

// Spread and rest:
const arr = [1, 2, 3];
const arr2 = [...arr, 4];         // [1,2,3,4]
const [first, ...rest] = arr2;    // first=1, rest=[2,3,4]

const base = { a: 1, b: 2 };
const extended = { ...base, b: 3, c: 4 }; // { a:1, b:3, c:4 }

// Destructuring:
const point = { x: 10, y: 20 };
const { x, y } = point;           // x=10, y=20

const tuple = [100, 200];
const [top, bottom] = tuple;      // top=100, bottom=200

// typeof and instanceof:
console.log(typeof 42, typeof "hi", typeof {}); // "number" "string" "object"
console.log([] instanceof Array);               // true

// Bitwise (rare in RN, but available):
console.log(5 & 3);  // 1
console.log(5 | 3);  // 7
console.log(5 ^ 3);  // 6
console.log(~5);     // -6
console.log(5 << 1); // 10
console.log(5 >> 1); // 2

// NaN (not a number due to invalid operation)
// this does not throw an error, but results in NaN
let result = 10 - "a" 


higher order function -> a function that takes a function as an argument or returns a function as a result
the function passed in can be called a callback function
array.map
array.forEach
array.filter
array.reduce

Topic to be covered by next session:
Let, Var, Const
Common JS operators
String methods
Array methods
Callback and promises

[W3 Schools](https://www.w3schools.com/js/js_control_flow.asp)
-> JS Arrays (Methods, Search, Sort, Iterations, Reference, Const)
-> String methods
-> Callback and promises

// the array reference cannot be changed when using const
// but can modify the contents of the array
const numbers = [45, 4, 9, 16, 25];

React doesn't use classes much
uses functional components with hooks

node js + vscode