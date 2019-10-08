package com.example.tutorial

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

import android.util.Log
import androidx.core.app.ActivityCompat
import android.location.Location
import java.net.URL
import java.util.*
import android.os.AsyncTask
import android.os.Looper
import android.view.WindowManager
import android.widget.ImageView
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

    private fun checkPermissions(permissions: Array<String>): Boolean {
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
            checkPermissions(PERMISSIONS)
        }
        return
    }

    private class getWeatherTask(context: MainActivity) : AsyncTask<Location, Void, JSONObject>() {
        private var AppID: String = "ae2d44ad65593980137e8deef6000cb8"
        private val activityReference: WeakReference<MainActivity> = WeakReference(context)

        override fun doInBackground(vararg locations: Location): JSONObject? {
            val location = locations[0]
            var JsonInfoString = ""
            var apiUrl = "https://api.openweathermap.org/data/2.5/weather?"
            apiUrl = apiUrl.plus("lat=${location.latitude}&lon=${location.longitude}")
            apiUrl = apiUrl.plus("&APPID=${AppID}")
            val weatherInfo = JSONObject(URL(apiUrl).readText())
            val city = weatherInfo.getString("name")
            val temperature = weatherInfo.getJSONObject("main").getDouble("temp")
            val humidity = weatherInfo.getJSONObject("main").getInt("humidity")
            var temp_min = weatherInfo.getJSONObject("main").getDouble("temp_min")
            var temp_max = weatherInfo.getJSONObject("main").getDouble("temp_max")
            val condition = weatherInfo.getJSONArray("weather").getJSONObject(0).getString("main")
            var timezone = weatherInfo.getInt("timezone")
            val currCalendar = Calendar.getInstance()
            currCalendar.timeInMillis = (weatherInfo.getInt("dt")+timezone).toLong()*1000

            apiUrl = "https://api.openweathermap.org/data/2.5/forecast?"
            apiUrl = apiUrl.plus("lat=${location.latitude}&lon=${location.longitude}")
            apiUrl = apiUrl.plus("&APPID=${AppID}")
            val forecastInfo = JSONObject(URL(apiUrl).readText())
            val forecastList = forecastInfo.getJSONArray("list")
            timezone = forecastInfo.getJSONObject("city").getInt("timezone")
            var next5hours = "["
            val days = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
            val currDay = currCalendar.get(Calendar.DAY_OF_WEEK)-1
            var day1 = days[currDay+1]; var day2 = days[currDay+2]; var day3 = days[currDay+3]
            var day1humidity = 0; var day2humidity = 0; var day3humidity = 0
            var day1humcnt = 0; var day2humcnt = 0; var day3humcnt = 0
            var day1mintemp = 1000.0; var day2mintemp = 1000.0; var day3mintemp = 1000.0
            var day1maxtemp = 0.0; var day2maxtemp = 0.0; var day3maxtemp = 0.0
            var day1condition = ""; var day2condition = ""; var day3condition = ""
            for(i in 0 until forecastList.length()) {
                val forecast = forecastList.getJSONObject(i)
                val forecastCalendar = Calendar.getInstance()
                forecastCalendar.timeInMillis = (forecast.getInt("dt")+timezone).toLong()*1000
                if(currCalendar.get(Calendar.DATE) == forecastCalendar.get(Calendar.DATE)) {
                    val new_min = forecast.getJSONObject("main").getDouble("temp_min")
                    if(new_min < temp_min) temp_min = new_min
                    val new_max = forecast.getJSONObject("main").getDouble("temp_max")
                    if(new_max > temp_max) temp_max = new_max
                }

                if(currCalendar.get(Calendar.DATE)+1 == forecastCalendar.get(Calendar.DATE)) {
                    val day_min = forecast.getJSONObject("main").getDouble("temp_min")
                    if(day_min < day1mintemp) day1mintemp = day_min
                    val day_max = forecast.getJSONObject("main").getDouble("temp_min")
                    if(day_max > day1maxtemp) day1maxtemp = day_max
                    day1humidity += forecast.getJSONObject("main").getInt("humidity")
                    day1humcnt += 1
                    if(forecastCalendar.get(Calendar.HOUR_OF_DAY) == 12)
                        day1condition = forecast.getJSONArray("weather").getJSONObject(0).getString("main")
                }

                if(currCalendar.get(Calendar.DATE)+2 == forecastCalendar.get(Calendar.DATE)) {
                    val day_min = forecast.getJSONObject("main").getDouble("temp_min")
                    if(day_min < day2mintemp) day2mintemp = day_min
                    val day_max = forecast.getJSONObject("main").getDouble("temp_min")
                    if(day_max > day2maxtemp) day2maxtemp = day_max
                    day2humidity += forecast.getJSONObject("main").getInt("humidity")
                    day2humcnt += 1
                    if(forecastCalendar.get(Calendar.HOUR_OF_DAY) == 12)
                        day2condition = forecast.getJSONArray("weather").getJSONObject(0).getString("main")
                }

                if(currCalendar.get(Calendar.DATE)+3 == forecastCalendar.get(Calendar.DATE)) {
                    val day_min = forecast.getJSONObject("main").getDouble("temp_min")
                    if(day_min < day3mintemp) day3mintemp = day_min
                    val day_max = forecast.getJSONObject("main").getDouble("temp_min")
                    if(day_max > day3maxtemp) day3maxtemp = day_max
                    day3humidity += forecast.getJSONObject("main").getInt("humidity")
                    day3humcnt += 1
                    if(forecastCalendar.get(Calendar.HOUR_OF_DAY) == 12)
                        day3condition = forecast.getJSONArray("weather").getJSONObject(0).getString("main")
                }

                if(i < 5) {
                    val hour = forecastCalendar.get(Calendar.HOUR_OF_DAY)
                    val hour_temp = forecast.getJSONObject("main").getDouble("temp")
                    val hour_condition = forecast.getJSONArray("weather").getJSONObject(0).getString("main")
                    val hour_humidity = forecast.getJSONObject("main").getInt("humidity")
                    next5hours = next5hours +
                            "{\"hour\":${hour}," +
                            "\"temperature\":${hour_temp},"+
                            "\"humidity\":${hour_humidity}," +
                            "\"condition\":\"${hour_condition}\"}"
                    if(i==4) next5hours += "]"
                    else next5hours += ","
                }
            }
            val currString = "{\"city\":\"${city}\"," +
                    "\"temperature\":${temperature}," +
                    "\"humidity\":${humidity}," +
                    "\"condition\":\"${condition}\"," +
                    "\"temp_min\":${temp_min}," +
                    "\"temp_max\":${temp_max}}"

            val next3days = "[{\"day\":\"${day1}\","+
                    "\"temp_min\":${day1mintemp},"+
                    "\"temp_max\":${day1maxtemp},"+
                    "\"humidity\":${day1humidity/day1humcnt},"+
                    "\"condition\":\"${day1condition}\"},"+
                    "{\"day\":\"${day2}\","+
                    "\"temp_min\":${day2mintemp},"+
                    "\"temp_max\":${day2maxtemp},"+
                    "\"humidity\":${day2humidity/day2humcnt},"+
                    "\"condition\":\"${day2condition}\"},"+
                    "{\"day\":\"${day3}\","+
                    "\"temp_min\":${day3mintemp},"+
                    "\"temp_max\":${day3maxtemp},"+
                    "\"humidity\":${day3humidity/day3humcnt},"+
                    "\"condition\":\"${day3condition}\"}]"

            JsonInfoString = "{\"current\":"+currString+
                    ",\"next5hours\":"+next5hours+
                    ",\"next3days\":"+next3days+"}"
            return JSONObject(JsonInfoString)
        }

        private fun setImage(image_activity: ImageView, mainWeather: String) {
            if(mainWeather == "Thunderstorm") image_activity.setImageResource(R.drawable.thunderstorm)
            else if(mainWeather == "Drizzle") image_activity.setImageResource(R.drawable.drizzle)
            else if(mainWeather == "Rain") image_activity.setImageResource(R.drawable.rain)
            else if(mainWeather == "Snow") image_activity.setImageResource(R.drawable.snow)
            else if(mainWeather == "Clear") image_activity.setImageResource(R.drawable.sun)
            else if(mainWeather == "Clouds") image_activity.setImageResource(R.drawable.cloud)
            else image_activity.setImageResource(R.drawable.drizzle)
        }

        override fun onPostExecute(result: JSONObject?) {
            Log.d("TASK_SUCCESS", result.toString())
            val activity = activityReference.get()
            if (activity == null || activity.isFinishing) return

            if (result != null) {
                val currJSONObject = result.getJSONObject("current")
                activity.city_text.text = currJSONObject.getString("city")
                activity.temperature_text.text = "%.1f".format(currJSONObject.getDouble("temperature")-273.15)
                activity.mintemperature_text.text = "%.1f".format(currJSONObject.getDouble("temp_min")-273.15)
                activity.maxtemperature_text.text = "%.1f".format(currJSONObject.getDouble("temp_max")-273.15)
                activity.humidity_text.text = "%d%% humidity".format(currJSONObject.getInt("humidity"))
                val mainWeather = currJSONObject.getString("condition")
                activity.condition_text.text = mainWeather
                setImage(activity.weatherImage, mainWeather)

                val next5hourJSONArray = result.getJSONArray("next5hours")
                var next5hours = arrayOf(activity.next1Hour,activity.next2Hour,activity.next3Hour,activity.next4Hour,activity.next5Hour)
                var next5hours_condition = arrayOf(activity.next1HourImage,activity.next2HourImage,
                    activity.next3HourImage,activity.next4HourImage,activity.next5HourImage)
                var next5hours_temp = arrayOf(activity.next1HourMinMaxTemp,activity.next2HourMinMaxTemp,
                    activity.next3HourMinMaxTemp,activity.next4HourMinMaxTemp,activity.next5HourMinMaxTemp)
                var next5hours_humidity = arrayOf(activity.next1HourHumidity,activity.next2HourHumidity,
                    activity.next3HourHumidity,activity.next4HourHumidity,activity.next5HourHumidity)
                for(i in 0 until 5) {
                    val nexthourJSONObject = next5hourJSONArray.getJSONObject(i)
                    next5hours[i].text = "%02d".format(nexthourJSONObject.getInt("hour"))
                    next5hours_temp[i].text = "%.1f".format(nexthourJSONObject.getDouble("temperature")-273.15)
                    next5hours_humidity[i].text = "%d%%".format(nexthourJSONObject.getInt("humidity"))
                    val nexthour_mainWeather = nexthourJSONObject.getString("condition")
                    setImage(next5hours_condition[i], nexthour_mainWeather)
                }

                val next3dayJSONArray = result.getJSONArray("next3days")
                var next3days = arrayOf(activity.next1Day,activity.next2Day,activity.next3Day)
                var next3days_condition = arrayOf(activity.next1DayImage,activity.next2DayImage,activity.next3DayImage)
                var next3days_minmaxtemp = arrayOf(activity.next1DayMinMaxTemp,activity.next2DayMinMaxTemp, activity.next3DayMinMaxTemp)
                var next3days_humidity = arrayOf(activity.next1DayHumidity,activity.next2DayHumidity,activity.next3DayHumidity)
                for(i in 0 until 3) {
                    val nextdayJSONObject = next3dayJSONArray.getJSONObject(i)
                    next3days[i].text = nextdayJSONObject.getString("day")
                    next3days_minmaxtemp[i].text = "%.1f°C/\n%.1f°C".format(nextdayJSONObject.getDouble("temp_max")-273.15,
                        nextdayJSONObject.getDouble("temp_min")-273.15)
                    next3days_humidity[i].text = "%d%%".format(nextdayJSONObject.getInt("humidity"))
                    val nextday_mainWeather = nextdayJSONObject.getString("condition")
                    setImage(next3days_condition[i], nextday_mainWeather)
                }
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)
        val context = this
        checkPermissions(PERMISSIONS)

        locationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationCallBack = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                getWeatherTask(context).execute(locationResult.lastLocation)
            }
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
        locationClient.removeLocationUpdates(locationCallBack)
    }
}
