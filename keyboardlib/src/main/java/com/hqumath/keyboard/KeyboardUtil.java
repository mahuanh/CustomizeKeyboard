package com.hqumath.keyboard;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import java.lang.reflect.Method;

/**
 * ****************************************************************
 * 文件名称: KeyboardUtil
 * 作    者: Created by gyd
 * 创建时间: 2018/11/29 14:03
 * 文件描述: 键盘工具类
 * 注意事项:
 * ****************************************************************
 */
public class KeyboardUtil {
    private Activity mActivity;
    private View mParent;

    private PopupWindow mWindow;
    private MyKeyboardView mKeyboardView;
    private boolean needInit;
    private boolean mScrollTo = false;//是否界面上移，适应键盘
//    private int mEditTextHeight;//编辑框高度 44dp
    private int mKeyboardHeight;//键盘高度 260dp
    private int mHeightPixels;//屏幕高度
    private int mKeyBoardMarginEditTextTopHeight;//键盘距离编辑框顶部最少距离 （用来计算键盘上推高度）

    public KeyboardUtil(Activity context, View parent) {
        this.mActivity = context;
        this.mParent = parent;
        LinearLayout mIncludeKeyboardview = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.include_keyboardview, null);
        RelativeLayout mKeyboardTopView = (RelativeLayout) mIncludeKeyboardview.findViewById(R.id.keyboard_top_rl);
        mKeyboardView = (MyKeyboardView) mIncludeKeyboardview.findViewById(R.id.keyboard_view);
        mWindow = new PopupWindow(mIncludeKeyboardview, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
        mWindow.setAnimationStyle(R.style.AnimBottom);   //滑出滑入动画
        mWindow.setOnDismissListener(mOnDismissListener);
        mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);//防止被底部工具栏遮挡
        int mEditTextHeight = dp2px(44);//44dp 编辑框高度
        mKeyboardHeight = dp2px(260);//260dp
        mKeyBoardMarginEditTextTopHeight = mEditTextHeight * 2;
        mHeightPixels = context.getResources().getDisplayMetrics().heightPixels;
        mKeyboardTopView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
    }

//    public void initKeyboard(EditText... editTexts) {
//        initKeyboard(MyKeyboardView.KEYBOARDTYPE_Num_Pwd, editTexts);
//    }

    /**
     * 键盘初始化，设置编辑框监听器
     *
     * @param keyBoardType 键盘类型
     * @param editTexts    编辑框
     */
    @SuppressWarnings("all")
    public void initKeyboard(final int keyBoardType,final boolean isPwd, EditText... editTexts) {
        for (final EditText editText : editTexts) {
            hideSystemSofeKeyboard(editText);
            editText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        show(keyBoardType, editText,isPwd);
                    }
                    return false;
                }
            });
        }
    }

    /**
     * 设置不需要使用这个键盘的edittext,解决切换问题
     *
     * @param edittexts
     */
    @SuppressWarnings("all")
    public void setOtherEdittext(EditText... editTexts) {
        for (EditText editText : editTexts) {
            editText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        //防止没有隐藏键盘的情况出现    new Handler().postDelayed(new Runnable())
                        hide();
                    }
                    return false;
                }
            });
        }
    }

    public void show(int keyBoardType, EditText editText,boolean isPwd){
        //隐藏系统
        KeyboardTool.hideInputForce(mActivity, editText);
        //初始化键盘
        if (mKeyboardView.getEditText() != editText || needInit)
            mKeyboardView.init(editText, mWindow, keyBoardType,isPwd);
        //显示自定义键盘
        if (mWindow != null && !mWindow.isShowing()){
            mWindow.showAtLocation(mParent, Gravity.BOTTOM, 0, 0);
            isShowKeyboardView();
        }
        //调整父控件位置，键盘不挡住编辑框
        int nKeyBoardToTopHeight = mHeightPixels - mKeyboardHeight;//屏幕高度-键盘高度
        int[] editLocal = new int[2];
        editText.getLocationOnScreen(editLocal);

        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mParent.getLayoutParams();
        if (editLocal[1] + mKeyBoardMarginEditTextTopHeight > nKeyBoardToTopHeight) {
            int height = editLocal[1] - lp.topMargin - nKeyBoardToTopHeight;
            int mScrollToValue = height + mKeyBoardMarginEditTextTopHeight;
            lp.topMargin = 0 - mScrollToValue;
            mParent.setLayoutParams(lp);
            mScrollTo = true;
        }
    }

    /**
     * 判定当前的自定义键盘是显示状态还是隐藏状态
     * 显示：则禁止截屏/录屏
     * 隐藏：则允许截屏/录屏
      */
    public void isShowKeyboardView(){
        if(mWindow.isShowing()){//自定义键盘显示中
            //自定义键盘 禁止截屏
            mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }else{
            //自定义键盘 允许截屏
            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    public boolean hide() {
        //

        if (mWindow != null && mWindow.isShowing()) {
            mWindow.dismiss();
            needInit = true;
            return true;
        }
        return false;
    }

    /**
     * 隐藏系统键盘
     *
     * @param editText
     */
    private static void hideSystemSofeKeyboard(EditText editText) {
        //SDK_INT >= 11
        try {
            Class<EditText> cls = EditText.class;
            Method setShowSoftInputOnFocus;
            setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
            setShowSoftInputOnFocus.setAccessible(true);
            setShowSoftInputOnFocus.invoke(editText, false);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int dp2px(float dpValue) {
        float scale = mActivity.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    //键盘消失，还原父控件位置
    private PopupWindow.OnDismissListener mOnDismissListener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            if (mScrollTo) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mScrollTo = false;
                        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mParent.getLayoutParams();
                        lp.topMargin = 0;
                        mParent.setLayoutParams(lp);
                    }
                });
            }
            isShowKeyboardView();
        }
    };

    //键盘距离编辑框顶部最少高度
    public void setKeyBoardMarginEditTextTopHeight(int mKeyBoardMarginEditTextTopHeight){
        this.mKeyBoardMarginEditTextTopHeight = mKeyBoardMarginEditTextTopHeight;
    }
}
