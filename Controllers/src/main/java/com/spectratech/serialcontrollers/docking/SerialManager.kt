package com.spectratech.serialcontrollers.docking

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.spectratech.serialcontrollers.INTENT_ACTION_GRANT_USB
import com.spectratech.serialcontrollers.usbcomm.CustomProber
import com.spectratech.serialcontrollers.usbcomm.SerialListener
import com.spectratech.serialcontrollers.usbcomm.SerialService
import com.spectratech.serialcontrollers.usbcomm.SerialSocket

class SerialManager private constructor() : SerialListener, ServiceConnection {

    companion object {

        private var instance: SerialManager = SerialManager()

        fun getInstance(): SerialManager {
            return instance
        }
        var connectStatus = MutableLiveData(ConnectStatus.Disconnected)
    }

    enum class ConnectStatus {
        Disconnected, PermissionRequesting, Connecting, Connected
    }

    private var usbSerialPort: UsbSerialPort? = null
    private var service: SerialService? = null

    private var serialDataListener: SerialDataListener? = null

    private var context: Context? = null

    private var deviceId: Int = 0
    private var portNum: Int = 0
    private var baudRate: Int = 115200


    override fun onSerialConnect() {
        connectStatus.postValue(ConnectStatus.Connected)
        android.util.Log.d("SerialManager", "COM Connected")
    }

    override fun onSerialConnectError(e: Exception?) {
        android.util.Log.d("SerialManager", "onSerialConnectError")
        disconnect()
    }

    override fun onSerialRead(data: ByteArray?) {
        android.util.Log.d("SerialManager", "onSerialRead: ${data?.size} lis:${serialDataListener}")

        data?.let {
            serialDataListener?.onDataArrive(it)
        }

    }

    override fun onSerialIoError(e: Exception?) {
        android.util.Log.d("SerialManager", "COM Connect IO Error")
        disconnect()
    }

    fun connect(
        context: Context,
        deviceId: Int,
        portNum: Int,
        baudRate: Int,
        serialDataListener: SerialDataListener
        ) {
        android.util.Log.d("SerialManager", "Start connect... ${serialDataListener}")
        this.deviceId = deviceId
        this.portNum = portNum
        this.baudRate = baudRate
        this.serialDataListener = serialDataListener

        this.context = context

        if (service == null) {
            android.util.Log.d("SerialManager", "Now bind service!")
            context.bindService(
                Intent(context, SerialService::class.java),
                this,
                Context.BIND_AUTO_CREATE
            )
        } else {
            android.util.Log.d("SerialManager", "Service existed. Connect USB COMM")
            connectUSBComm()
        }
    }


    fun disconnect() {
        //serialDataListener = null
        disconnectUSBComm()
    }

    fun send(b: ByteArray) {
        if (connectStatus.value != ConnectStatus.Connected) {
            return
        }
        try {
            service?.write(b)
        } catch (e: Exception) {
            onSerialIoError(e)
        }
    }

    fun isConnected(): ConnectStatus {
        return connectStatus.value ?: ConnectStatus.Disconnected
    }

    private fun connectUSBComm() {
        try {
            Log.d("DEBUG", "connectUSBComm")
            android.util.Log.d("SerialManager", "connectUSBComm")
            service?.attach(this)

            var device: UsbDevice? = null
            val usbManager = context?.getSystemService(Context.USB_SERVICE) as UsbManager



            for (v in usbManager.deviceList.values) if (v.deviceId == deviceId) device = v
            if (device == null) {
                return
            }
            var driver = UsbSerialProber.getDefaultProber().probeDevice(device)
            if (driver == null) {
                driver = CustomProber.customProber.probeDevice(device)
            }
            if (driver == null) {
                Log.d("DEBUG", "connectUSBComm no driver")
                return
            }
            if (driver.ports.size < this.portNum) {
                Log.d("DEBUG", "connectUSBComm port num err")
                return
            }
            usbSerialPort = driver.ports[portNum]

            if (!usbManager.hasPermission(driver.device)) {
                Log.d("DEBUG", "no permission for usb. Request User")
                val usbPermissionIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    Intent(INTENT_ACTION_GRANT_USB),
                    PendingIntent.FLAG_IMMUTABLE
                )
                usbManager.requestPermission(driver.device, usbPermissionIntent)
                connectStatus.postValue(ConnectStatus.PermissionRequesting)

                return
            }

            val usbConnection = usbManager.openDevice(driver.device)


            if (usbConnection == null) {
                Log.d("DEBUG", "no usb connection")
                return
            }else{
                Log.d("DEBUG", "HAS usb connection")
            }
            //Toast.makeText(this, "COM Connecting", Toast.LENGTH_SHORT).show()
            connectStatus.postValue(ConnectStatus.Connecting)
            usbSerialPort?.open(usbConnection)
            usbSerialPort?.setParameters(
                baudRate,
                UsbSerialPort.DATABITS_8,
                UsbSerialPort.STOPBITS_1,
                UsbSerialPort.PARITY_NONE
            )
            val socket = SerialSocket(context!!, usbConnection, usbSerialPort)
            service?.connect(socket)
            // usb connect is not asynchronous. connect-success and connect-error are returned immediately from socket.connect
            // for consistency to bluetooth/bluetooth-LE app use same SerialListener and SerialService classes
            onSerialConnect()
        } catch (e: Exception) {
            Log.e("DEBUG", "connectUSBComm e:$e")
            onSerialConnectError(e)
        }
    }

    private fun disconnectUSBComm() {
        try {
            service?.detach()
            usbSerialPort?.close()
        } catch(e:Exception) {
        }
        usbSerialPort = null
        service?.disconnect()
        connectStatus.postValue(ConnectStatus.Disconnected)
    }

    override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
        Log.d("DEBUG", "onServiceConnected")
        android.util.Log.d("SerialManager", "onServiceConnected")
        service = (binder as SerialService.SerialBinder).service

        Log.d("DEBUG", "binder ser:$service")
        connectUSBComm()
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        Log.d("DEBUG", "onServiceDisconnected")
        android.util.Log.d("SerialManager", "onServiceDisconnected")
        try {
            usbSerialPort?.close()
            usbSerialPort = null
            service?.disconnect()
            usbSerialPort = null
            service = null
        } catch (e: Exception) {
        }
    }
}