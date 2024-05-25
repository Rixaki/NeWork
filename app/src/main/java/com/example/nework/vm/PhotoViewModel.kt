package com.example.nework.vm

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nework.model.PhotoModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PhotoViewModel @Inject constructor() : ViewModel() {

    private val noPhoto = PhotoModel()

    private val privatePhoto = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = privatePhoto

    fun changePhoto(uri: Uri?, file: File?) {
        privatePhoto.value = PhotoModel(uri, file)
    }

    fun clearPhoto() {
        privatePhoto.value = null
    }
}