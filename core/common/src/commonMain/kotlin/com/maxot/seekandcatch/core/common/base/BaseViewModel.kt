package com.maxot.seekandcatch.core.common.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class BaseViewModel<UIState : BaseUIState, Event : BaseEvent>(
    initialState: UIState
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    protected fun updateState(reducer: (UIState) -> UIState) {
        _uiState.update(reducer)
    }

    abstract fun onEvent(event: Event)
}
