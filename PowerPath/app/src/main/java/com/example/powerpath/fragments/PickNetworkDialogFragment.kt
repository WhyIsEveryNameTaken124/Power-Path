package com.example.powerpath.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.example.powerpath.R
import com.example.powerpath.userData.NewDataManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PickNetworksDialogFragment : DialogFragment() {

    @Inject
    lateinit var dataManager: NewDataManager
    private lateinit var searchEditText: EditText
    private lateinit var listViewNetworks: ListView
    private lateinit var titleTextView: TextView
    private val selectedNetworks by lazy { dataManager.selectedNetworks }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_pick_network_dialog, null)

        searchEditText = view.findViewById(R.id.editTextSearch)
        listViewNetworks = view.findViewById(R.id.listViewNetworks)
        titleTextView = view.findViewById(R.id.textViewTitle)

        val networkNames = arrayOf("ABC Lataus", "ACCIONA - Cargacoches", "Alfapower (UK)", "Alizé Liberté", "Allego BV", "Aral pulse", "Atlante", "Autoenterprise / АвтоЭнтерпрайз", "autoPilDYK", "Avacon", "BarterGo (ES)", "Be Charge (Italy)", "Be Energised (has-to-be)", "BeCharged", "Belorusneft", "Blink Charging", "Blink Charging (Europe)", "Blue Corner (Belgium)", "BP Pulse (UK)", "Charge & Drive (Fortum - NO)", "Charge & Fuel", "Charge My Ride (Malta)", "ChargeIT mobility", "Chargemaster", "ChargePoint", "Chargy (LU)", "Circle K", "CLEVER", "Comfortcharge", "CV Charging Vehicles", "DEI Blue", "Duferco Energie", "e-Charge (Romania)", "E-Mobi", "e-space (GE)", "E-Way (RU)", "E.ON (CZ)", "E.ON (DE)", "E.ON (DK)", "E.ON (HU)", "E.ON Drive", "EAC e-charge", "Easy4You", "EcoCharge77", "EcoFactor Network", "Ecotap", "Ekoen", "Eldrive Lithuania (LT)", "Electra", "Electric 55 Charging (FR)", "Electro.Cars (RU)", "ElectRoad (UK)", "Electromaps", "Electroukraine", "Elektro Ljublana", "Elen", "Elinta Charge", "Elli (Volkswagen Group Charging GmbH)", "ELMO", "ELMotion", "ELMŰ", "ElpeFuture", "eMallorca (ES)", "Emobitaly (Italy)", "En Yakıt (TR)", "EnBW (D)", "Endesa", "Enel X", "EnergieDienst", "Engie", "Enovos", "eondrive.ro", "Essent (NL)", "Estonteca", "eTecnic", "EV Dot", "EV Georgia", "EV Loader", "EV Point Moldova", "EV-Box", "EV-Mag", "EVC", "evconnect.ro", "EVite (ch)", "Evmapa (CZ)", "EVnetNL", "EVSpots (RO)", "Evway", "EWE", "Ewiva (IT)", "EŞarj (TR)", "FastNed", "Fenie Energía (Spain)", "FORTISIS", "Free To X", "Freshmile", "Galactico.pl", "GioEV (TR)", "Go ToU", "GO+EAuto", "GOFAST (Gotthard Fastcharge)", "Greenflux", "Greenway", "Greenway Polska (PL)", "GridServe", "Grønn Kontakt", "GSS Power (ES)", "Hrvatski Telekom", "Iberdrola", "IECharge (FR)", "Ignitis UAB (LT)", "Incharge", "Infinity EV (Egypt)", "Innogy SE (RWE eMobility)", "InstaVolt Ltd", "Ionity", "it Charge (RU)", "Izivia (Sodetrel)", "K Lataus", "Kelag AG", "KiWhi Pass", "ladenetz.de", "LAKD (LT)", "Level2.ee", "Lidl", "MELIB (ES)", "MER", "Mercadona", "Mobib (Belgium)", "Mobiliti.hu", "MObiVE", "MOL", "Monta", "Motionbox", "MOVE (CH)", "MVM Partner Zrt.", "N-ERGIE", "Neogy", "Neste Lataus", "Nissan (ES) Dealer Network", "Nissan DE Freistrom", "Nissan UK Dealer Network", "NKM mobiliti", "Nomadpower", "NRGincharge", "OlifeEnergy", "OnCharge (TR)", "OpenLoop", "Optimum Way", "ORES", "Orion Telekom (Serbia)", "Orlencharge", "Osprey Charging", "Park & Charge (D)", "pass pass électrique", "PETROL", "PKP Mobility", "Place To Plug", "Plug N Go Ltd", "Plug n Roll", "Plugit", "plugpoint.ro", "PlugSurfing", "POD Point (UK)", "Polyfazer (RO)", "Power Dot", "PowerŞarj", "POWY", "Procredit", "Protergia Charge", "Punkt-E", "Qovoltis", "Recharge (Formerly Fortum Charge & Drive)", "Renault", "Repower (Italy)", "Repsol - Ibil (ES)", "Rosseti (RU)", "RPSnet", "Révéo (FR)", "SDEY (Fr)", "Sharz.Net", "Shell Recharge (ES) (Cable Energia)", "Shell Recharge Solutions (BE)", "Shell Recharge Solutions (DE)", "Sitronics Electro (RU)", "Slovenské elektrárne", "SMATRICS Netz", "Stadtwerke Clausthal-Zellerfeld", "Swarco E.Connect", "SWB / EWE", "Swisscharge (CH)", "Tank & Rast", "TANKE Wien Energie", "TEA.", "Tesla (including non-tesla)", "Tesla (Tesla-only charging)", "The GeniePoint Network ( EQUANS EV Solutions )", "ThePluginCompany (Belgium)", "Toka Energy", "ToraŞarj (TR)", "TotalEnergies (FR)", "Trenčiansky samosprávny kraj", "Trugo (TR)", "ubitricity", "Umeå Energi", "Unipark [Stova] (LT)", "UnitCharge (RU)", "Vattenfall InCharge", "Vend Electric", "VIRTA", "Vlotte", "Volthero", "Voltrun", "VR Schneller-Strom-tanken", "VSE", "WAT Mobilite", "Wenea", "YASNO E-mobility", "ZapGrid", "ZE-MO (Be)", "Zepto", "Zero Carbon World", "ZES", "ZEVS (RU)", "ZSE Drive", "Zunder", "ČEZ", "ŞarjON (TR)", "ЕВН (EVN) (Macedonia)", "Маланка (Belarus)", "РусГидро (Russia)", "СтавЭС (RU)")

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_multiple_choice, networkNames)
        listViewNetworks.adapter = adapter
        listViewNetworks.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        for (network in selectedNetworks) {
            val position = networkNames.indexOf(network)
            if (position != -1) {
                listViewNetworks.setItemChecked(position, true)
            }
        }

        listViewNetworks.setOnItemClickListener { _, _, position, _ ->
            val selectedNetwork = adapter.getItem(position)
            if (selectedNetwork != null) {
                if (selectedNetworks.contains(selectedNetwork)) {
                    selectedNetworks.remove(selectedNetwork)
                } else {
                    selectedNetworks.add(selectedNetwork)
                }
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        builder.setPositiveButton("Done") { _, _ ->
            dataManager.selectedNetworks = selectedNetworks
            dismiss()
        }

        builder.setView(view)
        builder.setTitle(resources.getString(R.string.text_networks))

        return builder.create().apply {
            //TODO make this work
            setCancelable(false)
        }
    }
}
