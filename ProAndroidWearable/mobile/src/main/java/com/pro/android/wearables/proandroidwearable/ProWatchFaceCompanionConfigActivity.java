package com.pro.android.wearables.proandroidwearable;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.companion.WatchFaceCompanion;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
public class ProWatchFaceCompanionConfigActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<DataApi.DataItemResult> {
    private String watchFacePeerId;
    private GoogleApiClient myGoogleApiClient;
    ComponentName componentName;
    private static final String PATH_WITH_FEATURE = "/watch_face_config/ProWatchFace";
    private static final String KEY_COLOR_TICK_MARK = "COLOR_TICK_MARK";
    private static final String KEY_COLOR_HOUR_HAND = "COLOR_HOUR_HAND";
    private static final String KEY_COLOR_MINUTE_HAND = "COLOR_MINUTE_HAND";
    private static final String KEY_COLOR_SECOND_HAND = "COLOR_SECOND_HAND";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pro_watch_face_config);
        TextView title = (TextView)findViewById(R.id.title);
        watchFacePeerId = getIntent().getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID);
        componentName = getIntent().getParcelableExtra(WatchFaceCompanion.EXTRA_WATCH_FACE_COMPONENT);
        myGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
    }
    @Override
    protected void onStart() {
        super.onStart();
        myGoogleApiClient.connect();
    }
    @Override
    protected void onStop() {
        if (myGoogleApiClient != null && myGoogleApiClient.isConnected()) {
            myGoogleApiClient.disconnect();
        }
        super.onStop();
    }
    @Override
    public void onConnected(Bundle bundle) {
        if(watchFacePeerId != null){
            Uri.Builder uriBuilder = new Uri.Builder();
            Uri uri = uriBuilder.scheme("wear").path(PATH_WITH_FEATURE).authority(watchFacePeerId).build();
            Wearable.DataApi.getDataItem(myGoogleApiClient, uri).setResultCallback(this);
        } else {
            noConnectedDeviceDialog();
        }
    }
    private void noConnectedDeviceDialog() {
        String noConnectText = getResources().getString(R.string.no_connected_device);
        String okButtonLabel = getResources().getString(R.string.ok_button_label);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage(noConnectText)
                .setCancelable(false)
                .setPositiveButton(okButtonLabel, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) { }
                });
        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();
    }
    @Override
    public void onConnectionSuspended(int i) {
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
    @Override
    public void onResult(DataApi.DataItemResult dataItemResult) {
        if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
            DataItem configDataItem = dataItemResult.getDataItem();
            DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
            DataMap configDataMap = dataMapItem.getDataMap();
        } else { }
    }
    private void setUpAllPickers(DataMap configData) {
        setUpColorPickerSelection(R.id.tickMarks, KEY_COLOR_TICK_MARK, configData, R.string.color_gray);
        setUpColorPickerSelection(R.id.hourHand, KEY_COLOR_HOUR_HAND, configData, R.string.color_blue);
        setUpColorPickerSelection(R.id.minuteHand, KEY_COLOR_MINUTE_HAND, configData, R.string.color_green);
        setUpColorPickerSelection(R.id.secondHand, KEY_COLOR_SECOND_HAND, configData, R.string.color_red);
        setUpColorPickerListener(R.id.tickMarks, KEY_COLOR_TICK_MARK);
        setUpColorPickerListener(R.id.hourHand, KEY_COLOR_HOUR_HAND);
        setUpColorPickerListener(R.id.minuteHand, KEY_COLOR_MINUTE_HAND);
        setUpColorPickerListener(R.id.secondHand, KEY_COLOR_SECOND_HAND);
    }
    private void setUpColorPickerSelection(int spinnerId, final String configKey, DataMap config, int defaultColorNameResId) {
        String defaultColorName = getString(defaultColorNameResId);
        int defaultColor = Color.parseColor(defaultColorName);
        int color;
        if (config != null) {
            color = config.getInt(configKey, defaultColor);
        } else {
            color = defaultColor;
        }
        Spinner spinner = (Spinner) findViewById(spinnerId);
        String[] colorNames = getResources().getStringArray(R.array.color_array);
        for (int i = 0; i < colorNames.length; i++) {
            if (Color.parseColor(colorNames[i]) == color) {
                spinner.setSelection(i);
                break;
            }
        }
    }
    private void setUpColorPickerListener(int spinnerId, final String configKey) {
        Spinner spinner = (Spinner) findViewById(spinnerId);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final String colorName = (String) parent.getItemAtPosition(position);
                sendConfigUpdateMessage(configKey, Color.parseColor(colorName));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }
    private void sendConfigUpdateMessage(String configKey, int color) {
        if(watchFacePeerId != null){
            DataMap newConfig = new DataMap();
            newConfig.putInt(configKey, color);
            byte[] rawConfigData = newConfig.toByteArray();
            Wearable.MessageApi.sendMessage(myGoogleApiClient, watchFacePeerId, PATH_WITH_FEATURE, rawConfigData);
        }
    }
}