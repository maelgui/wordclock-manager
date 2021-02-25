package fr.maelgui.wordclockmanager

import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class DeviceAdapter(
    private val mDataset: List<BluetoothDevice>,
    private val mListener: OnItemClickListener
) :
    RecyclerView.Adapter<DeviceAdapter.MyViewHolder>() {

    interface OnItemClickListener {
        fun onClick(item: BluetoothDevice?)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
        val device = mDataset[position]

        holder.itemView.setOnClickListener { mListener.onClick(device) }

        val textView = holder.itemView.findViewById<TextView>(R.id.deviceName)
        val imageView = holder.itemView.findViewById<ImageView>(R.id.deviceClass)

        textView.text = device.name
        imageView.setBackgroundResource(deviceClassResources[device.bluetoothClass.majorDeviceClass] ?: R.drawable.ic_baseline_bluetooth_24)
    }

    override fun getItemCount(): Int {
        return mDataset.size
    }

    companion object {
        private val deviceClassResources: Map<Int, Int> =
            object : HashMap<Int, Int>() {
                init {
                    put(BluetoothClass.Device.Major.AUDIO_VIDEO, R.drawable.ic_baseline_headset_24)
                    put(BluetoothClass.Device.Major.COMPUTER, R.drawable.ic_baseline_computer_24)
                    put(BluetoothClass.Device.Major.PHONE, R.drawable.ic_baseline_smartphone_24)
                }
            }
    }

}