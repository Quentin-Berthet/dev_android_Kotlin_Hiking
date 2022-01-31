package com.example.tp5_hiking.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.tp5_hiking.Auth
import com.example.tp5_hiking.activities.MenuActivity
import com.example.tp5_hiking.R
import com.example.tp5_hiking.models.HikingDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.ktorm.dsl.eq
import org.ktorm.entity.find


/**
 * A placeholder fragment containing a simple view.
 */
class LoginFragment : Fragment() {
    private lateinit var edtPseudo: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogin: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edtPseudo = view.findViewById(R.id.edtLoginPseudo)
        edtPassword = view.findViewById(R.id.edtLoginPassword)
        btnLogin = view.findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener(this::btnLoginOnClicked)
        if (Auth.getCurrentUser(view.context) != null) {
            val i = Intent(view.context, MenuActivity::class.java)
            startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        }
    }

    private fun btnLoginOnClicked(view: View) {
        GlobalScope.launch {
            if (Looper.myLooper() == null) {
                Looper.prepare()
            }
            val pseu = edtPseudo.text.toString()
            val pass = edtPassword.text.toString()
            val user = HikingDatabase.users.find { it.pseudo eq pseu }
            if (user == null) {
                showToast(getString(R.string.invalid_pseudo_password))
                return@launch
            }
            val result = BCrypt.verifyer().verify(pass.toCharArray(), user.password)
            if (!result.verified) {
                showToast(getString(R.string.invalid_pseudo_password))
                return@launch
            }
            Auth.setCurrentUser(view.context, user)
            val i = Intent(view.context, MenuActivity::class.java)
            startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show()
    }

    companion object {
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }
}
