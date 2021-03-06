package com.tooc.android.tooc.join

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.tooc.android.tooc.MainActivity
import com.tooc.android.tooc.R
import com.tooc.android.tooc.model.reservation.UsersLoginResponseData
import com.tooc.android.tooc.network.ApplicationController
import com.tooc.android.tooc.network.NetworkService
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.ctx
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    lateinit var networkService : NetworkService
    var backKeyPressedTime: Long = 0

    override fun onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis()
            showGuide()
            return
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish()
        }
        super.onBackPressed()
    }

    private fun showGuide() {
        var toast: Toast? = Toast.makeText(ctx, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_LONG)
        toast!!.show()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        init()
    }

    fun init() {
        networkService = ApplicationController.instance.networkService
        setOnClickListener()

    }

    private fun setOnClickListener() {
        tv_join_login.setOnClickListener {
            val intent = Intent(applicationContext, JoinActivity::class.java)
            startActivity(intent)
        }

        btn_login_login.setOnClickListener {
            postLoginResponse()
        }
    }

    private fun postLoginResponse() {
        val input_email = et_email_login.text.toString().trim()
        val input_pw = et_password_login.text.toString().trim()

        var jsonObject = JSONObject()
        jsonObject.put("email", input_email)
        jsonObject.put("password", input_pw)

        val gsonObject = JsonParser().parse(jsonObject.toString()) as JsonObject

        val postLoginResponse = networkService.postLoginResponse("application/json", gsonObject)


        postLoginResponse!!.enqueue(object : Callback<UsersLoginResponseData> {
            override fun onFailure(call: Call<UsersLoginResponseData>, t: Throwable) {
                Log.d("Log::LoginActivity","onFailure")

            }
            override fun onResponse(call: Call<UsersLoginResponseData>, response: Response<UsersLoginResponseData>) {
                response?.let {
                    when (it.code()) {
                        200 -> {
                            Log.d("TAGGGGGG","200")

                            SharedPreferencesController.instance!!.setPrefData("jwt", response.headers().value(0))
                            SharedPreferencesController.instance!!.setPrefData("auto_login", true)
                            SharedPreferencesController.instance!!.setPrefData("user_email", input_email)
                            SharedPreferencesController.instance!!.setPrefData("user_pw", input_pw)
                            SharedPreferencesController.instance!!.setPrefData("is_reserve", response.body()!!.isReserve)
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        }
                        403 -> {
                            Log.d("TAGGGGGG","403")

                        }
                        500 -> {
                            Log.d("TAGGGGGG","500")

                        }
                        else -> {
                            Log.d("TAGGGGGG","else")
                        }
                    }
                }
            }

        })
    }
}
