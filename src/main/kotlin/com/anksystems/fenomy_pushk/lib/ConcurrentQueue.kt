package com.anksystems.fenomy_pushk.lib

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import java.util.*

class ConcurrentQueue<T : Searchable<T>> {
    companion object {
        const val MAX_QUEUE_SIZE = 10000
    }

    private val mutex: Mutex = Mutex()
    private val queue: Queue<T> = LinkedList()
    private val getSemaphore = Semaphore(MAX_QUEUE_SIZE, MAX_QUEUE_SIZE)
    private val putSemaphore = Semaphore(MAX_QUEUE_SIZE)

    suspend fun put(element: T) {
        putSemaphore.acquire()
        mutex.withLock {
            if (!queue.any { it.compare(element) }) {
                getSemaphore.release()
                queue.add(element)
            }
        }
    }

    suspend fun getList(quantity: Int = 1): List<T>? {
        val result = mutableListOf<T>()
        mutex.withLock {
            repeat(Integer.min(quantity, queue.size)) {
                putSemaphore.release()
                result.add(queue.poll())
            }
        }
        return result.takeIf { it.size > 0 }
    }

    suspend fun get(): T {
        getSemaphore.acquire()
        mutex.withLock {
            putSemaphore.release()
            return queue.remove()
        }
    }

    suspend fun getOrNull(): T? {
        if (getSemaphore.tryAcquire()) {
            mutex.withLock {
                putSemaphore.release()
                return queue.remove()
            }
        } else
            return null
    }


}