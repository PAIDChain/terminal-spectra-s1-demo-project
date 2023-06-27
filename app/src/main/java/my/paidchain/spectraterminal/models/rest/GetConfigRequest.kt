package my.paidchain.spectraterminal.models.rest

import com.google.gson.annotations.SerializedName

data class GetConfigRequest (
    @SerializedName("id") val id: String?
)