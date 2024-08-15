package com.example.projectconnectandroid;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectconnectandroid.databinding.ActivityMainBinding;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import controller.DeviceController;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DeviceController deviceController;
    private static final Logger logger = Logger.getLogger(MainActivity.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ConnectToDeviceTask().execute("192.168.0.10"); // IP-адрес устройства B
            }
        });

        binding.launchAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LaunchAppTask().execute();
            }
        });
    }

    private class ConnectToDeviceTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Connecting to device B...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String deviceIP = params[0];
            try {
                File adbKeyDir = getFilesDir(); // Папка для хранения ключей
                deviceController = new DeviceController(deviceIP, adbKeyDir); // Подключаемся к устройству B
                deviceController.setupTcpConnection(deviceIP);
                return true;
            } catch (IOException e) {
                logger.severe("Failed to connect to device B: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                Toast.makeText(MainActivity.this, "Connected to device B", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Failed to connect", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class LaunchAppTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (deviceController == null) {
                Toast.makeText(MainActivity.this, "Not connected to device B", Toast.LENGTH_SHORT).show();
                cancel(true); // Отменяем задачу, если нет подключения
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (deviceController != null) {
                try {
                    deviceController.startApplicationOnDeviceB();
                    return true;
                } catch (IOException e) {
                    logger.severe("Failed to start app on device B: " + e.getMessage());
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                Toast.makeText(MainActivity.this, "The command is executed!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Failed to start app =(", Toast.LENGTH_LONG).show();
            }
        }
    }
}
