package com.rahul.rickandmorty.data.remote

import javax.inject.Inject

class CharacterRemoteDataSource @Inject constructor(
    private val characterService: CharacterService
) : BaseDataSource() {

    suspend fun getCharacters() = getResult { characterService.getAllCharacters() }
    suspend fun getAllCharactersByPage(page: Int) =
        getResult { characterService.getAllCharactersByPage(page) }

    suspend fun getCharacter(id: Int) = getResult { characterService.getCharacter(id) }
}