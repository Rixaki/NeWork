package com.example.nework.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.nework.BuildConfig
import com.example.nework.R
import com.example.nework.auth.AppAuth
import com.example.nework.databinding.ActivityMainBinding
import com.example.nework.ui.MyProfileFragment.Companion.USER_ID
import com.example.nework.vm.AuthViewModel
import com.example.nework.vm.UsersSelectorViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val authModel : AuthViewModel by viewModels()
    private val usersSelector :
            UsersSelectorViewModel by viewModels()//for clearUserRepo()

    @Inject
    lateinit var appAuth: AppAuth

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapKitFactory.initialize(this)
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navBottomView: BottomNavigationView = binding.bottomNavigation
        navBottomView.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.posts -> {
                    findNavController(R.id.nav_main).navigate(R.id.action_global_to_postsFeedFragment)
                    true
                }
                R.id.events -> {
                    findNavController(R.id.nav_main).navigate(R.id.action_global_to_eventsFeedFragment)
                    true
                }
                R.id.users -> {
                    findNavController(R.id.nav_main).navigate(R.id.action_global_to_userFeedFragment)
                    true
                }
                R.id.my_profile -> {
                    if (authModel.authenticated) {
                        val myId = authModel.data.asLiveData().value!!.id//!=0 with authenticated
                        findNavController(R.id.nav_main).navigate(
                            R.id.action_global_to_myProfileFragment,
                            Bundle().apply {
                                USER_ID = myId
                            })
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.my_profile_access_error),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    true
                }
                else -> {false}
            }
        }
        navBottomView.menu.let{
            it.setGroupVisible(R.id.unauthenticated, !authModel.authenticated)
            it.setGroupVisible(R.id.authenticated, authModel.authenticated)
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                appAuth.authState.collect {
                    invalidateOptionsMenu()
                }
            }
        }

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                //menu.clear();
                menuInflater.inflate(R.menu.top_menu, menu)

                menu.let {
                    it.setGroupVisible(R.id.unauthenticated, !authModel.authenticated)
                    it.setGroupVisible(R.id.authenticated, authModel.authenticated)
                }
            }

            override fun onPrepareMenu(menu: Menu) {
                menu.setGroupVisible(
                    R.id.authenticated,
                    authModel.authenticated
                )
                menu.setGroupVisible(
                    R.id.unauthenticated,
                    !authModel.authenticated
                )
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.signIn -> {
                        findNavController(R.id.nav_main).navigate(R.id.action_global_to_signInFragment)
                        true
                    }

                    R.id.signUp -> {
                        findNavController(R.id.nav_main).navigate(R.id.action_global_to_signUpFragment)
                        true
                    }

                    R.id.signOut -> {
                        MaterialAlertDialogBuilder(this@MainActivity)
                            .setTitle("Signing Out")
                            .setMessage("Are you want to from your account?")
                            .setIcon(R.drawable.baseline_logout_48)
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Sign Out", ) { _,_ ->
                                appAuth.removeAuth()
                            }
                            .show()
                        true
                    }

                    else -> {false}
                }
        })//addMenuProvider

        val navController = findNavController(R.id.nav_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.posts,
                R.id.events,
                R.id.users,
                R.id.my_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navBottomView.setupWithNavController(navController)
    }

    override fun onStop() {
        super.onStop()
        MapKitFactory.getInstance().onStop()
    }

    override fun onDestroy() {
        lifecycleScope.launch {
            usersSelector.clearUserRepo()
        }
        super.onDestroy()
    }
}