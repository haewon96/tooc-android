package com.hyeran.android.travely_user.reserve_state


import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.android.synthetic.main.fragment_reserve_state.*
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Gravity
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.hyeran.android.travely_user.R
import com.hyeran.android.travely_user.adapter.LuggagePictureAdapter
import com.hyeran.android.travely_user.data.LuggagePictureData
import com.hyeran.android.travely_user.dialog.*
import com.hyeran.android.travely_user.map.MapMorePreviewFragment
import com.hyeran.android.travely_user.model.reservation.ReservationReserveCodeData
import com.hyeran.android.travely_user.model.reservation.bagDtosData
import com.hyeran.android.travely_user.model.reservation.bagImgDtos
import com.hyeran.android.travely_user.network.ApplicationController
import com.hyeran.android.travely_user.network.NetworkService
import kotlinx.android.synthetic.main.fragment_map_more_preview.*
import kotlinx.android.synthetic.main.fragment_reserve.view.*
import kotlinx.android.synthetic.main.fragment_reserve_state.view.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import retrofit2.Call
import retrofit2.Response
import java.sql.Timestamp
import java.text.SimpleDateFormat


class ReserveStateFragment : Fragment(), OnMapReadyCallback {

    lateinit var networkService: NetworkService
    private lateinit var mMap3: GoogleMap
    private lateinit var mapView3: MapView
    private lateinit var mGoogleApiClient: GoogleApiClient

    var latitude3: Double = 0.0
    var longitude3: Double = 0.0
    lateinit var qrCode: String
    //  var latitude: Double = 0.0
    //  var longitude: Double = 0.0

    // Google API Client 생성
    private lateinit var mLocation2: Location
    private lateinit var locationManager2: LocationManager
    private lateinit var mLocationRequest2: LocationRequest

    private lateinit var fusedLocationProviderClient2: FusedLocationProviderClient
    private lateinit var locationRequest2: LocationRequest
    private lateinit var locationCallback2: MapMorePreviewFragment.MyLocationCallBack2


    override fun onMapReady(googleMap: GoogleMap) {
        MapsInitializer.initialize(context)
        mMap3 = googleMap
        getReservationReserveResponse(v)
        if (ActivityCompat.checkSelfPermission(activity!!, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity!!, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap3.isMyLocationEnabled = true
            mMap3.uiSettings.isMyLocationButtonEnabled = true
            mMap3.uiSettings.isCompassEnabled = true
            mMap3.uiSettings.isZoomGesturesEnabled = true
        }
    }

    var marker = LatLng(0.0, 0.0)
    fun setGoogleMap(shopName: String, latitude: Double, longitude: Double) {
        marker = LatLng(latitude, longitude)
        Log.d("TAGGG", "lat = " + latitude + "  long = " + longitude)
        mMap3.addMarker(MarkerOptions().position(marker).title(shopName).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_color_pin)))
//        mMap3.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 17f))
    }

    lateinit var luggagePictureAdapter: LuggagePictureAdapter

    var stateType: String? = null
    var reserveCode: String? = null
    var startTime: Long? = null
    var endTime: Long? = null
    var bagDtos = ArrayList<bagDtosData>()

    var password: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mGoogleApiClient = GoogleApiClient.Builder(activity!!)
                .addApi(LocationServices.API)
                .build()
        mGoogleApiClient.connect()

        arguments?.let { password = it.getString("password") }
    }

    lateinit var v: View
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_reserve_state, container, false)

        networkService = ApplicationController.instance.networkService
//        getReservationReserveResponse(v)

        v.btn_reservecancel_to_dialog.setOnClickListener {
          //  ReserveCancelDialog(context).show()
            ReserveCancelPasswordConfirmDialog(context).show()
        }
        v.iv_qrimage_reservestate.setOnClickListener {
            startActivity<ReserveQRCodeActivity>("qrCode" to qrCode)
        }

        v.btn_price_reservestate.setOnClickListener {
            KeepPriceDialog(context).show()
        }

        v.btn_bag_size_reservestate.setOnClickListener {
            BagSizeDialog(context).show()
        }

        mapView3 = v.findViewById(R.id.mv_store_map_reservestate)
        mapView3.onCreate(savedInstanceState)
        mapView3.getMapAsync(this)

        v.iv_store_guide_reservestate.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(activity!!,
                            android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(activity!!,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                var mLastKnownLocation: Location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
                latitude3 = mLastKnownLocation.latitude
                longitude3 = mLastKnownLocation.longitude

//                Log.d("ReserveStateFragment", "위도 : $latitude3, 경도 : $longitude3")
            }

            val mapChoiceDialog2: MapChoiceDialog = MapChoiceDialog(activity, latitude3, longitude3)
            mapChoiceDialog2.window.setGravity(Gravity.BOTTOM)
            mapChoiceDialog2.show()
        }

        return v
    }

    override fun onStart() {
        super.onStart()
        mapView3.onStart()

//        setRecyclerView()
    }

    override fun onStop() {
        super.onStop()

        if (mGoogleApiClient.isConnected) {
            mGoogleApiClient.disconnect()
        }
    }

//    fun setRecyclerView() {
//        var dataList= ArrayList<LuggagePictureData>()
//        dataList.add(LuggagePictureData("https://s3.ap-northeast-2.amazonaws.com/travely-project/KakaoTalk_20181231_201927117.jpg"))
//        dataList.add(LuggagePictureData("tesetest"))
//        dataList.add(LuggagePictureData("https://s3.ap-northeast-2.amazonaws.com/travely-project/KakaoTalk_20181231_201927117.jpg"))
//
//
//        luggagePictureAdapter = LuggagePictureAdapter(activity!!, dataList)
//        rv_luggage_picture.adapter = luggagePictureAdapter
//        var mLayoutManager = LinearLayoutManager(context)
//        mLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
//        rv_luggage_picture.layoutManager = mLayoutManager
//    }

    fun generateQRCode(view: View, contents: String) {
        var qrCodeWriter = QRCodeWriter()
        var bitmap: Bitmap = toBitmap(qrCodeWriter.encode(contents, BarcodeFormat.QR_CODE, 200, 200))

        view.iv_qrimage_reservestate.setImageBitmap(bitmap)
        Log.v("tag", bitmap.toString())
    }

    fun toBitmap(matrix: BitMatrix): Bitmap {
        val height: Int = matrix.height
        val width: Int = matrix.width
        val bmp: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0..width - 1) {
            for (y in 0..height - 1) {
                bmp.setPixel(x, y, if (matrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }

    fun getReservationReserveResponse(v: View) {
        var jwt: String? = SharedPreferencesController.instance!!.getPrefStringData("jwt")
        val getReservationReserveResponse = networkService.getReservationReserveResponse(jwt)

        getReservationReserveResponse!!.enqueue(object : retrofit2.Callback<ReservationReserveCodeData> {
            override fun onFailure(call: Call<ReservationReserveCodeData>, t: Throwable) {
                toast("fail" + t.message)
                Log.d("TAGG", "fail : " + t.message)
            }

            override fun onResponse(call: Call<ReservationReserveCodeData>, response: Response<ReservationReserveCodeData>) {
                response?.let {
                    when (it?.code()) {
                        200 -> {
                            stateType = response.body()!!.stateType.toString()
                            reserveCode = response.body()!!.reserveCode.toString()
                            startTime = response.body()!!.startTime
                            endTime = response.body()!!.endTime
                            bagDtos = response.body()!!.bagDtos
                            var latitude = response.body()!!.store.latitude
                            var longitude = response.body()!!.store.longitude
                            generateQRCode(v, response.body()!!.reserveCode)
                            toast("stateType = " + stateType)
                            if (stateType == "RESERVED") {
                                iv_circle_settlement_reservestate.setImageResource(R.drawable.ic_circle_empty)
                                iv_circle_storage_reservestate.setImageResource(R.drawable.ic_circle_empty)
                                iv_circle_collect_reservestate.setImageResource(R.drawable.ic_circle_empty)
                                iv_payment_complete_reservestate.setImageResource(R.drawable.box_pay_no)
                            } else if (stateType == "PAYED") {
                                iv_circle_settlement_reservestate.setImageResource(R.drawable.ic_circle_fill)
                                iv_circle_storage_reservestate.setImageResource(R.drawable.ic_circle_empty)
                                iv_circle_collect_reservestate.setImageResource(R.drawable.ic_circle_empty)
                                iv_payment_complete_reservestate.setImageResource(R.drawable.box_pay_yes)

                            } else if (stateType == "ARCHIVE") {
                                iv_circle_settlement_reservestate.setImageResource(R.drawable.ic_circle_fill)
                                iv_circle_storage_reservestate.setImageResource(R.drawable.ic_circle_fill)
                                iv_circle_collect_reservestate.setImageResource(R.drawable.ic_circle_empty)
                                iv_payment_complete_reservestate.setImageResource(R.drawable.box_pay_yes)
                            } else if (stateType == "PICKUP") {
                                iv_circle_settlement_reservestate.setImageResource(R.drawable.ic_circle_fill)
                                iv_circle_storage_reservestate.setImageResource(R.drawable.ic_circle_fill)
                                iv_circle_collect_reservestate.setImageResource(R.drawable.ic_circle_fill)
                                iv_payment_complete_reservestate.setImageResource(R.drawable.box_pay_yes)
                            } else if (stateType == "CANCEL") {
                                ReserveCancelDialog(ctx).show()
                                //ReserveCancelPasswordConfirmDialog(ctx).show()
                            }
                            //DATE
                            var dateTextFormat = SimpleDateFormat("yy년 MM월 dd일 EE요일")
                            tv_put_year_reservestate.text = dateTextFormat.format(startTime)
                            tv_find_year_reservestate.text = dateTextFormat.format(endTime)
                            //시간
                            var timeTextFormat = SimpleDateFormat("a hh시 mm분")
                            tv_put_ampm_reservestate.text = timeTextFormat.format(startTime)
                            tv_find_ampm_reservestate.text = timeTextFormat.format(endTime)
                            //QR
                            tv_qr_reserveCode.text = reserveCode.toString()
                            qrCode = reserveCode.toString()
                            //위도경도
                            setGoogleMap(response.body()!!.store.storeName, latitude, longitude)
                            //bagDtos//TODO bagDtos해야함
                            Log.d("TAGG", "bagDtos : " + bagDtos.toString())
                            toast("bagDtos Size : " + response.body()!!.bagDtos.size)
                            var final_priceUnit = response.body()!!.priceUnit + response.body()!!.extraChargeCount * response.body()!!.extraCharge
                            var total_amount: Int = 0
                            for (i in 0..response.body()!!.bagDtos.size - 1) {
                                Log.d("TAGGG", "bagDtos = " + response.body()!!.bagDtos[i].bagType)
                                if (response.body()!!.bagDtos[i].bagType == "CARRIER") {
                                    total_amount += response.body()!!.bagDtos[i].bagCount
                                    tv_carrier_num_reservestate.text = response.body()!!.bagDtos[i].bagCount.toString()
                                    tv_carrier_money_reservestate.text = (final_priceUnit * response.body()!!.bagDtos[i].bagCount).toString()
                                } else {
                                    total_amount += response.body()!!.bagDtos[i].bagCount
                                    tv_bag_num_reservestate.text = response.body()!!.bagDtos[i].bagCount.toString()
                                    tv_bag_money_reservestate.text = (final_priceUnit * response.body()!!.bagDtos[i].bagCount).toString()
                                }
                                mMap3.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 17f))


                                tv_total_num_reservestate.text = total_amount.toString()
                                tv_total_money_reservestate.text = (final_priceUnit * total_amount).toString()

                                tv_payment_amount_reservestate.text = (total_amount * response.body()!!.price).toString()

                                var dataList: ArrayList<bagImgDtos> = response.body()!!.bagImgDtos

                                luggagePictureAdapter = LuggagePictureAdapter(activity!!, dataList)
                                rv_luggage_picture.adapter = luggagePictureAdapter
                                var mLayoutManager = LinearLayoutManager(context)
                                mLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
                                rv_luggage_picture.layoutManager = mLayoutManager

                                tv_store_name_reservestate.text = response.body()!!.store.storeName
                                tv_store_location_reservestate.text = response.body()!!.store.address
                            }

                            //상가정보 입력
                            tv_store_name_reservestate.text = response.body()!!.store.storeName
                            tv_store_location_reservestate.text = response.body()!!.store.address

                            var timeDateFormat = SimpleDateFormat("HH:mm")
                            var openTime: String = timeDateFormat.format(response.body()!!.openTime)
                            var closeTime : String = timeDateFormat.format(response.body()!!.closeTime)
                            tv_store_start_reservestate.text = openTime
                            tv_store_end_reservestate.text = closeTime
//수, 목 휴무 9:00 ~ 12:00
                            Log.d("TAGG", "bagDtos : " + bagDtos.toString())
                        }
                        500 -> {
                            toast("500 error")
                        }
                        else -> {
                            toast("error")
                            Log.d("TAGG", "reserveStateFragment code = " + response.code().toString())
                        }
                    }
                }
            }
        })

    }

}
