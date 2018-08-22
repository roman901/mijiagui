package org.uwtech.mijiagui;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;


public class FloatingWindow extends Service {

    private Context mContext;
    private WindowManager mWindowManager;
    private View mView;
    private BroadcastReceiver br;

    private boolean wasInFocus = true;

    private String bluetoothMAC = "C2:75:BF:F4:33:B2"; // TODO(spark): make devices search dialog

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        setupLayout();
        moveView();

        tryToConnect(bluetoothMAC);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(br);
        Intent intent = new Intent(this, BLEService.class);
        stopService(intent);
        if (mView != null) {
            mWindowManager.removeView(mView);
        }
        super.onDestroy();
    }

    private void tryToConnect(String mac) {
        Intent intent = new Intent(this, BLEService.class);
        intent.putExtra("mac", mac);
        stopService(intent);
        startService(intent);
    }

    WindowManager.LayoutParams mWindowsParams;
    private void moveView() {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        int width = (int) (metrics.widthPixels * 0.5f);
        int height = (int) (metrics.heightPixels * 0.23f);

        mWindowsParams = new WindowManager.LayoutParams(
                width,//WindowManager.LayoutParams.WRAP_CONTENT,
                height,//WindowManager.LayoutParams.WRAP_CONTENT,
                //WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                //WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, // Not displaying keyboard on bg activity's EditText
                //WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, //Not work with EditText on keyboard
                PixelFormat.TRANSLUCENT);


        mWindowsParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
        //params.x = 0;
        mWindowsParams.y = 100;
        mWindowManager.addView(mView, mWindowsParams);

        mView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            long startTime = System.currentTimeMillis();
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (System.currentTimeMillis() - startTime <= 300) {
                    return false;
                }
                if (isViewInBounds(mView, (int) (event.getRawX()), (int) (event.getRawY()))) {
                    editTextReceiveFocus();
                } else {
                    editTextDontReceiveFocus();
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = mWindowsParams.x;
                        initialY = mWindowsParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mWindowsParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                        mWindowsParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mView, mWindowsParams);
                        break;
                }
                return false;
            }
        });
    }

    private boolean isViewInBounds(View view, int x, int y) {
        Rect outRect = new Rect();
        int[] location = new int[2];
        view.getDrawingRect(outRect);
        view.getLocationOnScreen(location);
        outRect.offset(location[0], location[1]);
        return outRect.contains(x, y);
    }

    private void editTextReceiveFocus() {
        if (!wasInFocus) {
            mWindowsParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
            mWindowManager.updateViewLayout(mView, mWindowsParams);
            wasInFocus = true;
        }
    }

    private void editTextDontReceiveFocus() {
        if (wasInFocus) {
            mWindowsParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
            mWindowManager.updateViewLayout(mView, mWindowsParams);
            wasInFocus = false;
        }
    }

    private void setupLayout() {

        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = layoutInflater.inflate(R.layout.overlay_window, null);

        br = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.d("MijiaGUI", "onReceive");
            }
        };

        IntentFilter intFilt = new IntentFilter(MainActivity.BLE_BROADCAST_MSG);
        registerReceiver(br, intFilt);

        ImageButton fCloseBtn = mView.findViewById(R.id.floatCloseBtn);
        fCloseBtn.setOnClickListener(view -> stopSelf());

    }


}