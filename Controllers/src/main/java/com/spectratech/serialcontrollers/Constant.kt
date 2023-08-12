package com.spectratech.serialcontrollers;

const val LOG_TAG = "serialcontrollers"

//const val BUNDLE_DIAGNOSTIC_MODE = "diagnosticMode"

const val DEFAULT_PORT = 8082
const val DEFAULT_WDT_INTERVAL = 0 // Default WDT disabled.
const val DEFAULT_API_TIMEOUT = 120L
const val DEFAULT_WDT_CLEAR_INTERVAL = 60000
const val DEFAULT_MDB_SLAVE_ADDRESS = 0x10 // Cashless #1

const val INTENT_ACTION_GRANT_USB = BuildConfig.LIBRARY_PACKAGE_NAME + ".GRANT_USB"
const val INTENT_ACTION_DISCONNECT = BuildConfig.LIBRARY_PACKAGE_NAME + ".Disconnect"
const val INTENT_FILTER = "INTENT_FILTER"
const val localPrefFileName = "localPref"

const val CH34_VID = 0x1A86 // For SSK com port
const val CH34_PID = 0x7523 // For SSK com port
