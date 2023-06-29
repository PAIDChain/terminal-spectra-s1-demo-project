package my.paidchain.spectraterminaldemo.common.printer

import android.graphics.Bitmap

class Printer {
    suspend fun print(bitmap: Bitmap) {
        PrinterController().print(bitmap)
    }
}