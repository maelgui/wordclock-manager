package fr.maelgui.wordclockmanager

import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class DevicesDialogFragment : DialogFragment() {

    private lateinit var listener: DevicesDialogListener

    interface DevicesDialogListener {
        fun onDialogNeutralClick(dialog: DialogFragment?)
        fun onDialogNegativeClick(dialog: DialogFragment?)
        fun onDialogItemClick(
            dialog: DialogFragment?,
            item: BluetoothDevice?
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is DevicesDialogListener) {
            listener = context
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        this.isCancelable = false
        val dialogView: View = layoutInflater.inflate(R.layout.dialog_devices, null)
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
            .setView(dialogView)
            .setNegativeButton("Back",
                DialogInterface.OnClickListener { _, _ ->
                    listener.onDialogNegativeClick(
                        this@DevicesDialogFragment
                    )
                })
            .setNeutralButton("Discover",
                DialogInterface.OnClickListener { _, _ ->
                    listener.onDialogNeutralClick(
                        this@DevicesDialogFragment
                    )
                })
        val devices: List<BluetoothDevice> =
            ArrayList(BluetoothAdapter.getDefaultAdapter().bondedDevices)
        val viewPaired: RecyclerView = dialogView.findViewById(R.id.bluetooth_paired_list)
        viewPaired.layoutManager = LinearLayoutManager(activity)
        viewPaired.adapter = DeviceAdapter(devices, object : DeviceAdapter.OnItemClickListener {
            override fun onClick(item: BluetoothDevice?) {
                listener.onDialogItemClick(this@DevicesDialogFragment, item)
            }
        })

        // Create the AlertDialog object and return it
        return builder.create()
    }


}