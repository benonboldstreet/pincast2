package com.example.pincast.data

/**
 * Result class to handle loading, success, and error states in a unified way
 */
class Result<T> private constructor(
    private val value: T? = null,
    private val error: Throwable? = null,
    val isLoading: Boolean = false
) {

    val isSuccess: Boolean
        get() = value != null && !isLoading && error == null

    val isFailure: Boolean
        get() = error != null && !isLoading

    fun getOrNull(): T? = value

    fun exceptionOrNull(): Throwable? = error

    companion object {
        fun <T> success(value: T): Result<T> = Result(value = value)
        
        fun <T> failure(error: Throwable): Result<T> = Result(error = error)
        
        fun <T> loading(): Result<T> = Result(isLoading = true)
    }
} 