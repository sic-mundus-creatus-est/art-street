package edu.rmas.artstreet.view_models

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import edu.rmas.artstreet.data.models.User
import edu.rmas.artstreet.data.repositories.AuthRepo
import edu.rmas.artstreet.data.repositories.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthVM() : ViewModel()
{
    val repo = AuthRepo()
    private val _signInFlow = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val signInFlow: StateFlow<Resource<FirebaseUser>?> = _signInFlow

    private val _signUpFlow = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val signUpFlow: StateFlow<Resource<FirebaseUser>?> = _signUpFlow

    private val _currentUserFlow = MutableStateFlow<Resource<User>?>(null)
    val currentUserFlow: StateFlow<Resource<User>?> = _currentUserFlow

    private val _allUsersFlow = MutableStateFlow<Resource<List<User>>?>(null)
    val allUsers: StateFlow<Resource<List<User>>?> = _allUsersFlow

    private val _userByIdFlow = MutableStateFlow<Resource<User>?>(null)
    val userById: StateFlow<Resource<User>?> = _userByIdFlow

    val currentUser: FirebaseUser?
        get() = repo.user

    fun getUserData() = viewModelScope.launch {
        val result = repo.getUser()
        _currentUserFlow.value = result
    }

    fun getAllUsersData() = viewModelScope.launch {
        val result = repo.getAllUsers()
        _allUsersFlow.value = result
    }

    init {
        if(repo.user != null){
            _signInFlow.value = Resource.Success(repo.user!!)
        }
    }

    fun signIn(email: String, password: String) = viewModelScope.launch{
        _signInFlow.value = Resource.Loading
        val result = repo.signIn(email, password)
        _signInFlow.value = result
    }

    fun signUp(fullName: String, phoneNumber: String, profileImage: Uri, email: String, username: String, password: String) = viewModelScope.launch {
        _signUpFlow.value = Resource.Loading
        val result = repo.signUp(fullName, phoneNumber, profileImage, email, username, password)
        _signUpFlow.value = result
    }

    fun signOut()
    {
        repo.signOut()
        _signInFlow.value = null
        _signUpFlow.value = null
        _currentUserFlow.value = null
    }

    fun getUserById(userId: String) = viewModelScope.launch {
        _userByIdFlow.value = Resource.Loading
        val result = repo.getUserById(userId)
        _userByIdFlow.value = result
    }
}

class AuthVMFactory : ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T
    {
        if(modelClass.isAssignableFrom(AuthVM::class.java))
        {
            return AuthVM() as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}