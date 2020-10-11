package com.rahul.rickandmorty.ui.characterdetail

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.rahul.rickandmorty.data.entities.Character
import com.rahul.rickandmorty.data.repository.CharacterRepository
import com.rahul.rickandmorty.utils.Resource

class CharacterDetailViewModel @ViewModelInject constructor(
    private val repository: CharacterRepository
) : ViewModel() {

    private val _id = MutableLiveData<Int>()

    private val _character = _id.switchMap { id ->
        repository.getCharacter(id)
    }
    val character: LiveData<Resource<Character>> = _character

    fun start(id: Int) {
        _id.value = id
    }

}
