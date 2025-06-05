package me.baldo.mappit.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Pin(
    @SerialName("id")
    val id: Uuid,
    @SerialName("created_at")
    val createdAt: Instant,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String,
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("user_id")
    val userId: Uuid,
)

@Serializable
data class AutoCompletePin(
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String,
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("user_id")
    val userId: Uuid,
)