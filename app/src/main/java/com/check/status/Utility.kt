package com.check.status

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment

/*
    This file consists of Helper functions to do the work as its mentioned in the function name.
 */
const val PRIMARY_CHANNEL_ID_0 = "001"
const val NOTIFICATION_ID = 1001

@JvmName("checkWiFiStatus")
fun checkWiFiStatus(context: Context?): Boolean {
    val wifiManager =
        context?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager
    return when (wifiManager.isWifiEnabled) {
        true -> true
        else -> false
    }
}

fun Fragment.setWiFiStatus(
    status: Boolean,
    WIFI_REQUEST_CODE: Int
) {
    val wifiManager =
        context?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager
    // Cannot enable wifi directly on VERSION >= 29
    // so instead use https://developer.android.com/reference/android/provider/Settings.Panel
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        startActivityForResult(Intent(Settings.Panel.ACTION_WIFI), WIFI_REQUEST_CODE)
    } else {
        when (status) {
            true -> wifiManager.isWifiEnabled = true
            false -> wifiManager.isWifiEnabled = false
        }
    }

}

@RequiresPermission(allOf = ["android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN"])
fun Fragment.setBluetoothStatus(status: Boolean, BLUETOOTH_REQUEST_CODE: Int) {
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    if (status && bluetoothAdapter?.isEnabled == false) {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, BLUETOOTH_REQUEST_CODE)
    } else {
        bluetoothAdapter?.disable()
    }
}

@RequiresPermission("android.permission.BLUETOOTH")
fun checkBluetoothStatus(context: Context?): Boolean {
    val bluetoothManager =
        context?.applicationContext?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    return when (bluetoothManager.adapter.isEnabled) {
        true -> true
        else -> false
    }
}

fun checkNetworkStatus(context: Context?): Boolean {
    val connectivityManager =
        context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            else -> false
        }
    }
    return false
}


fun createNotificationChannel(context: Context?) {
    val mNotificationManager =
        context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        // Channel 0
        val channel_0 = NotificationChannel(
            PRIMARY_CHANNEL_ID_0,
            "Broadcast Receiver for Phone Call",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel_0.enableLights(true)
        channel_0.lightColor = Color.GREEN
        channel_0.enableVibration(true)
        channel_0.description =
            "Notification Channel of Network status"
        mNotificationManager.createNotificationChannel(channel_0)
    }
}

fun sendNotification(message: String, context: Context?) {
    val mNotificationManager =
        context!!.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    val notifyIntent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
        context, NOTIFICATION_ID,
        notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
    )
    val builder = NotificationCompat.Builder(context, PRIMARY_CHANNEL_ID_0)
        .setContentTitle("Network status")
        .setContentText(message)
        .setColor(Color.GREEN)
        .setSmallIcon(R.mipmap.sym_def_app_icon)
        .setContentIntent(pendingIntent)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setOngoing(true)
        .setAutoCancel(false)
    mNotificationManager.notify(NOTIFICATION_ID, builder.build())
}

fun dismissNotifications(context: Context?) {
    val mNotificationManager =
        context!!.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    mNotificationManager.cancelAll()

}