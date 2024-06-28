package com.example.nework.ui

import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.nework.BuildConfig
import com.example.nework.R
import com.example.nework.auth.AppAuth
import com.example.nework.databinding.ActivityMainBinding
import com.example.nework.ui.UserFragment.Companion.USER_ID
import com.example.nework.util.MenuItemTarget
import com.example.nework.vm.AuthViewModel
import com.example.nework.vm.UsersSelectorViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.yandex.mapkit.MapKitFactory
import com.yandex.runtime.connectivity.internal.ConnectivitySubscription
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val authModel: AuthViewModel by viewModels()
    private val usersSelector:
            UsersSelectorViewModel by viewModels()//for clearUserRepo()

    @Inject
    lateinit var appAuth: AppAuth

    override fun onStart() {
        super.onStart()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            com.yandex.runtime.Runtime.getApplicationContext().registerReceiver(
                ConnectivitySubscription(), IntentFilter(), RECEIVER_EXPORTED/RECEIVER_NOT_EXPORTED);
        } else {
            com.yandex.runtime.Runtime.getApplicationContext().registerReceiver(
                ConnectivitySubscription(), IntentFilter());
        }

        /*
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                println("on receive") //do something with intent
            }
        }
        ContextCompat.registerReceiver(
            this,
            receiver,
            IntentFilter(),
            ContextCompat.RECEIVER_EXPORTED
        )
         */

        MapKitFactory.getInstance().onStart()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        MapKitFactory.initialize(this)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //val navHostFragment =
        //    supportFragmentManager.findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment
        //val navController = navHostFragment.navController
        val navController = binding.myNavHostFragment.getFragment<Fragment>().findNavController()

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        /*
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.posts,
                R.id.events,
                R.id.users,
                R.id.my_profile
            )
        )
         */

        val navBottomView: NavigationBarView = binding.bottomNavigation
        navBottomView.setupWithNavController(navController)
        if (authModel.authenticated) {
            with(navBottomView.menu.findItem(R.id.my_profile)) {
                this.isVisible = true

                //avatar icon in BottomNavigationView (draft)
                //https://habr.com/ru/articles/697578/
                lifecycleScope.launch(Dispatchers.Default) {
                    //supervisorScope { //is it mem-leakable?
                        Glide.with(this@MainActivity)
                            .asBitmap()
                            .load(authModel.data.asLiveData(Dispatchers.Default)
                                .value?.avatarUrl ?: "404")
                            .placeholder(R.drawable.baseline_account_circle_48)
                            .error(R.drawable.baseline_account_circle_48)
                            .apply(
                                RequestOptions.circleCropTransform()
                            )
                            .into(MenuItemTarget(this@MainActivity, this@with))
                    //}
                }
            }
        }
        navBottomView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.posts -> {
                    navController.navigate(R.id.action_global_to_postsFeedFragment)
                    true
                }

                R.id.events -> {
                    navController.navigate(R.id.action_global_to_eventsFeedFragment)
                    true
                }

                R.id.users -> {
                    navController.navigate(R.id.action_global_to_userFeedFragment)
                    true
                }

                R.id.my_profile -> {
                    //item.isVisible = authModel.authenticated
                    if (authModel.authenticated) {
                        val myId = authModel.data.asLiveData().value!!.id//!=0 with authenticated
                        navController.navigate(
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

                else -> {
                    false
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                appAuth.authState.collect {
                    invalidateOptionsMenu()
                    navBottomView.menu.findItem(R.id.my_profile).isVisible = authModel.authenticated
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
                        navController.navigate(R.id.action_global_to_signInFragment)
                        true
                    }

                    R.id.signUp -> {
                        navController.navigate(R.id.action_global_to_signUpFragment)
                        true
                    }

                    R.id.signOut -> {
                        MaterialAlertDialogBuilder(this@MainActivity)
                            .setTitle("Signing Out")
                            .setMessage("Are you want to from your account?")
                            .setIcon(R.drawable.baseline_logout_48)
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Sign Out") { _, _ ->
                                appAuth.removeAuth()
                            }
                            .show()
                        true
                    }

                    else -> {
                        false
                    }
                }
        })//addMenuProvider

        //setupActionBarWithNavController(navController, appBarConfiguration)
        //navBottomView.setupWithNavController(navController)
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