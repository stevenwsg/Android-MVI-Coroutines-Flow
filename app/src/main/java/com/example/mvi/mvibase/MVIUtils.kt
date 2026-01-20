package com.example.mvi.mvibase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Utility functions for MVI architecture
 */
object MVIUtils {

    /**
     * Combines multiple flows into a single flow of a combined state
     */
    fun <T1, T2, R> combineFlows(
        flow1: Flow<T1>,
        flow2: Flow<T2>,
        transform: (T1, T2) -> R
    ): Flow<R> {
        return combine(flow1, flow2) { t1, t2 ->
            transform(t1, t2)
        }
    }

    /**
     * Combines three flows into a single flow of a combined state
     */
    fun <T1, T2, T3, R> combineFlows(
        flow1: Flow<T1>,
        flow2: Flow<T2>,
        flow3: Flow<T3>,
        transform: (T1, T2, T3) -> R
    ): Flow<R> {
        return combine(flow1, flow2, flow3) { t1, t2, t3 ->
            transform(t1, t2, t3)
        }
    }

    /**
     * Sealed class pattern for handling loading, success, error states
     */
    sealed interface ResultState<out T> {
        data object Loading : ResultState<Nothing>
        data class Success<T>(val data: T) : ResultState<T>
        data class Error(val message: String, val throwable: Throwable? = null) : ResultState<Nothing>
    }

    /**
     * Extension function to transform Result to ResultState
     */
    fun <T> Result<T>.toResultState(): ResultState<T> {
        return if (isSuccess) {
            ResultState.Success(getOrNull()!!)
        } else {
            ResultState.Error(exceptionOrNull()?.message ?: "Unknown error", exceptionOrNull())
        }
    }
}