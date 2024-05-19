package com.example.powerpath

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.powerpath.userData.NewDataManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {

    @Inject
    lateinit var dataManager: NewDataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("PowerPathPrefs", MODE_PRIVATE)
        val savedEmail = sharedPreferences.getString("email", "")
        DataManager.email = savedEmail.toString()
        dataManager.email = savedEmail.toString()

        if (savedEmail.isNullOrEmpty()) {
            startActivity(Intent(this, SignInActivity::class.java))
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }

        finish()
    }
}
