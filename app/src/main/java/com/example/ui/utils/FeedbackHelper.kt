package com.example.ui.utils

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import android.view.View
import com.example.ui.UroViewModel

object FeedbackHelper {
    /**
     * Brief gentle haptic click and standard audio click for button taps.
     */
    fun triggerTap(view: View?, viewModel: UroViewModel) {
        if (view == null) return
        if (viewModel.hapticEnabled.value) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
        if (viewModel.soundEnabled.value) {
            view.playSoundEffect(SoundEffectConstants.CLICK)
        }
    }

    /**
     * Clear confirmatory vibration pattern and audio click for successful calculations or saves.
     */
    fun triggerConfirm(view: View?, viewModel: UroViewModel) {
        if (view == null) return
        if (viewModel.hapticEnabled.value) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }
        if (viewModel.soundEnabled.value) {
            view.playSoundEffect(SoundEffectConstants.CLICK)
        }
    }

    /**
     * Warning/rejection haptic pattern and audio click for validation errors or reset actions.
     */
    fun triggerReject(view: View?, viewModel: UroViewModel) {
        if (view == null) return
        if (viewModel.hapticEnabled.value) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                view.performHapticFeedback(HapticFeedbackConstants.REJECT)
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }
        if (viewModel.soundEnabled.value) {
            view.playSoundEffect(SoundEffectConstants.CLICK)
        }
    }
}
