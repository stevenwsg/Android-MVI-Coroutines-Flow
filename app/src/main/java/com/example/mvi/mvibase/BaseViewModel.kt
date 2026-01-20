package com.example.mvi.mvibase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base ViewModel for MVI architecture.
 *
 * @param I Intent type that implements BaseIntent
 * @param S State type
 * @param E Event type that implements BaseEvent
 */
abstract class BaseViewModel<I : BaseIntent, S, E : BaseEvent> : ViewModel() {

    private val _state: MutableStateFlow<S> by lazy {
        MutableStateFlow(initialState())
    }
    val state: StateFlow<S> = _state.asStateFlow()

    private val _event: MutableSharedFlow<E> = MutableSharedFlow()
    val event: SharedFlow<E> = _event.asSharedFlow()

    protected abstract fun initialState(): S

    fun processIntent(intent: I) {
        viewModelScope.launch {
            handleIntent(intent)
        }
    }

    protected abstract suspend fun handleIntent(intent: I)

    protected fun updateState(update: (S) -> S) {
        _state.update(update)
    }

    protected suspend fun sendEvent(event: E) {
        _event.emit(event)
    }

    protected fun setState(newState: S) {
        _state.value = newState
    }
}