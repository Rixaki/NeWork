package com.example.nework.vm

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.filter
import com.example.nework.api.AppApi
import com.example.nework.dao.UserDao
import com.example.nework.dto.Event
import com.example.nework.dto.User
import com.example.nework.model.ResponceState
import com.example.nework.repo.UserRepo
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


//ONLY SHOWING USER LIST
@HiltViewModel(assistedFactory = UsersViewModelFactory::class)
class UsersViewModel @AssistedInject constructor(
    private val appApi: AppApi,
    private val userRepo: UserRepo,
    private val userDao: UserDao,
    @Assisted private val userIds: List<Int> = emptyList(),
) : ViewModel() {
    private val _state = MutableStateFlow(ResponceState())
    val state = _state.asStateFlow()

    private val noList = emptyList<User>()
    private val _list = MutableStateFlow(noList)
    val list = _list.asStateFlow()

    init {
        getUsers()
        loadUsersByIds(userIds)
    }

    //get from api only for empty userRepo
    fun getUsers() {
        _state.update {
            ResponceState(
                loading = true,
            )
        }
        viewModelScope.launch {
            try {
                if (userDao.getSize() == 0) {
                    userRepo.getAll()//users from api
                }
            } catch (e: Exception) {
                _state.update {
                    ResponceState(
                        error = true,
                        lastErrorAction = "Api responce error with get users."
                    )
                }
            }
        }
    }

    private fun loadUsersByIds (listIds: List<Int>) {
        viewModelScope.launch {
            try {
                val result = listIds.map {
                    val response = appApi.getUserById(it)
                    response.body() ?: throw RuntimeException()
                }
                _state.update { ResponceState() }
                _list.value = result
            } catch (e: Exception) {
                _state.update {
                    ResponceState(
                        loading = false,
                        error = true,
                        lastErrorAction = "Converting from user identifiers error."
                    )
                }
            }
        }
    }

    fun getUserById(id: Int): User {
        var result = User(id = 0, login = "", name = "", avatar = null)
        viewModelScope.launch {
            try {
                result = userRepo.getUserById(id)
            } catch (e: Exception) {

            }
        }
        return result
    }

    fun clearModel() {
        _list.value = emptyList()
    }
}

@AssistedFactory
interface UsersViewModelFactory {
    fun create(date: List<Int>): UsersViewModel
}