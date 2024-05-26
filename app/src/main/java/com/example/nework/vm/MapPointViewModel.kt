package com.example.nework.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nework.dto.Coords
import com.example.nework.dto.Job
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapPointViewModel @Inject constructor(
): ViewModel(){
    private val _coords = MutableLiveData<Coords?>()
    val coords : LiveData<Coords?>
        get() = _coords

    fun changeCoords(coords: Coords? = null) = _coords.postValue(coords)
    fun clear() = _coords.postValue(null)
}