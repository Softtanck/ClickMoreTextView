package com.lulu.clickmoretextView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

/**
 * 优雅的实现 ”查看更多“
 */
private const val TAG = "ClickMoreTextView"

class ClickMoreTextView : View {
    companion object {
        var DEBUG = BuildConfig.DEBUG
    }

    private var textCharArray = charArrayOf()
    private var textPaint: TextPaint = TextPaint()
    public var moreTextPaint: TextPaint = TextPaint()
    private var textPaintTop = 0f
    private var isBreakFlag = false//排版标识

    /**
     * 正文间距 倍数
     */
    public var lineSpacingMultiplier = 1.0f

    /**
     * 最大行数
     */
    public var maxLines = Int.MAX_VALUE
        set(value) {
            field = value
            //重新测量
            requestLayout()
        }

    /**
     * 文本内容
     */
    var text = ""
        set(value) {
            field = value
            textCharArray = value.toCharArray()
        }

    /**
     * 文字大小
     */
    public var textSize = 80f
        set(value) {
            field = value
            textPaint.textSize = value
            invalidate()
        }

    /**
     * 文字颜色
     */
    public var textColor = 0x000000
        set(value) {
            field = value
            textPaint.color = value
            invalidate()
        }

    /**
     * 是否展示 More
     */
    public var isShowMore = false
        set(value) {
            isShouldShowMore = value
            field = value
            requestLayout()
        }

    /**
     * 通过各个条件判断当前是否应该展示 MoreText
     */
    private var isShouldShowMore = false

    /**
     * 查看更多
     */
    public var moreText = "查看更多"
        set(value) {
            field = value
        }

    /**
     * 更多文字颜色
     */
    public var moreTextColor = 0x000000
        set(value) {
            field = value
            moreTextPaint.color = value
            invalidate()
        }


    /**
     * 更多文字大小
     */
    public var moreTextSize = 80f
        set(value) {
            field = value
            moreTextPaint.textSize = value
            invalidate()
        }

    /**
     * 更多按钮宽度
     */
    private var moreTextW = -1f

    /**
     * 更多点击事件
     */
    private var moreTextClickListener: OnClickListener? = null

    public fun setMoreTextOnClickListener(clickListener: OnClickListener) {
        this.moreTextClickListener = clickListener
    }

    /**
     * 文字位置
     */
    private val textPositions = ArrayList<TextPosition>()

    /**
     * 行 Y 坐标
     */
    private val textLineYs = ArrayList<Float>()

    /**
     * 布局高度
     */
    private var layoutHeight = 0f

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.ClickMoreTextView, defStyle, 0
        )
        val text = a.getString(R.styleable.ClickMoreTextView_text)
        text?.let {
            this.text = it
        }
        this.maxLines = a.getInt(R.styleable.ClickMoreTextView_maxLines, Int.MAX_VALUE)
        this.isShowMore = a.getBoolean(R.styleable.ClickMoreTextView_isShowMore, false)

        this.textColor = a.getColor(R.styleable.ClickMoreTextView_textColor, 0x000000)
        this.textSize = a.getDimension(R.styleable.ClickMoreTextView_textSize, 80f)

        textPaint.isAntiAlias = true//抗锯齿

        this.moreTextColor = a.getColor(R.styleable.ClickMoreTextView_moreTextColor, 0x000000)
        this.moreTextSize = a.getDimension(R.styleable.ClickMoreTextView_textSize, 80f)

        moreTextPaint.isAntiAlias = true//抗锯齿
        moreTextPaint.flags = Paint.UNDERLINE_TEXT_FLAG//下划线
        moreTextPaint.isFakeBoldText = true
        a.recycle()
    }

    override fun requestLayout() {
        super.requestLayout()
        if (DEBUG) {
            Log.d(TAG, "requestLayout: last isBreakFlag: $isBreakFlag")
        }
        isBreakFlag = false
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)
        breakText(width)
        if (layoutHeight > 0) {
            height = layoutHeight.toInt()
        }
        if (DEBUG) {
            Log.d(
                TAG,
                "onMeasure: getLines():${getLines()} maxLines: $maxLines width:$width height:$height"
            )
        }
        if (getLines() > maxLines && maxLines - 1 > 0) {
            val textBottomH = textPaint.fontMetrics.bottom.toInt()
            height = (textLineYs[maxLines - 1]).toInt() + paddingBottom + textBottomH
        }
        setMeasuredDimension(width, height)
    }

    /**
     * Constructor->onFinishInflate->onMeasure..->onSizeChanged->onLayout->addOnGlobalLayoutListener->onWindowFocusChanged->onMeasure->onLayout
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        //breakText(w)
    }

    /**
     * 文字排版
     */
    private fun breakText(w: Int) {
        if (w <= 0) {
            return
        }
        if (isBreakFlag) {
            return
        }
        if (DEBUG) {
            Log.d(TAG, "breakText: 开始排版")
        }
        moreTextW = moreTextPaint.measureText(moreText)
        isBreakFlag = true
        val availableWidth = w - paddingRight
        textLineYs.clear()
        textPositions.clear()
        //x 的初始化位置
        val initX = paddingLeft.toFloat()
        var curX = initX
        var curY = paddingTop.toFloat()
        val textFontMetrics = textPaint.fontMetrics
        textPaintTop = textFontMetrics.top
        val lineHeight = textFontMetrics.bottom - textFontMetrics.top
        curY -= textFontMetrics.top//指定顶点坐标
        val size = textCharArray.size
        var i = 0
        while (i < size) {
            val textPosition = TextPosition()
            val c = textCharArray.get(i)
            val cW = textPaint.measureText(c.toString())
            //位置保存点
            textPosition.x = curX
            textPosition.y = curY
            textPosition.text = c.toString()
            //curX 向右移动一个字
            curX += cW
            if (isParagraph(i) ||//段落内
                isNeedNewLine(i, curX, availableWidth)
            ) { //折行
                textLineYs.add(curY)
                //断行需要回溯
                curX = initX
                curY += lineHeight * lineSpacingMultiplier
            }
            textPositions.add(textPosition)
            i++//移动游标
            //记录 MoreText位置
            recordMoreTextPosition(availableWidth, curX, curY, i)
        }
        //最后一行
        textLineYs.add(curY)
        curY += paddingBottom
        layoutHeight = curY + textFontMetrics.bottom//应加上后面的Bottom
        checkMoreTextShouldShow()//排版结束后，检查MoreText 是否应该展示
        if (DEBUG) {
            Log.d(TAG, "总行数： ${getLines()}")
        }
    }

    /**
     * "..." 索引值
     */
    private var dotIndex = -1

    /**
     * "..." 位置
     */
    private var dotPosition = TextPosition("...")

    /**
     * 记录 MoreText 位置
     */
    private fun recordMoreTextPosition(availableWidth: Int, curX: Float, curY: Float, index: Int) {
        if (isShowMore.not() || maxLines == Int.MAX_VALUE) {
            return
        }
        //只记录符合要求的第一个位置的
        if (dotIndex > 0 || index >= textCharArray.size) {
            return
        }
        val lines = getLines()
        if (lines != maxLines - 1) {
            return
        }
        val dotLen = textPaint.measureText("...")
        //目前在最后一行
        if (checkMoreTextForEnoughLine(curX, dotLen, availableWidth)//这一行满足一行时
            || checkMoreTextForParagraph(index)//当前是换行符
        ) {
            dotPosition.x = curX
            dotPosition.y = curY
            dotIndex = textPositions.size

            //点击区域
            val moreTextFontMetrics = moreTextPaint.fontMetrics
            moreTextClickArea.top = curY + moreTextFontMetrics.top
            moreTextClickArea.right = availableWidth.toFloat()
            moreTextClickArea.bottom = curY + moreTextFontMetrics.bottom
            moreTextClickArea.left = curX
        }
    }

    private fun checkMoreTextForEnoughLine(
        curX: Float,
        dotLen: Float,
        availableWidth: Int
    ) = curX + moreTextW + dotLen + textPaint.measureText("中") > availableWidth

    private fun checkMoreTextForParagraph(index: Int): Boolean {
        if ('\n' == textCharArray[index]) {//判断当前字符是否为 \n
            return true
        }
        return false
    }

    /**
     * 真正检查 MoreText 是否应该展示
     */
    private fun checkMoreTextShouldShow() {
        if (isShowMore.not()) {
            return
        }
        if (getLines() <= maxLines || maxLines == Int.MAX_VALUE) {
            isShouldShowMore = false
            return
        }
        if (dotIndex < 0) {
            return
        }
        isShouldShowMore = true
        val temp = arrayListOf<TextPosition>()
        for (textPosition in textPositions.withIndex()) {
            if (textPosition.index == dotIndex) {
                temp.add(dotPosition)
                break
            }
            temp.add(textPosition.value)
        }
        textPositions.clear()
        textPositions.addAll(temp)
    }

    /**
     * 是否是段落
     */
    private fun isParagraph(curIndex: Int): Boolean {
        if (textCharArray.size <= curIndex) {
            return false
        }
        if (textCharArray[curIndex] == '\n') {
            return true
        }
        return false
    }

    /**
     * 是否需要另起一行
     */
    private fun isNeedNewLine(
        curIndex: Int,
        curX: Float,
        maxWith: Int
    ): Boolean {
        if (textCharArray.size <= curIndex + 1) {//需要判断下一个 char
            return false
        }
        //判断下一个 char 是否到达边界
        if (curX + textPaint.measureText(textCharArray[curIndex + 1].toString()) > maxWith) {
            return true
        }
        if (curX > maxWith) {
            return true
        }
        return false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (DEBUG) {
            Log.d(TAG, "onDraw: ")
        }
        val posSize = textPositions.size
        for (i in 0 until posSize) {
            val textPosition = textPositions[i]
            if (textPosition.y + textPaintTop > height - paddingBottom) {
                break
            }
            if (DEBUG) {
                //Log.d(TAG, "onDraw: height： $height  height-paddingBottom:${ height-paddingBottom} textPosition.y+textPaintTop: ${textPosition.y + textPaintTop}")
            }
            canvas.drawText(textPosition.text, textPosition.x, textPosition.y, textPaint)
        }
        if (isShouldShowMore) {
            val moreTextY = dotPosition.y
            val moreTextX = width - moreTextW - paddingRight
            canvas.drawText(moreText, moreTextX, moreTextY, moreTextPaint)
        }
    }

    /**
     * 查看更多点击区域
     */
    private val moreTextClickArea = RectF()

    private var lastDownX = -1f
    private var lastDownY = -1f

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (isShouldShowMore.not()) {
            return false
        }
        event?.let {
            val x = event.x
            val y = event.y
            if (DEBUG) {
                Log.d(TAG, "onTouchEvent: x: $x y:$y event: ${event.action}")
            }
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastDownX = x
                    lastDownY = y
                    if (moreTextClickArea.contains(lastDownX, lastDownY)) {
                        return true
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (moreTextClickArea.contains(x, y)) {
                        if (DEBUG) {
                            Log.d(TAG, "onTouchEvent: 点击更多回调")
                        }
                        moreTextClickListener?.onClick(this)
                        return false
                    }
                }
                else -> {
                }
            }
        }
        return false
    }

    /**
     * 获取当前的行数
     */
    public fun getLines(): Int {
        return textLineYs.size
    }

    /**
     * 当前文字位置
     */
    class TextPosition(var text: String = "") {
        var x = 0f
        var y = 0f
    }
}
