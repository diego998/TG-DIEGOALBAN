package com.univalle.app.communication

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class UsbService(private val context: Context, private val onConnectionChanged: (Boolean) -> Unit) {

    private val ACTION_USB_PERMISSION = "com.example.USB_PERMISSION"
    private var usbManager: UsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var serialPort: UsbSerialPort? = null
    private var serialIoManager: SerialInputOutputManager? = null
    private var executor: ScheduledExecutorService? = null

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_USB_PERMISSION -> {
                    val device: UsbDevice? = intent?.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                    if (device != null) {
                        Log.d("UsbService", "Dispositivo detectado en el intent del permiso.")
                    } else {
                        Log.e("UsbService", "Error: el dispositivo es null en la respuesta de permisos. Intentando recuperar dispositivo manualmente.")
                        val connectedDevices = usbManager.deviceList
                        if (connectedDevices.isNotEmpty()) {
                            for (connectedDevice in connectedDevices.values) {
                                Log.d("UsbService", "Recuperando dispositivo conectado: ${connectedDevice.deviceName}")
                                if (usbManager.hasPermission(connectedDevice)) {
                                    connectToDevice(connectedDevice)
                                    return
                                } else {
                                    requestPermission(connectedDevice)
                                    return
                                }
                            }
                        } else {
                            Log.e("UsbService", "No hay dispositivos USB conectados.")
                        }
                        return
                    }

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        Log.d("UsbService", "Permiso concedido para el dispositivo: ${device.deviceName}")
                        connectToDevice(device)
                    } else {
                        Log.d("UsbService", "Permiso denegado para el dispositivo: ${device.deviceName}")
                        onConnectionChanged(false) // Estado desconectado si se niega el permiso
                    }
                }
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    val device: UsbDevice? = intent?.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    device?.let {
                        Log.d("UsbService", "Dispositivo conectado: ${it.deviceName}, solicitando permisos...")
                        requestPermission(it)
                    }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    Log.d("UsbService", "Dispositivo desconectado")
                    stopSerialCommunication()
                    onConnectionChanged(false) // Desconectado
                }
            }
        }
    }

    init {
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        context.registerReceiver(usbReceiver, filter)

        val usbFilter = IntentFilter()
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        context.registerReceiver(usbReceiver, usbFilter)
    }

    private fun requestPermission(device: UsbDevice) {
        val permissionIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_IMMUTABLE
        )
        if (!usbManager.hasPermission(device)) {
            Log.d("UsbService", "Solicitando permisos para el dispositivo: ${device.deviceName}")
            usbManager.requestPermission(device, permissionIntent)
        } else {
            Log.d("UsbService", "Permiso ya concedido para el dispositivo: ${device.deviceName}")
            connectToDevice(device)
        }
    }

    private fun connectToDevice(device: UsbDevice) {
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        if (availableDrivers.isEmpty()) {
            Log.e("UsbService", "No se encontraron controladores USB.")
            return
        }

        val driver = availableDrivers[0]
        val connection = usbManager.openDevice(driver.device)
        if (connection == null) {
            Log.e("UsbService", "Error al abrir conexión con el dispositivo USB.")
            return
        }

        serialPort = driver.ports[0] // La mayoría de dispositivos tienen solo un puerto
        try {
            serialPort?.open(connection)
            serialPort?.setParameters(
                115200, // Baud rate
                8, // Data bits
                UsbSerialPort.STOPBITS_1, // Stop bits
                UsbSerialPort.PARITY_NONE // Parity
            )
            startSerialCommunication()
            onConnectionChanged(true) // Conexión exitosa
        } catch (e: IOException) {
            Log.e("UsbService", "Error al abrir el puerto serial", e)
            stopSerialCommunication()
            onConnectionChanged(false)
        }
    }

    private fun startSerialCommunication() {
        serialPort?.let {
            serialIoManager = SerialInputOutputManager(it, object : SerialInputOutputManager.Listener {
                override fun onNewData(data: ByteArray) {
                    Log.d("UsbService", "Datos recibidos: ${String(data)}")
                    // Aquí puedes manejar los datos recibidos en tiempo real
                }

                override fun onRunError(e: Exception) {
                    Log.e("UsbService", "Error en la comunicación serial", e)
                }
            })
            Executors.newSingleThreadExecutor().submit(serialIoManager)
        }
    }

    private fun stopSerialCommunication() {
        serialIoManager?.stop()
        serialIoManager = null
        serialPort?.close()
        serialPort = null
    }

    fun writeDataInRealTime(data: ByteArray, interval: Long) {
        if (executor == null || executor?.isShutdown == true) {
            executor = Executors.newSingleThreadScheduledExecutor()
        }

        executor?.scheduleWithFixedDelay({
            try {
                serialPort?.write(data, 1000)
                Log.d("UsbService", "Datos enviados en tiempo real: ${String(data)}")
            } catch (e: IOException) {
                Log.e("UsbService", "Error al enviar datos en tiempo real", e)
            }
        }, 0, interval, TimeUnit.MILLISECONDS)
    }

    fun stopRealTimeCommunication() {
        executor?.shutdownNow()
    }

    fun unregisterReceiver() {
        context.unregisterReceiver(usbReceiver)
    }
}
