package me.baldo.mappit.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.baldo.mappit.data.model.Pin
import me.baldo.mappit.data.repositories.PinRepository

data class HomeState(
    val pins: List<Pin> = emptyList(),
    val showLocationDisabledWarning: Boolean = false,
    val showLocationPermissionDeniedWarning: Boolean = false,
    val showLocationPermissionPermanentlyDeniedWarning: Boolean = false,
    val showNoInternetConnectivityWarning: Boolean = false
)

interface HomeActions {
    fun setShowLocationDisabledWarning(show: Boolean)
    fun setShowLocationPermissionDeniedWarning(show: Boolean)
    fun setShowLocationPermissionPermanentlyDeniedWarning(show: Boolean)
    fun setShowNoInternetConnectivityWarning(show: Boolean)
    fun disableAllWarnings()
}

class HomeViewModel(
    private val pinRepository: PinRepository
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(pins = pinRepository.getPins()) }
        }
    }

    val actions = object : HomeActions {
        override fun setShowLocationDisabledWarning(show: Boolean) {
            _state.update { it.copy(showLocationDisabledWarning = show) }
        }

        override fun setShowLocationPermissionDeniedWarning(show: Boolean) {
            _state.update { it.copy(showLocationPermissionDeniedWarning = show) }
        }

        override fun setShowLocationPermissionPermanentlyDeniedWarning(show: Boolean) {
            _state.update { it.copy(showLocationPermissionPermanentlyDeniedWarning = show) }
        }

        override fun setShowNoInternetConnectivityWarning(show: Boolean) {
            _state.update { it.copy(showNoInternetConnectivityWarning = show) }
        }

        override fun disableAllWarnings() {
            _state.update {
                it.copy(
                    // showLocationDisabledWarning = false,
                    showLocationPermissionDeniedWarning = false,
                    showLocationPermissionPermanentlyDeniedWarning = false,
                    // showNoInternetConnectivityWarning = false
                )
            }
        }
    }
}