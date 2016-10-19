package com.pro.android.wearables.proandroidwearable;
import android.graphics.Color;
import android.net.Uri;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
public final class ProWatchFaceUtility {
    public static final String PATH_WITH_FEATURE = "/watch_face_config/ProWatchFace";
    public static final String KEY_COLOR_TICK_MARK = "COLOR_TICK_MARK";
    public static final String KEY_COLOR_HOUR_HAND = "COLOR_HOUR_HAND";
    public static final String KEY_COLOR_MINUTE_HAND = "COLOR_MINUTE_HAND";
    public static final String KEY_COLOR_SECOND_HAND = "COLOR_SECOND_HAND";
    public static final String COLOR_TICK_MARK_INTERACTIVE = "White";
    public static final String COLOR_HOUR_HAND_INTERACTIVE = "Blue";
    public static final String COLOR_MINUTE_HAND_INTERACTIVE = "Green";
    public static final String COLOR_SECOND_HAND_INTERACTIVE = "Red";
    private static int parseOptionColor(String optionColor) {
        return Color.parseColor(optionColor.toLowerCase());
    }
    public static final int COLOR_VALUE_TICK_MARK_INTERACTIVE = parseOptionColor(COLOR_TICK_MARK_INTERACTIVE);
    public static final int COLOR_VALUE_HOUR_HAND_INTERACTIVE = parseOptionColor(COLOR_HOUR_HAND_INTERACTIVE);
    public static final int COLOR_VALUE_MINUTE_HAND_INTERACTIVE = parseOptionColor(COLOR_MINUTE_HAND_INTERACTIVE);
    public static final int COLOR_VALUE_SECOND_HAND_INTERACTIVE = parseOptionColor(COLOR_SECOND_HAND_INTERACTIVE);
    public interface FetchConfigDataMapCallback {
        void onConfigDataMapFetched(DataMap config);
    }
    public static void fetchConfigDataMap(final GoogleApiClient client, final FetchConfigDataMapCallback callback) {
        Wearable.NodeApi.getLocalNode(client)
                .setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
                    @Override
                    public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {
                        String myLocalNode = getLocalNodeResult.getNode().getId();
                        Uri uri = new Uri.Builder()
                                .scheme("wear")
                                .path(ProWatchFaceUtility.PATH_WITH_FEATURE)
                                .authority(myLocalNode)
                                .build();
                        Wearable.DataApi.getDataItem(client, uri)
                                .setResultCallback(new DataItemResultCallback(callback));
                    }
                });
    }
    public static void putConfigDataItem(GoogleApiClient googleApiClient, DataMap newConfigData) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATH_WITH_FEATURE);
        DataMap configurationToPut = putDataMapRequest.getDataMap();
        configurationToPut.putAll(newConfigData);
        Wearable.DataApi.putDataItem(googleApiClient, putDataMapRequest.asPutDataRequest())
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                    }
                });
    }
    public static void overwriteKeysInConfigDataMap(final GoogleApiClient googleApiClient, final DataMap configKeysToOverwrite){
        ProWatchFaceUtility.fetchConfigDataMap(googleApiClient, new FetchConfigDataMapCallback() {
            @Override
            public void onConfigDataMapFetched(DataMap currentConfig) {
                DataMap overwriteConfig = new DataMap();
                overwriteConfig.putAll(currentConfig);
                overwriteConfig.putAll(configKeysToOverwrite);
                ProWatchFaceUtility.putConfigDataItem(googleApiClient, overwriteConfig);
            }
        });
    }
    private static class DataItemResultCallback implements ResultCallback<DataApi.DataItemResult> {
        private final FetchConfigDataMapCallback mCallback;
        public DataItemResultCallback(FetchConfigDataMapCallback callback) {
            mCallback = callback;
        }
        @Override
        public void onResult(DataApi.DataItemResult dataItemResult) {
            if (dataItemResult.getStatus().isSuccess()) {
                if (dataItemResult.getDataItem() != null) {
                    DataItem configDataItem = dataItemResult.getDataItem();
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
                    DataMap config = dataMapItem.getDataMap();
                    mCallback.onConfigDataMapFetched(config);
                } else {
                    mCallback.onConfigDataMapFetched(new DataMap());
                }
            }
        }
    }
    private ProWatchFaceUtility(){ // Empty Method
    }
}