package com.example.nework.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.nework.dao.UserDao
import com.example.nework.dto.SelectableUser
import com.example.nework.dto.User
import com.example.nework.dto.UserPreview
import com.example.nework.model.ResponceState
import com.example.nework.repo.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

//SELECTING USER LIST
@HiltViewModel
class UsersSelectorViewModel @Inject constructor(
    private val userRepo: UserRepo,
    private val userDao: UserDao
) : ViewModel() {
    private val empty = emptyList<SelectableUser>()
    private val noState = ResponceState()

    private val _list = MutableLiveData(empty)
    val list: LiveData<List<SelectableUser>>
        get() = _list

    private val _usersState = MutableLiveData(noState)
    val usersState: LiveData<ResponceState>
        get() = _usersState

    val users: LiveData<List<User>> = userRepo.data.asLiveData()

    init {
        getUsers()
    }

    private fun getUsers() {
        _usersState.postValue(
            ResponceState(
                loading = true,
            )
        )
        viewModelScope.launch(Dispatchers.Default) {
            try {
                if (userDao.getSize() == 0) {
                    userRepo.getAll()//users from api
                }
                _usersState.postValue(ResponceState())
            } catch (e: Exception) {
                _usersState.postValue(
                    ResponceState(
                        error = true,
                        lastErrorAction = "Api responce error with get users."
                    )
                )
            }
        }
    }

    fun setAllSelectorList(ids: List<Long>) {
        viewModelScope.launch(Dispatchers.Default) {
            val result = users.value?.map {
                SelectableUser(
                    id = it.id,
                    user = UserPreview(
                        avatar = it.avatar ?: "404",
                        name = it.name
                    ),
                    isPicked = ids.contains(it.id)
                )
            } ?: empty
            _list.postValue(result)
        }
    }

    fun refresh() {
        _usersState.value = (ResponceState(loading = true))
        viewModelScope.launch(Dispatchers.Default) {
            try {
                userRepo.getAll()
                _usersState.value = ResponceState()
            } catch (e: Exception) {
                _usersState.value = ResponceState(
                    error = true,
                    lastErrorAction = "Error with list refresh."
                )
            }
        }
    }

    fun sortList() {
        _list.value = _list.value?.sortedBy { !it.isPicked }//picked first
    }

    //for post/event edit
    fun getPickedIdsList(list: List<SelectableUser>): List<Long> {
        return list.filter { it.isPicked }.map { it.id }
    }

    fun clearModels() {
        _list.value = empty
        _usersState.value = noState
    }

    suspend fun clearUserRepo() = userRepo.clear()
}