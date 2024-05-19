package com.example.powerpath

import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.example.powerpath.databinding.ActivityFiltersBinding
import com.example.powerpath.fragments.PickConnectorDialogFragment
import com.example.powerpath.fragments.PickNetworksDialogFragment
import com.example.powerpath.retrofitApi.ApiServiceImpl
import com.example.powerpath.userData.NewDataManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FiltersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFiltersBinding
    private var selectedMinPower: Int = 0
    private var selectedMaxPower: Int = 0
    private var selectedRating: Int = 0
    private var selectedStationCount: Int = 0
    @Inject
    lateinit var dataManager: NewDataManager

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

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
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
        getFilters(dataManager.email)
    }

    private fun setupViews() {
        binding.llPaymentOptions.visibility = View.GONE
        binding.rgRatings.visibility = View.GONE
        binding.rgStationCount.visibility = View.GONE

        val powerOptions = arrayOf("3kW", "7kW", "18kW", "43kW", "100kW", "350kW")
        binding.tvValueFrom.setOnClickListener {
            val maxPower = selectedMaxPower + 1
            val filteredOptions = if (maxPower == null) powerOptions else powerOptions.filter {
                val power = it.removeSuffix("kW").toInt()
                maxPower > power
            }.toTypedArray()

            if (filteredOptions.isEmpty()) {
                return@setOnClickListener
            }

            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle(resources.getString(R.string.text_select_minimum_power))
            builder.setSingleChoiceItems(filteredOptions, -1) { dialog, item ->
                selectedMinPower = filteredOptions[item].removeSuffix("kW").toInt()
                binding.tvValueFrom.textSize = 18f
                binding.tvValueFrom.text = selectedMinPower.toString()
                dialog.dismiss()
            }
            val alert: AlertDialog = builder.create()
            alert.show()
        }

        binding.tvValueTo.setOnClickListener {
            val minPower = selectedMinPower - 1
            val filteredOptions = if (minPower == null) powerOptions else powerOptions.filter {
                val power = it.removeSuffix("kW").toInt()
                power > minPower
            }.toTypedArray()

            if (filteredOptions.isEmpty()) {
                return@setOnClickListener
            }

            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle(resources.getString(R.string.text_select_maximum_power))
            builder.setSingleChoiceItems(filteredOptions, -1) { dialog, item ->
                selectedMaxPower = filteredOptions[item].removeSuffix("kW").toInt()
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

    private fun validateFilters(): Boolean {
        var isValid = true
        var errorMessage = ""
        if (binding.etDurability.text.isNullOrEmpty()) {
            errorMessage = resources.getString(R.string.text_please_choose_durability)
        }
        if (!binding.checkBoxCard.isChecked && !binding.checkBoxFree.isChecked) {
            errorMessage = resources.getString(R.string.text_please_choose_payment_option)
        }
        
        if (errorMessage.isNotEmpty()) {
            AlertDialog.Builder(this@FiltersActivity)
                .setTitle(resources.getString(R.string.text_error))
                .setMessage(errorMessage)
                .setNeutralButton(resources.getString(R.string.text_ok)) { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
            isValid = false
        }
        return isValid
    }

    private fun getFilters(email: String) {  //TODO figure out why it's being called twice, dumbass
        val apiService = ApiServiceImpl()

        apiService.getFilters(email, {userFilter ->
            runOnUiThread {
                binding.tvValueFrom.textSize = 18f
                binding.tvValueTo.textSize = 18f
                binding.etDurability.setText((userFilter.durability/1000).toString())
            }
            binding.tvValueFrom.text = userFilter.powerRangeMin.toString()
            selectedMinPower = userFilter.powerRangeMin
            binding.tvValueTo.text = userFilter.powerRangeMax.toString()
            selectedMaxPower = userFilter.powerRangeMax
            dataManager.connectorType = when (userFilter.connectorType) {
                "CCS (Type 1)" -> "CCS Combo Type 1"
                "CCS (Type 2)" -> "CCS Combo Type 2"
                "CHAdeMO" -> "CHAdeMO"
                "GB-T DC - GB/T 20234" -> "GB/T"
                "NACS / Tesla Supercharger" -> "Supercharger"
                "Type 1 (J1772)" -> "Type 1 J1772"
                "Type 2" -> "Type 2 Mennekes"
                else -> ""
            }
            dataManager.selectedNetworks = userFilter.networks.toMutableList()
            when (userFilter.minimalRating) {
                2 -> binding.rating2.isChecked = true
                3 -> binding.rating3.isChecked = true
                4 -> binding.rating4.isChecked = true
                5 -> binding.rating5.isChecked = true
                else -> binding.ratingAny.isChecked = true
            }
            when (userFilter.stationCount) {
                2 -> binding.station2.isChecked = true
                4 -> binding.station4.isChecked = true
                6 -> binding.station6.isChecked = true
                else -> binding.stationAny.isChecked = true
            }
            binding.checkBoxCard.isChecked = userFilter.paid
            binding.checkBoxFree.isChecked = userFilter.free
        }, {
            //nothing for now
        })
    }

    private fun saveFilters(email :String, powerRange: Pair<Int, Int>, connectorType: String, networks: List<String>, minRating: Int, minStationCount: Int, paid: Boolean, free: Boolean, durability: Int) {
        val apiService = ApiServiceImpl()
        apiService.saveFilters(email, powerRange, connectorType, networks, minRating, minStationCount, paid, free, durability)
    }

    private fun onSave() {
        if (!validateFilters()) return
        val email = dataManager.email
        val powerRange = Pair(
            if (binding.tvValueFrom.text.toString() == "--") 0 else selectedMinPower,
            if (binding.tvValueTo.text.toString() == "--") 0 else selectedMaxPower
        )
        val connectorType = dataManager.connectorType
        val networks = dataManager.selectedNetworks
        val minRating = selectedRating
        val minStationCount = selectedStationCount
        val paid = binding.checkBoxCard.isChecked
        val free = binding.checkBoxFree.isChecked
        val durability = binding.etDurability.text.toString().toInt()
        saveFilters(email, powerRange, connectorType, networks, minRating, minStationCount, paid, free, durability)
        onBackPressed()
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("isInvoked", true)
        startActivity(intent)
    }
}