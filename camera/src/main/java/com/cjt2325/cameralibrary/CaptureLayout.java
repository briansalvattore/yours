package com.cjt2325.cameralibrary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cjt2325.cameralibrary.lisenter.CaptureLisenter;
import com.cjt2325.cameralibrary.lisenter.ReturnLisenter;
import com.cjt2325.cameralibrary.lisenter.TypeLisenter;


/**
 * =====================================
 * 作    者: 陈嘉桐 445263848@qq.com
 * 版    本：1.0.4
 * 创建日期：2017/4/26
 * 描    述：集成各个控件的布局
 * =====================================
 */

public class CaptureLayout extends FrameLayout {
    //拍照按钮监听
    private CaptureLisenter captureLisenter;
    //拍照或录制后接结果按钮监听
    private TypeLisenter typeLisenter;
    //退出按钮监听
    private ReturnLisenter returnLisenter;

    public void setTypeLisenter(TypeLisenter typeLisenter) {
        this.typeLisenter = typeLisenter;
    }

    public void setCaptureLisenter(CaptureLisenter captureLisenter) {
        this.captureLisenter = captureLisenter;
    }

    public void setReturnLisenter(ReturnLisenter returnLisenter) {
        this.returnLisenter = returnLisenter;
    }

    private ImageView btn_capture;
    private ImageView btn_confirm;
    private ImageView btn_cancel;
    /*private ReturnButton btn_return;*/
    //private TextView txt_tip;

    private int layout_width;
    private int layout_height;
    private int capture_size;
    private int button_size;

    public CaptureLayout(Context context) {
        this(context, null);
    }

    public CaptureLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CaptureLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //get width
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            layout_width = outMetrics.widthPixels;
        } else {
            layout_width = outMetrics.widthPixels / 2;
        }
        /*button_size = (int) (layout_width / 4.5f);*/
        button_size = (int) getResources().getDimension(R.dimen.button_size);
        capture_size = (int) getResources().getDimension(R.dimen.capture_size);
        int button_margin = (int) getResources().getDimension(R.dimen.capture_margin);
        layout_height = capture_size + button_margin;

        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.black_trans));

        initView();
        initEvent();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(layout_width, layout_height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    public void initEvent() {
        btn_cancel.setVisibility(INVISIBLE);
        btn_confirm.setVisibility(INVISIBLE);
    }

    public void startTypeBtnAnimator() {
        btn_capture.setVisibility(INVISIBLE);

        btn_cancel.setVisibility(VISIBLE);
        btn_confirm.setVisibility(VISIBLE);
        btn_cancel.setClickable(false);
        btn_confirm.setClickable(false);

        ObjectAnimator animator_cancel = ObjectAnimator.ofFloat(btn_cancel, "translationX", layout_width / 4, 0);
        animator_cancel.setDuration(200);
        animator_cancel.start();

        ObjectAnimator animator_confirm = ObjectAnimator.ofFloat(btn_confirm, "translationX", -layout_width / 4, 0);
        animator_confirm.setDuration(200);
        animator_confirm.start();

        animator_confirm.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                btn_cancel.setClickable(true);
                btn_confirm.setClickable(true);
            }
        });
    }

    @SuppressLint("RtlHardcoded")
    private void initView() {
        setWillNotDraw(false);

        btn_capture = new ImageView(getContext());
        LayoutParams btn_capture_param = new LayoutParams(capture_size, capture_size);
        btn_capture_param.gravity = Gravity.CENTER;
        btn_capture.setLayoutParams(btn_capture_param);
        btn_capture.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_photo_camera_white_48dp));
        btn_capture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (captureLisenter != null) {
                    captureLisenter.takePictures();
                }
            }
        });

        final int margin = (layout_width / 4) - button_size / 2;

        btn_cancel = new ImageView(getContext());
        LayoutParams btn_cancel_param = new LayoutParams(button_size, button_size);
        btn_cancel_param.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        btn_cancel_param.setMargins(margin, 0, 0, 0);
        btn_cancel.setLayoutParams(btn_cancel_param);
        btn_cancel.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_arrow_back_white_24dp));
        btn_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (typeLisenter != null) {
                    typeLisenter.cancel();
                }
                btn_cancel.setVisibility(INVISIBLE);
                btn_confirm.setVisibility(INVISIBLE);
                btn_capture.setVisibility(VISIBLE);
            }
        });


        btn_confirm = new ImageView(getContext());
        LayoutParams btn_confirm_param = new LayoutParams(button_size, button_size);
        btn_confirm_param.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        btn_confirm_param.setMargins(0, 0, margin, 0);
        btn_confirm.setLayoutParams(btn_confirm_param);
        btn_confirm.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_check_white_24dp));
        btn_confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (typeLisenter != null) {
                    typeLisenter.confirm();
                }
                btn_cancel.setVisibility(INVISIBLE);
                btn_confirm.setVisibility(INVISIBLE);
                btn_capture.setVisibility(VISIBLE);
            }
        });

        this.addView(btn_capture);
        this.addView(btn_cancel);
        this.addView(btn_confirm);
    }
}
