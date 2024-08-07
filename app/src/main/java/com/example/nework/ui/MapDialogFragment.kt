package com.example.nework.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.example.nework.R
import com.example.nework.util.DrawableImageProvider
import com.example.nework.util.toast
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import dagger.hilt.android.AndroidEntryPoint

//https://stackoverflow.com/questions/26586665/mapview-in-a-dialog
//https://code.luasoftware.com/tutorials/android/android-google-maps-load-supportmapfragment-in-alertdialog-dialogfragment
//https://yandex.ru/dev/mapkit/doc/ru/android/generated/getting_started
@AndroidEntryPoint
class MapDialogFragment(
    private val lat: Double,
    private val long: Double
) : DialogFragment() {

    private var _mapView: MapView? = null
    private var _boardView: Button? = null

    private val imageProvider by lazy {
        DrawableImageProvider(requireContext(), R.drawable.baseline_location_pin_48)
    }

    //@SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //MapKitFactory start/stop instance and initialize in activity
        val dialog = Dialog(requireContext())
        val inflater: LayoutInflater = this@MapDialogFragment.getLayoutInflater()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_map)

        _mapView = dialog.findViewById<MapView>(R.id.mapView)
        _boardView = dialog.findViewById<Button>(R.id.positionBoard)

        dialog.window!!.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val okButton = dialog.findViewById<Button>(R.id.okButton)
        okButton.setOnClickListener {
            this@MapDialogFragment.dismissNow()
        }

        setUpMap(lat, long)

        return dialog
    }

    private fun setUpMap(
        lat: Double,
        long: Double,
        zoom: Float = 17.0f,
        azimut: Float = 150.0f,
        tilt: Float = 30.0f
    ) {
        val map = _mapView?.mapWindow?.map
        if (map != null) {
            map.move(
                CameraPosition(
                    Point(lat, long),
                    zoom, azimut, tilt
                )
            )
            map.mapObjects.addPlacemark().apply {
                geometry = Point(lat, long)
                setIcon(imageProvider)
            }
            try {
                _boardView!!.text = getString(
                    R.string.show_position,
                    "%.4f".format(lat),
                    "%.4f".format(long)
                )
            } catch (e: Exception) {
                errorDestroy()
                if (!e.message.isNullOrBlank()) {
                    println("ERROR MESSAGE: ${e.message}")
                }
            }
        } else {
            errorDestroy()
        }
    }

    private fun clearModels() {
        _mapView = null
        _boardView = null
    }

    override fun onDestroy() {
        clearModels()
        super.onDestroy()
    }

    private fun errorDestroy() {
        //toast has NOT lifecycle, but getString method links with fragment lifecycle
        this@MapDialogFragment.toast(getString(R.string.error_with_creating_map))
        this@MapDialogFragment.onDestroy()
    }
}