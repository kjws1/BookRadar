package com.example.bookradar

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {  //AppCompatActivity()를 상속하는 게 중요 //OnMapReadyCallback : 지도가 사용될 준비가 될 때 호출되는 콜백 메소드를 가지고 있는 인터페이스 ex) onMapReady()
    var googleMap:GoogleMap? = null                            /// GoogleMap.OnMarkerClickListener 마커 클릭 담당
    var fLC:FusedLocationProviderClient? = null
    var callback:LocationCallback? = null
    var current:LatLng? = null //현재 위치 정보를 저장하는 변수

    var startM: Marker? = null
    var currentM: Marker? = null
    var plOptions: PolylineOptions? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)*/
        setContentView(R.layout.activity_map)

        var permissionArray = arrayOf<String>()
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)//checkSelfPermission() 현재 앱 권한을 갖고 있는지 // 허용이 안 되어 있으면...
            permissionArray = permissionArray.plus(android.Manifest.permission.ACCESS_COARSE_LOCATION) // 권한을 요청할 때 쓰는 array
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) // ACCESS_FINE_LOCATION 허용이 안 되어 있다면
            permissionArray= permissionArray.plus(android.Manifest.permission.ACCESS_FINE_LOCATION)

        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { //RequestMultiplePermissions() 여러개의 권한을 처리하기 위해
            if(it.all{ permission -> permission.value == true}) {  // 모든 권한이 허용이 됐는가?
                doLocation()
            }
            else // 권한이 하나라도 거부가 됐다면
                Toast.makeText(applicationContext, "권한거부", Toast.LENGTH_LONG).show()
        }

        if(permissionArray.size != 0) { //리스트가 없다는 건 권한 허용이 안 되어 있다는 걸 의미
            requestPermissionLauncher.launch(permissionArray)
        }
        else //이미 권한이 허용됨
            doLocation()
        /*val mapFragment:SupportMapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment //지도를 출력하는 뷰 객체를 얻어 옴
        mapFragment.getMapAsync(this) // 지도가 도착을 하면  onMapReady 메소드가 자동으로 호출됨*/
    } // oncreate

    fun doLocation(){
        val mapFragment:SupportMapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment //지도를 출력하는 뷰 객체를 얻어 옴
        mapFragment.getMapAsync(this) // 지도가 도착을 하면  onMapReady 메소드가 자동으로 호출됨
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap) {  //OnMapReadyCallback 상속 시 구현해야 하는 메소드 / 지도가 사용될 준비가 되면 호출되는 콜백 메소드
        googleMap = p0
        googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL // null 혀용을 위에서 했기 때문에 ? 연산자 사용

        fLC = LocationServices.getFusedLocationProviderClient(this)
        callback = object:LocationCallback(){
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                var list = p0.locations //위치 정보
                var location = list[0] // 첫번째 위치 정보만
                val latLng = LatLng(location.latitude, location.longitude)

                if(current == null || current?.equals(latLng) == false){ //처음이거나 바로 직전의 위치와 현재 위치가 다르면
                    val position = CameraPosition.Builder().target(latLng).zoom(15.0f).build() //카메라 포지션 객체 만들어줌
                    googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(position))//중심위치를 변경하는 메소드 매개변수 안에는 카메라 업데이트 객체를 넣어줘야 한다, CameraUpdateFactory 클래스에 의해 만들어짐 newCameraPosition(position) 카메라 업데이트 객체 생성
                    // position 정보를 카메라 업데이트 객체에다가 넣고 카메라 업데이트 객체를 이용하여 위치를 이동시킨다
                    current = latLng // 현재 위치를 다시 지정
                }

                if(currentM != startM) // 현재 마커가 스타트 마커가 아니면
                    currentM?.remove() //위치가 변경될 때마다 이전 마커는 없애고 현재 마커만 보이게

                val options = MarkerOptions()
                //options.title("현재위치") //마커 타이틀
                options.position(latLng)
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)) //마커 아이콘 색상 변경
                //var marker = googleMap?.addMarker(options)
                //marker?.showInfoWindow() // 마커 정보를 처음부터 자동으로 표시됨
                if(startM == null){ // 처음일 때
                    options.title("시작위치")
                    currentM = googleMap?.addMarker(options)
                    startM = currentM
                    startM?.showInfoWindow()
                    plOptions = PolylineOptions().add(latLng).color(Color.BLUE).visible(true).width(5f) // 위치 정보 추가 폭이 5f인 파란색 라인이 보여짐
                }else{
                    options.title("현재위치")
                    plOptions?.add(latLng)
                    currentM = googleMap?.addMarker(options)
                    currentM?.showInfoWindow()
                    googleMap?.addPolyline(plOptions!!)
                }
            }
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build() // 정확도를 높임 / 1초 마다 위치 정보 갱신
        fLC?.requestLocationUpdates(locationRequest, callback!!, Looper.getMainLooper()) // 위치 정보 업데이트 할 때마다

        /*val latLng = LatLng(37.49997694342797, 126.86799621055) //위도 경도 정보를 저장하는 객체 LatLng
        val position = CameraPosition.Builder() // 카메라 포지션 객체 만들어줌
            .target(latLng) //중심 위치 설정
            .zoom(16f) //줌 레벨 얼마나 지도를 확대해서 보여줄 것인가
            .build() //CameraPosition 객체 생성
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(position)) //중심위치를 변경하는 메소드 매개변수 안에는 카메라 업데이트 객체를 넣어줘야 한다, CameraUpdateFactory 클래스에 의해 만들어짐 newCameraPosition(position) 카메라 업데이트 객체 생성
        // position 정보를 카메라 업데이트 객체에다가 넣고 카메라 업데이트 객체를 이용하여 위치를 이동시킨다

        val markerOptions = MarkerOptions().run {//위치 정보, 타이틀 정보 등등 갖고 있음
            position(latLng)
            title("동양미래대학교") //마커 타이틀
            snippet("Tel:01-120")// 부가 정보 표시
          //icon() //마커 아이콘 변경
        }
        googleMap?.addMarker(markerOptions) // 지도 상에 마커가 표시가 됨*/
        //googleMap?.addMarker(markerOptions)?.showInfoWindow()

        googleMap?.setOnMarkerClickListener(this)  /// 마커 클릭 이벤트 리스너
    }

    override fun onDestroy(){
        super.onDestroy()
        if(fLC != null)
            fLC?.removeLocationUpdates(callback!!)
    }

    override fun onMarkerClick(p0: Marker): Boolean { /// 마커 클릭시 마커 정보를 넘겨줌과 동시에 올라오는 다이어로그가 표시됨
        var title = p0.title
        val bottomSheet = BottomSheetDialog(this, title!!)
        bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        return true
    }

}

class BottomSheetDialog(context: Context, title: String) : BottomSheetDialogFragment() /// 다이어로그 클래스 인자 안에 마커 데이터를 가져와야 함
{
    val tt = title
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.bottom_sheet, container, false)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)  /// 레이아웃에 데이터 삽입
    {
        super.onActivityCreated(savedInstanceState)
        view?.findViewById<Button>(R.id.buttonBorrow)?.setOnClickListener {
            Toast.makeText(context, "Bottom Sheet 안의 버튼 클릭", Toast.LENGTH_SHORT).show()
            dismiss()
        }
        view?.findViewById<TextView>(R.id.textLibraryName)?.text = tt
    }
}