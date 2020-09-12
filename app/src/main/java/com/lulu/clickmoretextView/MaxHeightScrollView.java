package com.lulu.clickmoretextView;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * @author zhanglulu on 2020/9/10.
 * for 支持最大高度的 ScrollView
 */
public class MaxHeightScrollView extends ScrollView {

    private int maxHeight;

    public MaxHeightScrollView(Context context) {
        this(context, null);
    }

    public MaxHeightScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaxHeightScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightScrollView);
        maxHeight = typedArray.getDimensionPixelSize(R.styleable.MaxHeightScrollView_newMaxHeight, Integer.MAX_VALUE);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,  MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST));
    }
}