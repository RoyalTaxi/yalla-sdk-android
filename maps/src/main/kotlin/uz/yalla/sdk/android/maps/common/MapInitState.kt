package uz.yalla.sdk.android.maps.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
internal class MapInitState {
    var isMapReady by mutableStateOf(false)
        private set

    var isInitialized by mutableStateOf(false)
        private set

    var hasMovedToLocation by mutableStateOf(false)
        private set

    var hasMovedToUserLocation by mutableStateOf(false)
        private set

    fun onMapReady() {
        isMapReady = true
    }

    fun onMovedToLocation(isUserLocation: Boolean) {
        hasMovedToLocation = true
        if (isUserLocation) {
            hasMovedToUserLocation = true
        }
    }

    fun onInitialized() {
        isInitialized = true
    }
}

@Composable
internal fun rememberMapInitState(): MapInitState = remember { MapInitState() }
