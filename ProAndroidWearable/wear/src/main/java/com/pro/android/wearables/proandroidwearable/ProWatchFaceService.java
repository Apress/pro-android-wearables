package com.pro.android.wearables.proandroidwearable;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import static android.graphics.Color.BLACK;
public class ProWatchFaceService extends CanvasWatchFaceService {
    private static final long WATCH_FACE_UPDATE_RATE = TimeUnit.SECONDS.toMillis(1);
    @Override
    public CanvasWatchFaceService.Engine onCreateEngine() {
        return new Engine();
    }
    private class Engine extends CanvasWatchFaceService.Engine {
        Calendar watchFaceTime;
        static final int UPDATE_TIME_MESSAGE = 0;
        boolean lowBitAmbientModeFlag, burnInProtectModeFlag, roundFlag;
        boolean firstDraw = true;
        Bitmap watchFaceBitmap, scaleWatchFaceBitmap;
        Drawable watchFaceDrawable;
        Paint pHourHand, pMinuteHand, pSecondHand, pTickMarks;
        Resources watchFaceResources = ProWatchFaceService.this.getResources();
        final Handler updateTimeHandler = new Handler(){
            @Override
            public void handleMessage(Message updateTimeMessage){
                switch (updateTimeMessage.what){
                    case UPDATE_TIME_MESSAGE:
                        invalidate();
                        if(isTimerEnabled()){
                            long msTime = System.currentTimeMillis();
                            long msDelay = WATCH_FACE_UPDATE_RATE - (msTime % WATCH_FACE_UPDATE_RATE);
                            updateTimeHandler.sendEmptyMessageDelayed(UPDATE_TIME_MESSAGE, msDelay);
                        }
                        break;
                }
            }
        };
        public void createHourHand(){
            pHourHand = new Paint();
            pHourHand.setARGB(255, 0, 0, 255);
            pHourHand.setStrokeWidth(6.f);
            pHourHand.setAntiAlias(true);
            pHourHand.setStrokeCap(Paint.Cap.ROUND);
        }
        public void createMinuteHand(){
            pMinuteHand = new Paint();
            pMinuteHand.setARGB(255, 0, 255, 0);
            pMinuteHand.setStrokeWidth(4.f);
            pMinuteHand.setAntiAlias(true);
            pMinuteHand.setStrokeCap(Paint.Cap.ROUND);
        }
        public void createSecondHand(){
            pSecondHand = new Paint();
            pSecondHand.setARGB(255, 255, 0, 0);
            pSecondHand.setStrokeWidth(2.f);
            pSecondHand.setAntiAlias(true);
            pSecondHand.setStrokeCap(Paint.Cap.SQUARE);
        }
        public void createTickMarks(){
            pTickMarks = new Paint();
            pTickMarks.setARGB(255, 255, 255, 255);
            pTickMarks.setStrokeWidth(2.f);
            pTickMarks.setAntiAlias(true);
            pTickMarks.setStrokeCap(Paint.Cap.SQUARE);
        }
        @Override
        public void onTimeTick(){
            super.onTimeTick();
            invalidate();
        }
        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            roundFlag = insets.isRound();
        }
        @Override
        public void onDestroy(){
            updateTimeHandler.removeMessages(UPDATE_TIME_MESSAGE);
            super.onDestroy();
        }
        @Override
        public void onPropertiesChanged(Bundle properties){
            super.onPropertiesChanged(properties);
            lowBitAmbientModeFlag = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            burnInProtectModeFlag = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
        }
        private boolean isTimerEnabled(){
            return isVisible() && !isInAmbientMode();
        }
        final BroadcastReceiver timeZoneReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                //      watchFaceTime.clear(intent.getStringExtra("time-zone"));
                watchFaceTime.setTimeZone(TimeZone.getDefault());
                //      watchFaceTime.setToNow();
                invalidate();
            }
        };
        boolean updateTimeZoneReceiver = false;
        private void registerTimeZoneReceiver(){
            if(updateTimeZoneReceiver) { return; }
            updateTimeZoneReceiver = true;
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            ProWatchFaceService.this.registerReceiver(timeZoneReceiver, intentFilter);
        }
        private void unregisterTimeZoneReceiver(){
            if(!updateTimeZoneReceiver) { return; }
            updateTimeZoneReceiver = false;
            ProWatchFaceService.this.unregisterReceiver(timeZoneReceiver);
        }
        @Override
        public void onVisibilityChanged(boolean visible){
            super.onVisibilityChanged(visible);
            if(visible) {
                registerTimeZoneReceiver();
                //          watchFaceTime.clear(TimeZone.getDefault().getID());
                watchFaceTime.setTimeZone(TimeZone.getDefault());
                //          watchFaceTime.setToNow();
            } else {
                unregisterTimeZoneReceiver();
            }
            checkTimer();
        }
        @Override
        public void onAmbientModeChanged(boolean ambientModeFlag) {
            super.onAmbientModeChanged(ambientModeFlag);
//            lowBitAmbientModeFlag = true;
            if(lowBitAmbientModeFlag) setAntiAlias(!ambientModeFlag);
//            burnInProtectModeFlag = true;
            if(burnInProtectModeFlag) setBurnInProtect(ambientModeFlag);
            ensureModeSupport();
            invalidate();
            checkTimer();
        }
        public void checkTimer(){
            updateTimeHandler.removeMessages(UPDATE_TIME_MESSAGE);
            if(isTimerEnabled()){
                updateTimeHandler.sendEmptyMessage(UPDATE_TIME_MESSAGE);
            }
        }
        private void setAntiAlias(boolean antiAliasFlag){
            pHourHand.setAntiAlias(antiAliasFlag);
            pMinuteHand.setAntiAlias(antiAliasFlag);
            pSecondHand.setAntiAlias(antiAliasFlag);
            pTickMarks.setAntiAlias(antiAliasFlag);
        }
        private void setBurnInProtect(boolean enabled){
            if(enabled) {
                pHourHand.setStrokeWidth(3.f);
                pMinuteHand.setStrokeWidth(2.f);
            } else {
                pHourHand.setStrokeWidth(6.f);
                pMinuteHand.setStrokeWidth(4.f);
            }
        }
        private void ensureModeSupport(){
            boolean enableLowBitAmbientMode = isInAmbientMode() && lowBitAmbientModeFlag;
            boolean enableBurnInAmbientMode = isInAmbientMode() && burnInProtectModeFlag;
            if(enableLowBitAmbientMode) {
                watchFaceDrawable = watchFaceResources.getDrawable(R.drawable.prowatchfacelow, null);
                watchFaceBitmap = ((BitmapDrawable) watchFaceDrawable).getBitmap();
                scaleWatchFaceBitmap = null;
                pHourHand.setAlpha(255);
                pMinuteHand.setAlpha(255);
                pSecondHand.setAlpha(255);
                pTickMarks.setAlpha(255);
                pHourHand.setColor(Color.WHITE);
                pMinuteHand.setColor(Color.WHITE);
                pSecondHand.setColor(Color.WHITE);
                pTickMarks.setColor(Color.WHITE);
            } else if(enableBurnInAmbientMode) {
                watchFaceDrawable = watchFaceResources.getDrawable(R.drawable.prowatchfacebur, null);
                watchFaceBitmap = ((BitmapDrawable) watchFaceDrawable).getBitmap();
                scaleWatchFaceBitmap = null;
                pHourHand.setAlpha(255);
                pMinuteHand.setAlpha(255);
                pSecondHand.setAlpha(255);
                pTickMarks.setAlpha(255);
                pHourHand.setColor(Color.GRAY);
                pMinuteHand.setColor(Color.GRAY);
                pSecondHand.setColor(Color.GRAY);
                pTickMarks.setColor(Color.GRAY);
            } else if(isInAmbientMode()) {
                watchFaceDrawable = watchFaceResources.getDrawable(R.drawable.prowatchfaceamb, null);
                watchFaceBitmap = ((BitmapDrawable) watchFaceDrawable).getBitmap();
                scaleWatchFaceBitmap = null;
                pHourHand.setAlpha(255);
                pMinuteHand.setAlpha(255);
                pSecondHand.setAlpha(255);
                pTickMarks.setAlpha(255);
                pHourHand.setColor(Color.BLACK);
                pMinuteHand.setColor(Color.BLACK);
                pSecondHand.setColor(Color.BLACK);
                pTickMarks.setColor(Color.BLACK);
            } else {
                watchFaceDrawable = watchFaceResources.getDrawable(R.drawable.prowatchfaceint, null);
                watchFaceBitmap = ((BitmapDrawable) watchFaceDrawable).getBitmap();
                scaleWatchFaceBitmap = null;
                pHourHand.setAlpha(255);
                pMinuteHand.setAlpha(255);
                pSecondHand.setAlpha(255);
                pTickMarks.setAlpha(255);
                pHourHand.setColor(Color.BLUE);
                pMinuteHand.setColor(Color.GREEN);
                pSecondHand.setColor(Color.RED);
                pTickMarks.setColor(Color.BLACK);
            }
        }
        @Override
        public void onCreate(SurfaceHolder surface){
            super.onCreate(surface);
            createHourHand();
            createMinuteHand();
            createSecondHand();
            createTickMarks();
            watchFaceTime = Calendar.getInstance();
            setWatchFaceStyle(new WatchFaceStyle.Builder(ProWatchFaceService.this)
                            .setHotwordIndicatorGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL)
                            .setShowSystemUiTime(false)
                            .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                            .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                            .setPeekOpacityMode(WatchFaceStyle.PEEK_OPACITY_MODE_TRANSLUCENT)
                            .setAmbientPeekMode(WatchFaceStyle.AMBIENT_PEEK_MODE_VISIBLE)
                            .setStatusBarGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL)
                            .setViewProtection(WatchFaceStyle.PROTECT_STATUS_BAR|WatchFaceStyle.PROTECT_HOTWORD_INDICATOR)
                            .setShowUnreadCountIndicator(true)
                            .build()
            );
        }
        @Override
        public void onDraw(Canvas watchface, Rect rect){
            //      super.onDraw(watchface, rect);
            watchface.drawColor(BLACK);
            //      watchFaceTime.setToNow();
            watchFaceTime.setTimeInMillis(System.currentTimeMillis());
            int width = rect.width();
            float centerX = width / 2.f;
            int height = rect.width();
            float centerY = height / 2.f;
            if(firstDraw) {
                if (roundFlag) {
                    watchFaceDrawable = watchFaceResources.getDrawable(R.drawable.round_face_test, null);
                } else {
                    watchFaceDrawable = watchFaceResources.getDrawable(R.drawable.square_face_test, null);
                }
                watchFaceBitmap = ((BitmapDrawable) watchFaceDrawable).getBitmap();
                firstDraw = false;
            }
            if(scaleWatchFaceBitmap == null || scaleWatchFaceBitmap.getWidth() != width || scaleWatchFaceBitmap.getHeight() != height){
                scaleWatchFaceBitmap = Bitmap.createScaledBitmap(watchFaceBitmap, width, height, true);
            }
            watchface.drawBitmap(scaleWatchFaceBitmap, 0, 0, null);
            int hours = watchFaceTime.get(Calendar.HOUR);
            int minutes = watchFaceTime.get(Calendar.MINUTE);
            int seconds = watchFaceTime.get(Calendar.SECOND);
            float hourRot = ((hours + (minutes / 60.f)) / 6.f) * (float) Math.PI;
            float hourLength = centerX - 80;
            float hourX = (float) Math.sin(hourRot) * hourLength;
            float hourY = (float) -Math.cos(hourRot) * hourLength;
            watchface.drawLine(centerX, centerY, centerX+hourX, centerY+hourY, pHourHand);
            float minuteRot = minutes / 30f * (float) Math.PI;
            float minuteLength = centerX - 40;
            float minuteX = (float) Math.sin(minuteRot) * minuteLength;
            float minuteY = (float) -Math.cos(minuteRot) * minuteLength;
            watchface.drawLine(centerX, centerY, centerX + minuteX, centerY + minuteY, pMinuteHand);
            float secondRot = seconds / 30f * (float) Math.PI;
            float secondLength = centerX - 20;
            if (!isInAmbientMode()) {
                float secondX = (float) Math.sin(secondRot) * secondLength;
                float secondY = (float) -Math.cos(secondRot) * secondLength;
                watchface.drawLine(centerX, centerY, centerX + secondX, centerY + secondY, pSecondHand);
            }
            float innerTicksRadius = centerX - 10;
            for (int ticksIndex = 0; ticksIndex < 12; ticksIndex++) {
                float ticksRot = (float) (ticksIndex * Math.PI * 2 / 12);
                float innerX = (float) Math.sin(ticksRot) * innerTicksRadius;
                float innerY = (float) -Math.cos(ticksRot) * innerTicksRadius;
                float outerX = (float) Math.sin(ticksRot) * centerX;
                float outerY = (float) -Math.cos(ticksRot) * centerX;
                watchface.drawLine(centerX + innerX, centerY + innerY, centerX + outerX, centerY + outerY, pTickMarks);
            }
        }
    }
}