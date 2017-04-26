package com.bigocto.hacksourcecode;

import android.os.PersistableBundle;
import android.os.Trace;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JavaInjectTest test = new JavaInjectTest();
        test.test2();

        isBoolMethod(false);

        inerClassTest inerClassTest = new inerClassTest();
        inerClassTest.test2();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        System.out.print("aaa");
        super.onResume();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean isBoolMethod(boolean t){
        if (t){
            System.out.print("It is true");
            return true;
        }else {
            System.out.printf("It is false");
            return false;
        }
    }

    class inerClassTest extends JavaInjectTest{
        @Override
        public void test2() {
            super.test2();
            System.out.print("Main test");
        }
    }

}
