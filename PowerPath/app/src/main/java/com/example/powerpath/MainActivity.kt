package com.example.powerpath

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.powerpath.fragments.PinInfoFragment
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject
import com.example.powerpath.api.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback, PinInfoFragment.OnRenameListener {

    private lateinit var filtersButton: FloatingActionButton
    private lateinit var mMap: GoogleMap
    private val markersMap = HashMap<LatLng, Marker>()

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

        filtersButton = findViewById(R.id.buttonFilters)
        filtersButton.setOnClickListener {
            val intent = Intent(this, FiltersActivity::class.java)
            startActivity(intent)
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = true
            mMap = googleMap

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

            mMap.setOnMapLongClickListener { latLng: LatLng? ->
                showSavePinDialog(latLng!!, false)
            }

            mMap.setOnMarkerClickListener { marker ->
                val infoFragment = PinInfoFragment.newInstance(marker.title!!, marker.position)
                infoFragment.show(supportFragmentManager, infoFragment.tag)
                true
            }

            getPins(DataManager.email)

//            GlobalScope.launch {
//                val decodedPath: List<LatLng> = PolyUtil.decode(extractOverviewPolylineString("42.65030150660556,23.381500463654387", "42.65257491466009,23.382200638366317"))
//
//                withContext(Dispatchers.Main) {
//                    googleMap.addPolyline(
//                        PolylineOptions().addAll(decodedPath).width(10f).color(Color.BLUE)
//                    )
//
//                    val builder = LatLngBounds.Builder()
//                    for (latLng in decodedPath) {
//                        builder.include(latLng)
//                    }
//
//                    val bounds = builder.build()
//                    val padding = 100
//
//                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
//                }
//            }

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

    private fun setPin(location: LatLng, title: String) {
        val marker = mMap.addMarker(MarkerOptions().position(location).title(title))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 18f))
        if (marker != null) {
            markersMap[location] = marker
        }
    }

    private fun removePin(location: LatLng) {
        markersMap[location]?.remove()
        markersMap.remove(location)
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
            if (jsonResponse != null) {
                val jsonObj = JSONObject(jsonResponse)
                val routes = jsonObj.getJSONArray("routes")
                if (routes.length() > 0) {
                    val route = routes.getJSONObject(0)
                    val overviewPolyline = route.getJSONObject("overview_polyline")
                    return overviewPolyline.getString("points")
                }
            } else {
                //TODO error handling
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null //add error handling so app doesn't crash pls
    }

    override fun showSavePinDialog(location: LatLng, isRenaming: Boolean) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(if (isRenaming) resources.getString(R.string.text_rename_destination) else resources.getString(R.string.text_save_destination))
        builder.setMessage(if (isRenaming) resources.getString(R.string.text_new_name) else resources.getString(R.string.text_save_destination_as))
        val inputEditText = EditText(this)
        builder.setView(inputEditText)
        builder.setPositiveButton(resources.getString(R.string.text_ok)) { _, _ ->
            val userInput = inputEditText.text.toString()
            if (isRenaming) {
                removePin(location)
            }
            setPin(location, userInput)
            savePin(DataManager.email, userInput, location.latitude, location.longitude)
        }
        builder.setNegativeButton(resources.getString(R.string.text_cancel)
        ) { dialog, _ -> dialog.cancel() }
        builder.create().show()
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
    private fun savePin(email: String, name: String, latitude: Double, longitude: Double) {
        val url = "https://power-path-backend-3e6dc9fdeee0.herokuapp.com/save_pin"
        val jsonBody = JSONObject().apply {
            put("email", email)
            put("name", name)
            put("latitude", String.format("%.6f", latitude).toDouble())
            put("longitude", String.format("%.6f", longitude).toDouble())
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("===", "save pin error: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.d("===", "save pin error: ${response.message}")
                } else {
                    Log.d("===", "save pin successful: ${response.message}")
                }
            }
        })
    }

    private fun getPins(email: String) {
        val url = "https://power-path-backend-3e6dc9fdeee0.herokuapp.com/get_pins?email=$email"

        val request = Request.Builder()
            .url(url)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("===", "get pins error: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.d("===", "get pins error: ${response.message}")
                } else {
                    val responseData = response.body?.string()

                    val jsonArray = JSONArray(responseData)

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val pin = Pin(
                            id = jsonObject.getInt("id"),
                            latitude = jsonObject.getDouble("latitude"),
                            longitude = jsonObject.getDouble("longitude"),
                            name = jsonObject.getString("name"),
                            user_id = jsonObject.getInt("user_id")
                        )
                        runOnUiThread {
                            setPin(LatLng(pin.latitude, pin.longitude), pin.name)
                        }
                    }
                }
            }
        })
    }
}