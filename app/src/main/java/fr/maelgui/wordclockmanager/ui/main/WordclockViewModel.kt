package fr.maelgui.wordclockmanager.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fr.maelgui.wordclockmanager.BluetoothService
import fr.maelgui.wordclockmanager.MainActivity
import java.util.*
import kotlin.collections.ArrayList

class WordclockViewModel : ViewModel() {
    private val state: MutableLiveData<BluetoothService.State> = MutableLiveData(BluetoothService.State.NONE)
    private val time: MutableLiveData<Calendar?> = MutableLiveData(null)
    private val temperature: MutableLiveData<Double?> = MutableLiveData(null)
    private val mode: MutableLiveData<Int> = MutableLiveData(-1)
    private val function: MutableLiveData<Int> = MutableLiveData(-1)
    private val temperatures: MutableLiveData<ArrayList<Pair<Calendar,Double>>> = MutableLiveData(ArrayList())
    private val rotation: MutableLiveData<Int?> = MutableLiveData(null)

    fun getTemperature(): LiveData<Double?> {
        return temperature
    }

    fun getTime(): LiveData<Calendar?> {
        return time
    }

    fun getState(): LiveData<BluetoothService.State> {
        return state
    }

    fun getMode(): LiveData<Int> {
        return mode
    }

    fun getFunction(): LiveData<Int> {
        return function
    }

    fun getTemperatures(): LiveData<ArrayList<Pair<Calendar, Double>>> {
        return temperatures
    }

    fun getRotation(): LiveData<Int?> {
        return rotation
    }

    fun addTemperature(temperature: Double, time: Calendar) {
        val updatedItems = temperatures.value
        updatedItems!!.add(Pair(time, temperature))
        temperatures.value = updatedItems
    }
}
