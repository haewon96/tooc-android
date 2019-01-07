package com.hyeran.android.travely_user.mypage

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hyeran.android.travely_user.MainActivity
import com.hyeran.android.travely_user.R
import com.hyeran.android.travely_user.adapter.MypageMyReviewAdapter
import com.hyeran.android.travely_user.model.mypage.ReviewLookupData
import com.hyeran.android.travely_user.network.ApplicationController
import com.hyeran.android.travely_user.network.NetworkService
import kotlinx.android.synthetic.main.fragment_myreview.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyreviewFragment:Fragment() {
    lateinit var networkService : NetworkService

    lateinit var mypageMyReviewAdapter: MypageMyReviewAdapter

    lateinit var v : View

    val dataList : ArrayList<ReviewLookupData> by lazy {
        ArrayList<ReviewLookupData>()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_myreview, container, false)
        init()
        getReviewLookupResponse()
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setRecyclerView()

        iv_back_mypage.setOnClickListener {
            (ctx as MainActivity).replaceFragment(MypageFragment())
        }
    }

    private fun init() {
        networkService = ApplicationController.instance.networkService
    }

    private fun getReviewLookupResponse() {
        var jwt : String? = SharedPreferencesController.instance!!.getPrefStringData("jwt")
        var getReviewLookupResponse = networkService.getReviewLookupResponse(jwt)
        getReviewLookupResponse!!.enqueue(object : Callback<ArrayList<ReviewLookupData>>{
            override fun onFailure(call: Call<ArrayList<ReviewLookupData>>, t: Throwable) {
                toast(t.message.toString())
                Log.d("TAGG","In MtreviewFragment Communication onFail : "+t.message)
                Log.d("TAGGGGG","In MtreviewFragment Communication onFail : "+t.message)
                Log.e("TAGGGGG","In MtreviewFragment Communication onFail : "+t.message)
            }
            override fun onResponse(call: Call<ArrayList<ReviewLookupData>>, response: Response<ArrayList<ReviewLookupData>>) {
                response?.let {
                    toast("4")
                    when (it.code()) {
                        200 -> {
                            toast("아싸성공")



                            response.body()!![0].content
//                            var dataList_review : ArrayList<ReviewLookupData> = response.body()!!.reviewLookup

//                            if (dataList_review.size > 0) {
//                                val position = mypageMyReviewAdapter.itemCount
//                                mypageMyReviewAdapter.dataList.addAll(dataList_review)
//                                mypageMyReviewAdapter.notifyItemInserted(position)
//                            } else {
//                            }
                            toast("리뷰 조회 성공")
                        }
                        204 -> {
                            toast("작성 리뷰 없음")
                        }
                        400 -> {
                            toast("잘못된 접근")
                        }
                        500 -> {
                            toast("서버에러")
                        }
                        else -> {
                            toast("error")
                        }
                    }
                }
            }
        })
    }

    private fun setRecyclerView() {
//        var dataList: ArrayList<MypageMyReviewData> = ArrayList()
//        dataList.add(MypageMyReviewData("동대문엽기떡볶이 홍대점", "서울시 마포구 샬라샬라", "2018","3","11", "위치가 역이랑 가까워서 좋았어요!\n건물도 깨끗했고, 상가 근처에 예쁜 카페가 많아서 구경할거리도 많더라구요!"))
//        dataList.add(MypageMyReviewData("동대문엽기떡볶이 강남점", "서울시 마포구 샬라샬라2", "2017","1","23","감사합니다." ))
//        dataList.add(MypageMyReviewData("동대문엽기떡볶이 수유점", "서울시 마포구 샬라샬라3", "2016","7","9", "ㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇㅇ"))

        mypageMyReviewAdapter = MypageMyReviewAdapter(activity!!, dataList)
        rv_review_myreview.adapter = mypageMyReviewAdapter
        rv_review_myreview.layoutManager = LinearLayoutManager(activity)
    }

}