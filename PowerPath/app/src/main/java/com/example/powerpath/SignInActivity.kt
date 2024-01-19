package com.example.powerpath

import android.content.SharedPreferences
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
import com.google.firebase.firestore.FirebaseFirestore


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

    private var mode = 0 //0 = signup, 1 = login

    private lateinit var firestore: FirebaseFirestore

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        firestore = FirebaseFirestore.getInstance()

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
                } else if (mode == 0 && isEmailUsed()) {
                    tvEmailError.visibility = View.VISIBLE
                    tvEmailError.text = resources.getText(R.string.text_email_already_used)
                } else {
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
                saveCredentials()
            }

            if (tvEmailError.visibility != View.VISIBLE && tvPasswordError.visibility!= View.VISIBLE) {
                doSignUp()
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
        if (email != null && password != null) {
            doLogIn()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        changeToSignUp()
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
        tvForgotPassword.visibility = View.VISIBLE
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

    private fun getPasswordFromEmail(email: String): String {
        //TODO
        return ""
    }

    private fun forgotPasswordAction() {
        //TODO
        //a.se
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

    private fun isEmailUsed(): Boolean {
        //TODO
        return false
    }

    private fun saveCredentials() {
        //TODO
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
        val user: MutableMap<String, Any> = mutableMapOf()
        user["email"] = etEmail.text
        user["password"] = etPassword.text

        firestore.collection("users")
            .add(user)
            .addOnSuccessListener { _ ->
                Toast.makeText(this@SignInActivity, "Yes", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { _ ->
                Toast.makeText(this@SignInActivity, "No", Toast.LENGTH_SHORT).show()
            }
    }

    private fun doLogIn() {
//        db.collection("client")
//            .get()
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    for (doc in task.result!!) {
//                        val a = doc.getString("Email")
//                        val b = doc.getString("Password")
//                        val a1 = etEmail.text.toString().trim()
//                        val b1 = etPassword.text.toString().trim()
//
//                        if (a.equals(a1, ignoreCase = true) && b == b1) {
//                            val home = Intent(this@SignInActivity, MainActivity::class.java)
//                            startActivity(home)
//                            Toast.makeText(this@SignInActivity, "Logged In", Toast.LENGTH_SHORT).show()
//                            break
//                        } else {
//                            Toast.makeText(this@SignInActivity, "Cannot login, incorrect Email and Password", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                }
//            }
    }
}


