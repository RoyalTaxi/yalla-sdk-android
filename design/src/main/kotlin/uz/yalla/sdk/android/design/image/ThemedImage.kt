package uz.yalla.sdk.android.design.image

import uz.yalla.sdk.android.resources.R

enum class ThemedImage(
    val light: Int,
    val dark: Int
) {
    BlurryLogo(R.drawable.img_light_blurry_logo, R.drawable.img_dark_blurry_logo),
    CloseCircle(R.drawable.img_light_close_circle, R.drawable.img_dark_close_circle),
    Login(R.drawable.img_light_login, R.drawable.img_dark_login),
    Logout(R.drawable.img_light_logout, R.drawable.img_dark_logout),
    MapPin(R.drawable.img_light_map_pin, R.drawable.img_dark_map_pin),
    NotificationMute(R.drawable.img_light_notification_mute, R.drawable.img_dark_notification_mute),
    OrderHistory(R.drawable.img_light_order_history, R.drawable.img_dark_order_history),
    OrderSearch(R.drawable.img_light_order_search, R.drawable.img_dark_order_search),
    Safety(R.drawable.img_light_safety, R.drawable.img_dark_safety),
    ShieldCheck(R.drawable.img_light_shield_check, R.drawable.img_dark_shield_check),
    TariffCard(R.drawable.img_light_tariff_card, R.drawable.img_dark_tariff_card),
    TrashCan(R.drawable.img_light_trash_can, R.drawable.img_dark_trash_can),
}
