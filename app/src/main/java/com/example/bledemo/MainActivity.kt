package com.example.bledemo

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bledemo.bledevices.DevicesListAdapter
import com.example.bledemo.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : AppCompatActivity() {

    var bluetoothAdapter: BluetoothAdapter? = null
    var bluetoothLeScanner: BluetoothLeScanner? = null
    var mHandler: Handler? = Handler()
    var device: BluetoothDevice? = null
    var mGatt: BluetoothGatt? = null
    var gattService: BluetoothGattService? = null
    var gattCharacteristic: BluetoothGattCharacteristic? = null
    val REQUEST_ENABLE_BLUETOOTH = 1

    private var packetsIteration = 0
    var counter = 0
    val packets = ByteArray(20);

    val arrayChunks: Array<ByteArray>? = null

    var mBinding: ActivityMainBinding? = null
    private var mScanResults = ArrayList<BluetoothDevice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        requestpermissions()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    private fun requestpermissions(): Boolean {
        val permissions = arrayOf<String>(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_SCAN
        )
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("TAG", "Location permission not granted")
            ActivityCompat.requestPermissions(this, permissions, 1234)
            return false
        }
        return true
    }

    @SuppressLint("MissingPermission")
    private fun initialize() {
        mScanResults.clear()
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.i("TAG", "BLE not supported")
        }
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.i("TAG", "BLE is supported")
            val bluetoothManager =
                getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            if (bluetoothManager != null) {
                bluetoothAdapter = bluetoothManager.adapter
            }
            if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled()) {
                val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH)
            } else {
                bluetoothLeScanner = bluetoothAdapter?.getBluetoothLeScanner()
                val scanFilter = ScanFilter.Builder().build()
                val filters: MutableList<ScanFilter> = ArrayList()
                filters.add(scanFilter)
                val settings =
                    ScanSettings.Builder().setScanMode(ScanSettings.MATCH_MODE_AGGRESSIVE).build()
                bluetoothLeScanner?.startScan(filters, settings, mScanCallback)
            }

            /*
            mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScan();
                }
            }, 1500);*/

            /*
            if (!bluetoothAdapter.isMultipleAdvertisementSupported()) {
                // Unable to run the server on this device, get a better device
                Log.i("TAG", "No advertising support");
                //finish();
                return;
            }

             */
        }
    }

    @SuppressLint("MissingPermission")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                bluetoothLeScanner = bluetoothAdapter?.getBluetoothLeScanner()
                val scanFilter = ScanFilter.Builder().build()
                val filters: MutableList<ScanFilter> = ArrayList()
                filters.add(scanFilter)
                val settings =
                    ScanSettings.Builder().setScanMode(ScanSettings.MATCH_MODE_AGGRESSIVE).build()
                bluetoothLeScanner?.startScan(filters, settings, mScanCallback)
            } else if (resultCode == RESULT_CANCELED) {
                // Handle if the user canceled the operation
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopScan() {
        setUpList()
        bluetoothLeScanner?.stopScan(mScanCallback)
        Log.i("TAG", "stopped scan")
    }

    private fun setUpList() {
        /*if (mScanResults.isEmpty()) {
            return;
        }

         */
        var devicesListAdapter: DevicesListAdapter? = null
//        val device: BluetoothDevice? = mScanResults.get(deviceAddress)
        mBinding?.recyclerView?.layoutManager = LinearLayoutManager(this)
        devicesListAdapter =
            DevicesListAdapter(mScanResults, object : DevicesListAdapter.OnItemClickListener {
                override fun onItemClick(item: BluetoothDevice) {
                    connectDevice(item)
                }
            })
        mBinding?.recyclerView?.adapter = devicesListAdapter
//        val viewModel = GattServerViewModel(device)
//        mBinding.connectGattServerButton.setOnClickListener { v -> connectDevice(device) }
    }

    @SuppressLint("MissingPermission")
    private fun connectDevice(device: BluetoothDevice?) {
        mGatt = device?.connectGatt(this, false, bluetoothGattCallback)
        this.device = device
    }

    private val mScanCallback: ScanCallback = object : ScanCallback() {
        @RequiresApi(Build.VERSION_CODES.R)
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            //addScanResult(result);
            val resultDevice = result.getDevice()
            val deviceAddress: String = resultDevice?.getAddress()!!
            mScanResults.add(resultDevice)
            Log.i("TAG", "Devices $deviceAddress :: $resultDevice :: ${mScanResults.size}")
            mHandler!!.postDelayed({ stopScan() }, 2000)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
        }
    }

    fun scanBLE(view: View?) {
        if (requestpermissions()) {
            Thread {
                initialize()
            }.start()
        }
    }

    private val bluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //setConnected(true);
                Log.i("TAG", "state connected : $device")
                showToast("State connected $device")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //log("Disconnected from device");
                Log.i("TAG", "state disconnected : $device")
                disconnectGattServer()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_FAILURE) {
                Log.i("TAG", "gatt failed : $device")
                disconnectGattServer();
//                return
            } else if (status == BluetoothGatt.GATT_SUCCESS) {
                // handle anything not SUCCESS as failure
                // logError("Connection not GATT sucess status " + status);
//                showToast("gatt success $gattService : $gattCharacteristic")
                gattService = gatt?.services?.last()!!
                gattCharacteristic =
                    gatt?.services?.last()?.characteristics?.last()!!

                Log.i("TAG", "gatt success : ${gattService?.uuid} : ${gattCharacteristic?.uuid}")
//                disconnectGattServer()
//                return
            }

        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            showToast("onCharacteristicChanged : ${characteristic.value}")

        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.i("TAG", "write success")
            showToast("onCharacteristicWrite : ${characteristic?.value}")
//            if (packetsIteration <= counter) {
//                write(gattService, gattCharacteristic, arrayChunks?.get(packetsIteration))
//                packetsIteration++
//            }
//            read(gattService, gattCharacteristic)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
//            showToast("onCharacteristicRead : ${characteristic.value}")
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnectGattServer() {
        //log("Closing Gatt connection");
        //clearLogs();
        //mConnected = false;
        if (mGatt != null) {
            showToast("Gatt Server disconnected: $device")
            Log.i("TAG", "Gatt Server disconnected$device")
            mGatt?.disconnect()
            mGatt?.close()
        }
    }

    @SuppressLint("MissingPermission")
    fun sendData(view: View) {
//        mGatt?.requestMtu(300)
        Thread {
            split()
        }.start()
    }

    @SuppressLint("MissingPermission")
    private fun split() {
//        mGatt?.beginReliableWrite()

        val dataHandler = DataHandler()
//        val hexData = mBinding?.editTextTextData?.text.toString()
        val hexData = "2D2D2D2D2D424547494E2043455254494649434154452D2D2D2D2D0A4D4949427254434341564B6741774942416749554472354B66336F39346A6951464445492B446754632B4A59626F3877436759494B6F5A497A6A3045417749770A67594578437A414A42674E5642415954416B52464D5241774467594456515149444164435A584A7263326870636D55784544414F42674E564241634D42304A6C0A636D747A61476C795A5445554D424947413155454367774C5A586868625842735A53356A62323078466A415542674E564241734D4455467763477870593246300A615739754D5341774867594456515144444264585A5749675532566A64584A6C4945567459576C7349464A7662335167513045774868634E4D6A45774D7A41310A4D4451794E6A517A5768634E4D6A49774D7A41314D4451794E6A517A576A43426754454C4D416B474131554542684D43524555784544414F42674E564241674D0A42304A6C636D747A61476C795A5445514D4134474131554542777748516D567961334E6F61584A6C4D525177456759445651514B4441746C654746746347786C0A4C6D4E76625445784D433847413155454377776F5647567A644342446232353059574E3049464E6C593356796158523549464A6C6358566C633351675357356A0A4C6A45744D437347413155454177776B5647567A644342445A584A3061575A70593246305A53425362323930494568766247526C6369424451544343415349770A4451594A4B6F5A496876634E4151454242514144676745504144434341516F4367674542414A4762422B4837432B4F4F3074567155593139426B59544E6278790A6835675746515A427A7A4F455273446E5333756A53595A6C47776E31346235514F7645636C674B3859657164443749764E777159735268417979384135496F0A46704A385A66312F766E3364413978306F4E68585563575837636C39695535666435572B4F485A6146627772456833354944664E564645506C622F47766151470A375447386B6A3855773970484B706E5058572B6E314A694E6842744D36517A7A3666335658534C74345751752B4F7856316457706A2B612F337035784C52530A7754305274596B325A7049306756663467436C7032794A474564705A584C6674736B6D69475A7A2F6830523956644E3459532F38597A586735432F4B6F39650A4C334C4A594B70466D644470473156375A636B4B534E6E306B377854414B743756327257736D5A616A536968664F4C3173362F58393745434177454141614E6A0A4D47457744675944565230504151482F4241514441674F6F4D435947413155644A5151664D423047434373474151554642774D424267677242674546425163440A416A416642674E5648534D45474441576742516652416E42643476366A4D345757613846363466496F693267596A416442674E564851344546675155486945770A5149572F3869724C573571505A364E39716A36464C4E73774451594A4B6F5A496876634E4151454C4251414467674542414677495267466E67336571427341750A38477A6C675951444D734531377A61643845506C68514D4E626546792F596F347779483669474F6E694C3759376D356E436368306A506D4632575A627A5564410A627857362B7A3846344B6564473845465133542F70502B394D4B7933696A5A63483956766B436A425A543351673478595A556E4559456E442F72774F4C58694A0A4B3542433443426E79306B6171382F51634131632B50775579483338683963424A346E58756D6A595A4231477A566639567479385442454C57652B63622F32500A55464C7362307330314B623134744B2F7278492F764A69594D556B666F33695A5A6D37783870347651355A786E626E637661475A7847584D6B6635386671664E0A4A664276566833476362347A706D57594F6B354376444F4C3455577A4537673979432F7857424F49346A4D5237365342643430684149304857705257665368770A7A4E553D0A2D2D2D2D2D454E442043455254494649434154452D2D2D2D2D"
        if (dataHandler.hexStringToByteArray(hexData) != null) {
            val senddata = dataHandler.hexStringToByteArray(hexData)
//        write(gattService, gattCharacteristic, senddata)

            var start = 0;
            var i = 0
            while (start <= senddata?.size!!) {
                val chunkLength = Math.min(20, senddata.size - start)
                val chunk = ByteArray(chunkLength)
                System.arraycopy(senddata, start, chunk, 0, chunkLength);
                start += 20
                arrayChunks?.set(packetsIteration, chunk)
                packetsIteration++
                counter++
                write(gattService, gattCharacteristic, chunk)
                Thread.sleep(200)
//            write(chunk)
            }
//        for (i in 0 until senddata.size) {
//            var end: Int = start + chunkLength
//            if (end > senddata.size) {
//                end = senddata.size
//            }
//            System.arraycopy(senddata, start, packets[start], end, chunkLength);
//            start += 20
//            write(gattService, gattCharacteristic, packets)
//        }
        } else
            showToast("Please enter valid data")
    }

    private fun hexStringToByteArray(hex: String): ByteArray {
        val result = ByteArray(hex.length / 2)
        for (i in 0 until hex.length step 2) {
            val byteValue = Integer.parseInt(hex.substring(i, i + 2), 16)
            result[i / 2] = byteValue.toByte()
        }
        return result
    }

    @SuppressLint("MissingPermission")
    fun write(
        service: BluetoothGattService?,
        characteristic: BluetoothGattCharacteristic?,
        aData: ByteArray?
    ) {

        val mBluetoothGattCharacteristic = mGatt?.getService(service?.uuid)
            ?.getCharacteristic(characteristic?.uuid)
        mBluetoothGattCharacteristic?.setValue(aData)
        mGatt?.writeCharacteristic(mBluetoothGattCharacteristic)

    }

    @SuppressLint("MissingPermission")
    fun read(
        service: BluetoothGattService?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        mGatt?.readCharacteristic(
            mGatt?.getService(service?.uuid)
                ?.getCharacteristic(characteristic?.uuid)
        )
    }

    private fun showToast(content: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(applicationContext, content, Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("MissingPermission")
    fun reliableWrite(view: View) {
        mGatt?.executeReliableWrite()
    }

}