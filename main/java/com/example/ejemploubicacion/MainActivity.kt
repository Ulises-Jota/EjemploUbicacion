package com.example.ejemploubicacion

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.OnSuccessListener


class MainActivity : AppCompatActivity() {

    private val permisoFineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val permisoCoarseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION

    private val CODIGO_SOLICITUD_PERMISO = 100

    var fusedLocationClient: FusedLocationProviderClient? = null

    var locationRequest: LocationRequest? = null

    var callback: LocationCallback? = null

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = FusedLocationProviderClient(this)
        inicializarLocationRequest()
    }

    @SuppressLint("RestrictedApi")
    private fun inicializarLocationRequest(){
        locationRequest = LocationRequest()
        locationRequest?.interval = 10000
        locationRequest?.fastestInterval = 5000
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun validarPermisosUbicacion(): Boolean {
        val hayUbicacionPrecisa = ActivityCompat.checkSelfPermission(this, permisoFineLocation) == PackageManager.PERMISSION_GRANTED
        val hayUbicacionOrdinaria = ActivityCompat.checkSelfPermission(this, permisoCoarseLocation) == PackageManager.PERMISSION_DENIED

        return hayUbicacionPrecisa && hayUbicacionOrdinaria
    }

    private fun obtenerUbicacion() {
        // Para obtener la última ubicación (consume menos recursos pero no es la más precisa)
/*
        fusedLocationClient?.lastLocation?.addOnSuccessListener(this, object: OnSuccessListener<Location>{

            override fun onSuccess(location: Location?) {
                if (location != null){
                    Toast.makeText(applicationContext, "${location.latitude} - ${location.longitude}", Toast.LENGTH_SHORT).show()
                }
            }
        })
*/

        // Para obtener la ubicación a medida que cambia (consume más recursos pero es la más precisa)
        callback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                for (ubicacion in locationResult?.locations!!){
                    Toast.makeText(applicationContext, "${ubicacion.latitude} - ${ubicacion.longitude}", Toast.LENGTH_LONG).show()
                }
            }
        }

        fusedLocationClient?.requestLocationUpdates(locationRequest, callback, null)
    }

    private fun pedirPermisos() {
        val deboProveerContexto = ActivityCompat.shouldShowRequestPermissionRationale(this, permisoFineLocation)

        if (deboProveerContexto){
            // Mandar mensaje con explicación adicional
            solicitudPermiso()
        } else {
            solicitudPermiso()
        }
    }

    private fun solicitudPermiso(){
        requestPermissions(arrayOf(permisoCoarseLocation, permisoFineLocation), CODIGO_SOLICITUD_PERMISO)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            CODIGO_SOLICITUD_PERMISO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    obtenerUbicacion()
                } else {
                    Toast.makeText(this, "No diste permiso para acceder a la ubicación", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun detenerActualizacionUbicacion(){
        fusedLocationClient?.removeLocationUpdates(callback)
    }

    override fun onStart() {
        super.onStart()

        if (validarPermisosUbicacion()) {
            obtenerUbicacion()
        } else {
            pedirPermisos()
        }
    }

    override fun onPause() {
        super.onPause()
        detenerActualizacionUbicacion()
    }


}


