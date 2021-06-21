package com.parthiv.test.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parthiv.test.data.core.ApiError
import com.parthiv.test.data.core.Success
import com.parthiv.test.network.MainRepositoryContract
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val mainRepositoryContract: MainRepositoryContract) :
    ViewModel() {

    private val _data = MutableLiveData<String>()

    val data: LiveData<String>
        get() = _data

    fun getData() = viewModelScope.launch {
        when (val apiData = mainRepositoryContract.getUser()) {
            is Success -> {
                _data.postValue(apiData.data.data?.name)
            }
            is ApiError -> {
                _data.postValue("Error")
            }
        }
    }
}