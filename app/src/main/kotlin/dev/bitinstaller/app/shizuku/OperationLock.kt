package dev.bitinstaller.app.shizuku

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Simple non-reentrant lock for guarding concurrent patch operations.
 *
 * Prevents race conditions when the user taps "Patch" rapidly on
 * multiple targets — only one operation runs at a time.
 */
class OperationLock {

    private val active = AtomicBoolean(false)

    /** Try to acquire. Returns true if this caller acquired the lock. */
    fun tryAcquire(): Boolean = active.compareAndSet(false, true)

    /** Release the lock so another operation can proceed. */
    fun release() {
        active.set(false)
    }
}
