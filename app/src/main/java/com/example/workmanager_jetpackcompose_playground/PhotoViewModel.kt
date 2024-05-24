package com.example.workmanager_jetpackcompose_playground

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PhotoViewModel : ViewModel() {

    private val _imageUri = MutableLiveData(Uri.parse(DEFAULT_IMAGE))
    val imageUri: LiveData<Uri?> = _imageUri

    fun updateUncompressedUri(uri: Uri?) {
        _imageUri.value = uri
    }

    companion object {
        const val DEFAULT_IMAGE =
            "https://news-cdn.varzesh3.com/pictures/2024/05/22/B/lsofyvjz.jpeg"
    }
}