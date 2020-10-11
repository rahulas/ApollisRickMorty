package com.rahul.rickandmorty.data.repository

import androidx.lifecycle.MutableLiveData
import com.rahul.rickandmorty.app.Constants
import com.rahul.rickandmorty.app.MainApplication
import com.rahul.rickandmorty.data.entities.Character
import com.rahul.rickandmorty.data.local.CharacterDao
import com.rahul.rickandmorty.data.remote.CharacterRemoteDataSource
import com.rahul.rickandmorty.utils.AppPreferences
import com.rahul.rickandmorty.utils.Resource
import com.rahul.rickandmorty.utils.isNumeric
import com.rahul.rickandmorty.utils.performGetOperation
import javax.inject.Inject

class CharacterRepository @Inject constructor(
    private val remoteDataSource: CharacterRemoteDataSource,
    private val localDataSource: CharacterDao
) {
    private var character = MutableLiveData<Resource<List<Character>>>()

    fun getCharacter(id: Int) = performGetOperation(
        databaseQuery = { localDataSource.getCharacter(id) },
        networkCall = { remoteDataSource.getCharacter(id) },
        saveCallResult = { localDataSource.insert(it) }
    )

    fun getCharacters() = performGetOperation(
        databaseQuery = { localDataSource.getAllCharacters() },
        networkCall = { remoteDataSource.getCharacters() },
        saveCallResult = { localDataSource.insertAll(it.results) }
    )

    fun getCharacters(pageNum: Int) = performGetOperation(
        databaseQuery = { localDataSource.getAllCharacters() },
        networkCall = { remoteDataSource.getAllCharactersByPage(pageNum) },
        saveCallResult = {
            if (it.info.next != null) {
                val nxt: String = it.info.next
                val len: Int = it.info.next.length
                var pageNum: Int
                pageNum = if (isNumeric(nxt.substring(len - 2, len))) {
                    nxt.substring(len - 2, len).toInt() - 1
                } else {
                    nxt.substring(len - 1, len).toInt() - 1
                }
                AppPreferences.put(MainApplication.getContext(), Constants.CURRENT_PAGE, pageNum)
            } else {
                AppPreferences.put(MainApplication.getContext(), Constants.HAS_NEXT_PAGE, false)
            }
            localDataSource.insertAll(it.results)
        }
    )
}