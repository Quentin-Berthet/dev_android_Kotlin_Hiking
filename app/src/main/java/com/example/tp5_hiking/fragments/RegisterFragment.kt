package com.example.tp5_hiking.fragments

import android.database.sqlite.SQLiteConstraintException
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
import com.example.tp5_hiking.models.HikingDatabase
import com.example.tp5_hiking.R
import com.example.tp5_hiking.Utils
import com.example.tp5_hiking.models.User
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.ktorm.entity.add
import java.sql.SQLException

/**
 * A placeholder fragment containing a simple view.
 */
class RegisterFragment : Fragment() {
    private lateinit var edtPseudo: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnRegister: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edtPseudo = view.findViewById(R.id.edtRegisterPseudo)
        edtPassword = view.findViewById(R.id.edtRegisterPassword)
        btnRegister = view.findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener(this::btnRegisterOnClicked)
    }

    private fun btnRegisterOnClicked(view: View) {
        GlobalScope.launch {
            if (Looper.myLooper() == null) {
                Looper.prepare()
            }

            val pseu = edtPseudo.text.toString()
            val pass = edtPassword.text.toString()
            if (pseu.isEmpty() || pseu.length > Utils.PSEUDO_MAX_LENGTH) {
                showToast(getString(R.string.pseudo_length))
                return@launch
            }
            if (pass.isEmpty()) {
                showToast(getString(R.string.password_not_empty))
                return@launch
            }
            val bcryptHashPassword = BCrypt.withDefaults().hashToString(Utils.BCRYPT_COST, pass.toCharArray())
            try {
                val user = User {
                    pseudo = edtPseudo.text.trim().toString()
                    password = bcryptHashPassword
                }
                HikingDatabase.users.add(user)
                showToast(getString(R.string.you_can_login))
            } catch (se: SQLException) {
                when (se.cause) {
                    is SQLiteConstraintException -> showToast(getString(R.string.pseudo_already_taken))
                    else -> showToast(getString(R.string.error_occurred_registration))
                }
            }
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
        fun newInstance(): RegisterFragment {
            return RegisterFragment()
        }
    }
}
