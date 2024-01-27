package com.example.powerpath

import com.example.powerpath.fragments.PickNetworksDialogFragment
import android.app.AlertDialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.example.powerpath.databinding.ActivityFiltersBinding
import com.example.powerpath.fragments.PickConnectorDialogFragment
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject


class FiltersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFiltersBinding
    private var selectedMinPower: Int = 0
    private var selectedMaxPower: Int = 0
    private var selectedRating: Int = 0
    private var selectedStationCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFiltersBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        window.statusBarColor = resources.getColor(R.color.keese_blue)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black)

        val text = SpannableString(resources.getString(R.string.text_filters))
        text.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.white)),
            0,
            text.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        val backArrow = ContextCompat.getDrawable(this@FiltersActivity, R.drawable.ic_back_arrow)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        supportActionBar?.apply {
            setBackgroundDrawable(
                ColorDrawable(
                    ContextCompat.getColor(
                        this@FiltersActivity,
                        R.color.keese_blue
                    )
                )
            )
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(backArrow)
            elevation = 0f
            title = text
        }
        setupViews()
    }

    private fun setupViews() {
        binding.llPaymentOptions.visibility = View.GONE
        binding.rgRatings.visibility = View.GONE
        binding.rgStationCount.visibility = View.GONE

        val powerOptions = arrayOf("3kW", "7kW", "18kW", "43kW", "100kW", "350kW")
        binding.tvValueFrom.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle(resources.getString(R.string.text_select_minimum_power))
            builder.setSingleChoiceItems(powerOptions, -1
            ) { dialog, item ->
                val pattern = Regex("""(\d+)kW""")
                selectedMinPower = pattern.find(powerOptions[item])?.groups?.get(1)?.value?.toInt()!!
                binding.tvValueFrom.textSize = 18f
                binding.tvValueFrom.text = selectedMinPower.toString()
                dialog.dismiss()
            }
            val alert: AlertDialog = builder.create()
            alert.show()
        }
        binding.tvValueTo.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle(resources.getString(R.string.text_select_maximum_power))
            builder.setSingleChoiceItems(
                powerOptions, -1
            ) { dialog, item ->
                val pattern = Regex("""(\d+)kW""")
                selectedMaxPower = pattern.find(powerOptions[item])?.groups?.get(1)?.value?.toInt()!!
                binding.tvValueTo.textSize = 18f
                binding.tvValueTo.text = selectedMaxPower.toString()
                dialog.dismiss()
            }
            val alert: AlertDialog = builder.create()
            alert.show()
        }


        val clickListener = View.OnClickListener {
            when (it) {
                binding.llConnectorTypes -> {
                    val dialogFragment = PickConnectorDialogFragment()
                    dialogFragment.show(supportFragmentManager, "pickConnectorDialog")
                }
                binding.llNetworks -> {
                    val dialogFragment = PickNetworksDialogFragment()
                    dialogFragment.show(supportFragmentManager, "pickNetworksDialog")

                }
                binding.llRatings -> {
                    if (binding.rgRatings.visibility == View.GONE) {
                        Handler().postDelayed({
                            binding.rgRatings.visibility = View.VISIBLE
                            binding.rgRatings.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
                        }, 500)
                        rotateClockwise(binding.ivRatings)
                    } else {
                        binding.rgRatings.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out))
                        Handler().postDelayed({
                            binding.rgRatings.visibility = View.GONE
                        }, 500)
                        rotateCounterclockwise(binding.ivRatings)
                    }
                }
                binding.llStationCount -> {
                    if (binding.rgStationCount.visibility == View.GONE) {
                        Handler().postDelayed({
                            binding.rgStationCount.visibility = View.VISIBLE
                            binding.rgStationCount.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
                        }, 500)
                        rotateClockwise(binding.ivStationCount)
                    } else {
                        binding.rgStationCount.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out))
                        Handler().postDelayed({
                            binding.rgStationCount.visibility = View.GONE
                        }, 500)
                        rotateCounterclockwise(binding.ivStationCount)
                    }
                }
                binding.llPaymentType -> {
                    if (binding.llPaymentOptions.visibility == View.GONE) {
                        Handler().postDelayed({
                            binding.llPaymentOptions.visibility = View.VISIBLE
                            binding.llPaymentOptions.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
                        }, 500)
                        rotateClockwise(binding.ivPaymentType)
                    } else {
                        binding.llPaymentOptions.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out))
                        Handler().postDelayed({
                            binding.llPaymentOptions.visibility = View.GONE
                        }, 500)
                        rotateCounterclockwise(binding.ivPaymentType)
                    }
                }
            }
        }
        binding.llConnectorTypes.setOnClickListener(clickListener)
        binding.llNetworks.setOnClickListener(clickListener)
        binding.llRatings.setOnClickListener(clickListener)
        binding.llStationCount.setOnClickListener(clickListener)
        binding.llPaymentType.setOnClickListener(clickListener)

        binding.rgRatings.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.rating2.id -> selectedRating = 2
                binding.rating3.id -> selectedRating = 3
                binding.rating4.id -> selectedRating = 4
                binding.rating5.id -> selectedRating = 5
                binding.ratingAny.id -> selectedRating = 0
            }
        }

        binding.rgStationCount.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.station2.id -> selectedStationCount = 2
                binding.station4.id -> selectedStationCount = 4
                binding.station6.id -> selectedStationCount = 6
                binding.stationAny.id -> selectedStationCount = 1
            }
        }

        binding.buttonSave.setOnClickListener {
            onSave()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun rotateClockwise(imageView: ImageView) {
        val rotateAnimation = RotateAnimation(
            0f,
            90f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        rotateAnimation.duration = 500
        rotateAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {}

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        rotateAnimation.fillAfter = true
        imageView.startAnimation(rotateAnimation)
    }

    private fun rotateCounterclockwise(imageView: ImageView) {
        val rotateAnimation = RotateAnimation(
            90f,
            0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        rotateAnimation.duration = 500
        rotateAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {}

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        rotateAnimation.fillAfter = true
        imageView.startAnimation(rotateAnimation)
    }

    private fun validateFilters(powerRange: Pair<Int, Int>, connectorType: String): Boolean {
        var isValid = true
        if (powerRange.first > powerRange.second) {
            AlertDialog.Builder(this@FiltersActivity)
                .setTitle(resources.getString(R.string.text_error))
                .setMessage(resources.getString(R.string.text_please_select_valid_range))
                .setNeutralButton(resources.getString(R.string.text_ok)) { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
            isValid = false
        }
        if (connectorType == "") {
            AlertDialog.Builder(this@FiltersActivity)
                .setTitle(resources.getString(R.string.text_error))
                .setMessage(resources.getString(R.string.text_please_select_connector_type))
                .setNeutralButton(resources.getString(R.string.text_ok)) { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
            isValid = false
        }
        return isValid
    }

    private fun getFilters() {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("power-path-backend-3e6dc9fdeee0.herokuapp.com")
            .addPathSegment("get_filters")
            .addQueryParameter("email", DataManager.email)
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(">>>", "get filters error: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.d(">>>", "get filters error: ${response.message}")
                } else {
                    val responseData = response.body?.string()
                    val jsonObject = JSONObject(responseData.toString())
                    val powerRangeMin = jsonObject.optDouble("power_range_min", 0.0)
                    val powerRangeMax = jsonObject.optDouble("power_range_max", 0.0)
                    val connectorType = jsonObject.optString("connector_type")

                    val networksArray = jsonObject.optJSONArray("networks")
                    val networks = mutableListOf<String>()
                    if (networksArray != null) {
                        for (i in 0 until networksArray.length()) {
                            networks.add(networksArray.optString(i))
                        }
                    }

                    val minimalRating = jsonObject.optDouble("minimal_rating", 0.0)
                    val stationCount = jsonObject.optInt("station_count", 0)
                    val paid = jsonObject.optBoolean("paid", false)
                    val free = jsonObject.optBoolean("free", false)
                }
            }
        })
    }

    private fun saveFilters(email :String, powerRange: Pair<Int, Int>, connectorType: String, networks: List<String>, minRating: Int, minStationCount: Int, paid: Boolean, free: Boolean) {
        val url = "https://power-path-backend-3e6dc9fdeee0.herokuapp.com/save_filters"
        val jsonBody = JSONObject().apply {
            put("email", email)
            put("power_range", JSONArray().apply {
                put(powerRange.first)
                put(powerRange.second)
            })
            put("connector_type", connectorType)
            put("networks", JSONArray(networks))
            put("minimal_rating", minRating)
            put("station_count", minStationCount)
            put("paid", paid)
            put("free", free)
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("===", "save filters error: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.d("===", "save filters error: ${response.message}")
                } else {
                    Log.d("===", "save filters successful: ${response.message}")
                }
            }
        })
    }

    private fun onSave() {
        val email = DataManager.email
        val powerRange = Pair(
            if (binding.tvValueFrom.text.toString() == "--") 0 else selectedMinPower,
            if (binding.tvValueTo.text.toString() == "--") 0 else selectedMaxPower
        )
        val connectorType = DataManager.connectorType
        val networks = DataManager.selectedNetworks
        val minRating = selectedRating
        val minStationCount = selectedStationCount
        val paid = binding.checkBoxCard.isChecked
        val free = binding.checkBoxFree.isChecked
        if (!validateFilters(powerRange, connectorType)) return
        saveFilters(email, powerRange, connectorType, networks, minRating, minStationCount, paid, free)
    }
}