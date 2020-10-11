package com.rahul.rickandmorty.data.entities

data class Info(
    val count: Int,
    val next: String,
    val pages: Int,
    val prev: String,
    val currentPage: Int = 0
)