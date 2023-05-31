package com.example.weatherforecast;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.weatherforecast.databinding.ActivityMainBinding;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.txtErrorMsg.setVisibility(View.GONE);
        binding.txtWindLabel.setVisibility(View.GONE);
        binding.txtHumidityLable.setVisibility(View.GONE);
        binding.txtPressure.setVisibility(View.GONE);
        String lastCity = getLastCity();
        if (!lastCity.isEmpty()) {
            binding.etPlace.setText(lastCity);
            new WeatherForecast().execute();
        }
        binding.btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new WeatherForecast().execute();
            }
        });
    }

    private String getLastCity() {
        Context context = getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("LastCity", "");
    }

    private class WeatherForecast extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.screen.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String response;
            try {
                String city = binding.etPlace.getText().toString();
                saveCity(city);
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://yahoo-weather5.p.rapidapi.com/weather?location=" + city + "&format=json&u=c")
                        .get()
                        .addHeader("X-RapidAPI-Key", "a0339919a5mshbc4e22fca609929p144e50jsn185f35928a7e")
                        .addHeader("X-RapidAPI-Host", "yahoo-weather5.p.rapidapi.com")
                        .build();
                okhttp3.Response apiResponse = client.newCall(request).execute();
                assert apiResponse.body() != null;
                response = apiResponse.body().string();
                apiResponse.close();
            } catch (Exception e) {
                response = null;
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Error in doInBackground: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                if (result != null && !result.isEmpty()) {
                    JSONObject json = new JSONObject(result);
                    JSONObject currentObservation = json.getJSONObject("current_observation");
                    JSONObject det = currentObservation.getJSONObject("condition");
                    JSONObject wind = currentObservation.getJSONObject("wind");
                    JSONObject humidity = currentObservation.getJSONObject("atmosphere");
                    JSONObject pressure = currentObservation.getJSONObject("atmosphere");
                    long date = currentObservation.getLong("pubDate");
                    String weatherText = "Last viewed at: " + new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(new Date(date * 1000));
                    String temp = Integer.toString(det.getInt("temperature")) + "\u2103";
                    String weatherDesc = det.getString("text");
                    String windSpeed = Integer.toString(wind.getInt("speed"));
                    String humidityValue = Integer.toString(humidity.getInt("humidity"));
                    String pressureValue = Double.toString(pressure.getDouble("pressure"));

                    binding.txtTemp.setText(temp);
                    binding.txtDAT.setText(weatherText);
                    binding.txtWeatherInfo.setText(weatherDesc);
                    binding.wind.setText(windSpeed);
                    binding.humidity.setText(humidityValue);
                    binding.pressure.setText(pressureValue);
                    binding.txtWindLabel.setVisibility(View.VISIBLE);
                    binding.txtHumidityLable.setVisibility(View.VISIBLE);
                    binding.txtPressure.setVisibility(View.VISIBLE);
                    binding.progressBar.setVisibility(View.GONE);
                    binding.screen.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(MainActivity.this, "Error: Data couldn't be fetched", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                binding.progressBar.setVisibility(View.GONE);
                binding.screen.setVisibility(View.GONE);
                binding.txtErrorMsg.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveCity(String city) {
        Context context = getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("LastCity", city);
        editor.apply();
    }
}
