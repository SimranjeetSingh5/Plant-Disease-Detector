package com.ai.Agro.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ai.Agro.databinding.ActivityVerifyOtpBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import `in`.aabhasjindal.otptextview.OTPListener
import java.util.concurrent.TimeUnit


class VerifyOtpActivity : AppCompatActivity() {
    private var resendToken: PhoneAuthProvider.ForceResendingToken?=null
    private var verificationId: String? = null
    private lateinit var mAuth: FirebaseAuth
    private var currUserPhoneNumber:String? = null
    private lateinit var binding: ActivityVerifyOtpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        verificationId = intent.getStringExtra("verificationId")
        resendToken = intent.getParcelableExtra("resendToken")
        currUserPhoneNumber = intent.getStringExtra("mobile")
        mAuth = FirebaseAuth.getInstance()

        binding.resendText2.setOnClickListener {

            resendVerificationCode(currUserPhoneNumber!!,resendToken!!)
        }
        binding.verifyButton.setOnClickListener {
            if (binding.otpView.otp.toString().trim().isEmpty()) {
                Toast.makeText(this@VerifyOtpActivity, "Please Enter valid Otp", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            if (verificationId != null) {
                binding.progressBar.visibility = View.VISIBLE
                binding.verifyButton.visibility = View.INVISIBLE
                val credential = PhoneAuthProvider.getCredential(
                    verificationId!!,
                    binding.otpView.otp.toString()
                )
                signInWithPhoneAuthCredential(credential)
            }
        }
        binding.textMobile.setText(
            String.format(
                "+91-%s", intent.getStringExtra("mobile")
            )
        )

        binding.otpView.otpListener = (object : OTPListener {
            override fun onInteractionListener() {

            }

            override fun onOTPComplete(otp: String) {
                binding.verifyButton.isEnabled = true
            }
        })
    }
    private fun resendVerificationCode(
        phone: String,
        token: PhoneAuthProvider.ForceResendingToken
    ) {
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .setForceResendingToken(token)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d(MainActivity.TAG, "onVerificationCompleted:$credential")

        }

        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(this@VerifyOtpActivity, e.message, Toast.LENGTH_SHORT).show()
            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e is FirebaseTooManyRequestsException) {
                Toast.makeText(this@VerifyOtpActivity, "Sms Quota Exceeded", Toast.LENGTH_SHORT).show()
            }

        }

        override fun onCodeSent(
            newVerificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            Log.d(MainActivity.TAG, "onCodeSent:$verificationId")
            Toast.makeText(this@VerifyOtpActivity, "Otp Sent", Toast.LENGTH_SHORT).show()

        }
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                binding.progressBar.visibility = View.VISIBLE
                binding.verifyButton.visibility = View.INVISIBLE
                if (task.isSuccessful) {

                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                } else {
                    Toast.makeText(this@VerifyOtpActivity, task.exception!!.message, Toast.LENGTH_LONG).show()
                }
            }
    }
}