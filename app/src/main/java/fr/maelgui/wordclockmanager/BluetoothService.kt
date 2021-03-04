package fr.maelgui.wordclockmanager

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.*
import android.util.Log

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.collections.ArrayList


class BluetoothService : Service() {
    private val mAdapter:BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private val binder = LocalBinder()

    private var mListener: BluetoothServiceListener? = null;

    private var mConnectThread: ConnectThread? = null
    private var mConnectedThread: ConnectedThread? = null
    private var mState = State.NONE

    companion object {
        private const val TAG = "BluetoothWordclockService"
        private val BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        private const val MESSAGE_STATE_CHANGED = 0
        private const val MESSAGE_RECEIVED = 1
    }

    enum class State {
        NONE, CONNECTING, CONNECTED, FAILED
    }


    inner class LocalBinder : Binder() {
        fun getService(): BluetoothService = this@BluetoothService;
        fun setListener(listener: BluetoothServiceListener) {
            mListener = listener
            mListener?.onStateChanged(mState);
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        mConnectThread?.cancel()
        mConnectedThread?.cancel()
    }

    interface BluetoothServiceListener {
        fun onMessageReceived(message: WordclockMessage)
        fun onStateChanged(state: State)
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    @Synchronized fun connect(device:BluetoothDevice) {
        // Cancel any thread attempting to make a connection
        if (mState == State.CONNECTING)
        {
            mConnectThread?.cancel()
            mConnectThread = null
        }
        // Cancel any thread currently running a connection
        mConnectedThread?.cancel()
        mConnectedThread = null

        // Start the thread to connect with the given device
        mConnectThread = ConnectThread(device)
        mConnectThread!!.start()
    }
    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    @Synchronized private fun connected(socket: BluetoothSocket) {
        // Cancel the thread that completed the connection
        mConnectThread?.cancel()
        mConnectThread = null
        // Cancel any thread currently running a connection
        mConnectedThread?.cancel()
        mConnectedThread = null

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = ConnectedThread(socket)
        mConnectedThread!!.start()
    }
    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param msg The message to write
     * @see ConnectedThread#write(MessagesProto.Message)
     */
    fun send(msg: WordclockMessage) {
        // Create temporary object
        val r: ConnectedThread
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != State.CONNECTED) return
            mConnectedThread!!.write(msg)
        }
    }

    private fun setState(state: State) {
        Log.d(TAG, "State changed: $mState -> $state")
        mState = state
        handler.obtainMessage(MESSAGE_STATE_CHANGED, mState).sendToTarget()
    }

    private val handler = object:  Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_STATE_CHANGED -> mListener?.onStateChanged(msg.obj as State)
                MESSAGE_RECEIVED -> mListener?.onMessageReceived(msg.obj as WordclockMessage)
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private inner class ConnectThread(private val device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(BT_UUID)
        }

        override fun run() {
            Log.d(TAG, "Connecting to: ${device.name} (${device.address})")
            setState(BluetoothService.State.CONNECTING)

            // Cancel discovery because it otherwise slows down the connection.
            //mAdapter?.cancelDiscovery()

            mmSocket?.let { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                try {
                    socket.connect()
                }
                catch (e: IOException) {
                    Log.e(TAG, "Could not connect to ${device.name}", e)
                    setState(BluetoothService.State.FAILED)
                    socket.close()
                }

                // Reset the ConnectThread because we're done
                synchronized (this@BluetoothService) {
                    mConnectThread = null
                }
                // Start the connected thread
                connected(socket)
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream

        override fun run() {
            Log.d(TAG, "Connected")
            setState(BluetoothService.State.CONNECTED)

            // Keep listening to the InputStream until an exception occurs.
            while (mState == BluetoothService.State.CONNECTED) {

                try
                {
                    val e = WordclockMessage.Error.fromInt(mmInStream.read())
                    val c = WordclockMessage.Command.fromInt(mmInStream.read())
                    val l = mmInStream.read()
                    val m = ArrayList<Int>()

                    for (i in 0 until l) {
                        m.add(mmInStream.read())
                    }


                    val msg = WordclockMessage(e, c, l, m)
                    Log.d(TAG, "Received: $msg")
                    handler.obtainMessage(MESSAGE_RECEIVED, msg).sendToTarget();
                }
                catch (e:IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                    setState(BluetoothService.State.FAILED)
                    break
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(msg: WordclockMessage) {
            Log.d(TAG, "Send: $msg")
            try
            {
                mmOutStream.write(msg.error.ordinal)
                mmOutStream.write(msg.command.ordinal)
                mmOutStream.write(msg.length)

                for (i in 0 until msg.length) {
                    mmOutStream.write(msg.message[i])
                }


            }
            catch (e:IOException) {
                Log.e(TAG, "Error occurred when sending data", e)
            }
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }

}