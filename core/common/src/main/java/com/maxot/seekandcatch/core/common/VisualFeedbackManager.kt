package com.maxot.seekandcatch.core.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VisualFeedbackManager @Inject constructor() {
    private val _isLifeWasted = MutableStateFlow(false)
    val isLifeWasted: StateFlow<Boolean> = _isLifeWasted.asStateFlow()

    fun triggerLifeWasted() {
        _isLifeWasted.value = true
    }

    fun resetLifeWasted() {
        _isLifeWasted.value = false
    }
}
