package fr.maelgui.wordclockmanager.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fr.maelgui.wordclockmanager.BluetoothService
import fr.maelgui.wordclockmanager.WordclockConst
import fr.maelgui.wordclockmanager.WordclockMessage
import java.util.*
import kotlin.collections.ArrayList

class WordclockViewModel : ViewModel() {
    private val state: MutableLiveData<BluetoothService.State> = MutableLiveData(BluetoothService.State.NONE)
    private val time: MutableLiveData<Calendar?> = MutableLiveData(null)
    private val temperature: MutableLiveData<Double?> = MutableLiveData(null)
    private val humidity: MutableLiveData<Int?> = MutableLiveData(null)
    private val light: MutableLiveData<Int?> = MutableLiveData(null)
    private val mode: MutableLiveData<WordclockConst.Mode> = MutableLiveData(WordclockConst.Mode.ERROR)
    private val function: MutableLiveData<WordclockConst.Function> = MutableLiveData(WordclockConst.Function.ERROR)
    private val temperatures: MutableLiveData<ArrayList<Pair<Calendar,Double>>> = MutableLiveData(ArrayList())
    private val rotation: MutableLiveData<WordclockConst.Rotation> = MutableLiveData(WordclockConst.Rotation.ROT_0)
    private val brightness: MutableLiveData<Int?> = MutableLiveData(null)

    fun getTemperature(): LiveData<Double?> {
        return temperature
    }

    fun getHumidity(): LiveData<Int?> {
        return humidity
    }

    fun getLight(): LiveData<Int?> {
        return light
    }

    fun getTime(): LiveData<Calendar?> {
        return time
    }

    fun getState(): LiveData<BluetoothService.State> {
        return state
    }

    fun getMode(): LiveData<WordclockConst.Mode> {
        return mode
    }

    fun getFunction(): LiveData<WordclockConst.Function> {
        return function
    }

    fun getTemperatures(): LiveData<ArrayList<Pair<Calendar, Double>>> {
        return temperatures
    }

    fun getRotation(): LiveData<WordclockConst.Rotation> {
        return rotation
    }

    fun getBrightness(): LiveData<Int?> {
        return brightness
    }

    private fun addTemperature(temperature: Double, time: Calendar) {
        val updatedItems = temperatures.value
        updatedItems!!.add(Pair(time, temperature))
        temperatures.value = updatedItems
    }

    fun messageReceived(message: WordclockMessage) {
        when (message.command) {
            WordclockMessage.Command.MODE -> mode.value = WordclockConst.Mode.fromInt(message.message[0])
            WordclockMessage.Command.FUNCTION -> function.value = WordclockConst.Function.fromInt(message.message[0])
            WordclockMessage.Command.TEMPERATURE -> {
                temperature.value = (message.message[0] + (message.message[1] shl 8)) / 10.0
            }
            WordclockMessage.Command.HUMIDITY -> {
                humidity.value = (message.message[0] + (message.message[1] shl 8)) / 10
            }
            WordclockMessage.Command.LIGHT -> {
                light.value = message.message[0] * 100 / 255
            }
            WordclockMessage.Command.TEMPERATURES -> {
                val time = Calendar.Builder()
                    .setDate(Calendar.getInstance().get(Calendar.YEAR), message.message[0], message.message[1])
                    .setTimeOfDay(message.message[2], message.message[3], message.message[4])
                    .build()
                val temperature = (message.message[6] + (message.message[7] shl 8)) / 10.0
                addTemperature(temperature, time)
            }
            WordclockMessage.Command.ROTATION -> rotation.value = WordclockConst.Rotation.fromInt(message.message[0])
            WordclockMessage.Command.TIMER -> function.value = WordclockConst.Function.TIMER
            WordclockMessage.Command.BRIGHTNESS -> brightness.value = message.message[0]
        }
    }
}
