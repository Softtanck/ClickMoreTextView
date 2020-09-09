package com.lulu.clickmoretextView

import android.content.Context
import android.graphics.Canvas
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
    private var textCharArray: CharArray?= null
    private var textPaint: TextPaint = TextPaint()
    public var moreTextPaint: TextPaint = TextPaint()
    private var textPaintTop = 0f
    private var isBreakFlag = false//排版标识
    /**
     * 文字大小
     */
    public var textSize = 80f
        set(value) {
            field = value
            textPaint.textSize = value
        }

    /**
     * 文字颜色
     */
    public var textColor = 0x000000
        set(value) {
            field = value
            textPaint.color = value
        }
    /**
     * 文字透明度
     */
    public var textAlpha = 255
        set(value) {
            field = value
            textPaint.alpha = value
        }
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

    public var isShowMore = false
        set(value) {
            field = value
            requestLayout()
        }

    /**
     * 查看更多
     */
    public var moreText = "查看更多"
        set(value) {
            field = value
            requestLayout()
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
    private var layoutHeight  = 0f

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
            attrs, R.styleable.ClickMoreTextView, defStyle, 0)
        val text = a.getString(R.styleable.ClickMoreTextView_text)
        text?.let {
            this.text = it
        }
        this.maxLines = a.getInt(R.styleable.ClickMoreTextView_maxLines, Int.MAX_VALUE)
        this.isShowMore = a.getBoolean(R.styleable.ClickMoreTextView_isShowMore, false)
        a.recycle()

        textPaint.color = textColor
        textPaint.alpha = textAlpha
        textPaint.textSize = textSize
        textPaint.isAntiAlias = true//抗锯齿

        moreTextPaint.color = textColor
        moreTextPaint.alpha = textAlpha
        moreTextPaint.isFakeBoldText = true
        moreTextPaint.textSize = textSize
        moreTextPaint.isAntiAlias = true//抗锯齿

        isEnabled = true
    }

    override fun requestLayout() {
        super.requestLayout()
        Log.d(TAG, "requestLayout: last isBreakFlag: $isBreakFlag")
        isBreakFlag = false
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)
        breadText(width)
        if (layoutHeight > 0 ) {
            height = layoutHeight.toInt()
        }
        Log.d(TAG, "onMeasure: getLines():${getLines()} maxLines: $maxLines width:$width height:$height")
        if (getLines() > maxLines && maxLines - 1 > 0) {
            val textBottomH = textPaint.fontMetrics.bottom.toInt()
            height = (textLineYs[maxLines-1]).toInt() + paddingBottom + textBottomH
        }
        setMeasuredDimension(width, height)
    }

    /**
     * Constructor->onFinishInflate->onMeasure..->onSizeChanged->onLayout->addOnGlobalLayoutListener->onWindowFocusChanged->onMeasure->onLayout
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        //breadText(w)
    }

    /**
     * 文字排版
     */
    private fun breadText(w: Int) {
        if (w <= 0) {
            return
        }
        if (isBreakFlag) {
            return
        }
        Log.d(TAG, "breadText: 开始排版")
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
        val size = textCharArray?.size
        size?.let {
            var i = 0
            while (i < size) {
                val textPosition = TextPosition()
                val c = textCharArray?.get(i)
                val cW = textPaint.measureText(c.toString())
                //位置保存点
                textPosition.x = curX
                textPosition.y = curY
                textPosition.text = c.toString()
                //curX 向右移动一个字
                curX += cW
                if (isParagraph(textCharArray, i) ||//段落内
                    isNeedNewLine(textCharArray, i, curX, availableWidth)) { //折行
                    textLineYs.add(curY)
                    //断行需要回溯
                    curX = initX
                    curY += lineHeight * lineSpacingMultiplier
                }
                textPositions.add(textPosition)
                i++//移动游标
                if (checkNeedMoreText(availableWidth, curX, curY)) {
                    break
                }
            }
            //最后一行
            textLineYs.add(curY)
            curY += paddingBottom
            layoutHeight = curY + textFontMetrics.bottom//应加上后面的Bottom
            Log.d(TAG, "总行数： ${getLines()}" )
        }

    }

    /**
     * 检查 MoreText 插入点
     */
    private fun checkNeedMoreText(availableWidth: Int, curX: Float, curY: Float): Boolean {
        if (isShowMore.not() || maxLines == Int.MAX_VALUE) {
            return false
        }
        val dotLen = textPaint.measureText("...")
        moreTextW = moreTextPaint.measureText(moreText)
        val moreTextFontMetrics = moreTextPaint.fontMetrics
        val lines = getLines()
        if (lines == maxLines - 1) {//此时说明正在遍历 最后一行
            if (curX + moreTextW + dotLen + textPaint.measureText("中") > availableWidth) {
                val element = TextPosition()
                element.x = curX
                element.y = curY
                element.text = "..."
                textPositions.add(element)
                //点击区域
                moreTextClickArea.top = curY + moreTextFontMetrics.top
                moreTextClickArea.right = availableWidth.toFloat()
                moreTextClickArea.bottom = curY + moreTextFontMetrics.bottom
                moreTextClickArea.left = curX
                return true
            }
        }
        return false
    }

    /**
     * 是否是段落
     */
    private fun isParagraph(charArray: CharArray?, curIndex: Int): Boolean {
        charArray?.let {
            if (charArray.size <= curIndex) {
                return false
            }
            if (charArray[curIndex] == '\n') {
                return true
            }
        }
        return false
    }

    /**
     * 是否需要另起一行
     */
    private fun isNeedNewLine(
        charArray: CharArray?,
        curIndex: Int,
        curX: Float,
        maxWith: Int
    ) : Boolean{
        charArray?.let {
            if (charArray.size <= curIndex+1) {//需要判断下一个 char
                return false
            }
            //判断下一个 char 是否到达边界
            if (curX + textPaint.measureText(charArray[curIndex+1].toString()) > maxWith) {
                return true
            }
        }
        if (curX > maxWith) {
            return true
        }
        return false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d(TAG, "onDraw: ")
        val posSize = textPositions.size
        for (i in 0 until posSize) {
            val textPosition = textPositions[i]
            if (textPosition.y + textPaintTop > height-paddingBottom) {
                break
            }
            //Log.d(TAG, "onDraw: height： $height  height-paddingBottom:${ height-paddingBottom} textPosition.y+textPaintTop: ${textPosition.y + textPaintTop}")
            canvas.drawText(textPosition.text, textPosition.x, textPosition.y, textPaint)
        }
        if (isShowMore && maxLines == getLines() && posSize > 0) {
            val moreTextY = textPositions[posSize - 1].y
            val moreTextX = width - moreTextW - paddingRight
            canvas.drawText(moreText, moreTextX, moreTextY, moreTextPaint)
        }
    }

    /**
     * 查看更多点击区域
     */
    private val moreTextClickArea = RectF()

    /**
     *
     */
    private var curMoreTextUp = false
    private var isDownArea = false

    private var lastDownX = -1f
    private var lastDownY = -1f

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            val x = event.x
            val y = event.y
            Log.d(TAG, "onTouchEvent: x: $x y:$y event: ${event.action}")
            when(it.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastDownX = x
                    lastDownY = y
                }
                MotionEvent.ACTION_UP -> {
                    if (moreTextClickArea.contains(lastDownX, lastDownY) &&
                            moreTextClickArea.contains(x, y)) {
                        Log.d(TAG, "onTouchEvent: 点击更多回调")
                        moreTextClickListener?.onClick(this)
                    }
                }
                else -> {}
            }
        }
        return true
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
    class TextPosition {
        var text = ""
        var x = 0f
        var y = 0f
    }
}
