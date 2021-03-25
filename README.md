# SideEffectManager class

## Overview

Suppose you have many threads who all try to do something like download a file to local disk. You don't want them all to do it. How will you coordinate them? The `SideEffectManager` class allows all the threads to try to execute the workload themselves, but do so via the `SideEffectManager`, which will ensure that only one of them does it.

## This repo

- The class itself is in `src/main/java/foo/SideEffectManager.java`.
- There is a demo in `src/main/java/foo/App.java` where 
- The `Quz` class is just to demonstrate housing a `SideEffectManager` in another object, such that the workload is controlled separately within the scope of each instance.
