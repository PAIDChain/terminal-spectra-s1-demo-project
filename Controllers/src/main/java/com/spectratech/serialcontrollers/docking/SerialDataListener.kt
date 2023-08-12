package com.spectratech.serialcontrollers.docking

interface SerialDataListener {
    fun onDataArrive(data: ByteArray)

}