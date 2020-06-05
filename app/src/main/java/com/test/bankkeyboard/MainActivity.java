package com.test.bankkeyboard;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hqumath.keyboard.KeyboardUtil;
import com.hqumath.keyboard.MyKeyboardView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout mMain_ll;//主布局
        EditText mPwd1_ed;//随机键盘1
        EditText mPwd2_ed;//随机键盘2

        mMain_ll = findViewById(R.id.main_ll);
        mPwd1_ed = findViewById(R.id.pwd1_ed);
        mPwd2_ed = findViewById(R.id.pwd2_ed);

        KeyboardUtil keyboardUtil = new KeyboardUtil(this, mMain_ll);
        keyboardUtil.initKeyboard(MyKeyboardView.KEYBOARDTYPE_Only_Num_Pwd, false, mPwd1_ed);//随机纯数字键盘
        keyboardUtil.initKeyboard(MyKeyboardView.KEYBOARDTYPE_ABC, false, mPwd2_ed);//随机键盘
    }
}
