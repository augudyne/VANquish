package com.projects.valerian.samplemapapplication

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*

class MapsActivity : AppCompatActivity() {

    private lateinit var locationMarker: Marker
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var hasLocationPermission = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.content) as SupportMapFragment

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapFragment.getMapAsync {
            setupMap(it)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_SEND_SUGGESTION ->
                if (resultCode == Activity.RESULT_OK) showSnackbar("Thanks for your suggestion!")
            else ->
                super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) =
            when (requestCode) {
                REQUEST_LOCATION_PERMISSION -> {
                    hasLocationPermission = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                }
                else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }

    private fun setupMap(googleMap: GoogleMap) {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION)
        } else {
            hasLocationPermission = true
            googleMap.run {
                isMyLocationEnabled = true

                setOnMapLongClickListener { latLng ->
                    locationMarker.run {
                        position = latLng
                        snippet = "%.4f, %.4f".format(position.longitude, position.latitude)
                        setOnInfoWindowClickListener {
                            launchAddSuggestionActivity(this@MapsActivity, position)
                        }
                    }
                }

                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    moveCamera(CameraUpdateFactory.zoomTo(14.0f))
                    location?.let {
                        val coords = LatLng(it.latitude, it.longitude)
                        locationMarker = addMarker(MarkerOptions().default(coords)).apply {
                            setOnInfoWindowClickListener {
                                launchAddSuggestionActivity(this@MapsActivity, coords)
                            }
                            showInfoWindow()
                        }

                        moveCamera(CameraUpdateFactory.newLatLng(coords))
                    } ?: Snackbar.make(main, "Unable to get last known location", Snackbar.LENGTH_LONG).show()

                }
            }
        }
    }

    private fun MarkerOptions.default(latLng: LatLng) = MarkerOptions()
            .position(latLng)
            .title("Suggest safety improvement here")
            .snippet("%.4f, %.4f".format(latLng.latitude, latLng.longitude))


    private fun launchAddSuggestionActivity(context: Context, latLng: LatLng) {
        startActivityForResult(AddSuggestionActivity.createIntent(
                context,
                latLng.latitude,
                latLng.longitude),
                REQUEST_CODE_SEND_SUGGESTION)
    }

    private fun showSnackbar(msg: String) {
        Snackbar.make(main, msg, Snackbar.LENGTH_LONG).show()
    }

    companion object {
        const val REQUEST_LOCATION_PERMISSION = 0
        const val REQUEST_CODE_SEND_SUGGESTION = 1
    }
}
