# Resources:
- [Debugging Playbook - Medium](https://medium.com/deuk/navigating-the-bug-maze-android-developers-debugging-playbook-213c8953f359)
- [Adv Debugging pt 1 - Medium](https://medium.com/appcent/advanced-debugging-in-android-part-1-983936c1ad58)
- [Adv Debugging pt 2 - Medium](https://medium.com/appcent/advanced-debugging-in-android-part-2-39ba7f7895ef)
- [ADB Guide - Medium](https://medium.com/@EazSoftware/a-comprehensive-guide-to-adb-android-debug-bridge-the-unsung-hero-for-android-developers-28b349037436)
  - ADB is really useful for debugging headless devices because there is no UI to interact with, so you can use ADB commands to access the device's shell, view logs, and perform various debugging tasks remotely.

## Overview


## Best Practices for Fixing Bugs in Android Development
Understand your codebase - aids in intuitvely identifying sources of error
Capturing relevant logs - Logcat, Crashlytics, Timber, custom logging -> filtering logs by tag, level, or message content to find relevant information
- remove log statements after fixing the bug to avoid cluttering logs and potentially exposing sensitive information on production builds.
Stacktrace analysis - export logs via adb commands to a text file for easier analysis, use tools like Android Studio's Logcat or third-party log analyzers to visualize and filter logs effectively.
- identify the "beginning of crash" and "searching for "caused by" tags - these are built into the stack trace to help you find the root cause of the crash.
- enhanced with Log.d() statements to print variable values and execution flow, which can help identify where the code is failing. - remove these statements after fixing the bug to avoid cluttering logs and potentially exposing sensitive information on production builds.
  - add these to critical sections (object instantiation, lifecycle events, network calls, etc.) to get insights into the app's behavior leading up to the bug.
Navigating API version compatibility - stem from incorrect AppCompat configurations or lack of comprehensive API compatibility testing
Diverse Device Testing - use emulators and physical devices with different screen sizes, OS versions, and hardware capabilities to identify device-specific issues.
Using Break Points - set breakpoints in Android Studio to pause execution and inspect variable values, call stacks, and program flow at runtime. This can help identify where the code is failing and understand the state of the application at that moment.
Evaluating and Peeking into variables - at a breakpoint in debug mode, you can inspect and modify values of variables in real time without continuing execution. This allows you to test hypotheses about what might be causing the bug and see how changes affect the program's behavior.
- Right click and select "Evaluate Expression" to evaluate complex expressions or modify variable values on the fly. This can help you understand how different inputs affect the code and identify potential fixes.


Reproducing the bug consistently
Isolating the root cause
Testing the fix thoroughly
Communicating with the team about the bug and the fix


## Common Types of Bugs and How to Fix Them


## Tools and Techniques for Debugging Android Apps


## Common Pitfalls to Avoid When Fixing Bugs in Android Development



## Root Cause Analysis and Post-Incident Remediation