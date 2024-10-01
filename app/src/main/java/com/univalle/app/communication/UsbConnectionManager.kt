package com.univalle.app.communication

import android.hardware.usb.*
import android.util.Log
import java.util.concurrent.Executors

/**
 * UsbConnectionManager maneja la conexión física y la transferencia de datos entre
 * el dispositivo Android y la Raspberry Pi Pico.
 */
class UsbConnectionManager(private val usbManager: UsbManager) {

    private lateinit var usbConnection: UsbDeviceConnection
    private lateinit var usbInterface: UsbInterface
    private lateinit var usbEndpointOut: UsbEndpoint
    private lateinit var usbEndpointIn: UsbEndpoint

    private val TIMEOUT = 1000 // Tiempo de espera para la transferencia de datos en milisegundos
    private val forceClaim = true // Indica si se debe forzar la reclamación de la interfaz USB

    /**
     * Abre la conexión USB con el dispositivo especificado.
     * @param device El dispositivo USB con el cual se establecerá la conexión.
     */
    fun openConnection(device: UsbDevice) {
        Log.d("USB", "Opening connection with device: $device")

        // Iterar sobre todas las interfaces del dispositivo USB
        for (i in 0 until device.interfaceCount) {
            val usbInterface = device.getInterface(i)
            if (usbInterface.endpointCount > 1) {
                // Buscar el endpoint OUT e IN
                for (j in 0 until usbInterface.endpointCount) {
                    val endpoint = usbInterface.getEndpoint(j)
                    if (endpoint.direction == UsbConstants.USB_DIR_OUT) {
                        usbEndpointOut = endpoint
                    } else if (endpoint.direction == UsbConstants.USB_DIR_IN) {
                        usbEndpointIn = endpoint
                    }
                }

                // Abrir la conexión y reclamar la interfaz
                usbConnection = usbManager.openDevice(device)
                if (::usbConnection.isInitialized) {
                    usbConnection.claimInterface(usbInterface, forceClaim)
                    Log.d("USB", "Connection opened successfully")
                } else {
                    Log.e("USB", "Cannot open connection with device: $device")
                }
                break
            }
        }
    }

    /**
     * Envía datos a través del endpoint OUT al dispositivo USB.
     * @param data Los datos a enviar en forma de ByteArray.
     */
    fun sendData(data: ByteArray) {
        if (::usbConnection.isInitialized && ::usbEndpointOut.isInitialized) {
            val executor = Executors.newSingleThreadExecutor()
            executor.submit {
                val result = usbConnection.bulkTransfer(usbEndpointOut, data, data.size, TIMEOUT)
                Log.d("USB", "Bulk transfer result: $result, expected: ${data.size}")
            }
        } else {
            Log.e("USB", "USB connection or endpoint is not initialized.")
        }
    }

    /**
     * Recibe datos desde el endpoint IN del dispositivo USB en tiempo real.
     * @param callback Función lambda que se ejecutará al recibir datos.
     */
    fun onDataReceived(callback: (data: ByteArray) -> Unit) {
        if (::usbConnection.isInitialized && ::usbEndpointIn.isInitialized) {
            val executor = Executors.newSingleThreadExecutor()
            executor.submit {
                val buffer = ByteArray(usbEndpointIn.maxPacketSize)
                val result = usbConnection.bulkTransfer(usbEndpointIn, buffer, buffer.size, TIMEOUT)
                if (result > 0) {
                    callback(buffer.copyOf(result))
                } else {
                    Log.e("USB", "Failed to receive data or no data received.")
                }
            }
        } else {
            Log.e("USB", "USB connection or endpoint is not initialized.")
        }
    }

    /**
     * Cierra la conexión USB liberando la interfaz y cerrando la conexión.
     */
    fun closeConnection() {
        if (::usbConnection.isInitialized && ::usbInterface.isInitialized) {
            usbConnection.releaseInterface(usbInterface)
            usbConnection.close()
            Log.d("USB", "Connection closed")
        }
    }
}
