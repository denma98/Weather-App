import android.icu.text.TimeZoneFormat.GMTOffsetPatternType
import com.example.weatherapp.HistoricalWeather
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.TimeZone

interface HistoricalApiInterface {
    @GET("archive")
    fun getHistoricalWeather(
        @Query("latitude") latitude: String,
        @Query("longitude") longitude: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("hourly") hourlyData: String = "temperature_2m,relative_humidity_2m,rain",
        @Query("daily") dailyData: String = "temperature_2m_max,temperature_2m_min,sunrise,sunset",
        @Query("wind_speed_unit") windSpeedUnit: String = "ms",
        @Query("timezone") timeZone: String = "GMT"
    ): Call<HistoricalWeather>
}
