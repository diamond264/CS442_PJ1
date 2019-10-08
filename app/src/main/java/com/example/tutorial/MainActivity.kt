package com.example.tutorial

import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

import android.util.Log
import androidx.core.app.ActivityCompat

import android.location.Geocoder
import android.location.Location
import java.io.IOException
import java.net.URL
import java.util.*
import android.os.AsyncTask
import android.os.Looper
import com.google.android.gms.location.*
import org.json.JSONObject
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity() {
    private var PERMISSION_ALL = 1
    private var PERMISSIONS = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_NETWORK_STATE,
        android.Manifest.permission.INTERNET
    )
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallBack: LocationCallback

    private lateinit var backgroundWeatherTask: getWeatherTask

    private fun checkPermissions(context: Context, permissions: Array<String>): Boolean {
        for(permission in permissions) {
            if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_ALL)
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            return
        } else {
            checkPermissions(this, PERMISSIONS)
        }
        return
    }

//    private fun getWeatherFromLocation(location: Location?) {
//        if (location != null) {
//            Log.d("Location", "${location.latitude}, ${location.longitude}")
//            Thread {
//            var apiUrl = "https://api.openweathermap.org/data/2.5/weather?"
//            apiUrl = apiUrl.plus("lat=${location.latitude}&lon=${location.longitude}")
//            apiUrl = apiUrl.plus("&APPID=${AppID}")
//
//            var returnValue = URL(apiUrl).readText()
//            Log.d("sfd",returnValue)
//            }.start()
//        }
//    }

    private class getWeatherTask(context: MainActivity) : AsyncTask<Location, Void, JSONObject>() {
        private var AppID: String = "ae2d44ad65593980137e8deef6000cb8"
        private val activityReference: WeakReference<MainActivity> = WeakReference(context)

        override fun doInBackground(vararg locations: Location): JSONObject? {
            val location = locations[0]
            var JsonInfoString = ""
            Log.d("Location", "${location.latitude}, ${location.longitude}")
            var apiUrl = "https://api.openweathermap.org/data/2.5/weather?"
            apiUrl = apiUrl.plus("lat=${location.latitude}&lon=${location.longitude}")
            apiUrl = apiUrl.plus("&APPID=${AppID}")
            val weatherInfo = JSONObject(URL(apiUrl).readText())
            val city = weatherInfo.getString("name")
            val temperature = weatherInfo.getJSONObject("main").getDouble("temp")
            val humidity = weatherInfo.getJSONObject("main").getInt("humidity")
            val condition = weatherInfo.getJSONArray("weather").getJSONObject(0).getString("main")
            val temp_min = weatherInfo.getJSONObject("main").getDouble("temp_min")
            val temp_max = weatherInfo.getJSONObject("main").getDouble("temp_max")
            val currString = "{\"city\":\"${city}\"," +
                    "\"temperature\":${temperature}," +
                    "\"humidity\":${humidity}," +
                    "\"condition\":\"${condition}\"," +
                    "\"temp_min\":${temp_min}," +
                    "\"temp_max\":${temp_max}}"

            apiUrl = "https://api.openweathermap.org/data/2.5/forecast?"
            apiUrl = apiUrl.plus("lat=${location.latitude}&lon=${location.longitude}")
            apiUrl = apiUrl.plus("&APPID=${AppID}")
            val forecastInfo = JSONObject(URL(apiUrl).readText()).getJSONArray("list").toString()
            Log.d("sfd",currString)

            JsonInfoString = "{\"current\":"+currString+",\"forecast\":"+forecastInfo+"}"
            return JSONObject(JsonInfoString)
        }

        override fun onPostExecute(result: JSONObject?) {
            Log.d("success", result.toString())
            val activity = activityReference.get()
            if (activity == null || activity.isFinishing) return

            if (result != null) {
                val currJSONObject = result.getJSONObject("current")
                activity.city_text.text = currJSONObject.getString("city")
                activity.temperature_text.text = "%.1f".format(currJSONObject.getDouble("temperature")-273.15)
                activity.condition_text.text = currJSONObject.getString("condition")
                activity.mintemperature_text.text = "%.1f".format(currJSONObject.getDouble("temp_min")-273.15)
                activity.maxtemperature_text.text = "%.1f".format(currJSONObject.getDouble("temp_max")-273.15)
                activity.humidity_text.text = "%d%% humidity".format(currJSONObject.getInt("humidity"))
            }

        }
    }

    private fun getLocation() {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("ERROR", "Permission Denied")
            return
        }

        locationClient = LocationServices.getFusedLocationProviderClient(this)
        locationClient.lastLocation.addOnSuccessListener { location ->
//                getWeatherFromLocation(location)
                backgroundWeatherTask.execute(location)
            }.addOnFailureListener {
                Log.e("ERROR", "location error is ${it.message}")
                it.printStackTrace()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermissions(this, PERMISSIONS)

        backgroundWeatherTask = getWeatherTask(this)
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest()
        locationCallBack = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                Log.d("logfor","callback")
//                getWeatherFromLocation(locationResult.lastLocation)
//                backgroundWeatherTask.execute(locationResult.lastLocation)
            }
        }

        locationRequest.run {
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            interval = 10000
            fastestInterval = 5000
        }

        locationClient.requestLocationUpdates(locationRequest,
            locationCallBack,
            Looper.getMainLooper())

        refreshButton.setOnClickListener {
            Log.d("hasdfsa","dfd")
            textView_442.text = "CS442"
            getLocation()
        }
    }

    override fun onResume() {
        super.onResume()
        locationClient.requestLocationUpdates(locationRequest,
            locationCallBack,
            Looper.getMainLooper())
    }

    override fun onPause() {
        super.onPause()
//        locationClient.removeLocationUpdates(locationCallBack)
    }
}
