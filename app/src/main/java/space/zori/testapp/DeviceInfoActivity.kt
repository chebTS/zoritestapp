package space.zori.testapp

import android.bluetooth.BluetoothDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_device_info.*

class DeviceInfoActivity : AppCompatActivity() {

    lateinit var device: BluetoothDevice
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_info)
        if (intent.hasExtra(EXTRA_DEVICE)) {
            device = intent.extras?.get(EXTRA_DEVICE) as BluetoothDevice
            Log.i(TAG, device.toString())
            txtInfo.text = "${device.name} \n ${device.address}"
            //${device.uuids.forEach { it.uuid.toString()}
        }
        btnConnect.setOnClickListener {
            if (device.bondState == BluetoothDevice.BOND_NONE){

            }
        }
    }

    companion object {

        /**
         * Tag for Log
         */
        private val TAG = "DeviceInfoActivity"

        /**
         * Return Intent extra
         */
        var EXTRA_DEVICE = "device"
    }
}
