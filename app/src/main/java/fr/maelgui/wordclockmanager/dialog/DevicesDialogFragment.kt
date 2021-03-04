package fr.maelgui.wordclockmanager.dialog

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import fr.maelgui.wordclockmanager.DeviceAdapter
import fr.maelgui.wordclockmanager.R


class DevicesDialogFragment : BottomSheetDialogFragment() {

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
        else {
            throw RuntimeException(context.toString()
                    + " must implement ItemClickListener");
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dialogView: View = inflater.inflate(R.layout.dialog_devices, null)

        val devices: List<BluetoothDevice> =
            ArrayList(BluetoothAdapter.getDefaultAdapter().bondedDevices)
        val viewPaired: RecyclerView = dialogView.findViewById(R.id.bluetooth_paired_list)
        viewPaired.layoutManager = LinearLayoutManager(activity)
        viewPaired.adapter = DeviceAdapter(devices, object : DeviceAdapter.OnItemClickListener {
            override fun onClick(item: BluetoothDevice?) {
                listener.onDialogItemClick(this@DevicesDialogFragment, item)
            }
        })

        return dialogView
    }

    /*override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        this.isCancelable = false
        val dialogView: View = activity!!.layoutInflater.inflate(R.layout.dialog_devices, null)
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
    }*/


}