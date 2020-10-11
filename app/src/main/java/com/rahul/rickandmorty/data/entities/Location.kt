package com.rahul.rickandmorty.data.entities

import com.google.gson.annotations.SerializedName


data class Location(
    @SerializedName("name")
    val locationName: String,
    @SerializedName("url")
    val locationUrl: String) {
}