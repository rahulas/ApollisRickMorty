package com.rahul.rickandmorty.data.remote

import com.rahul.rickandmorty.data.entities.Character
import com.rahul.rickandmorty.data.entities.CharacterList
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CharacterService {
    @GET("character")
    suspend fun getAllCharacters(): Response<CharacterList>

    @GET("character")
    suspend fun getAllCharactersByPage(@Query("page") page: Int): Response<CharacterList>

    @GET("character/{id}")
    suspend fun getCharacter(@Path("id") id: Int): Response<Character>
}