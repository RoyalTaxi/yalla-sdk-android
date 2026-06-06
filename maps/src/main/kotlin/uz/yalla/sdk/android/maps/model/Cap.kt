package uz.yalla.sdk.android.maps.model

sealed class Cap {
    data object Butt : Cap()

    data object Round : Cap()

    data object Square : Cap()
}
