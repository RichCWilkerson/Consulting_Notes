Variables and scope in modern JavaScript

```JavaScript
// var (function-scoped): Avoid in modern code. It leaks out of blocks and can be redeclared.
function demoVar() {
    for (var i = 0; i < 3; i++) {}
    console.log(i); // 3 — var escapes block scope
    var x = 1;
    var x = 2;      // redeclaration allowed
}
```


// let (block-scoped): Use for reassigned variables; cannot be redeclared in the same scope.

function demoLet() {
for (let i = 0; i < 3; i++) {}
// console.log(i); // ReferenceError — i is block-scoped
let y = 1;
// let y = 2;     // SyntaxError — cannot redeclare
y = 2;           // reassignment allowed
}


// const (block-scoped, no reassignment) Use by default; object/array contents can still be mutated.

function demoConst() {
const z = 1;
// z = 2;        // TypeError — cannot reassign const

const obj = { a: 1 };
obj.a = 2;       // OK — mutating a property
// obj = { a: 3 }; // TypeError — reassigning the binding
}


	•	Node.js v24.11.1 to /usr/local/bin/node
	•	npm v11.6.2 to /usr/local/bin/npm
Make sure that /usr/local/bin is in your $PATH.
