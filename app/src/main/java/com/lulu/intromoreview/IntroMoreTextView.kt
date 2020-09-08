package com.lulu.intromoreview

import android.content.Context
import android.graphics.Canvas
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View

/**
 * TODO: document your custom view class.
 */
class IntroMoreTextView : View {


    private var textCharArray: CharArray?= null
    private var textPaint: TextPaint = TextPaint()
    /**
     * 文字大小
     */
    public var textSize = 48f
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

    /**
     * 文字位置
     */
    private val textPositions = ArrayList<TextPosition>()

    /**
     * 行 Y 坐标
     */
    private val textLineYs = ArrayList<Float>()


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
        //        val a = context.obtainStyledAttributes(
        //            attrs, R.styleable.IntroMoreTextView, defStyle, 0
        //        )
        //        a.recycle()
        textPaint.color = textColor
        textPaint.alpha = textAlpha
        textPaint.textSize = textSize
        textPaint.isAntiAlias = true//抗锯齿
    }

    /**
     * Constructor->onFinishInflate->onMeasure..->onSizeChanged->onLayout->addOnGlobalLayoutListener->onWindowFocusChanged->onMeasure->onLayout
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        breadText(w)
    }

    /**
     * 文字排版
     */
    private fun breadText(w: Int) {
        val availableWidth = w - paddingRight
        textLineYs.clear()
        textPositions.clear()
        //x 的初始化位置
        val initX = paddingLeft.toFloat()
        var curX = initX
        var curY = paddingTop.toFloat()
        val textFontMetrics = textPaint.fontMetrics
        val lineHeight = textFontMetrics.bottom - textFontMetrics.top
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
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (i in 0 until textPositions.size) {
            val textPosition = textPositions[i]
            canvas.drawText(textPosition.text, textPosition.x, textPosition.y, textPaint)
        }
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
