package me.baldo.mappit.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Like(
    @SerialName("user_id")
    val userId: Uuid,
    @SerialName("pin_id")
    val pinId: Uuid
)