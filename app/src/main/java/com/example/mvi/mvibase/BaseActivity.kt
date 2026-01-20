package com.example.mvi.mvibase

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

/**
 * Base Activity for MVI architecture.
 *
 * @param I Intent type that implements BaseIntent
 * @param S State type
 * @param E Event type that implements BaseEvent
 * @param VM ViewModel type that extends BaseViewModel
 */
abstract class BaseActivity<I : BaseIntent, S, E : BaseEvent, VM : BaseViewModel<I, S, E>> : AppCompatActivity() {

    protected abstract val viewModel: VM

    protected abstract fun render(state: S)

    protected abstract fun handleEvent(event: E)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeState()
        observeEvents()
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    render(state)
                }
            }
        }
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect { event ->
                    handleEvent(event)
                }
            }
        }
    }

    protected fun dispatchIntent(intent: I) {
        viewModel.processIntent(intent)
    }
}