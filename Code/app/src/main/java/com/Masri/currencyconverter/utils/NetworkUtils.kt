    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.withContext
    import okhttp3.OkHttpClient
    import okhttp3.Request
    import org.json.JSONObject
    import java.util.concurrent.TimeUnit
    import java.io.IOException
    
    suspend fun fetchLiveRates(): Map<String, Double> {

        val url = "https://data.fixer.io/api/latest?access_key=5144da2a7d9833c31eb755a1e15e4b78"
    
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    
        val request = Request.Builder().url(url).build()
    
        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
    
                // Always close the response body
                response.use {
                    val responseBody = it.body?.string()
    
                    println("API Response: $responseBody")
    
                    if (!it.isSuccessful) {
                        throw Exception("Failed to fetch rates: ${it.code} - $responseBody")
                    }
    
                    if (responseBody.isNullOrEmpty()) {
                        throw Exception("API response is empty")
                    }
    
                    val json = JSONObject(responseBody)
    
                    // Check for API errors first
                    if (json.has("error")) {
                        val error = json.getJSONObject("error")
                        throw Exception("API Error: ${error.getString("info")}")
                    }
    
                    if (!json.has("rates")) {
                        throw Exception("API error: Rates not found in the response")
                    }
    
                    val rates = json.getJSONObject("rates")
                    val rateMap = mutableMapOf<String, Double>()
                    rates.keys().forEach { key ->
                        rateMap[key] = rates.getDouble(key)
                    }
                    rateMap
                }
            } catch (e: IOException) {
                throw Exception("Network error: ${e.message}", e)
            } catch (e: Exception) {
                throw Exception("Error fetching rates: ${e.message}", e)
            }
        }
    }
