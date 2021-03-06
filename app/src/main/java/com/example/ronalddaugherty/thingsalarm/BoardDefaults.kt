package com.example.ronalddaugherty.thingsalarm

import android.os.Build
import com.google.android.things.pio.PeripheralManagerService

object BoardDefaults {

    private val DEVICE_EDISON_ARDUINO = "edison_arduino"
    private val DEVICE_EDISON = "edison"
    private val DEVICE_RPI3 = "rpi3"
    private val DEVICE_NXP = "imx6ul"
    private var sBoardVariant = ""

    val i2cBus: String
        get() {
            return when (boardVariant) {
                DEVICE_EDISON_ARDUINO -> "I2C6"
                DEVICE_EDISON -> "I2C1"
                DEVICE_RPI3 -> "I2C1"
                DEVICE_NXP -> "I2C2"
                else -> throw IllegalArgumentException("Unknown device: " + Build.DEVICE)
            }
        }

   val gpioForButton: String
        get() {
            return when (boardVariant) {
                DEVICE_RPI3 -> "BCM5"
                else -> throw IllegalArgumentException("Unknown device: " + Build.DEVICE)
            }
        }



    val gpioForLED: String
        get() {
            return when (boardVariant) {
                DEVICE_RPI3 -> "BCM6"
                else -> throw IllegalArgumentException("Unknown device: " + Build.DEVICE)
            }
        }
    val pirPin: String
        get() {
            return when (boardVariant) {
                DEVICE_RPI3 -> "BCM4"
                else -> throw UnsupportedOperationException("Unknown device: " +Build.DEVICE)
            }
        }

    val pwmPin: String
        get() {
            return when (boardVariant) {
                DEVICE_RPI3 -> "PWM1"
                else -> throw UnsupportedOperationException("Unknow device: " +Build.DEVICE)
            }
        }

    private val boardVariant: String
        get() {
            if (!sBoardVariant.isEmpty()) {
                return sBoardVariant
            }
            sBoardVariant = Build.DEVICE
            if (sBoardVariant == DEVICE_EDISON) {
                val pioService = PeripheralManagerService()
                val gpioList = pioService.gpioList
                if (gpioList.size != 0) {
                    val pin = gpioList[0]
                    if (pin.startsWith("IO")) {
                        sBoardVariant = DEVICE_EDISON_ARDUINO
                    }
                }
            }
            return sBoardVariant
        }
}