package com.rahul.rickandmorty.ui.characters

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.rahul.rickandmorty.data.entities.Character
import com.rahul.rickandmorty.data.repository.CharacterRepository
import com.rahul.rickandmorty.utils.Resource

class CharactersViewModel @ViewModelInject constructor(
    private val repository: CharacterRepository
) : ViewModel() {

    private val _pageNum = MutableLiveData<Int>()

    private val _characters = _pageNum.switchMap { id ->
        repository.getCharacters(id)
    }

    val characters: LiveData<Resource<List<Character>>> = _characters

    fun start(id: Int) {
        _pageNum.value = id
    }
}
