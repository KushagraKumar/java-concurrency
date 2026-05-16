# Level 1: Thread Basics

## Focus
Thread lifecycle management, starting, joining, and cooperative cancellation.

## Problems
- **P1.1 — Thread Lifecycle & Cooperative Cancellation**
    - **Part A:** Correct usage of `Thread.join()` to coordinate main thread completion.
    - **Part B:** Implementing clean shutdown using `Thread.interrupt()` and proper cleanup, avoiding deprecated methods like `Thread.stop()`.

## Key Concepts
- `Thread.start()` vs `Thread.run()`
- `Thread.join()` (Happens-before guarantee)
- `Thread.interrupt()` and `isInterrupted()`
- `InterruptedException` propagation
- Cooperative cancellation patterns
