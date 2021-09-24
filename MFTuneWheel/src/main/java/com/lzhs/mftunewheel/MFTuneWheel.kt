package com.lzhs.mftunewheel

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.text.DecimalFormat
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import kotlin.math.*

/**
 *  广播 调频频道选择器
 */
class MFTuneWheel : View {

    /**
     * 绘制 文字的画笔
     */
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 绘制 刻度的画笔
     */
    private val paint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    /**
     *普通刻度线的高度
     */
    private var scaleHeight = 40.0f

    /**
     *普通刻度线的颜色
     */
    private var scaleNormalColor = Color.LTGRAY

    /**
     * 普通刻度线的宽度
     */
    private var scaleNormalWidth = 5f

    /**
     *普通刻度线的高度  与  5 刻度的高度差
     */
    private var scaleHeightDiff = 22.0f

    /**
     *中线刻度线的高度  与  普通刻度的高度差
     */
    private var scaleMaxHeightDiff = 30.0f


    /**
     * 普通刻度线的宽度
     */
    private var scaleHeightWidth = 7f

    /**
     *刻度位5的 刻度线颜色
     */
    private var scaleNormalFiveColor = Color.WHITE

    /**
     * 要表现的第一个刻度值
     */
    private var minItem = 875

    /**
     * 需要表现的最后一个刻度值
     */
    private var maxItem = 950

    /**
     * 需要显示 的刻度的总数
     */
    private var itemCount = 30

    /**
     * 刻度之间的间距
     */
    private var spacing = 0.0f

    /**
     * 绘制文字的大小
     */
    private var fontSize = 16

    /**
     *绘制的文字与刻度之间的距离
     */
    private var textDiff = 20.0f

    /**
     * 绘制文字的颜色
     */
    private var textColor = Color.WHITE

    /**
     * 中心点位置，以此点建立坐标系（x,y）
     */
    private lateinit var centerCoordinates: Pair<Float, Float>

    /**
     * 常量 a²,b²
     */
    private lateinit var axisSquare: Pair<Float, Float>

    /**
     * 正常显示时 最中间的那个刻度的取值范围
     */
    private lateinit var centerBoundary: Pair<Float, Float>

    /**
     *存储 当手指按下时中间 突出部分的区域
     */
    private lateinit var chooserCenterP: Pair<Float, Float>

    /**
     *存储  总长度的 边界
     */
    private lateinit var drawBoundary: Pair<Float, Float>

    /**
     * 上一次停留的点
     */
    private var lastX = 0f

    /**
     * 滑动时，手指移动的距离 可能有正数，或者负数
     */
    private var moveX = 0f

    /**
     * 用来存储 每个点的信息
     */
    private val points: CopyOnWriteArrayList<ItemPoint> = CopyOnWriteArrayList()

    /**
     * s手指按下时中心点的信息
     */
    private lateinit var centerPoint: ItemPoint

    /**
     * 记录手势的状态
     */
    private var actionTouch = MotionEvent.ACTION_UP

    /**
     * 当 中心点的Item 发生变化时回调
     */
    private var listener: ((String) -> Unit)? = null

    /**
     * 格式化后数据
     */
    private val itemDecimal = { item: Int -> DecimalFormat("##.0").format(item / 10f) }

    /**
     * 该方法将根据 X 轴坐标 计算出Y轴坐标
     */
    private val itemY =
        { x: Float -> sqrt(((1f - x.pow(2) / axisSquare.first) * axisSquare.second)) + (height / 3f) }

    /**
     * 单线程线程池用来 大量计算 坐标点的改变
     */
    private val executorsPool = Executors.newSingleThreadExecutor()

    companion object {
        private const val TAG = "MFTuneWheel"
    }

    init {
        // 初始化画笔
        paint.apply {
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.FILL
        }
        textPaint.apply {
            textSize = fontSize * context.resources.displayMetrics.density
            color = textColor
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.FILL
        }
    }

    constructor(mContext: Context) : super(mContext) {

    }

    constructor(mContext: Context, attrs: AttributeSet) : super(mContext, attrs) {
        val typeArr = mContext.obtainStyledAttributes(attrs, R.styleable.MFTuneWheel)
        val tempMinItem = typeArr.getInt(R.styleable.MFTuneWheel_min_item, minItem)
        val tempMaxItem = typeArr.getInt(R.styleable.MFTuneWheel_max_item, maxItem)
        val tempItemCount = typeArr.getInt(R.styleable.MFTuneWheel_item_count, itemCount)
        if (tempItemCount in 16..54) itemCount = tempItemCount
        if (tempMaxItem - tempMinItem > 15) {
            minItem = tempMinItem
            maxItem = tempMaxItem
        }
        val tempScaleHeight = typeArr.getFloat(R.styleable.MFTuneWheel_scale_height, scaleHeight)
        if (tempScaleHeight >= 10f && tempScaleHeight < 250f) scaleHeight = tempScaleHeight
        scaleNormalColor =
            typeArr.getColor(R.styleable.MFTuneWheel_scale_normal_color, scaleNormalColor)
        scaleNormalWidth =
            typeArr.getFloat(R.styleable.MFTuneWheel_scale_normal_width, scaleNormalWidth)
        scaleHeightDiff =
            typeArr.getFloat(R.styleable.MFTuneWheel_scale_height_diff, scaleHeightDiff)
        scaleMaxHeightDiff =
            typeArr.getFloat(R.styleable.MFTuneWheel_scale_max_height_diff, scaleMaxHeightDiff)
        scaleHeightWidth =
            typeArr.getFloat(R.styleable.MFTuneWheel_scale_height_width, scaleHeightWidth)
        scaleNormalFiveColor =
            typeArr.getColor(R.styleable.MFTuneWheel_scale_normal_five_color, scaleNormalFiveColor)
        fontSize = typeArr.getInt(R.styleable.MFTuneWheel_font_size, fontSize)
        textDiff = typeArr.getFloat(R.styleable.MFTuneWheel_text_diff, textDiff)
        textColor =
            typeArr.getColor(R.styleable.MFTuneWheel_text_color, textColor)
    }

    fun setListener(listener: ((String) -> Unit)) {
        this.listener = listener
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        // 初始化只需要 计算一个常量
        spacing = (width * 1f - 100f) / (itemCount * 1f)
        centerCoordinates = Pair(
            50f + spacing * itemCount / 2f,
            (height) / 2f
        )
        axisSquare = Pair(
            (centerCoordinates.first).pow(2),
            (centerCoordinates.second).pow(2)
        )
        centerBoundary = Pair(
            centerCoordinates.first - spacing / 4,
            centerCoordinates.first + spacing / 4
        )

        chooserCenterP = Pair(
            centerCoordinates.first - spacing * 3.5f,
            centerCoordinates.first + spacing * 3.5f
        )
        val startX = centerCoordinates.first - spacing * (itemCount / 2f)
        drawBoundary = Pair(
            startX - spacing / 2,
            startX + spacing * itemCount + spacing / 2
        )
        calculateCoordinatePoints()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                moveX = 0.0f
                lastX = event.x
                actionTouch = MotionEvent.ACTION_DOWN
                changeItemContent()
            }
            MotionEvent.ACTION_MOVE -> {
                moveX = lastX - event.x
                if (abs(moveX) >= 1) {
                    changeItemContent()
                    lastX = event.x
                }
                actionTouch = MotionEvent.ACTION_MOVE
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                actionTouch = MotionEvent.ACTION_UP
                moveX = centerPoint.startP.first - centerCoordinates.first
                changeItemContent()
                return false
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (it in points) {
            if (it.startP.first < drawBoundary.first || it.startP.first > drawBoundary.second)
                continue
            if (it.startP.first in centerBoundary.first..centerBoundary.second)
                changeCenterItem(it)
            drawScale(canvas, it.startP, it.endP, it.itemVal, it.type)
        }
    }

    /**
     * 根据手指滑动的距离来改变 屏幕中 显示的数组的信息
     */
    private fun changeItemContent() {
        executorsPool.execute {
            val startX = points.first().startP.first - moveX
            val endX = points.last().startP.first - moveX
            if (centerCoordinates.first in startX..endX) {
                points.forEach {
                    val coordinateX = it.startP.first - centerCoordinates.first - moveX
                    val tempItem = getItemPoint(coordinateX, it.itemVal)
                    it.apply {
                        startP = tempItem.startP
                        endP = tempItem.endP
                        type = tempItem.type
                        itemVal = tempItem.itemVal
                    }
                }
                changePointCount()
                postInvalidate()
            }
        }
    }

    /**
     * 将要计算出偏移量一实现 删除点位的同时
     */
    private fun changePointCount() {
        if (moveX == 0f) return
        points.filter { (it.startP.first < drawBoundary.first || it.startP.first > drawBoundary.second) }
            .forEach { points.remove(it) }
        if (moveX < 0f &&
            points.first().itemVal > minItem &&
            points.first().startP.first > (drawBoundary.first - spacing)
        ) {
            do {
                val coordinateX = (points.first().startP.first - spacing) - centerCoordinates.first
                points.add(0, getItemPoint(coordinateX, points.first().itemVal - 1))
            } while (points.first().startP.first > drawBoundary.first)
        } else if (moveX > 0f &&
            points.last().itemVal < maxItem &&
            points.last().startP.first < (drawBoundary.second + spacing)
        ) {
            do {
                val coordinateX = (points.last().startP.first + spacing) - centerCoordinates.first
                points.add(getItemPoint(coordinateX, points.last().itemVal + 1))
            } while (points.last().startP.first < drawBoundary.second)
        }
    }

    /**
     * 初始化   坐标点的位置 并保存 在数组中
     * 根据数据的下标俩创建数据
     */
    private fun calculateCoordinatePoints() {
        points.clear()
        var offset = 0f// 向 中间靠齐
        if (itemCount % 2 != 0) offset = spacing / 2
        for (index in 0..itemCount) {
            val coordinateX = (50f + index * spacing - offset) - centerCoordinates.first
            points.add(getItemPoint(coordinateX, minItem + index))
        }
    }

    /**
     *  该方法将根据用户 录入 椭圆坐标系里面的X坐标返回一个点的信息
     */
    private fun getItemPoint(coordinateX: Float, itemVal: Int): ItemPoint {
        var endY = itemY(coordinateX)
        var endX = centerCoordinates.first + coordinateX
        val startP = Pair(endX, endY)
        endY -= scaleHeight
        var type: SCALE_TYPE
        var endP: Pair<Float, Float>
        when {// 绘制 三种 基础的刻度 1、普通刻度 2、数值为5的刻度 3、中心点的刻度
            endX in centerBoundary.first..centerBoundary.second -> {
                endP = Pair(endX, endY - scaleMaxHeightDiff)
                type = SCALE_TYPE.CENTER_MAIN
            }
            itemVal % 5 == 0 -> {
                endP = Pair(endX, endY - scaleHeightDiff)
                type = SCALE_TYPE.NORMAL_FIVE
            }
            else -> {
                endP = Pair(endX, endY)
                type = SCALE_TYPE.NORMAL
            }
        }
        //绘制手指按下时 中间突出部分
        if ((actionTouch == MotionEvent.ACTION_DOWN || actionTouch == MotionEvent.ACTION_MOVE)
            && endX in chooserCenterP.first..chooserCenterP.second
        ) {
            type = SCALE_TYPE.CHOOSER_CENTER_MAIN
            when (endX) {
                in chooserCenterP.first + spacing * 0f..chooserCenterP.first + spacing * 1f ->
                    endP = Pair(endX, endY - scaleHeightDiff * 1f)
                in chooserCenterP.first + spacing * 1f..chooserCenterP.first + spacing * 2f ->
                    endP = Pair(endX, endY - scaleHeightDiff * 2f)
                in chooserCenterP.first + spacing * 2f..chooserCenterP.first + spacing * 3f ->
                    endP = Pair(endX, endY - scaleHeightDiff * 3f)
                in chooserCenterP.first + spacing * 3f..chooserCenterP.first + spacing * 4f ->
                    endP = Pair(endX, endY - scaleHeightDiff * 4f)
                in chooserCenterP.first + spacing * 4f..chooserCenterP.first + spacing * 5f ->
                    endP = Pair(endX, endY - scaleHeightDiff * 3f)
                in chooserCenterP.first + spacing * 5f..chooserCenterP.first + spacing * 6f ->
                    endP = Pair(endX, endY - scaleHeightDiff * 2f)
                in chooserCenterP.first + spacing * 6f..chooserCenterP.first + spacing * 7f ->
                    endP = Pair(endX, endY - scaleHeightDiff * 1f)
            }
        }
        return ItemPoint(startP, endP, type, itemVal)
    }


    /**
     *  内联绘制 的刻度线
     */
    private fun drawScale(
        canvas: Canvas,
        startPair: Pair<Float, Float>,
        endPair: Pair<Float, Float>,
        itemVal: Int,
        type: SCALE_TYPE
    ) {
        when (type) { // 根据 不同的刻度类别设置 画笔的参数
            SCALE_TYPE.CHOOSER_CENTER_MAIN -> {
                paint.apply {
                    LinearGradient(
                        startPair.first, startPair.second,
                        endPair.first, endPair.second,
                        Color.WHITE, Color.RED,
                        Shader.TileMode.MIRROR
                    ).also { shader = it }
                    color = Color.RED
                    strokeWidth = scaleHeightWidth + 2
                }
            }
            SCALE_TYPE.CENTER_MAIN -> {
                paint.apply {
                    shader = null
                    color = Color.RED
                    strokeWidth = scaleHeightWidth + 1
                }
            }
            SCALE_TYPE.NORMAL -> {
                paint.apply {
                    color = scaleNormalColor
                    shader = null
                    strokeWidth = scaleNormalWidth
                }
            }
            SCALE_TYPE.NORMAL_FIVE -> {
                paint.apply {
                    color = scaleNormalFiveColor
                    shader = null
                    strokeWidth = scaleHeightWidth
                }
            }
        }
        var paintAlpha = 255
        val alphaL = drawBoundary.first + spacing * 10f
        val alphaR = drawBoundary.second - spacing * 10f
        if (startPair.first > drawBoundary.first && startPair.first < alphaL)
            paintAlpha = (255 * (startPair.first / (drawBoundary.first + (spacing * 10f)))).toInt()
        else if (startPair.first < drawBoundary.second && startPair.first > alphaR)
            paintAlpha = (255 * (1 + ((alphaR - startPair.first) / (spacing * 10f)))).toInt()
        paint.alpha = paintAlpha
        textPaint.alpha = paintAlpha
        if (itemVal % 5 == 0)
            drawText(canvas, startPair, itemVal)
        canvas.drawLine(startPair.first, startPair.second, endPair.first, endPair.second, paint)

    }

    /**
     * 绘制刻度为5的文字
     */
    private fun drawText(
        canvas: Canvas,
        startPair: Pair<Float, Float>,
        keyVal: Int
    ) {
        val textX = startPair.first - textPaint.textSize
        val textY = startPair.second + textPaint.textSize + textDiff
        canvas.drawText(itemDecimal.invoke(keyVal), textX, textY, textPaint)
    }

    /**
     * 设置中点的信息，并回调中心点信息改变回调方法
     */
    private fun changeCenterItem(item: ItemPoint) {
        this.centerPoint = item
        listener?.invoke(itemDecimal.invoke(item.itemVal))
    }
}


/**
 * 各种刻度的 分类
 */
enum class SCALE_TYPE {
    CHOOSER_CENTER_MAIN,// 选中时 中心区域 主刻度
    CENTER_MAIN,   // 中心点 主刻度
    NORMAL,// 普通  刻度
    NORMAL_FIVE,// 普通  5 加粗 刻度

}

/**
 * 保存计算出来的数据
 */
data class ItemPoint(
    /**
     * 绘制 刻度柱开始的位置
     */
    var startP: Pair<Float, Float>,
    /**
     * 绘制 刻度柱结束的位置
     */
    var endP: Pair<Float, Float>,
    /**
     * 要绘制的柱子的类型
     */
    var type: SCALE_TYPE,

    /**
     * 改坐标点锁携带的数据
     */
    var itemVal: Int
)


