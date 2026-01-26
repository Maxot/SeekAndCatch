package com.maxot.seekandcatch.core.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VisualFeedbackManager @Inject constructor() {
    private val _isLifeWasted = MutableStateFlow(false)
    val isLifeWasted: StateFlow<Boolean> = _isLifeWasted.asStateFlow()

    private var feedbackJob: Job? = null

    fun triggerLifeWasted(scope: CoroutineScope) {
        _isLifeWasted.value = false
        feedbackJob?.cancel()
        feedbackJob = scope.launch {
            _isLifeWasted.value = true
            delay(1000)
            _isLifeWasted.value = false
        }
    }

    fun resetLifeWasted() {
        feedbackJob?.cancel()
        _isLifeWasted.value = false
    }
}
