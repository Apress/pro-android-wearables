package com.pro.android.wearables.proandroidwearable;
import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import java.util.concurrent.TimeUnit;
public class ProWatchFaceConfigListenerService extends WearableListenerService implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient myGoogleApiClient;
    @Override
    public void onMessageReceived(MessageEvent messageEvent){
        byte[] rawData = messageEvent.getData();
        DataMap keysToOverwrite = DataMap.fromByteArray(rawData);
        if(myGoogleApiClient == null) {
            myGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Wearable.API)
                    .build();
        }
        if(!myGoogleApiClient.isConnected()) {
            ConnectionResult myConnectionResult = myGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);
        }
        ProWatchFaceUtility.overwriteKeysInConfigDataMap(myGoogleApiClient, keysToOverwrite);
    }
    @Override
    public void onConnected(Bundle bundle) {
    }
    @Override
    public void onConnectionSuspended(int i) {
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}