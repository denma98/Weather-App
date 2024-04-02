        package com.example.weatherapp
    
        import HistoricalApiInterface
        import android.annotation.SuppressLint
        import android.app.DatePickerDialog
//        import android.arch.persistence.room.Room
        import android.os.Bundle
        import android.util.Log
        import android.widget.Button
        import android.widget.DatePicker
        import android.widget.SearchView
        import androidx.activity.enableEdgeToEdge
        import androidx.appcompat.app.AppCompatActivity
        import androidx.lifecycle.lifecycleScope
        import androidx.room.Room

        import com.example.weatherapp.databinding.ActivityMainBinding
        import kotlinx.coroutines.Dispatchers
        import kotlinx.coroutines.launch
        import kotlinx.coroutines.withContext
        import retrofit2.Call
        import retrofit2.Callback
        import retrofit2.Response
        import retrofit2.Retrofit
        import retrofit2.converter.gson.GsonConverterFactory
        import java.sql.Timestamp
        import java.text.SimpleDateFormat
        import java.util.Calendar
        import java.util.Date
        import java.util.Locale

        //96e2e2d2e3cd6c22982de9d6160b23cc
        class MainActivity : AppCompatActivity() {
            private lateinit var selectDateButton: Button
            private lateinit var weatherDao: WeatherDao
            //date variable
            private var date: String = "2024-01-01"
            private var lat = "0.0"
            private var long = "0.0"
    
            private val binding: ActivityMainBinding by lazy {
                ActivityMainBinding.inflate(layoutInflater)
            }
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                enableEdgeToEdge()
                setContentView(binding.root) // Set the content view first

                val weatherDatabase = WeatherDatabase.DatabaseBuilder.getInstance(applicationContext)
                weatherDao = weatherDatabase.weatherDao()

                selectDateButton = findViewById(R.id.selectDateButton)
                selectDateButton.setOnClickListener {
                    showDatePickerDialog()
    //                fetchWeatherData(binding.CityName.text.toString())
                }
                fetchWeatherData("London")
    //            fetchHistoricalWeatherData(lat, long, date)
                searchCity()
            }
    
    
            private fun findLatLong(CityName: String) {
                val retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://api.openweathermap.org/data/2.5/")
                    .build().create(ApiInterface::class.java)
    
                val response = retrofit.getWeatherData(CityName, "96e2e2d2e3cd6c22982de9d6160b23cc", "metric")
    
                response.enqueue(object : Callback<WeatherApp> {
                    @SuppressLint("SetTextI18n")
                    override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                        val responseBody = response.body()
                        if (response.isSuccessful && responseBody != null) {
                            val latitude = responseBody.coord.lat.toString()
                            val longitude = responseBody.coord.lon.toString()
                            lat = latitude
                            long = longitude
    
                        }
    
                    }
    
                    override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                        Log.d("Error", t.message.toString())
                    }
                })
            }
    
    
            private fun showDatePickerDialog() {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    
                val datePickerDialog = DatePickerDialog(
                    this,
                    { view: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                        // Adjust the month and day to ensure they have leading zeros if necessary
                        val formattedMonth = String.format(Locale.getDefault(), "%02d", selectedMonth + 1)
                        val formattedDayOfMonth = String.format(Locale.getDefault(), "%02d", selectedDayOfMonth)
                        date = "$selectedYear-$formattedMonth-$formattedDayOfMonth"
                        findLatLong(binding.CityName.text.toString())
                        fetchWeatherData(binding.CityName.text.toString())
    
                    },
                    year,
                    month,
                    dayOfMonth
                )
                datePickerDialog.show()
            }
    
    
    
            private fun searchCity(){
                val searchView = binding.searchView
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        if( query != null){
                            fetchWeatherData(query)
                        }
                        return false
                    }
    
                    override fun onQueryTextChange(newText: String?): Boolean {
                        return false
                    }
                })
            }
    
    
            private fun fetchWeatherData(CityName: String){
                val retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://api.openweathermap.org/data/2.5/")
                    .build().create(ApiInterface::class.java)
    
                val response = retrofit.getWeatherData(CityName, "96e2e2d2e3cd6c22982de9d6160b23cc", "metric")
    
                response.enqueue(object : Callback<WeatherApp>{
                    @SuppressLint("SetTextI18n")
                    override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                        val responseBody = response.body()
                        if(response.isSuccessful && responseBody != null){
                            val latitude = responseBody.coord.lat.toString()
                            val longitude = responseBody.coord.lon.toString()
                            val temperature = responseBody.main.temp.toString()
                            val humidity = responseBody.main.humidity.toString()
                            val windSpeed = responseBody.wind.speed.toString()
                            val sunRise = responseBody.sys.sunrise.toString()
                            val sunSet = responseBody.sys.sunset.toString()
                            val seaLevel = responseBody.main.pressure.toString()
                            val maxTemp = responseBody.main.temp_max
                            val minTemp = responseBody.main.temp_min
                            val condition = responseBody.weather.firstOrNull()?.main?:toString()
                            binding.Temperature.text = "$temperature°C"
                            binding.windSpeed.text = "$windSpeed m/s"
                            binding.seaLevel.text = "$seaLevel hPa"
                            binding.CityName.text = CityName
    
    
    
                            val selectedDate = date
                            val currDate = date()
                            if( selectedDate == currDate){
                                binding.Condition.text = condition
                                binding.MaxTemp.text = "MinTemp: $maxTemp°C"
                                binding.MinTemp.text = "MaxTemp: $minTemp°C"
                                binding.humidity.text = "$humidity%"
                                binding.sunriseTime.text = "$sunRise"
                                binding.Date.text = date
                                binding.sunsetTime.text = "$sunSet"
                                binding.Day.text = day(Timestamp(System.currentTimeMillis()))

                                val weatherData = WeatherData(
                                    cityName = CityName,
                                    maxTemp = maxTemp,
                                    minTemp = minTemp,
                                    date = selectedDate
                                )

                                lifecycleScope.launch {
                                    // Perform database operation asynchronously on a background thread
                                    withContext(Dispatchers.IO) {
                                        weatherDao.upsert(weatherData)
                                    }
                                }

                            }
    
                            else if (selectedDate < currDate) {
                                fetchHistoricalWeatherData(lat, long, selectedDate, selectedDate, callback = {
                                })
                            }
    
                            if (selectedDate > date()) {

                                Log.d("in if", "if")
                                fetchAverageHistoricalWeatherData(latitude, longitude, date, date)
                                // update ui to show average data


                            }
    
                            //print statement saying called
                            Log.d("Historical", "Called")
                        }
                    }
                    override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                        Log.d("Error", t.message.toString())
                    }

                })
            }


            private fun fetchHistoricalWeatherData(latitude: String, longitude: String, date: String, dateEnd: String, callback: (List<HistoricalWeather>) -> Unit) {
                val retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://archive-api.open-meteo.com/v1/")
                    .build().create(HistoricalApiInterface::class.java)

                val response = retrofit.getHistoricalWeather(latitude, longitude, date, dateEnd)

                response.enqueue(object : Callback<HistoricalWeather> {
                    @SuppressLint("SetTextI18n")
                    override fun onResponse(
                        call: Call<HistoricalWeather>,
                        response: Response<HistoricalWeather>
                    ) {
                        val historicalResponse = response.body()
                        Log.d("Date outside", date)
                        Log.d("historical response", historicalResponse.toString())
                        Log.d("Response", response.toString())

                        if (response.isSuccessful && historicalResponse != null) {
                            val maxTemp = historicalResponse.daily.temperature_2m_max[0]
                            val minTemp = historicalResponse.daily.temperature_2m_min[0]
                            val sunrise = historicalResponse.daily.sunrise.toString()
                            val sunset = historicalResponse.daily.sunset.toString()
                            val humidityList = historicalResponse.hourly.relative_humidity_2m
                            val maxHumidity = humidityList.maxOrNull() ?: 0.0
                            val humidity = maxHumidity.toString()
                            val sunriseTime = extractTimeFromDateTime(sunrise)
                            val sunsetTime = extractTimeFromDateTime(sunset)

//

                            val weatherData = WeatherData(
                                cityName = binding.CityName.text.toString(),
                                maxTemp = maxTemp,
                                minTemp = minTemp,
                                date = date
                            )
                            val currDate = date();
                            // log currdate and date
                            Log.d("Date in else", date)
                            // log currdate
                            Log.d("Date in mu", currDate)

                            //convert date variable to calendar instance
                            val dateCalendar = SimpleDateFormat("yyyy-MM-dd").parse(date)
                            val calendar = Calendar.getInstance()
                            calendar.time = dateCalendar

                            val currentDate = SimpleDateFormat("yyyy-MM-dd").parse(currDate)
                            val curr = Calendar.getInstance()
                            curr.time = currentDate

//                            Log.d("Date in current date calendar instance", currDateCalendar.toString())
//                            Log.d("Date in input date calendar instance", dateCalendar.toString())
                            //log calendar.after(curr)
//                            Log.d("Date in current date calendar instance", calendar.after())

//                            if(calendar.after(Calendar.getInstance())){
//                                Log.d("in if", "if")
//                                fetchAverageHistoricalWeatherData(latitude, longitude, date, dateEnd)
//                            }

                            lifecycleScope.launch {
                                // Perform database operation asynchronously on a background thread
                                withContext(Dispatchers.IO) {
                                    weatherDao.upsert(weatherData)
                                }
                            }

                            Log.d("Date in IF", date)
                            runOnUiThread {
                                Log.d("Date on UI", date)
                                binding.MaxTemp.text = "MaxTemp: $maxTemp°C"
                                binding.MinTemp.text = "MinTemp: $minTemp°C"
                                binding.sunriseTime.text = "$sunriseTime:00"
                                binding.sunsetTime.text = "$sunsetTime :00"
                                binding.humidity.text = "$humidity%"
                                binding.Date.text = date
                            }
                        }
                    }

                    override fun onFailure(call: Call<HistoricalWeather>, t: Throwable) {
                        Log.d("Error", t.message.toString())
                    }
                })
            }

            @SuppressLint("SetTextI18n")
            private fun fetchAverageHistoricalWeatherData(latitude: String, longitude: String, date: String, dateEnd: String) {
                // Initialize variables to store the sum of min and max temperatures
                var sumMaxTemp = 0.0
                var sumMinTemp = 0.0
                //log working?
                Log.d("working", "working")
                val dateCalendar = SimpleDateFormat("yyyy-MM-dd").parse(date)
                //create calendar instance
                // Get the current year
                val calendar = Calendar.getInstance()
                calendar.time= dateCalendar

                for(i in 1..10){
                    calendar.add(Calendar.YEAR, -1)
                    val dateN = SimpleDateFormat("yyyy-MM-dd").format(calendar.time)
                    Log.d("Date in for loop", date)
                    fetchHistoricalWeatherData(latitude, longitude, dateN, dateN) { historicalWeather ->
                        // Add the min and max temperatures to the sums
                        sumMaxTemp += historicalWeather[0].daily.temperature_2m_max[0]
                        sumMinTemp += historicalWeather[0].daily.temperature_2m_min[0]
                    }
                }

                // Iterate over the last 10 years
//                for (year in currentYear downTo currentYear - 10) {
//                    // Format the date string for the specific year
//                    val dateInYear = date.replaceRange(0, 4, year.toString())
//
//                    // Fetch the historical weather data for the date
//                    fetchHistoricalWeatherData(latitude, longitude, dateInYear, dateInYear) { historicalWeather ->
//                        // Add the min and max temperatures to the sums
//                        sumMaxTemp += historicalWeather[0].daily.temperature_2m_max[0]
//                        sumMinTemp += historicalWeather[0].daily.temperature_2m_min[0]
//                    }
//                }

                // Calculate the averages
                val avgMaxTemp = sumMaxTemp / 10
                val avgMinTemp = sumMinTemp / 10

                // Display the averages on the UI
                runOnUiThread {
                    binding.MaxTemp.text = "Avg MaxTemp: $avgMaxTemp°C"
                    binding.MinTemp.text = "Avg MinTemp: $avgMinTemp°C"
                    //update date in ui
                    binding.Date.text = date
                }
            }



            private fun extractTimeFromDateTime(dateTime: String): String {
                // Assuming the dateTime format is like "YYYY-MM-DDTHH:mm:ss"
                val time = dateTime.substringAfter('T').substringBeforeLast(':')
                return time
            }
    
    
            private fun date(): String {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                return date.toString()
            }
    
    
            private fun day(timestamp: Timestamp): String {
                val date = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
                return date.toString()
            }
        }