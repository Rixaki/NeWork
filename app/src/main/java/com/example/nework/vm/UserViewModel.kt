package com.example.nework.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nework.api.AppApi
import com.example.nework.dao.UserDao
import com.example.nework.dto.User
import com.example.nework.model.ResponceState
import com.example.nework.repo.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

val defUser = User(id = 0, login = "", name = "", avatar = "404")

@HiltViewModel()
class UserViewModel @Inject constructor(
    private val appApi: AppApi,
    private val userRepo: UserRepo,
    private val userDao: UserDao,
) : ViewModel() {
    private val _state = MutableStateFlow(ResponceState())
    val state = _state.asStateFlow()

    private val _user = MutableLiveData(defUser)
    val user : LiveData<User>
        get() = _user

    fun setUserById(id: Long) {
        viewModelScope.launch {
            try {
                _user.postValue(userRepo.getUserById(id))
            } catch (_: Exception) {
                try {
                    val response = appApi.getUserById(id)
                    _user.postValue(response.body())
                } catch (e: Exception) {
                    _state.update {
                        ResponceState(
                            error = true,
                            lastErrorAction = "Api responce error with finding user (id ${id})."
                        )
                    }
                }
            }
        }
    }

    fun clearModel() {
        _user.postValue(defUser)
    }
}
