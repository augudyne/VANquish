package com.projects.valerian.samplemapapplication.api

import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SuggestionsAPI {

    enum class InfrastructureType(val readable: String, val description: String) {
        @SerializedName("ROUNDABOUT")
        ROUNDABOUT("Roundabout", "Small and Medium roundabouts slow down traffic in residential areas"),
        @SerializedName("Bike Lane")
        BIKE_LANE("Bike Lane", "Designated lane space for bikes, providing safety to motorists, cyclists, and pedestrians"),
        @SerializedName("STREETLIGHT")
        STREETLIGHT("Streetlight", "Increase visibility in the area"),
        @SerializedName("FOUR_WAY_STOP")
        FOUR_WAY_STOP("Four way stop", "Drivers must acknowledge each other and right of way"),
        @SerializedName("SPEED_HUMP")
        SPEED_BUMP("Speed hump", "Force drivers to slow down when going over speed hump")
    }

    class Suggestion(val lat: Double, val lon: Double, val items: List<InfrastructureType>)

    @POST("api/suggestions")
    fun postSuggestion(@Body content: Suggestion): Single<Any>

    @GET("api/suggestions")
    fun getSuggestions():
}