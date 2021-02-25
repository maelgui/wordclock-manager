package fr.maelgui.wordclockmanager

import android.bluetooth.BluetoothAdapter
import android.content.*
import android.icu.util.TimeZone
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import fr.maelgui.wordclockmanager.ui.main.MainFragment
import fr.maelgui.wordclockmanager.ui.main.WordclockViewModel


class MainActivity : AppCompatActivity(), BluetoothService.BluetoothServiceListener {

    companion object {
        private const val TAG = "MainBluetooth"
        private const val APP_PREFERENCES = "APP_PREFERENCES"
        private const val REQUEST_ENABLE_BT = 1
    }

    private var appPreferences: SharedPreferences? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var model: WordclockViewModel

    private var bluetoothService: BluetoothService? = null

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as BluetoothService.LocalBinder
            bluetoothService = binder.getService()
            binder.setListener(this@MainActivity)
            Log.e(TAG, "Service connected")
        }

        override fun onServiceDisconnected(className: ComponentName) {
            bluetoothService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow()
        }

        model = ViewModelProvider(this).get(WordclockViewModel::class.java)

        appPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        Log.e(TAG, "Activity created")



    }

    override fun onStart() {
        super.onStart()
        Intent(this, BluetoothService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        Log.e(TAG, "Activity started")

    }

    private fun initBluetooth() {
        Log.d(TAG, "Check bluetooth status.")

        // Check bluetooth support
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Device doesn't support bluetooth", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            if (!bluetoothAdapter!!.isEnabled()) {
                Log.d(TAG, "Enabling bluetooth.")
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else {
                getDeviceAddress()
            }
        }
    }

    private fun getDeviceAddress() {
        Log.d(TAG, "Retrieve address.")
        val deviceAddress: String? =
            appPreferences?.getString(getString(R.string.device_address), null)
        if (deviceAddress == null) {
            val dialog: DialogFragment = DevicesDialogFragment()
            dialog.show(supportFragmentManager, "devices")
        } else {
            connect(deviceAddress)
        }
    }

    private fun connect(deviceAddress: String?) {
        Log.d(TAG, "Start connection thread...")
        if (bluetoothAdapter != null && deviceAddress != null) {
            bluetoothService!!.connect(bluetoothAdapter!!.getRemoteDevice(deviceAddress))
        }
    }

    private fun retrieveValues() {
        val keys = arrayOf(
            MessagesProto.Message.Key.MODE,
            MessagesProto.Message.Key.FUNCTION,
            MessagesProto.Message.Key.TEMPERATURE,
            MessagesProto.Message.Key.BRIGHTNESS,
            MessagesProto.Message.Key.ROTATION,
            MessagesProto.Message.Key.TEMPERATURES
        )

        for (key in keys) {
            val msg = MessagesProto.Message.newBuilder().setKey(key).build()
            bluetoothService?.send(msg)
        }
    }

    override fun onMessageReceived(message: MessagesProto.Message) {
        Log.d(TAG, "${message.key} received ${message.value}")
        when (message.key) {
            MessagesProto.Message.Key.MODE -> (model.getMode() as MutableLiveData).value = message.value.toInt()
            MessagesProto.Message.Key.FUNCTION -> (model.getFunction() as MutableLiveData).value = message.value.toInt()
            MessagesProto.Message.Key.TEMPERATURE -> {
                val t: Double = ((message.value and 0xFF00) shr 8) + ((message.value and 0xFF) shr 6) * 0.25
                (model.getTemperature() as MutableLiveData).value = t
            }
            MessagesProto.Message.Key.TEMPERATURES -> {
                val temperature: Double = ((message.value and 0xFF00) shr 8) + ((message.value and 0xFF) shr 6) * 0.25
                val time = (message.value shr 32) and 0xFFFFFFFF
                model.addTemperature(temperature, time)
            }
            MessagesProto.Message.Key.ROTATION -> (model.getRotation() as MutableLiveData).value = message.value.toInt()
        }
    }

    override fun onStateChanged(state: BluetoothService.State) {
        (model.getState() as MutableLiveData).value = state
        when (state) {
            BluetoothService.State.NONE -> {
                initBluetooth();
            }
            BluetoothService.State.CONNECTED -> {
                val timestamp = System.currentTimeMillis()
                val msg = MessagesProto.Message.newBuilder()
                    .setKey(MessagesProto.Message.Key.TIME)
                    .setValue(timestamp/1000).build()
                bluetoothService?.send(msg)

                retrieveValues()
            }
            BluetoothService.State.CONNECTING -> {
            }
            BluetoothService.State.FAILED -> {
            }
        }
    }

    fun getBluetoothService(): BluetoothService? {
        return bluetoothService
    }
}