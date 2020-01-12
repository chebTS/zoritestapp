package space.zori.testapp

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.ClipData
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private var mBtAdapter: BluetoothAdapter? = null

    private var mNewDevicesArrayAdapter: ArrayAdapter<BluetoothDevice>? = null
    lateinit var scanButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED)

        // Initialize the button to perform device discovery
        scanButton = findViewById(R.id.button_scan)
        scanButton.setOnClickListener { v ->
            doDiscovery()
            v.visibility = View.GONE
        }

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
/*
        val pairedDevicesArrayAdapter = ArrayAdapter<BluetoothDevice>(this, R.layout.device_name)
        mNewDevicesArrayAdapter = ArrayAdapter<BluetoothDevice>(this, R.layout.device_name)
*/

        val pairedDevicesArrayAdapter = DeviceAdapter(this, R.layout.device_name, R.id.txtDevice, mutableListOf())
        mNewDevicesArrayAdapter = DeviceAdapter(this, R.layout.device_name, R.id.txtDevice, mutableListOf())

        // Find and set up the ListView for paired devices
        val pairedListView = findViewById<ListView>(R.id.paired_devices)
        pairedListView.adapter = pairedDevicesArrayAdapter
        pairedListView.onItemClickListener = mDeviceClickListener
        pairedListView.onItemLongClickListener = mDeviceLongClickListener

        // Find and set up the ListView for newly discovered devices
        val newDevicesListView = findViewById<ListView>(R.id.new_devices)
        newDevicesListView.adapter = mNewDevicesArrayAdapter
        newDevicesListView.onItemClickListener = mDeviceClickListener
        newDevicesListView.onItemLongClickListener = mDeviceLongClickListener

        // Register for broadcasts when a device is discovered
        var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        this.registerReceiver(mReceiver, filter)

        // Register for broadcasts when discovery has finished
        filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        this.registerReceiver(mReceiver, filter)

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter()


        // Get a set of currently paired devices
        val pairedDevices = mBtAdapter!!.bondedDevices

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size > 0) {
            findViewById<TextView>(R.id.title_paired_devices).visibility = View.VISIBLE
            for (device in pairedDevices) {
                //pairedDevicesArrayAdapter.add(device.name + "\n" + device.address)
                pairedDevicesArrayAdapter.add(device)
            }
        } else {
            val noDevices = resources.getText(R.string.none_paired).toString()
            //pairedDevicesArrayAdapter.add(noDevices)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter!!.cancelDiscovery()
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver)
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private fun doDiscovery() {
        Log.d(TAG, "doDiscovery()")

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true)
        mNewDevicesArrayAdapter?.clear()
        setTitle(R.string.scanning)

        // Turn on sub-title for new devices
        findViewById<TextView>(R.id.title_new_devices).visibility = View.VISIBLE

        // If we're already discovering, stop it
        if (mBtAdapter!!.isDiscovering) {
            mBtAdapter!!.cancelDiscovery()
        }

        // Request discover from BluetoothAdapter
        mBtAdapter!!.startDiscovery()
    }


    private val mDeviceLongClickListener =
        AdapterView.OnItemLongClickListener { parent, v, position, id ->
            mBtAdapter!!.cancelDiscovery()

            val info = (v as TextView).text.toString()

            val clipboard =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Zori", info)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Text Copied", Toast.LENGTH_SHORT).show()


            true
        }

    /**
     * The on-click listener for all devices in the ListViews
     */
    private val mDeviceClickListener = AdapterView.OnItemClickListener { av, v, pos, arg3 ->
        // Cancel discovery because it's costly and we're about to connect
        mBtAdapter!!.cancelDiscovery()

        // Get the device MAC address, which is the last 17 chars in the View
        val info = (v as TextView).text.toString()
        val address = info.substring(info.length - 17)

        // Create the result Intent and include the MAC address
        val device = av.getItemAtPosition(pos) as BluetoothDevice
        val intent = Intent(this, DeviceInfoActivity::class.java)
        intent.putExtra(DeviceInfoActivity.EXTRA_DEVICE, device)
        startActivity(intent)

        // Set result and finish this Activity
        /*
        val intent = Intent()
        intent.putExtra(EXTRA_DEVICE_ADDRESS, address)
        setResult(Activity.RESULT_OK, intent)
        finish()*/
    }

    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND == action) {
                // Get the BluetoothDevice object from the Intent
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // If it's already paired, skip it, because it's been listed already
                if (device.bondState != BluetoothDevice.BOND_BONDED) {
                    //mNewDevicesArrayAdapter!!.add(device.name + "\n" + device.address)
                    mNewDevicesArrayAdapter!!.add(device)
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                setProgressBarIndeterminateVisibility(false)
                setTitle(R.string.select_device)
                scanButton.visibility = View.VISIBLE
                if (mNewDevicesArrayAdapter!!.count == 0) {
                    val noDevices = resources.getText(R.string.none_found).toString()
                    //mNewDevicesArrayAdapter!!.add(noDevices)
                    //mNewDevicesArrayAdapter!!.add(noDevices)
                }
            }
        }
    }

    companion object {

        /**
         * Tag for Log
         */
        private val TAG = "MainActivity"

        /**
         * Return Intent extra
         */
        var EXTRA_DEVICE_ADDRESS = "device_address"
    }
}
