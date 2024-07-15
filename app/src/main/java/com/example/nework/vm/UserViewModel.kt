package com.example.nework.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nework.api.AppApi
import com.example.nework.dao.UserDao
import com.example.nework.dto.User
import com.example.nework.entity.UserEntity
import com.example.nework.model.ResponceState
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
                val response = appApi.getUserById(id)
                val user = response.body()
                _user.postValue(user)
                if (user != null) {userDao.insert(UserEntity.fromDto(user))}
            } catch (_: Exception) {
                try {
                    _state.update {
                        ResponceState(
                            error = true,
                            lastErrorAction = "Error with finding user in API (id ${id}). Trying to find user in local bd..."
                        )
                    }
                    _state.update {
                        ResponceState()
                    }
                    val user = userDao.getUserById(id).toDto()
                    _user.postValue(user)
                } catch (e: Exception) {
                    _state.update {
                        ResponceState(
                            error = true,
                            lastErrorAction = "Error with finding user in local bd (id ${id})."
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
