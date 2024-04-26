package com.example.powerpath.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.powerpath.DataManager
import com.example.powerpath.R
import com.example.powerpath.api.Network
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PinInfoFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pin_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val text = arguments?.getString(ARG_TEXT) ?: ""
        val latitude = arguments?.getDouble(ARG_LATITUDE) ?: 0.0
        val longitude = arguments?.getDouble(ARG_LONGITUDE) ?: 0.0
        val latLng = LatLng(latitude, longitude)
        view.findViewById<TextView>(R.id.tvPinTitle).text = text
        view.findViewById<TextView>(R.id.tvCoordinates).text = "$latitude\n$longitude"

        view.findViewById<Button>(R.id.button).setOnClickListener {
            (activity as? OnButtonPressedListener)?.showSavePinDialog(latLng, true)
            dismiss()
        }

        view.findViewById<Button>(R.id.button_directions).setOnClickListener {
            (activity as? OnButtonPressedListener)?.calcDirections("$latitude,$longitude")
            dismiss()
        }

        view.findViewById<ImageView>(R.id.btnDelete).setOnClickListener {
            val network = Network()
            network.deletePin(DataManager.email, text, latitude, longitude, {dismiss()}, {dismiss()})
        }
    }

    companion object {
        private const val ARG_TEXT = "text_arg"
        private const val ARG_LATITUDE = "latitude_arg"
        private const val ARG_LONGITUDE = "longitude_arg"

        fun newInstance(text: String, latLng: LatLng): PinInfoFragment {
            val fragment = PinInfoFragment()
            val args = Bundle()
            args.putString(ARG_TEXT, text)
            args.putDouble(ARG_LATITUDE, latLng.latitude)
            args.putDouble(ARG_LONGITUDE, latLng.longitude)
            fragment.arguments = args
            return fragment
        }
    }

    interface OnButtonPressedListener {
        fun showSavePinDialog(location: LatLng, isRenaming: Boolean)

        fun calcDirections(location: String)
    }
}
