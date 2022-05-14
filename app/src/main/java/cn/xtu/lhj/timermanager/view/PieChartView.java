package cn.xtu.lhj.timermanager.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import java.util.List;

import cn.xtu.lhj.timermanager.R;

public class PieChartView extends View {

    /**
     * 最大的饼图是否显示在左上角第一个
     */
    private boolean mShowMaxPartFirst = false;

    /**
     * 饼图开始绘制起始角度
     */
    private float mStartAngle = -180;
    /**
     * 最大扇形与其他扇形的间距
     */
    private int mMaxSpacing = 0;
    /**
     * 饼图半径
     */
    private float mRadius;

    /**
     * 标注的第一段引线的长度
     */
    private int markLineOne = 40;
    /**
     * 标注的第二段引线的长度
     */
    private int markLineTwo = 20;
    /**
     * 引线颜色
     */
    private int markLineColor = Color.BLACK;
    /**
     * 最长文本的宽度
     */
    private float maxLengthTextLength = 180;
    /**
     * 标注文本字体大小
     */
    private int mTextSize = 150;
    /**
     * 标注文本字体颜色
     */
    private int mTextColor = Color.BLACK;
    /**
     * 饼图绘制的数据
     */
    private List<Part> mPartList;
    /**
     * 饼图绘制位置
     */
    private float mOriginLeft = 200;
    private float mOriginTop = 80;
    private float mOriginRight = mOriginLeft + mRadius * 2;
    private float mOriginBottom = mOriginTop + mRadius * 2;
    /**
     * 涂料
     */
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);


    /**
     * 最大扇形突出显示，偏移后的圆心偏移原先圆心的直线距离，
     * 结合三角函数，可以计算出x、y轴各偏移多少
     */
    private double mMoveRadius;
    /**
     * 占比最大的数据扇区
     */
    private Part mMaxPart;

    public PieChartView(Context context) {
        this(context, null);
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * 初始化，处理xml中定义的数据
     */
    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PieChartView);
        // 最大的饼图是否显示在左上角第一个
        mShowMaxPartFirst = typedArray.getBoolean(R.styleable.PieChartView_pie_chart_show_max_part_first, false);
        // 起始绘制角度，-180~+180
        mStartAngle = typedArray.getFloat(R.styleable.PieChartView_pie_chart_start_angle, mStartAngle);
        // 最大扇区与其他整体的偏移量
        mMaxSpacing = typedArray.getDimensionPixelSize(R.styleable.PieChartView_pie_chart_max_spacing, mMaxSpacing);
        // 第一根标注引线的长度
        markLineOne = 15;
        // 第二根标注引线的长度
        markLineTwo = 8;
        // 标注引线的颜色
        markLineColor = typedArray.getColor(R.styleable.PieChartView_pie_chart_mark_line_color, markLineColor);
        // 标注文本大小
        mTextSize = 14;
        // 标注文本颜色
        mTextColor = typedArray.getColor(R.styleable.PieChartView_pie_chart_mark_text_color, mTextColor);
        typedArray.recycle();
        mMoveRadius = Math.sqrt(mMaxSpacing * mMaxSpacing * 2);
    }

    /**
     * 设置饼图展示的数据
     *
     * @param partList 数据
     */
    public void setPartsData(List<Part> partList) {
        mPartList = partList;
        post(new Runnable() {
            @Override
            public void run() {
                dataMeasure();
                machiningData();
                invalidate();
            }
        });
    }

    /**
     * 绘制区域确定
     * 由于要根据数据内容-文本确定绘制参数，所以需要在设置数据后重新操作
     */
    private void dataMeasure() {
        //
        int viewWidth = getMeasuredWidth();
        int viewHeight = getMeasuredHeight();
        //
        float contentWidth = viewWidth - getPaddingLeft() - getPaddingRight();
        float contentHeight = viewHeight - getPaddingBottom() - getPaddingTop();

        // 找出最长文本对应的字符长度
        if (mPartList != null && mPartList.size() > 0) {
            Part maxNameLengthPart = mPartList.get(0);
            mPaint.setTextSize(mTextSize);
            for (Part part : mPartList) {
                if (maxNameLengthPart.name.length() < part.name.length()) {
                    maxNameLengthPart = part;
                }
            }
            maxLengthTextLength = mPaint.measureText(maxNameLengthPart.name);
        }

        // 水平上单边标注需要留的最大空间
        float horizontalMarkSize = markLineOne + markLineTwo + maxLengthTextLength;
        // 竖直上单边标注需要留出的空间
        float verticalMarkSize = markLineOne + markLineTwo + mTextSize;
        // 如果以水平方向为依据，填满的半径为：
        float horizontalRadius = contentWidth / 2 - horizontalMarkSize;
        // 如果以竖直方向为依据，填满的半径为：
        float verticalRadius = contentHeight / 2 - verticalMarkSize;
        // 使用最小值用于半径
        if (horizontalRadius > verticalRadius) {
            mRadius = verticalRadius;
        } else {
            mRadius = horizontalRadius;
        }
        mOriginLeft = viewWidth / 2.0f - mRadius;
        mOriginTop = viewHeight / 2.0f - mRadius;
        //
        mOriginRight = mOriginLeft + mRadius * 2;
        mOriginBottom = mOriginTop + mRadius * 2;
    }

    /**
     * 原始数据加工
     */
    private void machiningData() {
        // 计算每个part中的num总和，用于后面算出每个part的百分比，用于划分整个圆
        if (mPartList == null || mPartList.size() == 0) {
            return;
        }
        float sum = 0;
        mMaxPart = mPartList.get(0);
        int maxPosition = 0;
        for (int i = 0, size = mPartList.size(); i < size; i++) {
            //找出最大值
            Part part = mPartList.get(i);
            //标记比例最大的扇形
            if (part.num > mMaxPart.num) {
                mMaxPart = part;
                maxPosition = i;
            }
            //计算所有Part的总和，后面用于根据百分比划分整个圆
            sum = sum + part.num;
        }
        if (mShowMaxPartFirst) {
            //将最大的扇形移动到第一个
            mPartList.set(0, mPartList.set(maxPosition, mPartList.get(0)));
        }
        for (int i = 0, size = mPartList.size(); i < size; i++) {
            Part part = mPartList.get(i);
            if (i == 0) {
                //全图从-180度开始顺时针绘制
                part.startAngle = mStartAngle;
            } else {
                //前一个扇形绘制结束的位置就是下一个扇形开始绘制的位置
                part.startAngle = mPartList.get(i - 1).startAngle + mPartList.get(i - 1).sweepAngle;
            }
            part.sweepAngle = part.num / sum * 360;

            // 扇形中心角度
            part.midAngle = (part.sweepAngle / 2 + part.startAngle);
            // 求角度对应的三角函数对应的x轴的值,用于三角函数计算
            part.midAngleToRadians = Math.toRadians(part.midAngle);
            //
            part.midAngleCos = Math.cos(part.midAngleToRadians);
            part.midAngleSin = Math.sin(part.midAngleToRadians);
            if (part == mMaxPart) {
                // 求三角函数值，求突出显示的最大扇形在两个x、y需要移动的距离
                float hMove = (float) (mMoveRadius * part.midAngleCos);
                float vMove = (float) (mMoveRadius * part.midAngleSin);
                // 由于最大的扇形显示在第一位的左上角，全图从-180度开始顺时针绘制
                part.rectf = new RectF(mOriginLeft + hMove, mOriginTop + vMove, mOriginRight + hMove, mOriginBottom + vMove);
            } else {
                part.rectf = new RectF(mOriginLeft, mOriginTop, mOriginRight, mOriginBottom);
            }
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mPartList == null || mPartList.size() == 0) {
            return;
        }
        for (Part part : mPartList) {
            mPaint.setStrokeWidth(2);
            mPaint.setColor(part.color);
            canvas.drawArc(part.rectf, part.startAngle, part.sweepAngle, true, mPaint);
            // 标注信息
            mark(canvas, part);
        }
    }

    /**
     * 标注名称、百分比信息
     */
    private void mark(Canvas canvas, Part part) {
        // 圆心
        float circleMidx = mOriginLeft + mRadius;
        float circleMidy = mOriginTop + mRadius;
        // 起始点
        float startx;
        float starty;
        if (part == mMaxPart) {
            startx = circleMidx + (float) ((mRadius + mMoveRadius) * part.midAngleCos);
            starty = circleMidy + (float) ((mRadius + mMoveRadius) * part.midAngleSin);
        } else {
            startx = circleMidx + (float) (mRadius * part.midAngleCos);
            starty = circleMidy + (float) (mRadius * part.midAngleSin);
        }
        //结束点
        float endx = startx + (float) (markLineOne * part.midAngleCos);
        float endy = starty + (float) (markLineOne * part.midAngleSin);
        //开始标注点
        float startWordx = endx + (float) (markLineTwo * part.midAngleCos);
        float startWordy = endy;
        //
        mPaint.setColor(markLineColor);
        canvas.drawLine(startx, starty, endx, endy, mPaint);
        canvas.drawLine(endx, endy, startWordx, startWordy, mPaint);
        float textOffset;
        if (part.midAngleCos < 0) {
            // 左边半圆，要把文字长度算进去，然后+5作为与线之间的间距
            textOffset = mPaint.measureText(part.name) + 5;
        } else {
            // 右半圆，-5作为文字与线之间的间距
            textOffset = -5;
        }
        mPaint.setColor(mTextColor);
        mPaint.setTextSize(mTextSize);
        canvas.drawText(part.name, startWordx - textOffset, startWordy, mPaint);
    }

    /**
     * 每个扇区的数据
     */
    public static class Part {
        /**
         * 名称
         */
        private String name;
        /**
         * 数值
         */
        private float num;
        private RectF rectf;
        /**
         * 开始角度
         */
        private float startAngle;
        /**
         * 扇形中心角
         */
        private float midAngle;
        /**
         * 求角度对应的三角函数对应的x轴的值,用于三角函数计算
         */
        private double midAngleToRadians;
        /**
         * 当前扇区中间角的cos值
         */
        private double midAngleCos;
        /**
         * 当前扇区中间角的sin值
         */
        private double midAngleSin;
        /**
         * 扇形形状角度
         */
        private float sweepAngle;

        /**
         * 扇形颜色
         */
        @ColorInt
        private int color;

        /**
         * @param name 扇形区域名称
         * @param num  扇形区域配置，不用特别按360或100整额处理
         *             因为最终会设置的每个值来调配百分比
         * @param color 扇形区域颜色
         */
        public Part(String name, float num, @ColorInt int color) {
            this.name = name;
            if (num <= 0) {
                throw new IllegalArgumentException("num <= 0 is illegal");
            }
            this.num = num;
            this.color = color;
        }
    }
}
