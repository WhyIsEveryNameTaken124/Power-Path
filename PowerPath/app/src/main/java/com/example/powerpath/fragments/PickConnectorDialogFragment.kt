package com.example.powerpath.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.powerpath.DataManager
import com.example.powerpath.R
import com.example.powerpath.adapters.ConnectorAdapter
import com.example.powerpath.classes.ConnectorItem

class PickConnectorDialogFragment : DialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var selectedConnector: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_pick_connector_dialog, null)

        recyclerView = view.findViewById(R.id.recyclerView)

        val items = listOf(
            ConnectorItem(R.mipmap.ccscombotype1, "CCS Combo Type 1"),
            ConnectorItem(R.mipmap.ccscombotype2, "CCS Combo Type 2"),
            ConnectorItem(R.mipmap.chademo, "CHAdeMO"),
            ConnectorItem(R.mipmap.gbt, "GB/T"),
            ConnectorItem(R.mipmap.supercharger, "Supercharger"),
            ConnectorItem(R.mipmap.type1j1772, "Type 1 J1772"),
            ConnectorItem(R.mipmap.type2mennekes, "Type 2 Mennekes")
        )

        val adapter = ConnectorAdapter(items) {
            //TODO make selected
            selectedConnector = ""
        }
        recyclerView.adapter = adapter

        builder.setPositiveButton("Done") { _, _ ->
            selectedConnector = adapter.selectedItemText.toString()
            DataManager.connectorType = selectedConnector
            dismiss()
        }

        builder.setView(view)
        builder.setTitle(resources.getString(R.string.text_connector_types))

        return builder.create().apply {
            //TODO make this work
            setCancelable(false)
        }
    }
}
