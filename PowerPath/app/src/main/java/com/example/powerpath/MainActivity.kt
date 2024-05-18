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
import androidx.lifecycle.lifecycleScope
import com.example.powerpath.fragments.PinInfoFragment
import com.example.powerpath.retrofitApi.ApiServiceImpl
import com.example.powerpath.retrofitApi.dataClasses.Pin
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity(), OnMapReadyCallback, PinInfoFragment.OnButtonPressedListener {

    private lateinit var filtersButton: FloatingActionButton
    private lateinit var searchButton: FloatingActionButton
    private lateinit var mMap: GoogleMap
    private val markersMap = HashMap<LatLng, Marker>()
    private val directionLocations = mutableListOf<LatLng>()
    private val polylines = mutableListOf<Polyline>()
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


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

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        filtersButton = findViewById(R.id.buttonFilters)
        filtersButton.setOnClickListener {
            val intent = Intent(this, FiltersActivity::class.java)
            startActivity(intent)
        }
        searchButton = findViewById(R.id.buttonSearch)
        searchButton.setOnClickListener {
            getLastLocation {
                getClosestStation(it)
            }
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
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

    private suspend fun displayDirections(coordinates: Array<String>) {
        clearPolylines()
        for (i in 0 until coordinates.size - 1) {
            val start = coordinates[i]
            val destination = coordinates[i + 1]


            val decodedPath: List<LatLng> = PolyUtil.decode(extractOverviewPolylineString(start, destination))
            val polyline = mMap.addPolyline(
                PolylineOptions().addAll(decodedPath).width(10f).color(Color.BLUE)
            )
            polylines.add(polyline)
            val builder = LatLngBounds.Builder()
            for (latLng in decodedPath) {
                builder.include(latLng)
            }
        }
        val builder = LatLngBounds.Builder()
        builder.include(coordinates[0].split(",").let { LatLng(it[0].toDouble(), it[1].toDouble()) })
        builder.include(coordinates[coordinates.size - 1].split(",").let { LatLng(it[0].toDouble(), it[1].toDouble()) })
        val bounds = builder.build()
        val padding = 100
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }

    private fun clearPolylines() {
        for (polyline in polylines) {
            polyline.remove()
        }
        polylines.clear()
    }
    private fun removeAllPins(locations: List<LatLng>) {
        locations.forEach { location ->
            removePin(location)
        }
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

    override fun calcDirections(location: String) {
        getLastLocation { latLng ->
            startFindRouteTask(DataManager.email, "${latLng.latitude},${latLng.longitude}", location)
        }
    }

    private suspend fun getPath(start: String, destination: String): String? {
        val apiService = ApiServiceImpl()
        return apiService.getPath(start, destination)
    }
    private fun savePin(email: String, name: String, latitude: Double, longitude: Double) {
        val apiService = ApiServiceImpl()
        apiService.savePin(email, name, latitude, longitude)
    }

    private fun getPins(email: String) {
        val apiService = ApiServiceImpl()
        apiService.getPins(email) { responseData ->

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

    private fun startFindRouteTask(email: String, start: String, destination: String) {
        val client = OkHttpClient()
        val url = "https://power-path-backend-3e6dc9fdeee0.herokuapp.com/find_route?email=$email&start=$start&destination=$destination"

        val request = Request.Builder()
            .url(url)
            .build()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    responseBody?.let {
                        val taskId = JSONObject(it).getString("task_id")
                        Log.d("TaskId", taskId)
                        checkTaskStatus(taskId)
                    }
                } else {
                    Log.d("HTTPError", "start find route error: HTTP error code: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e("NetworkError", "Error sending request", e)
            }
        }
    }

    private fun checkTaskStatus(taskId: String) {
        val snackbar = Snackbar.make(
            findViewById(android.R.id.content),
            resources.getText(R.string.text_calculating_route),
            Snackbar.LENGTH_INDEFINITE
        )
        snackbar.view.setBackgroundColor(ContextCompat.getColor(this, R.color.keese_blue))
        snackbar.show()
        lifecycleScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()
            val url = "https://power-path-backend-3e6dc9fdeee0.herokuapp.com/task-status/$taskId"

            val request = Request.Builder()
                .url(url)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                responseBody?.let {
                    val json = JSONObject(it)
                    when (json.getString("state")) {
                        "PENDING" -> {
                            delay(10000)
                            checkTaskStatus(taskId)
                        }

                        "SUCCESS" -> {
                            val result = json.optString("result")
                            Log.d("TaskStatus", result)
                            val jsonArray = JSONArray(result)
                            if (jsonArray.length() == 0 || jsonArray.length() == 1 || jsonArray.get(jsonArray.length()-1) == jsonArray.get(jsonArray.length()-2)) {
                                withContext(Dispatchers.Main) {
                                    val builder: AlertDialog.Builder =
                                        AlertDialog.Builder(this@MainActivity)
                                    builder.setTitle(resources.getText(R.string.text_impossible_path))
                                    builder.setMessage(resources.getText(R.string.text_could_not_find_a_path))
                                    builder.setPositiveButton(resources.getString(R.string.text_ok)) { _, _ ->

                                    }
                                    builder.show()
                                    snackbar.dismiss()
                                }
                            } else {
                                val pinsArray =
                                    Array(jsonArray.length()) { i -> jsonArray.getString(i) }
                                withContext(Dispatchers.Main) {
                                    removeAllPins(directionLocations)
                                    directionLocations.clear()
                                    for (i in 0 until jsonArray.length()) {
                                        val coordinateString = jsonArray.getString(i)

                                        val latLng = coordinateString.split(",")
                                            .let { LatLng(it[0].toDouble(), it[1].toDouble()) }

                                        setPin(latLng, "stop")
                                        directionLocations.add(latLng)
                                    }

                                    displayDirections(pinsArray)
                                    snackbar.dismiss()
                                }
                            }
                        }

                        else -> {
                            withContext(Dispatchers.Main) {
                                snackbar.dismiss()
                            }
                        }
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    snackbar.setText(resources.getText(R.string.text_something_went_wrong))
                    snackbar.duration = Snackbar.LENGTH_SHORT
                    snackbar.view.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.keese_blue))
                    snackbar.show()
                    delay(5000)
                    snackbar.dismiss()
                }
            }
        }
    }

    private fun getLastLocation(callback: (LatLng) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    callback(LatLng(latitude, longitude))
                }
            }
    }

    private fun getClosestStation(location: LatLng) {
        val apiService = ApiServiceImpl()
        apiService.getClosestStation(location) { responseData ->
            try {
                val jsonObject = JSONObject(responseData)
                val latitude = jsonObject.getDouble("latitude")
                val longitude = jsonObject.getDouble("longitude")
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        setPin(LatLng(latitude, longitude), "closest")
                        mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    latitude,
                                    longitude
                                ), 18f
                            )
                        )
                    }
                }
            } catch (e: JSONException) {
                Log.e("===", "Failed to parse JSON response", e)
                runOnUiThread {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle(resources.getString(R.string.text_error))
                        .setMessage(resources.getString(R.string.text_could_not_find_nearby_stations))
                        .setNeutralButton(resources.getString(R.string.text_ok)) { _, _ -> }
                        .show()
                }
            }
        }
    }

    override fun onBackPressed() {}
}