package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class UroViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = UroRepository(database.uroDao())

    private val prefs = application.getSharedPreferences("uro_settings", android.content.Context.MODE_PRIVATE)
    private val _appTheme = MutableStateFlow(prefs.getString("selected_theme", "BLUE") ?: "BLUE")
    val appTheme: StateFlow<String> = _appTheme.asStateFlow()

    fun setTheme(theme: String) {
        _appTheme.value = theme
        prefs.edit().putString("selected_theme", theme).apply()
    }

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean("sound_enabled", true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
    }

    private val _hapticEnabled = MutableStateFlow(prefs.getBoolean("haptic_enabled", true))
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    fun setHapticEnabled(enabled: Boolean) {
        _hapticEnabled.value = enabled
        prefs.edit().putBoolean("haptic_enabled", enabled).apply()
    }

    private val _fontScale = MutableStateFlow(prefs.getFloat("font_scale", 1.0f))
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    fun setFontScale(scale: Float) {
        _fontScale.value = scale
        prefs.edit().putFloat("font_scale", scale).apply()
    }

    private val _isPremium = MutableStateFlow(prefs.getBoolean("is_premium", false))
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    fun activateLicense(key: String): Boolean {
        val normalized = key.trim().uppercase()
        val isValid = normalized == "REYNEL-PRO" || normalized.startsWith("REYNEL-") || normalized.startsWith("URO-") || normalized.contains("PRO")
        if (isValid) {
            _isPremium.value = true
            prefs.edit().putBoolean("is_premium", true).apply()
        }
        return isValid
    }

    fun deactivateLicense() {
        _isPremium.value = false
        prefs.edit().putBoolean("is_premium", false).apply()
    }

    val cases: StateFlow<List<PatientCase>> = repository.allCases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val records: StateFlow<List<CalculationRecord>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected case for the Case Detail screen
    private val _selectedCaseId = MutableStateFlow<Long?>(null)
    val selectedCaseId = _selectedCaseId.asStateFlow()

    val recordsForSelectedCase: StateFlow<List<CalculationRecord>> = _selectedCaseId
        .flatMapLatest { caseId ->
            if (caseId == null) flowOf(emptyList())
            else repository.getRecordsForCase(caseId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedCase: StateFlow<PatientCase?> = _selectedCaseId
        .flatMapLatest { caseId ->
            if (caseId == null) flowOf(null)
            else repository.allCases.map { list -> list.find { it.id == caseId } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectCase(caseId: Long?) {
        _selectedCaseId.value = caseId
    }

    fun createCase(title: String, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val trimmed = title.trim()
            val name = if (trimmed.isEmpty()) {
                "Caso Clínico #${(System.currentTimeMillis() % 10000).toInt()}"
            } else trimmed
            val id = repository.insertCase(PatientCase(title = name))
            onCreated(id)
        }
    }

    fun deleteCase(caseId: Long) {
        viewModelScope.launch {
            repository.deleteCaseById(caseId)
            if (_selectedCaseId.value == caseId) {
                _selectedCaseId.value = null
            }
        }
    }

    fun saveCalculation(
        calculatorType: String,
        inputParameters: String,
        result: String,
        caseId: Long? = null,
        onSaved: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.insertRecord(
                CalculationRecord(
                    caseId = caseId,
                    calculatorType = calculatorType,
                    inputParameters = inputParameters,
                    result = result
                )
            )
            onSaved()
        }
    }

    fun deleteRecord(recordId: Long) {
        viewModelScope.launch {
            repository.deleteRecordById(recordId)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.deleteAllRecords()
            repository.deleteAllCases()
        }
    }
}
