package com.example.scholarly

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var studentIdEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordTextView: TextView
    private lateinit var permissionButton: Button

    // Permission launcher for notifications
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("FCM", "Permission granted via launcher, fetching token")
            getFCMToken()
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize AdMob
        initializeAdMob()

        // Initialize views
        initializeViews()

        // Set click listeners
        setupListeners()
    }

    private fun initializeAdMob() {
        MobileAds.initialize(this) { initializationStatus ->
            Log.d("AdMob", "AdMob initialized with status: $initializationStatus")
        }
    }

    private fun initializeViews() {
        studentIdEditText = findViewById(R.id.username)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.signInbtn)
        forgotPasswordTextView = findViewById(R.id.btnforgot)
        permissionButton = findViewById(R.id.btnPermission)
    }

    private fun setupListeners() {
        forgotPasswordTextView.setOnClickListener {
            startActivity(Intent(this, ForgotPassword::class.java))
        }

        loginButton.setOnClickListener {
            val studentId = studentIdEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (studentId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter Student ID and Password", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(studentId, password)
            }
        }
    }

    // Called when btnPermission is clicked
    fun requestPermission(view: View) {
        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("FCM", "Notification permission already granted, fetching token")
                    getFCMToken()
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS) -> {
                    Toast.makeText(this, "Please allow notifications for FCM", Toast.LENGTH_SHORT).show()
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    Log.d("FCM", "Requesting notification permission")
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            Log.d("FCM", "Pre-Android 13, fetching token directly")
            getFCMToken()
        }
    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                Toast.makeText(this, "Failed to fetch FCM token: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            val msg = getString(R.string.msg_token_fmt, token)
            Log.d("FCM", "Generated Token: $token") // Log the raw token
            Toast.makeText(this, "Token: $token", Toast.LENGTH_LONG).show() // Show full token in Toast

            // Optional: Store the token if you need to "give" it somewhere
            saveToken(token)
        })
    }

    private fun saveToken(token: String) {
        // Example: Save to SharedPreferences
        getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().apply {
            putString("FCM_TOKEN", token)
            apply()
        }
        Log.d("FCM", "Token saved to SharedPreferences")
    }

    private fun loginUser(studentId: String, password: String) {
        val apiService = ApiClient.retrofit.create(APIService::class.java)

        apiService.loginUser(LoginRequest(studentId, password))
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { responseBody ->
                            if (responseBody.success) {
                                saveUserData(responseBody)
                                startActivity(Intent(this@MainActivity, LogsActivity::class.java))
                                finish()
                            } else {
                                showToast("Login failed: ${responseBody.message}")
                            }
                        }
                    } else {
                        showToast("Login failed: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    showToast("Login failed: ${t.message}")
                }
            })
    }

    private fun saveUserData(responseBody: LoginResponse) {
        getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().apply {
            putString("SESSION_TOKEN", responseBody.token)
            responseBody.student?.let { student ->
                putString("STUDENT_ID", student.studentId)
                putString("STUDENT_NAME", student.name)
                putString("SCHOLARSHIP_TYPE", student.scholarshipType)
                putString("COURSE", student.course)
                putString("DEPARTMENT", student.department)
                putString("DUTY_STATUS", student.hkDutyStatus)
            }
            apply()
        }
    }

    private fun showToast(message: String?) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }
}