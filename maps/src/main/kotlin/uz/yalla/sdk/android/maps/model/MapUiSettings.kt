package uz.yalla.sdk.android.maps.model

import androidx.compose.runtime.Stable

@Stable
class MapUiSettings(
    val compassEnabled: Boolean = true,
    val indoorLevelPickerEnabled: Boolean = true,
    val mapToolbarEnabled: Boolean = true,
    val myLocationButtonEnabled: Boolean = true,
    val rotationGesturesEnabled: Boolean = true,
    val scrollGesturesEnabled: Boolean = true,
    val scrollGesturesEnabledDuringRotateOrZoom: Boolean = true,
    val tiltGesturesEnabled: Boolean = true,
    val zoomControlsEnabled: Boolean = true,
    val zoomGesturesEnabled: Boolean = true
) {
    override fun toString(): String = "MapUiSettings(" +
        "compassEnabled=$compassEnabled, indoorLevelPickerEnabled=$indoorLevelPickerEnabled, " +
        "mapToolbarEnabled=$mapToolbarEnabled, myLocationButtonEnabled=$myLocationButtonEnabled, " +
        "rotationGesturesEnabled=$rotationGesturesEnabled, scrollGesturesEnabled=$scrollGesturesEnabled, " +
        "scrollGesturesEnabledDuringRotateOrZoom=$scrollGesturesEnabledDuringRotateOrZoom, " +
        "tiltGesturesEnabled=$tiltGesturesEnabled, zoomControlsEnabled=$zoomControlsEnabled, " +
        "zoomGesturesEnabled=$zoomGesturesEnabled)"

    override fun equals(other: Any?): Boolean = other is MapUiSettings &&
        compassEnabled == other.compassEnabled &&
        indoorLevelPickerEnabled == other.indoorLevelPickerEnabled &&
        mapToolbarEnabled == other.mapToolbarEnabled &&
        myLocationButtonEnabled == other.myLocationButtonEnabled &&
        rotationGesturesEnabled == other.rotationGesturesEnabled &&
        scrollGesturesEnabled == other.scrollGesturesEnabled &&
        scrollGesturesEnabledDuringRotateOrZoom == other.scrollGesturesEnabledDuringRotateOrZoom &&
        tiltGesturesEnabled == other.tiltGesturesEnabled &&
        zoomControlsEnabled == other.zoomControlsEnabled &&
        zoomGesturesEnabled == other.zoomGesturesEnabled

    override fun hashCode(): Int {
        var result = compassEnabled.hashCode()
        result = 31 * result + indoorLevelPickerEnabled.hashCode()
        result = 31 * result + mapToolbarEnabled.hashCode()
        result = 31 * result + myLocationButtonEnabled.hashCode()
        result = 31 * result + rotationGesturesEnabled.hashCode()
        result = 31 * result + scrollGesturesEnabled.hashCode()
        result = 31 * result + scrollGesturesEnabledDuringRotateOrZoom.hashCode()
        result = 31 * result + tiltGesturesEnabled.hashCode()
        result = 31 * result + zoomControlsEnabled.hashCode()
        result = 31 * result + zoomGesturesEnabled.hashCode()
        return result
    }

    fun copy(
        compassEnabled: Boolean = this.compassEnabled,
        indoorLevelPickerEnabled: Boolean = this.indoorLevelPickerEnabled,
        mapToolbarEnabled: Boolean = this.mapToolbarEnabled,
        myLocationButtonEnabled: Boolean = this.myLocationButtonEnabled,
        rotationGesturesEnabled: Boolean = this.rotationGesturesEnabled,
        scrollGesturesEnabled: Boolean = this.scrollGesturesEnabled,
        scrollGesturesEnabledDuringRotateOrZoom: Boolean = this.scrollGesturesEnabledDuringRotateOrZoom,
        tiltGesturesEnabled: Boolean = this.tiltGesturesEnabled,
        zoomControlsEnabled: Boolean = this.zoomControlsEnabled,
        zoomGesturesEnabled: Boolean = this.zoomGesturesEnabled
    ): MapUiSettings = MapUiSettings(
        compassEnabled = compassEnabled,
        indoorLevelPickerEnabled = indoorLevelPickerEnabled,
        mapToolbarEnabled = mapToolbarEnabled,
        myLocationButtonEnabled = myLocationButtonEnabled,
        rotationGesturesEnabled = rotationGesturesEnabled,
        scrollGesturesEnabled = scrollGesturesEnabled,
        scrollGesturesEnabledDuringRotateOrZoom = scrollGesturesEnabledDuringRotateOrZoom,
        tiltGesturesEnabled = tiltGesturesEnabled,
        zoomControlsEnabled = zoomControlsEnabled,
        zoomGesturesEnabled = zoomGesturesEnabled
    )
}
