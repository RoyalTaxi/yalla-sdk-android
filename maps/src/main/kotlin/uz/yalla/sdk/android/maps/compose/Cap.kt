package uz.yalla.sdk.android.maps.compose

import com.google.android.gms.maps.model.ButtCap
import com.google.android.gms.maps.model.RoundCap
import com.google.android.gms.maps.model.SquareCap
import uz.yalla.sdk.android.maps.model.Cap
import uz.yalla.sdk.android.maps.model.JointType
import com.google.android.gms.maps.model.Cap as GoogleCap
import com.google.android.gms.maps.model.JointType as GoogleJointType

private val buttCap = ButtCap()

private val roundCap = RoundCap()

private val squareCap = SquareCap()

internal fun Cap.toGoogleCap(): GoogleCap = when (this) {
    Cap.Butt -> buttCap
    Cap.Round -> roundCap
    Cap.Square -> squareCap
}

internal fun JointType.toGoogleJointType(): Int = when (this) {
    JointType.Default -> GoogleJointType.DEFAULT
    JointType.Bevel -> GoogleJointType.BEVEL
    JointType.Round -> GoogleJointType.ROUND
}
