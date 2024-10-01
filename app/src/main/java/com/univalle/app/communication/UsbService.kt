package com.univalle.app.communication

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleService

/**
 * UsbService es un servicio que maneja la comunicación USB entre un dispositivo Android y una Raspberry Pi Pico.
 * Proporciona métodos para enviar datos y recibir datos en tiempo real a través de la conexión USB.
 */
class UsbService : LifecycleService() {

    private lateinit var usbManager: UsbManager
    private lateinit var permissionIntent: PendingIntent

    // Acción personalizada para el permiso USB
    private val ACTION_USB_PERMISSION = "com.univalle.app.USB_PERMISSION"

    // Instancia del gestor de conexión USB
    private lateinit var usbConnectionManager: UsbConnectionManager

    // BroadcastReceiver para manejar eventos relacionados con la conexión USB
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            synchronized(this) {
                val action = intent.action
                if (ACTION_USB_PERMISSION == action) {
                    // Permiso concedido para el dispositivo USB
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.let {
                            usbConnectionManager.openConnection(it)
                        }
                    } else {
                        Log.d("USB", "Permission denied for device $device")
                    }
                } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                    // El dispositivo USB fue desconectado
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    device?.let { usbConnectionManager.closeConnection() }
                } else {

                }
            }
        }
    }

    /**
     * Método onCreate se ejecuta al iniciar el servicio.
     * Configura el gestor de USB, el filtro de intentos y verifica los dispositivos conectados.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_IMMUTABLE)

        // Registrar el BroadcastReceiver para recibir eventos relacionados con USB
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        registerReceiver(usbReceiver, filter, RECEIVER_NOT_EXPORTED)

        // Inicializar el gestor de conexión USB
        usbConnectionManager = UsbConnectionManager(usbManager)

        // Verificar si hay dispositivos USB conectados y solicitar permisos
        checkConnectedDevices()
    }

    /**
     * Método onDestroy se ejecuta cuando el servicio es destruido.
     * Se desregistra el BroadcastReceiver.
     */
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
    }

    /**
     * Verifica los dispositivos USB conectados y solicita permisos para cada uno.
     */
    private fun checkConnectedDevices() {
        val deviceList = usbManager.deviceList
        deviceList.values.forEach { device ->
            requestPermission(device)
        }
    }

    /**
     * Solicita permiso para acceder a un dispositivo USB específico.
     * @param device El dispositivo USB para el cual se solicita permiso.
     */
    private fun requestPermission(device: UsbDevice) {
        usbManager.requestPermission(device, permissionIntent)
    }

    /**
     * Envía datos a la Raspberry Pi Pico a través de la conexión USB.
     * @param data Los datos a enviar en forma de ByteArray.
     */
    fun sendDataToRaspberry(data: ByteArray) {
        usbConnectionManager.sendData(data)
    }

    /**
     * Registra un callback para recibir datos de la Raspberry Pi Pico en tiempo real.
     * @param callback Función lambda que se ejecutará al recibir datos.
     */
    fun onDataReceived(callback: (data: ByteArray) -> Unit) {
        usbConnectionManager.onDataReceived(callback)
    }
}
