package me.baldo.mappit.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.baldo.mappit.data.model.Pin
import me.baldo.mappit.data.repositories.CameraPositionDto
import me.baldo.mappit.data.repositories.CameraRepository
import me.baldo.mappit.data.repositories.PinRepository

data class HomeState(
    val pins: List<Pin> = emptyList(),
    val savedCameraPosition: CameraPositionDto = CameraPositionDto(0.0, 0.0, 0f, 0f),
    val showLocationDisabledWarning: Boolean = false,
    val showLocationPermissionDeniedWarning: Boolean = false,
    val showLocationPermissionPermanentlyDeniedWarning: Boolean = false,
    val showNoInternetConnectivityWarning: Boolean = false
)

interface HomeActions {
    fun updatePins()
    fun saveCameraPosition(cameraPosition: CameraPositionDto)
    fun setShowLocationDisabledWarning(show: Boolean)
    fun setShowLocationPermissionDeniedWarning(show: Boolean)
    fun setShowLocationPermissionPermanentlyDeniedWarning(show: Boolean)
    fun setShowNoInternetConnectivityWarning(show: Boolean)
    fun disableAllWarnings()
}

class HomeViewModel(
    private val pinRepository: PinRepository,
    private val cameraRepository: CameraRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    val actions = object : HomeActions {
        override fun updatePins() {
            viewModelScope.launch {
                _state.update { it.copy(pins = pinRepository.getPins()) }
                Log.i("TAG", "Pins loaded: ${_state.value.pins}")
            }
        }

        override fun saveCameraPosition(cameraPosition: CameraPositionDto) {
            viewModelScope.launch {
                cameraRepository.setCameraPosition(cameraPosition)
            }
        }

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

    init {
        viewModelScope.launch {
            _state.update { it.copy(savedCameraPosition = cameraRepository.cameraPosition.first()) }
        }
        actions.updatePins()
    }
}