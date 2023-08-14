package com.spectratech.serialcontrollers;

//import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.spectratech.serialcontrollers.docking.SerialDataListener
import com.spectratech.serialcontrollers.docking.SerialManager
import com.spectratech.serialcontrollers.usbcomm.CustomProber

public class Serialcontrollers private constructor(){
    companion object {
        private var instance: Serialcontrollers = Serialcontrollers()

        fun getInstance(): Serialcontrollers {
            return instance
        }

        private val serialManager = SerialManager.getInstance()
    }
    private var deviceId = 0
    private var portNum = 0
    private val baudRate = 115200
    public val comStatus = SerialManager.connectStatus

    private fun setDeviceIdAndPort(id: Int, port: Int) {
        deviceId = id
        portNum = port
        android.util.Log.d("SerialManager", "setDeviceIdAndPort($id, $port)")
    }

    public fun disconnectSerial() {
        Log.d("DEBUG", "disable serial")
        if (serialManager.isConnected() != SerialManager.ConnectStatus.Disconnected) serialManager.disconnect()
        //stopService(Intent(this, SerialService::class.java))
    }

    public fun connectSerial(context: Context, serialDataListener: SerialDataListener) {
        if (serialManager.isConnected() == SerialManager.ConnectStatus.Disconnected
            || serialManager.isConnected() == SerialManager.ConnectStatus.PermissionRequesting
        ) {
            serialManager.connect(
                context,
                deviceId,
                portNum,
                baudRate,
                serialDataListener
            )
        }
    }

    public fun sendSerial(byteArray: ByteArray) {
        serialManager.send(byteArray)
    }

    public fun refresh(context: Context): Boolean{
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val usbDefaultProber = UsbSerialProber.getDefaultProber()
        val usbCustomProber = CustomProber.customProber
        for (device in usbManager.deviceList.values) {
            var driver = usbDefaultProber.probeDevice(device)
            if (driver == null) {
                driver = usbCustomProber.probeDevice(device)
            }
            if (driver != null) {
                for (port in driver.ports.indices) {
                    if(device.vendorId == CH34_VID && device.productId == CH34_PID) {
                        //???listItems.add(ListItem(device, port, driver))
                        setDeviceIdAndPort(device.deviceId, port)
                        return true;
                    }
                }
            } else {
                //listItems.add(ListItem(device, 0, null))
                setDeviceIdAndPort(device.deviceId, 0)
            }
        }
        return false
    }
}
