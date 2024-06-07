package com.example.nework.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.nework.api.AppApi
import com.example.nework.dao.UserDao
import com.example.nework.dto.SelectableUser
import com.example.nework.dto.User
import com.example.nework.dto.UserPreview
import com.example.nework.model.ResponceState
import com.example.nework.repo.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

//SELECTING USER LIST
@HiltViewModel
class UsersSelectorViewModel @Inject constructor(
    private val appApi: AppApi,
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

    val users : LiveData<List<User>> = userRepo.data.asLiveData()

    init {
        getUsers()
    }

    fun getUsers() {
        _usersState.postValue(
            ResponceState(
                loading = true,
            )
        )
        viewModelScope.launch {
            try {
                if (userDao.getSize() == 0) {
                    userRepo.getAll()//users from api
                }
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

    fun setAllSelectorList(ids: List<Int>) {
        viewModelScope.launch {
            if (users.value != emptyList<User>()) {
                val result = users.value!!.map {
                    SelectableUser(
                        id = it.id,
                        user = UserPreview(
                            avatar = it.avatar ?: "404",
                            name = it.name
                        ),
                        isPicked = ids.contains(it.id)
                    )
                }
                _list.postValue(result)
            }
        }
    }

    /*
    fun changeList(newUserSelectableList: List<SelectableUser>) {
        _list.value = newUserSelectableList.sortedBy { !it.isPicked }//picked first
    }
     */

    fun sortList() {
        _list.value = _list.value?.sortedBy { !it.isPicked }//picked first
    }

    //for post/event edit
    fun getPickedIdsList(list: List<SelectableUser>) : List<Int> {
        return list.filter{ it.isPicked }.map{ it.id }
    }

    fun clearModels() {
        _list.value = empty
        _usersState.value = noState
    }

    suspend fun clearUserRepo() = userRepo.clear()
}