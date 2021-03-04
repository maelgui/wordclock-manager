package fr.maelgui.wordclockmanager

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import fr.maelgui.wordclockmanager.dialog.DevicesDialogFragment
import fr.maelgui.wordclockmanager.ui.main.MainFragment
import fr.maelgui.wordclockmanager.ui.main.WordclockViewModel
import java.util.*


class MainActivity : AppCompatActivity(), BluetoothService.BluetoothServiceListener,
    DevicesDialogFragment.DevicesDialogListener {

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

    fun refreshValues() {
        val keys = arrayOf(
            WordclockMessage.Command.MODE,
            WordclockMessage.Command.FUNCTION,
            WordclockMessage.Command.TEMPERATURE,
            WordclockMessage.Command.HUMIDITY,
            WordclockMessage.Command.LIGHT,
            WordclockMessage.Command.BRIGHTNESS,
            WordclockMessage.Command.ROTATION,
            WordclockMessage.Command.TEMPERATURES
        )

        for (key in keys) {
            val msg = WordclockMessage.Builder(key).build()
            bluetoothService?.send(msg)
        }
    }

    override fun onMessageReceived(message: WordclockMessage) {
        Log.d(MainActivity.TAG, "${message.command} received")
        model.messageReceived(message)
    }

    override fun onStateChanged(state: BluetoothService.State) {
        (model.getState() as MutableLiveData).value = state
        when (state) {
            BluetoothService.State.NONE -> {
                initBluetooth();
            }
            BluetoothService.State.CONNECTED -> {

                val time = Calendar.getInstance()
                val msgBuilder = WordclockMessage.Builder(WordclockMessage.Command.TIME)

                msgBuilder.message.add(time.get((Calendar.YEAR)))
                msgBuilder.message.add(time.get((Calendar.MONTH)))
                msgBuilder.message.add(time.get((Calendar.DAY_OF_MONTH)))
                msgBuilder.message.add(time.get((Calendar.HOUR)))
                msgBuilder.message.add(time.get((Calendar.MINUTE)))
                msgBuilder.message.add(time.get((Calendar.SECOND)))

                bluetoothService?.send(msgBuilder.build())

                refreshValues()
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

    override fun onDialogNeutralClick(dialog: DialogFragment?) {
        TODO("Not yet implemented")
    }

    override fun onDialogNegativeClick(dialog: DialogFragment?) {
        TODO("Not yet implemented")
    }

    override fun onDialogItemClick(dialog: DialogFragment?, item: BluetoothDevice?) {
        appPreferences?.edit()!!.putString(getString(R.string.device_address), item!!.address).apply()
        dialog!!.dismiss()
    }
}