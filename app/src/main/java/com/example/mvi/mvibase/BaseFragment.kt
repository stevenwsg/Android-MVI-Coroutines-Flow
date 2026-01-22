package com.example.mvi.mvibase

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

/**
 * Base Fragment for MVI architecture.
 * Provides the same MVI pattern as BaseActivity but for Fragments.
 *
 * @param I Intent type that implements BaseIntent
 * @param S State type
 * @param E Event type that implements BaseEvent
 * @param VM ViewModel type that extends BaseViewModel
 */
abstract class BaseFragment<I : BaseIntent, S, E : BaseEvent, VM : BaseViewModel<I, S, E>> : Fragment() {

    protected abstract val viewModel: VM

    protected abstract fun render(state: S)

    protected abstract fun handleEvent(event: E)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeState()
        observeEvents()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    render(state)
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
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