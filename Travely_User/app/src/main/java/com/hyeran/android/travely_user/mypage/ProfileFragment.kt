package com.hyeran.android.travely_user.mypage

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.hyeran.android.travely_user.R
import com.hyeran.android.travely_user.SplashActivity
import com.hyeran.android.travely_user.model.ProfileResponseData
import com.hyeran.android.travely_user.network.ApplicationController
import com.hyeran.android.travely_user.network.NetworkService
import kotlinx.android.synthetic.main.fragment_profile.*

import java.util.regex.Pattern
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.regex.Matcher

class ProfileFragment : Fragment() {

    lateinit var networkService: NetworkService
    private var mImage: MultipartBody.Part? = null

    val REQUEST_CODE_SELECT_IMAGE : Int = 1004

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_profile, container, false)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setClickListener()
        checkValidation()
        init()
//        getProfileResponse()
    }

    private fun init() {
        networkService = ApplicationController.instance.networkService
    }

    private fun setClickListener() {
        btn_logout_profile.setOnClickListener {
            SharedPreferencesController.instance!!.removeAllData(this!!.context!!)

            val intent = Intent(activity, SplashActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            startActivity(intent)
        }



        tv_modification_profile.setOnClickListener {

            if (tv_modification_profile.text == "수정") {   //edittext 막기
                tv_modification_profile.setText("완료")

                et_name_profile.isEnabled = true
                et_email_profile.isEnabled = true
                et_password_profile.isEnabled = true
                et_password_confirm_profile.isEnabled = true
                et_phone_profile.isEnabled = true

            } else if (tv_modification_profile.text == "완료") {   //edittext 수정 가능
                tv_modification_profile.setText("수정")
                et_name_profile.isEnabled = false

                et_name_profile.isEnabled = false
                et_email_profile.isEnabled = false
                et_password_profile.isEnabled = false
                et_password_confirm_profile.isEnabled = false
                et_phone_profile.isEnabled = false
            }
        }

        iv_photo_modify_profile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = android.provider.MediaStore.Images.Media.CONTENT_TYPE
            intent.data = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
        }

    }


    // 유효성 검사
    private fun checkValidation() {

        var name_validation = false
        var email_validation = false
        var pw_validation = false
        var pw_confirm_validation = false

        // 이름: 공백인지 아닌지
        et_name_profile.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                name_validation = et_name_profile.text.toString().trim().isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 이메일: 이메일 형식인지 아닌지
        et_email_profile.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (checkEmail(et_email_profile.text.toString().trim())) {
                    iv_email_check_profile.visibility = View.VISIBLE
                    email_validation = true
                }
                else {
                    iv_email_check_profile.visibility = View.INVISIBLE
                    email_validation = true
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 패스워드: 8-20자, 영어+번호+특수문자
        et_password_profile.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (et_password_profile.text.toString().length != 0) {
                    iv_password_check_profile.visibility = View.VISIBLE
                } else if (et_password_profile.text.toString().length == 0) {
                    iv_password_check_profile.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 패스워드 확인
        et_password_confirm_profile.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (et_password_confirm_profile.text.toString().length != 0) {
                    if (et_password_profile.text.toString() == et_password_confirm_profile.text.toString()) {
                        iv_passwordconfirm_check_profile.visibility = View.VISIBLE
                        iv_passwordconfirm_x_profile.visibility = View.GONE
                    } else {
                        iv_passwordconfirm_check_profile.visibility = View.GONE
                        iv_passwordconfirm_x_profile.visibility = View.VISIBLE
                    }
                } else if (et_password_confirm_profile.text.toString().length == 0) {
                    iv_passwordconfirm_check_profile.visibility = View.GONE
                    iv_passwordconfirm_x_profile.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun checkEmail(email : String) : Boolean{
        val regex = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$"
        val p : Pattern = Pattern.compile(regex)
        val m : Matcher = p.matcher(email)
        val isNormal : Boolean = m.matches()
        return isNormal
    }




}