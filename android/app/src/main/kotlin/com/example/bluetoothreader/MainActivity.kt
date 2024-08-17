package com.example.bluetoothreader

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager  // Import this package
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val CHANNEL = "samples.flutter.dev/bluetooth"
    private val REQUEST_BLUETOOTH_PERMISSIONS = 1
    private val bluetoothDevicesList = mutableListOf<String>()

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestBluetoothPermissions()
        }

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            if (call.method == "getBluetoothDevices") {
                val devices = getBluetoothDevices()
                if (devices != null) {
                    result.success(devices)
                } else {
                    result.error("UNAVAILABLE", "Bluetooth devices not available.", null)
                }
            } else {
                result.notImplemented()
            }
        }
    }

    private fun getBluetoothDevices(): List<String>? {
        if (bluetoothAdapter == null) {
            return null
        }

        // Get paired devices
        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
        pairedDevices.forEach { device ->
            bluetoothDevicesList.add("${device.name} (${device.address})")
        }

        // Register a receiver for discovering new devices
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)

        // Start Bluetooth discovery
        bluetoothAdapter.startDiscovery()

        // Wait for discovery to finish
        Thread.sleep(12000) // 12 seconds, adjust this if necessary

        unregisterReceiver(receiver)

        return bluetoothDevicesList
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    val deviceName = it.name
                    val deviceAddress = it.address // MAC address
                    if (deviceName != null && !bluetoothDevicesList.contains("$deviceName ($deviceAddress)")) {
                        bluetoothDevicesList.add("$deviceName ($deviceAddress)")
                    }
                }
            }
        }
    }

    private fun requestBluetoothPermissions() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        ActivityCompat.requestPermissions(this, permissions, REQUEST_BLUETOOTH_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, proceed with Bluetooth operations
            } else {
                // Permissions denied, handle accordingly
            }
        }
    }
}
