package example.nordicid.com.nursampleandroid;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nordicid.nurapi.NurRespDevCaps;
import com.nordicid.nurapi.NurRespReaderInfo;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "NUR_SAMPLE"; //Can be used for filtering Log's at Logcat

    private Button mConnectButton;
    private TextView mConnectionStatusTextView;

    //These values will be shown in the UI
    private String mUiConnStatusText;
    private int mUiConnStatusTextColor;
    private String mUiConnButtonText;
    private final NurHelper nurHelper = NurHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //This app uses portrait orientation only
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Init beeper to make some noise at various situations
        Beeper.init(this);
        Beeper.setEnabled(true);
        nurHelper.init(this);

        mConnectButton = (Button) findViewById(R.id.button_connect);
        mConnectionStatusTextView = (TextView) findViewById((R.id.text_conn_status));

        mUiConnStatusTextColor = Color.RED;
        mUiConnButtonText = nurHelper.getConnectButtonText();
        showOnUI();
    }

    static boolean mAppPaused = false;

    /**
     * Update content of some global variables to UI items
     */
    private void showOnUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionStatusTextView.setText(mUiConnStatusText);
                mConnectionStatusTextView.setTextColor(mUiConnStatusTextColor);
                mConnectButton.setText(mUiConnButtonText);
            }
        });
    }


    public void onButtonSensors(View v) {
        nurHelper.showSensors();
    }


    /**
     * @param v
     */
    public void onButtonUpdateServer(View v) {
        nurHelper.updateFirmware(1);
    }

    public void onButtonUpdateLocal(View v) {
        nurHelper.updateFirmware(0);
    }

    /**
     * Handle barcode scan click. Start Barcode activity (only if reader support acessories). See Barcode.java
     *
     * @param v View parameter as passed from the system when the button is clicked.
     */
    public void onButtonBarcode(View v) {
        nurHelper.showBarcodePage();
    }

    /**
     * Handle inventory click. Start Inventory activity. See inventory.java
     *
     * @param v View parameter as passed from the system when the button is clicked.
     */
    public void onButtonInventory(View v) {
        nurHelper.showInventoryPage();
    }

    /**
     * Handle tag write click. Start Write Tag activity. See Write.java
     *
     * @param v View parameter as passed from the system when the button is clicked.
     */
    public void onButtonWrite(View v) {
        nurHelper.onWriteTagPage();
    }

    /**
     * Handle tag trace click. Start Write Tag activity. See Trace.java
     *
     * @param v View parameter as passed from the system when the button is clicked.
     */
    public void onButtonTrace(View v) {
        nurHelper.onTracePage();
    }

    /**
     * Handle power off click.
     * Sends PowerOff command to reader.
     *
     * @param v View parameter as passed from the system when the button is clicked.
     */
    public void onPowerOffClick(View v) {
        nurHelper.powerOff();

    }

    /**
     * Handle reader connection button click.
     * First is check if Bluetooth adapter is ON or OFF.
     * Then Bluetooth scan is performed to search devices from near.
     * User can select device from list to connect.
     * It's useful to store last connected device MAC to persistent memory inorder to reconnect later on to same device without selecting from list. This demo doesn't do MAC storing.
     *
     * @param v View parameter as passed from the system when the button is clicked.
     */
    public void onConnectClick(View v) {
        nurHelper.connect();
    }

    void showConnecting() {
        if (nurHelper.isConnected()) {
            mUiConnStatusText = "Connecting to " + nurHelper.getConnectionAddress();
            mUiConnStatusTextColor = Color.YELLOW;
        } else {
            mUiConnStatusText = "Disconnected";
            mUiConnStatusTextColor = Color.RED;
            mUiConnButtonText = "CONNECT";
        }
        showOnUI();
    }

    /**
     * DeviceList activity result
     *
     * @param requestCode We are intrest code "NurDeviceListActivity.REQUEST_SELECT_DEVICE" (32778)
     * @param resultCode  If RESULT_OK user has selected device and then we create NurDeviceSpec (spec) and transport (hAcTr)
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        nurHelper.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        mAppPaused = true;
        Log.i(TAG, "onPause()");
        super.onPause();
        //if (hAcTr != null)
        //    hAcTr.onPause();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume()");
        super.onResume();
        nurHelper.reset();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop()");
        super.onStop();
        nurHelper.onStop();
        //if (hAcTr != null)
        //    hAcTr.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
        nurHelper.destroy();
    }

    /**
     * Handle barcode scan click.
     *
     * @param v View parameter as passed from the system when the button is clicked.
     */
    public void onButtonReaderInfo(View v) {
        handleAboutClick();
    }

    /**
     * Show some general information about the reader and application
     */
    void handleAboutClick() {

        String appversion = "0.0";
        try {
            appversion = this.getPackageManager().getPackageInfo("example.nordicid.com.nursampleandroid", 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        final View dialogLayout = getLayoutInflater().inflate(R.layout.about_dialog, null);

        final TextView appVersion = (TextView) dialogLayout.findViewById(R.id.app_version);
        appVersion.setText(getString(R.string.about_dialog_app) + " " + appversion);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogLayout);

        final TextView readerAttachedTextView = (TextView) dialogLayout.findViewById(R.id.reader_attached_is);
        readerAttachedTextView.setText(getString(R.string.attached_reader_info));

        final TextView nurApiVersion = (TextView) dialogLayout.findViewById(R.id.nur_api_version);
        nurApiVersion.setText(getString(R.string.about_dialog_nurapi) + " " + nurHelper.getFileVersion());
        nurApiVersion.setVisibility(View.VISIBLE);

        final TextView nurApiAndroidVersion = (TextView) dialogLayout.findViewById(R.id.nur_apiandroid_version);
        nurApiAndroidVersion.setText(getString(R.string.about_dialog_nurapiandroid) + " " + nurHelper.getNurApiAndroidVersion());
        nurApiAndroidVersion.setVisibility(View.VISIBLE);

        final TextView nurUpdateLibVersion = (TextView) dialogLayout.findViewById(R.id.nur_updatelib_version);
        nurUpdateLibVersion.setText(getString(R.string.about_dialog_updatelib) + " " + nurHelper.getNurDeviceUpdateVersion());
        nurUpdateLibVersion.setVisibility(View.VISIBLE);

        if (nurHelper.isConnected()) {

            readerAttachedTextView.setText(getString(R.string.attached_reader_info));

            try {
                NurRespReaderInfo readerInfo = nurHelper.getReaderInfo();
                NurRespDevCaps devCaps = nurHelper.getDeviceCaps();


                final TextView modelTextView = (TextView) dialogLayout.findViewById(R.id.reader_info_model);
                modelTextView.setText(getString(R.string.about_dialog_model) + " " + readerInfo.name);
                modelTextView.setVisibility(View.VISIBLE);

                final TextView serialTextView = (TextView) dialogLayout.findViewById(R.id.reader_info_serial);
                serialTextView.setText(getString(R.string.about_dialog_serial) + " " + readerInfo.serial);
                serialTextView.setVisibility(View.VISIBLE);

                final TextView serialDeviceTextView = (TextView) dialogLayout.findViewById(R.id.reader_info_device_serial);
                serialDeviceTextView.setText(getString(R.string.about_dialog_device_serial) + " " + readerInfo.altSerial);
                serialDeviceTextView.setVisibility(View.VISIBLE);

                final TextView firmwareTextView = (TextView) dialogLayout.findViewById(R.id.reader_info_firmware);
                firmwareTextView.setText(getString(R.string.about_dialog_firmware) + " " + readerInfo.swVersion);
                firmwareTextView.setVisibility(View.VISIBLE);

                final TextView bootloaderTextView = (TextView) dialogLayout.findViewById(R.id.reader_bootloader_version);
                bootloaderTextView.setText(getString(R.string.about_dialog_bootloader) + " " + nurHelper.getSecondaryVersion());
                bootloaderTextView.setVisibility(View.VISIBLE);

                final TextView secChipTextView = (TextView) dialogLayout.findViewById(R.id.reader_sec_chip_version);
                secChipTextView.setText(getString(R.string.about_dialog_sec_chip) + " " + devCaps.secChipMajorVersion + "." + devCaps.secChipMinorVersion + "." + devCaps.secChipMaintenanceVersion + "." + devCaps.secChipReleaseVersion);
                secChipTextView.setVisibility(View.VISIBLE);

                if (NurHelper.IsAccessorySupported()) {
                    final TextView accessoryTextView = (TextView) dialogLayout.findViewById(R.id.accessory_version);
                    accessoryTextView.setText(getString(R.string.about_dialog_accessory) + " " + nurHelper.getFrameworkFullApplicationVersion());
                    accessoryTextView.setVisibility(View.VISIBLE);

                    final TextView accessoryBldrTextView = (TextView) dialogLayout.findViewById(R.id.accessory_bootloader_version);
                    accessoryBldrTextView.setText(getString(R.string.about_dialog_accessory_bldr) + " " + nurHelper.getFrameworkBootloaderVersion());
                    accessoryBldrTextView.setVisibility(View.VISIBLE);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            readerAttachedTextView.setText(getString(R.string.no_reader_attached));
        }

        builder.show();
    }
}
