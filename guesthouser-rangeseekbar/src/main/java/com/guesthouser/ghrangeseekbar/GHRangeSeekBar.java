package com.guesthouser.ghrangeseekbar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ImageView;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by GuestHouser on 7/20/16.
 */
public class GHRangeSeekBar<T extends Number> extends ImageView {

    /**
     * Widget that lets users select a minimum and maximum value on a given numerical range.
     * The range value types can be one of Long, Double, Integer, Float, Short, Byte or BigDecimal.<br />
     * <br />
     * Improved {@link android.view.MotionEvent} handling for smoother use, anti-aliased painting for improved aesthetics.
     *
     * @param <T> The Number type of the range values. One of Long, Double, Integer, Float, Short, Byte or BigDecimal.
     * <p/>
     * <p/>
     * <p/>
     * https://code.google.com/p/range-seek-bar/
     * <p/>
     * Apache License
     * <p/>
     * <p/>
     * @author Guesthouser (support@guesthouser.com)
     */

    public static final Integer DEFAULT_MINIMUM = 0;
    public static final Integer DEFAULT_MAXIMUM = 100;
    public static final int HEIGHT_IN_DP = 30;
    private static final int INITIAL_PADDING_IN_DP = 0;//original 8
    private int lineHeight = 20;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap thumbImage = BitmapFactory.decodeResource(getResources(), R.drawable.ic_thumb);
    private float thumbWidth = thumbImage.getWidth();
    private float thumbHalfWidth = 0.5f * thumbWidth;
    private float thumbHalfHeight = 0.5f * thumbImage.getHeight();
    private float INITIAL_PADDING;
    private float padding;
    private T absoluteMinValue, absoluteMaxValue;
    private NumberType numberType;
    private double absoluteMinValuePrim, absoluteMaxValuePrim;
    private double normalizedMinValue = 0d;
    private double normalizedMaxValue = 1d;
    private Thumb pressedThumb = null;
    private boolean notifyWhileDragging = false;
    private OnRangeSeekBarChangeListener<T> listener;

    private int lineHighlightedColor = 0xFF00BA8C;
    private int lineBgColor = 0xFFFFFFFF;

    /**
     * An invalid pointer id.
     */
    public static final int INVALID_POINTER_ID = 255;

    // Localized constants from MotionEvent for compatibility
    // with API < 8 "Froyo".
    public static final int ACTION_POINTER_INDEX_MASK = 0x0000ff00, ACTION_POINTER_INDEX_SHIFT = 8;

    private float mDownMotionX;

    private int mActivePointerId = INVALID_POINTER_ID;

    private int mScaledTouchSlop;

    private boolean mIsDragging;

    private RectF mRect;

    private ValueTypes mValueType = ValueTypes.LINEAR;

    /**
     * valid points to be set in rangebar and filter
     */
    private ArrayList<Integer> rangeList = new ArrayList<>();


    public GHRangeSeekBar(Context context) {
        this(context, null);
    }

    public GHRangeSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GHRangeSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        setRangeToDefaultValues();
        setValuePrimAndNumberType();
        INITIAL_PADDING = dpToPx(context, INITIAL_PADDING_IN_DP);

        mRect = new RectF(padding,
                thumbHalfHeight - lineHeight / 2,
                getWidth() - padding,
                thumbHalfHeight + lineHeight / 2);

        // make RangeSeekBar focusable. This solves focus handling issues in case EditText widgets are being used along with the RangeSeekBar within ScollViews.
        setFocusable(true);
        setFocusableInTouchMode(true);
        mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @SuppressWarnings("unchecked")
    // only used to set default values when initialised from XML without any values specified
    private void setRangeToDefaultValues() {
        this.absoluteMinValue = (T) DEFAULT_MINIMUM;
        this.absoluteMaxValue = (T) DEFAULT_MAXIMUM;
        setValuePrimAndNumberType();
        setRangeBarArrayList();
    }

    private void setValuePrimAndNumberType() {
        absoluteMinValuePrim = absoluteMinValue.doubleValue();
        absoluteMaxValuePrim = absoluteMaxValue.doubleValue();
        numberType = NumberType.fromNumber(absoluteMinValue);
    }

    /**
     * Should the widget notify the listener callback while the user is still dragging a thumb? Default is false.
     *
     * @param flag
     */
    public void setNotifyWhileDragging(boolean flag) {
        this.notifyWhileDragging = flag;
    }

    /**
     * Returns the absolute minimum value of the range that has been set at construction time.
     *
     * @return The absolute minimum value of the range.
     */
    public T getAbsoluteMinValue() {
        return absoluteMinValue;
    }

    /**
     * Returns the absolute maximum value of the range that has been set at construction time.
     *
     * @return The absolute maximum value of the range.
     */
    public T getAbsoluteMaxValue() {
        return absoluteMaxValue;
    }

    /**
     * Returns the currently selected min value.
     *
     * @return The currently selected min value.
     */
    public T getSelectedMinValue(ValueTypes v) {

        switch (v) {
            case LINEAR:
                return normalizedToValue(normalizedMinValue);
            case DISCRETE:
                int min1 = getSelectedMinValue(ValueTypes.LINEAR).intValue();
                Integer min2 = getClosestValue(min1);
                setSelectedMinValue((T) min2);
                return (T) min2;
            default:
                return normalizedToValue(normalizedMinValue);
        }

    }

    /**
     * Sets the currently selected minimum value. The widget will be invalidated and redrawn.
     *
     * @param value The Number value to set the minimum value to. Will be clamped to given absolute minimum/maximum range.
     */
    public void setSelectedMinValue(T value) {
        // in case absoluteMinValue == absoluteMaxValue, avoid division by zero when normalizing.
        if (0 == (absoluteMaxValuePrim - absoluteMinValuePrim)) {
            setNormalizedMinValue(0d);
        } else {
            setNormalizedMinValue(valueToNormalized(value));
        }
    }

    /**
     * Returns the currently selected max value.
     *
     * @return The currently selected max value.
     */
    public T getSelectedMaxValue(ValueTypes v) {
        switch (v) {
            case LINEAR:
                return normalizedToValue(normalizedMaxValue);
            case DISCRETE:
                int max1 = getSelectedMaxValue(ValueTypes.LINEAR).intValue();
                Integer max2 = getClosestValue(max1);
                setSelectedMaxValue((T) max2);
                return (T) max2;
            default:
                return normalizedToValue(normalizedMaxValue);
        }
    }

    /**
     * Sets the currently selected maximum value. The widget will be invalidated and redrawn.
     *
     * @param value The Number value to set the maximum value to. Will be clamped to given absolute minimum/maximum range.
     */
    public void setSelectedMaxValue(T value) {
        // in case absoluteMinValue == absoluteMaxValue, avoid division by zero when normalizing.
        if (0 == (absoluteMaxValuePrim - absoluteMinValuePrim)) {
            setNormalizedMaxValue(1d);
        } else {
            setNormalizedMaxValue(valueToNormalized(value));
        }
    }

    /**
     * Registers given listener callback to notify about changed selected values.
     *
     * @param listener The listener to notify about changed selected values.
     */
    public void setOnRangeSeekBarChangeListener(OnRangeSeekBarChangeListener<T> listener) {
        this.listener = listener;
    }

    /**
     * Handles thumb selection and movement. Notifies listener callback on certain events.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!isEnabled()) {
            return false;
        }

        int pointerIndex;

        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                // Remember where the motion event started
                mActivePointerId = event.getPointerId(event.getPointerCount() - 1);
                pointerIndex = event.findPointerIndex(mActivePointerId);
                mDownMotionX = event.getX(pointerIndex);

                pressedThumb = evalPressedThumb(mDownMotionX);

                // Only handle thumb presses.
                if (pressedThumb == null) {
                    return super.onTouchEvent(event);
                }

                setPressed(true);
                invalidate();
                onStartTrackingTouch();
                trackTouchEvent(event);
                attemptClaimDrag();

                break;
            case MotionEvent.ACTION_MOVE:
                if (pressedThumb != null) {

                    if (mIsDragging) {
                        trackTouchEvent(event);
                    } else {
                        // Scroll to follow the motion event
                        pointerIndex = event.findPointerIndex(mActivePointerId);
                        final float x = event.getX(pointerIndex);
                        final float y = event.getRawY();
                        int[] loc = new int[2];
                        getLocationOnScreen(loc);
//                        if(y>loc[1] && y<(loc[1]+getHeight())) {

                        if (Math.abs(x - mDownMotionX) > mScaledTouchSlop) {
                            setPressed(true);
                            invalidate();
                            onStartTrackingTouch();
                            trackTouchEvent(event);
                            attemptClaimDrag();
                        }
//                        }
                    }

                    if (notifyWhileDragging && listener != null) {
                        setValueChanges();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsDragging) {
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                    setPressed(false);
                } else {
                    // Touch up when we never crossed the touch slop threshold
                    // should be interpreted as a tap-seek to that location.
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                }

                pressedThumb = null;
                invalidate();
                if (listener != null) {
                    setValueChanges();
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = event.getPointerCount() - 1;
                // final int index = ev.getActionIndex();
                mDownMotionX = event.getX(index);
                mActivePointerId = event.getPointerId(index);
                invalidate();
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsDragging) {
                    onStopTrackingTouch();
                    setPressed(false);
                }
                invalidate(); // see above explanation
                break;
        }
        return true;
    }

    private final void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & ACTION_POINTER_INDEX_MASK) >> ACTION_POINTER_INDEX_SHIFT;

        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose
            // a new active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mDownMotionX = ev.getX(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    private final void trackTouchEvent(MotionEvent event) {
        final int pointerIndex = event.findPointerIndex(mActivePointerId);
        final float x = event.getX(pointerIndex);
        final float y = event.getRawY();
        int[] loc = new int[2];
        getLocationOnScreen(loc);
//        if(y>loc[1] && y<(loc[1]+getHeight())) {
        if (Thumb.MIN.equals(pressedThumb)) {
            setNormalizedMinValue(screenToNormalized(x));
        } else if (Thumb.MAX.equals(pressedThumb)) {
            setNormalizedMaxValue(screenToNormalized(x));
        }
//        }


    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any ancestors from stealing events in the drag.
     */
    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    /**
     * This is called when the user has started touching this widget.
     */
    void onStartTrackingTouch() {
        mIsDragging = true;
    }

    /**
     * This is called when the user either releases his touch or the touch is canceled.
     */
    void onStopTrackingTouch() {
        mIsDragging = false;
    }

    /**
     * Ensures correct size of the widget.
     */
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 200;
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }

        int height = thumbImage.getHeight() + dpToPx(getContext(), HEIGHT_IN_DP);
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
            height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
        }
        setMeasuredDimension(width, height);
    }

    /**
     * Draws the widget on the given canvas.
     */
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(lineBgColor);
        paint.setAntiAlias(true);

        padding = INITIAL_PADDING + thumbHalfWidth;

        // draw seek bar background line
        mRect.left = padding;
        mRect.right = getWidth() - padding;
        //canvas.drawRect(mRect, paint);
        canvas.drawRoundRect(mRect, lineHeight, lineHeight, paint);

        // draw seek bar active range line
        mRect.left = normalizedToScreen(normalizedMinValue);
        mRect.right = normalizedToScreen(normalizedMaxValue);

        paint.setColor(lineHighlightedColor);
        //canvas.drawRect(mRect, paint);
        canvas.drawRoundRect(mRect, lineHeight, lineHeight, paint);


        // draw minimum thumb if not a single thumb control
        drawThumb(normalizedToScreen(normalizedMinValue), canvas);

        // draw maximum thumb
        drawThumb(normalizedToScreen(normalizedMaxValue), canvas);

    }


    /**
     * Thumb constants (min and max).
     */
    public enum ValueTypes {
        LINEAR, DISCRETE
    }

    /**
     * Overridden to save instance state when device orientation changes. This method is called automatically if you assign an id to the RangeSeekBar widget using the {@link #setId(int)} method. Other members of this class than the normalized min and max values don't need to be saved.
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable("SUPER", super.onSaveInstanceState());
        bundle.putDouble("MIN", normalizedMinValue);
        bundle.putDouble("MAX", normalizedMaxValue);
        return bundle;
    }

    /**
     * Overridden to restore instance state when device orientation changes. This method is called automatically if you assign an id to the RangeSeekBar widget using the {@link #setId(int)} method.
     */
    @Override
    protected void onRestoreInstanceState(Parcelable parcel) {
        final Bundle bundle = (Bundle) parcel;
        super.onRestoreInstanceState(bundle.getParcelable("SUPER"));
        normalizedMinValue = bundle.getDouble("MIN");
        normalizedMaxValue = bundle.getDouble("MAX");
    }

    /**
     * Draws the "normal" resp. "pressed" thumb image on specified x-coordinate.
     *
     * @param screenCoord The x-coordinate in screen space where to draw the image.
     * @param canvas      The canvas to draw upon.
     */
    private void drawThumb(float screenCoord, Canvas canvas) {
        canvas.drawBitmap(thumbImage, screenCoord - thumbHalfWidth, 0, paint);
    }

    /**
     * Decides which (if any) thumb is touched by the given x-coordinate.
     *
     * @param touchX The x-coordinate of a touch event in screen space.
     * @return The pressed thumb or null if none has been touched.
     */
    private Thumb evalPressedThumb(float touchX) {
        Thumb result = null;
        boolean minThumbPressed = isInThumbRange(touchX, normalizedMinValue);
        boolean maxThumbPressed = isInThumbRange(touchX, normalizedMaxValue);
        if (minThumbPressed && maxThumbPressed) {
            // if both thumbs are pressed (they lie on top of each other), choose the one with more room to drag. this avoids "stalling" the thumbs in a corner, not being able to drag them apart anymore.
            result = (touchX / getWidth() > 0.5f) ? Thumb.MIN : Thumb.MAX;
        } else if (minThumbPressed) {
            result = Thumb.MIN;
        } else if (maxThumbPressed) {
            result = Thumb.MAX;
        }
        return result;
    }

    /**
     * Decides if given x-coordinate in screen space needs to be interpreted as "within" the normalized thumb x-coordinate.
     *
     * @param touchX               The x-coordinate in screen space to check.
     * @param normalizedThumbValue The normalized x-coordinate of the thumb to check.
     * @return true if x-coordinate is in thumb range, false otherwise.
     */
    private boolean isInThumbRange(float touchX, double normalizedThumbValue) {
        return Math.abs(touchX - normalizedToScreen(normalizedThumbValue)) <= thumbHalfWidth;
    }

    /**
     * Sets normalized min value to value so that 0 <= value <= normalized max value <= 1. The View will get invalidated when calling this method.
     *
     * @param value The new normalized min value to set.
     */
    private void setNormalizedMinValue(double value) {
        normalizedMinValue = Math.max(0d, Math.min(1d, Math.min(value, normalizedMaxValue)));
        invalidate();
    }

    /**
     * Sets normalized max value to value so that 0 <= normalized min value <= value <= 1. The View will get invalidated when calling this method.
     *
     * @param value The new normalized max value to set.
     */
    private void setNormalizedMaxValue(double value) {
        normalizedMaxValue = Math.max(0d, Math.min(1d, Math.max(value, normalizedMinValue)));
        invalidate();
    }

    /**
     * Converts a normalized value to a Number object in the value space between absolute minimum and maximum.
     *
     * @param normalized
     * @return
     */
    @SuppressWarnings("unchecked")
    private T normalizedToValue(double normalized) {
        double v = absoluteMinValuePrim + normalized * (absoluteMaxValuePrim - absoluteMinValuePrim);
        // TODO parameterize this rounding to allow variable decimal points
        return (T) numberType.toNumber(Math.round(v * 100) / 100d);
    }

    /**
     * Converts the given Number value to a normalized double.
     *
     * @param value The Number value to normalize.
     * @return The normalized double.
     */
    private double valueToNormalized(T value) {
        if (0 == absoluteMaxValuePrim - absoluteMinValuePrim) {
            // prevent division by zero, simply return 0.
            return 0d;
        }
        return (value.doubleValue() - absoluteMinValuePrim) / (absoluteMaxValuePrim - absoluteMinValuePrim);
    }

    /**
     * Converts a normalized value into screen space.
     *
     * @param normalizedCoord The normalized value to convert.
     * @return The converted value in screen space.
     */
    private float normalizedToScreen(double normalizedCoord) {
        return (float) (padding + normalizedCoord * (getWidth() - 2 * padding));
    }

    /**
     * Converts screen space x-coordinates into normalized values.
     *
     * @param screenCoord The x-coordinate in screen space to convert.
     * @return The normalized value.
     */
    private double screenToNormalized(float screenCoord) {
        int width = getWidth();
        if (width <= 2 * padding) {
            // prevent division by zero, simply return 0.
            return 0d;
        } else {
            double result = (screenCoord - padding) / (width - 2 * padding);
            return Math.min(1d, Math.max(0d, result));
        }
    }

    /**
     * Callback listener interface to notify about changed range values.
     *
     * @param <T> The Number type the RangeSeekBar has been declared with.
     * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
     */
    public interface OnRangeSeekBarChangeListener<T> {

        void onRangeSeekBarValuesChanged(GHRangeSeekBar<?> bar, T minValue, T maxValue);
    }

    /**
     * Thumb constants (min and max).
     */
    private enum Thumb {
        MIN, MAX
    }

    /**
     * Utility enumeration used to convert between Numbers and doubles.
     *
     * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
     */
    private enum NumberType {
        LONG, DOUBLE, INTEGER, FLOAT, SHORT, BYTE, BIG_DECIMAL;

        public static <E extends Number> NumberType fromNumber(E value) throws IllegalArgumentException {
            if (value instanceof Long) {
                return LONG;
            }
            if (value instanceof Double) {
                return DOUBLE;
            }
            if (value instanceof Integer) {
                return INTEGER;
            }
            if (value instanceof Float) {
                return FLOAT;
            }
            if (value instanceof Short) {
                return SHORT;
            }
            if (value instanceof Byte) {
                return BYTE;
            }
            if (value instanceof BigDecimal) {
                return BIG_DECIMAL;
            }
            throw new IllegalArgumentException("Number class '" + value.getClass().getName() + "' is not supported");
        }

        public Number toNumber(double value) {
            switch (this) {
                case LONG:
                    return Long.valueOf((long) value);
                case DOUBLE:
                    return value;
                case INTEGER:
                    return Integer.valueOf((int) value);
                case FLOAT:
                    return Float.valueOf((float) value);
                case SHORT:
                    return Short.valueOf((short) value);
                case BYTE:
                    return Byte.valueOf((byte) value);
                case BIG_DECIMAL:
                    return BigDecimal.valueOf(value);
            }
            throw new InstantiationError("can't convert " + this + " to a Number object");
        }
    }


    private int dpToPx(Context context, int dp) {
        return Math.round(dp * getPixelScaleFactor(context));
    }

    private float getPixelScaleFactor(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT);
    }


    private void setValueChanges() {
        listener.onRangeSeekBarValuesChanged(this, getSelectedMinValue(mValueType), getSelectedMaxValue(mValueType));
    }

    private void setRangeBarArrayList() {
        int MAX = getMaxRange((Integer) getAbsoluteMaxValue());
        int i = getMinRange((Integer) getAbsoluteMinValue());
        if (rangeList == null)
            rangeList = new ArrayList<>();
        else
            rangeList.clear();

        do {
            rangeList.add(i);
            int length = String.valueOf(i).length();
            int step_min = (int) Math.pow(10, length - 1);

            int step = stepCalculation(i, step_min);

            i += step;
        }
        while (i <= MAX);

    }


    public static int getMinRange(int min) {
        if (min < 10)
            return min;
        int roundTo = (int) Math.pow(10, String.valueOf(min).length() - 1);
        return (int) Math.floor(min / roundTo) * roundTo;
    }

    public static int getMaxRange(int max) {
        int roundTo = (int) Math.pow(10, String.valueOf(max).length() - 1);
        return (int) Math.ceil((float) max / (float) roundTo) * roundTo;
    }

    private int stepCalculation(int i, int stepMin) {
        if (stepMin <= 1)
            return 1;
        else if (i >= stepMin && i < stepMin * 2)
            return stepMin / 10;
        else if (i >= stepMin * 2 && i < stepMin * 4)
            return (stepMin / 10) * 2;
        else
            return (stepMin / 10) * 5;

    }

    private int getClosestValue(int val) {
        int min = Math.abs(rangeList.get(0) - val);
        int closest = val;

        for (int i : rangeList) {
            final int diff = Math.abs(i - val);

            if (diff < min) {
                min = diff;
                closest = i;
            }
        }
        return closest;
    }

    /************************** imp methods for user ***********************/

    public void setLineHighlightedColor(int lineHighlightedColor) {
        this.lineHighlightedColor = lineHighlightedColor;
    }

    public void setLineBgColor(int lineBgColor) {
        this.lineBgColor = lineBgColor;
    }

    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
        mRect = new RectF(padding,
                thumbHalfHeight - lineHeight / 2,
                getWidth() - padding,
                thumbHalfHeight + lineHeight / 2);
    }

    public void setThumbImage(Bitmap thumbImage) throws NullPointerException {
        this.thumbImage = thumbImage;
        thumbWidth = thumbImage.getWidth();
        thumbHalfWidth = 0.5f * thumbWidth;
        thumbHalfHeight = 0.5f * thumbImage.getHeight();
        setLineHeight(lineHeight);
    }

    public void resetSelectedValues() {
        setSelectedMinValue(absoluteMinValue);
        setSelectedMaxValue(absoluteMaxValue);
    }

    public void setRangeValues(T minValue, T maxValue) {
        this.absoluteMinValue = minValue;
        this.absoluteMaxValue = maxValue;
        setValuePrimAndNumberType();
        setRangeBarArrayList();
    }

    public void setmValueType(ValueTypes mValueType) {
        this.mValueType = mValueType;
        resetSelectedValues();
        if(listener!=null)
            listener.onRangeSeekBarValuesChanged(this,getSelectedMinValue(mValueType),getSelectedMaxValue(mValueType));
    }

}