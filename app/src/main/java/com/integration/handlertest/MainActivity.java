package com.integration.handlertest;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "wang";
    private Handler mHandlerA;

    private TextView mTvMsg;
    private StringBuilder mStringBuilder;
    private int mInt;

    Handler mHandlerMain = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            Log.i(TAG, "HandlerMain handleMessage: 收到消息 "
                    + Thread.currentThread().getName() +" : "+ msg.obj);
            mStringBuilder = new StringBuilder().append("HandlerMain handleMessage: 收到消息 ")
                    .append(Thread.currentThread().getName()).append(" : ").append(msg.obj);

            mTvMsg.setText(mStringBuilder);
        }
    };
    private Handler mHandlerB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTvMsg = findViewById(R.id.tvMsg);
        ThreadA threadA = new ThreadA();
        threadA.start();
        ThreadB threadB = new ThreadB();
        threadB.start();

    }

    public class ThreadA extends Thread {

        @Override
        public void run() {
            super.run();
            Looper.prepare();
            mHandlerA = new Handler(Looper.myLooper()){
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    Log.i(TAG, "HandlerA handleMessage: 收到消息"
                    +Thread.currentThread().getName() +" : "+ msg.obj);
                    mInt++;
                    if (mInt >= 4) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            Log.i(TAG, "HandlerA : quitSafely "+ mInt);
                            Looper.myLooper().quitSafely();
                        } else {
                            Log.i(TAG, "HandlerA : quit "+ mInt);
                            Looper.myLooper().quit();
                        }
                    }

                    Toast.makeText(getApplicationContext(), "HandlerA handleMessage: 收到消息\"\n"
                            + Thread.currentThread().getName() + " : \" + msg.obj", Toast.LENGTH_SHORT).show();

                    Message message = Message.obtain();
                    message.obj = msg.obj;
                    mHandlerB.sendMessageDelayed(message, 1000);

                }
            };

            Looper.loop();
        }
    }


    public class ThreadB extends Thread {
        @Override
        public void run() {
            super.run();
            Looper.prepare();
            final Looper looper = Looper.myLooper();
            mHandlerB = new Handler(looper){
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    Log.i(TAG, "HandlerB handleMessage: 收到消息"
                            + Thread.currentThread().getName() + " : " + msg.obj);
                    if (mInt >= 4) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            Looper.myLooper().quitSafely();
                            Log.i(TAG, "HandlerB : quitSafely "+ mInt);
                        } else {
                            Log.i(TAG, "HandlerB : quit "+ mInt);
                            Looper.myLooper().quit();
                        }
                    }

                    Toast.makeText(getApplicationContext(), "HandlerB handleMessage: 收到消息\"\n"
                            + Thread.currentThread().getName() + " : \" + msg.obj", Toast.LENGTH_SHORT).show();

                    Message message = Message.obtain();
                    message.obj = msg.obj;
                    mHandlerA.sendMessageDelayed(message, 1000);

                }
            };

            for (int i = 0; i < 1; i++) {

                Message message = Message.obtain();
                message.what = 1;
                message.obj = System.currentTimeMillis() + "";
                mHandlerA.sendMessageDelayed(message, 1000);
                Log.i(TAG, "run:ThreadB  线程 "+Thread.currentThread().getName()
                        +" 发送了消息 "+message.obj);
                SystemClock.sleep(1000);
            }
            Looper.loop();



        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        mHandlerA.removeCallbacksAndMessages(null);
        mHandlerB.removeCallbacksAndMessages(null);
        mHandlerMain.removeCallbacksAndMessages(null);
    }
}