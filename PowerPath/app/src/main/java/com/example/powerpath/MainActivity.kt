package com.example.powerpath

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.json.JSONObject

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
        supportActionBar?.hide()

//        if (savedInstanceState == null) {
//            val intent = Intent(this, SignInActivity::class.java)
//            startActivity(intent)
//        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = true

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation
                .addOnSuccessListener(this) { location ->
                    if (location != null) {
                        val currentLatLng = LatLng(
                            location.latitude,
                            location.longitude
                        )
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f))
                    }
                }

            GlobalScope.launch {
                val decodedPath: List<LatLng> = PolyUtil.decode(extractOverviewPolylineString("42.65030150660556,23.381500463654387", "42.65257491466009,23.382200638366317"))

                withContext(Dispatchers.Main) {
                    googleMap.addPolyline(
                        PolylineOptions().addAll(decodedPath).width(10f).color(Color.BLUE)
                    )

                    val builder = LatLngBounds.Builder()
                    for (latLng in decodedPath) {
                        builder.include(latLng)
                    }

                    val bounds = builder.build()
                    val padding = 100

                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                }
            }

        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun setPin(googleMap: GoogleMap, location: LatLng) {
        googleMap.addMarker(MarkerOptions().position(location).title("Marker Title"))
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, re-run the onMapReady logic
                val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?
                mapFragment?.getMapAsync(this)
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
            }
        }
    }
    private suspend fun extractOverviewPolylineString(start: String, destination: String): String? {
        val jsonResponse = getPath(start, destination)
        try {
            val jsonObj = JSONObject(jsonResponse)
            val routes = jsonObj.getJSONArray("routes")
            if (routes.length() > 0) {
                val route = routes.getJSONObject(0)
                val overviewPolyline = route.getJSONObject("overview_polyline")
                return overviewPolyline.getString("points")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null //add error handling so app doesn't crash pls
    }

    private suspend fun getPath(start: String, destination: String): String? {
        val url = "https://power-path-backend-3e6dc9fdeee0.herokuapp.com/get_path?start=$start&destination=$destination"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val client = OkHttpClient()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.string()
                } else {
                    Log.d("===", "getPath error: HTTP error code: ${response.code}")
                    null
                }
            } catch (e: IOException) {
                Log.d("===", "getPath error: $e")
                null
            }
        }
    }

}