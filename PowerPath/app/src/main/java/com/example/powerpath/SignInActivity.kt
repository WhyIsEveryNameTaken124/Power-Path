package com.example.powerpath

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.powerpath.retrofitApi.ApiServiceImpl
import com.example.powerpath.userData.NewDataManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvEmail: TextView
    private lateinit var etEmail: EditText
    private lateinit var tvEmailError: TextView
    private lateinit var tvPassword: TextView
    private lateinit var etPassword: EditText
    private lateinit var tvPasswordError: TextView
    private lateinit var tvForgotPassword: TextView
    private lateinit var cbRememberMe: CheckBox
    private lateinit var buttonSignUp: Button
    private lateinit var tvLogIn: TextView

    @Inject
    lateinit var dataManager: NewDataManager

    private var mode = 0 //0 = signup, 1 = login

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.navigationBarColor = ContextCompat.getColor(this, R.color.background)

        tvTitle = findViewById(R.id.tvTitle)
        tvEmail = findViewById(R.id.tvEmail)
        etEmail = findViewById(R.id.etEmail)
        tvEmailError = findViewById(R.id.tvEmailError)
        tvPassword = findViewById(R.id.tvPassword)
        etPassword = findViewById(R.id.etPassword)
        tvPasswordError = findViewById(R.id.tvPasswordError)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        cbRememberMe = findViewById(R.id.cbRememberMe)
        buttonSignUp = findViewById(R.id.button)
        tvLogIn = findViewById(R.id.tvLogIn)

        setLogInText()
        setForgotPasswordText()

        etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(etEmail.text.toString()).matches()) {
                    tvEmailError.visibility = View.VISIBLE
                    tvEmailError.text = resources.getText(R.string.text_enter_valid_email)
                }  else {
                    tvEmailError.visibility = View.GONE
                }
            }
        }

        etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                // The EditText has lost focus (been clicked off)
                // Add your code to handle the event here
            }
        }

        buttonSignUp.setOnClickListener {
            if (cbRememberMe.isChecked) {
                val sharedPreferences = getSharedPreferences("LogIn", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("email", etEmail.text.toString())
                editor.putString("password", etPassword.text.toString())
                editor.apply()
            }

            if (mode == 0) {
                doSignUp()
            } else {
                doLogIn()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val sharedPreferences = getSharedPreferences("LogIn", MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "")
        val password = sharedPreferences.getString("password", "")
        etEmail.setText(email)
        etPassword.setText(password)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (mode == 1) {
            changeToSignUp()
        }
    }

    private fun changeToLogIn() {
        mode = 1
        tvTitle.text = resources.getText(R.string.text_log_in)
        buttonSignUp.text = resources.getText(R.string.text_log_in)
        etEmail.setText("")
        etEmail.clearFocus()
        tvEmailError.visibility = View.GONE
        etPassword.setText("")
        etPassword.clearFocus()
        tvPasswordError.visibility = View.GONE
        cbRememberMe.isChecked = false
        tvForgotPassword.visibility = View.GONE
        tvLogIn.visibility = View.GONE
    }

    private fun changeToSignUp() {
        mode = 0
        tvTitle.text = resources.getText(R.string.text_sign_up)
        buttonSignUp.text = resources.getText(R.string.text_sign_up)
        etEmail.setText("")
        etEmail.clearFocus()
        tvEmailError.visibility = View.GONE
        etPassword.setText("")
        etPassword.clearFocus()
        tvPasswordError.visibility = View.GONE
        cbRememberMe.isChecked = false
        tvForgotPassword.visibility = View.GONE
        tvLogIn.visibility = View.VISIBLE
    }

    private fun forgotPasswordAction() {
        //TODO
    }

    private fun setLogInText() {
        val text = resources.getText(R.string.text_have_account)
        val spannableString = SpannableString(text)
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                changeToLogIn()
            }
        }
        spannableString.setSpan(clickableSpan, 25, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val underlineSpan = UnderlineSpan()
        spannableString.setSpan(underlineSpan, 25, 31, 0)
        val colorSpan = ForegroundColorSpan(Color.RED)
        spannableString.setSpan(colorSpan, 25, 31, 0)
        tvLogIn.text = spannableString
        tvLogIn.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setForgotPasswordText() {
        tvForgotPassword.paintFlags = tvForgotPassword.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        tvForgotPassword.setOnClickListener {
            forgotPasswordAction()
        }
    }

    private fun validateFields(): Boolean {
        if (etEmail.text.isNullOrEmpty()) {
            Toast.makeText(this@SignInActivity, R.string.text_no_email, Toast.LENGTH_SHORT).show()
            return false
        }
        if (etPassword.text.isNullOrEmpty()) {
            Toast.makeText(this@SignInActivity, R.string.text_no_password, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun doSignUp() {
        val apiService = ApiServiceImpl()
        apiService.signup(etEmail.text.toString(), etPassword.text.toString(), {
            dataManager.email = etEmail.text.toString()
            if (cbRememberMe.isChecked) {
                val sharedPreferences = getSharedPreferences("PowerPathPrefs", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("email", etEmail.text.toString())
                editor.apply()
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }, {
            runOnUiThread {
                Toast.makeText(this@SignInActivity, "signup error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun doLogIn() {
        val apiService = ApiServiceImpl()
        apiService.login(etEmail.text.toString(), etPassword.text.toString(), {
            dataManager.email = etEmail.text.toString()
            if (cbRememberMe.isChecked) {
                val sharedPreferences = getSharedPreferences("PowerPathPrefs", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("email", etEmail.text.toString())
                editor.apply()
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }, {
            runOnUiThread {
                Toast.makeText(this@SignInActivity, "login error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}


