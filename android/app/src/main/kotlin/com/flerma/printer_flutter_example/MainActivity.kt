package com.flerma.printer_flutter_example

import android.device.PrinterManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Base64
import android.widget.Toast
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import kotlin.concurrent.thread


class MainActivity : FlutterActivity() {
    private val appChannel = "flerma.flutter.printer_flutter_example/kotlin"
    private val printTextMethod = "print-text"
    private val printImageMethod = "print-image"

    private var mPrinterManager: PrinterManager? = null
    private var mPrintHandler: Handler? = null

    override fun onDestroy() {
        super.onDestroy()
        if (mPrinterManager != null) {
            mPrinterManager!!.close()
        }
    }

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        GeneratedPluginRegistrant.registerWith(flutterEngine)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*thread(start = true) {
            Looper.prepare()
            mPrintHandler = object : Handler() {
                override fun handleMessage(msg: Message) {
                    try {
                        when (msg.what) {
                            PrinterPrintCode.TEXT.ordinal, PrinterPrintCode.BITMAP.ordinal, PrinterPrintCode.BARCODE.ordinal -> doPrint(
                                getPrinterManager()!!,
                                msg.what,
                                msg.obj
                            ) //Print
                            PrinterPrintCode.FORWARD.ordinal -> {
                                getPrinterManager()!!.paperFeed(50)
                            }
                        }
                    } catch (e: RuntimeException) {
                    }
                }
            }
            Looper.loop()
        }*/

        MethodChannel(
            flutterEngine!!.dartExecutor.binaryMessenger,
            appChannel
        ).setMethodCallHandler { call, result ->
            when (call.method) {
                printTextMethod -> {
                    val text =
                        call.argument<String>("text")
                    if (text != null)
                        printTextPrinter(text)
                }
                printImageMethod -> {
                    var base64Text =
                        call.argument<String>("image_base64")
                    if (base64Text != null) {
                        if (base64Text.contains("base64,")) {
                            base64Text = base64Text.substring(base64Text.indexOf(",") + 1)
                        }
                        val imageBytes = Base64.decode(base64Text, 0)
                        val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        printPicturePrinter(image)
                    }
                }
            }
        }
    }

    private fun getPrinterManager(): PrinterManager? {
        try {
            if (mPrinterManager == null) {
                mPrinterManager = PrinterManager()
                mPrinterManager?.open()
            }
            return mPrinterManager
        } catch (e: RuntimeException) {
            mPrinterManager = null
            Toast.makeText(this, "Dispositivo android sin impresora", Toast.LENGTH_SHORT).show()
        }
        return null
    }

    private fun printTextPrinter(text: String) {
        doPrint(
            getPrinterManager()!!,
            PrinterPrintCode.TEXT.ordinal,
            text
        )
    }

    private fun printPicturePrinter(bitmap: Bitmap) {
        doPrint(
            getPrinterManager()!!,
            PrinterPrintCode.BITMAP.ordinal,
            bitmap
        )
        /*val msg = mPrintHandler!!.obtainMessage(PrinterPrintCode.BITMAP.ordinal)
        msg.obj = bitmap
        msg.sendToTarget()*/
    }

    private fun forwardPrinter() {
        mPrintHandler!!.obtainMessage(PrinterPrintCode.FORWARD.ordinal).sendToTarget()
    }

    private fun doPrint(printerManager: PrinterManager, type: Int, content: Any) {
        if (printerManager.status == PrinterStatusCode.OK.value) {
            printerManager.setupPage(384, -1)
            when (type) {
                PrinterPrintCode.TEXT.ordinal -> {
                    val fontInfo = FontStylePanel.getFontInfo()
                    var fontSize = 24
                    var fontStyle = 0x0000
                    var fontName: String? = "simsun"
                    if (fontInfo != null) {
                        fontSize = fontInfo.getInt("font-size", 24)
                        fontStyle = fontInfo.getInt("font-style", 0)
                        fontName = fontInfo.getString("font-name", "simsun")
                    }
                    var height = 0
                    val texts = (content as String).split("\n")
                        .toTypedArray() //Split print content into multiple lines
                    for (text in texts) {
                        height += printerManager.drawText(
                            text,
                            0,
                            height,
                            fontName,
                            fontSize,
                            false,
                            false,
                            0
                        )
                    }
                    for (text in texts) {
                        height += printerManager.drawTextEx(
                            text,
                            5,
                            height,
                            384,
                            -1,
                            fontName,
                            fontSize,
                            0,
                            fontStyle,
                            0
                        )
                    }
                    height = 0
                }
                PrinterPrintCode.BITMAP.ordinal -> {
                    val bitmap = content as Bitmap
                    if (bitmap != null) {
                        printerManager.drawBitmap(bitmap, 30, 0) //print pictures
                    }
                }
            }
            printerManager.printPage(0)
            printerManager.paperFeed(180)
        } else {
            PrinterStatusCode.getByValue(printerManager.status)?.let { showStatusPrinter(it) }
        }
    }

    private fun showStatusPrinter(status: PrinterStatusCode) {
        runOnUiThread {
            var message = "Error!"
            when (status) {
                PrinterStatusCode.OUT_OF_PAPER -> {
                    message = "Impresora sin papel"
                }
                PrinterStatusCode.OVER_HEAT -> {
                    message = "Impresora sobrecalentada"
                }
                PrinterStatusCode.UNDER_VOLTAGE -> {
                    message = "Impresora con poca bateria"
                }
                PrinterStatusCode.BUSY -> {
                    message = "Impresora ocupada"
                }
                PrinterStatusCode.ERROR -> {
                    message = "Error en impresora"
                }
                PrinterStatusCode.ERROR_DRIVER -> {
                    message = "Error en el driver"
                }
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}

enum class PrinterStatusCode(val value: Int) {
    OK(0),
    OUT_OF_PAPER(-1),
    OVER_HEAT(-2),
    UNDER_VOLTAGE(-3),
    BUSY(-4),
    ERROR(-256),
    ERROR_DRIVER(-257);

    companion object {
        fun getByValue(value: Int) = values().firstOrNull { it.value == value }
    }
}

enum class PrinterPrintCode {
    TEXT, BITMAP, BARCODE, FORWARD
}
