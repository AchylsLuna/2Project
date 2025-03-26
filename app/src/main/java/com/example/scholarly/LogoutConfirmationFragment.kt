package com.example.scholarly

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment

class LogoutConfirmationFragment : DialogFragment() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_logout_confirmation, container, false)
        sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        val btnYes = view.findViewById<Button>(R.id.btnYesLogout)
        val btnNo = view.findViewById<Button>(R.id.btnNoLogout)

        btnYes.setOnClickListener {
            performLogout()
        }

        btnNo.setOnClickListener {
            dismiss() // Close the dialog if the user selects "No"
        }

        return view
    }

    private fun performLogout() {
        sharedPreferences.edit().clear().apply()
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}
