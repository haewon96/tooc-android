package com.hyeran.android.travely_manager.mypage

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hyeran.android.travely_manager.MainActivity
import com.hyeran.android.travely_manager.R
import kotlinx.android.synthetic.main.fragment_tooc.*
import org.jetbrains.anko.support.v4.ctx

class ToocFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_tooc, container, false)
        return v
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        iv_back_tooc.setOnClickListener {
            (ctx as MainActivity).replaceFragment(SetFragment())
        }
    }
}