package com.sgv.calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.support.v4.content.res.ResourcesCompat;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.OverScroller;
import com.sgv.calendar.domain.Event;

import java.util.*;

import static com.sgv.calendar.CompactCalendarView.*;


class CompactCalendarController {

    public static final int IDLE = 0;
    public static final int EXPOSE_CALENDAR_ANIMATION = 1;
    public static final int EXPAND_COLLAPSE_CALENDAR = 2;
    public static final int ANIMATE_INDICATORS = 3;
    private static final int VELOCITY_UNIT_PIXELS_PER_SECOND = 1000;
    private static final int LAST_FLING_THRESHOLD_MILLIS = 300;
    private static final int DAYS_IN_WEEK = 7;
    private static final float SNAP_VELOCITY_DIP_PER_SECOND = 400;
    private static final float ANIMATION_SCREEN_SET_DURATION_MILLIS = 700;
    public int minYear = Calendar.getInstance().get(Calendar.YEAR);
    public int maxYear = 2050;
    //Line Color
    Paint rowLinePaint = new Paint();
    private int eventIndicatorStyle = SMALL_INDICATOR;
    private int currentDayIndicatorStyle = FILL_LARGE_INDICATOR;
    private int selectedDayIndicatorStyle = SMALL_INDICATOR;
    private int weekNamesBackgroundStyle = FILL;
    private int paddingWidth = 50;
    private int paddingHeight = 50;
    private int textHeight;
    private int textWidth;
    private int widthPerDay;
    private int monthsScrolledSoFar;
    private int heightPerDay;
    private int textSize = 13;
    private int textMonthNameSize = 16;
    private int textWeekNameSize = 14;
    private float eventIndicatiorRadius=2.0f;
    private int width;
    private int height;
    private int paddingRight;
    private int paddingLeft;
    private int maximumVelocity;
    private int densityAdjustedSnapVelocity;
    private int distanceThresholdForAutoScroll;
    private int targetHeight;
    private int animationStatus = 0;
    private int firstDayOfWeekToDraw = Calendar.MONDAY;
    private float xIndicatorOffset;
    private float multiDayIndicatorStrokeWidth;
    private float bigCircleIndicatorRadius;
    private float smallIndicatorRadius;
    private float growFactor = 0f;
    private float screenDensity = 1;
    private float growfactorIndicator;
    private float distanceX;
    private float circleScale = 0.85f;
    private long lastAutoScrollFromFling;
    private boolean useThreeLetterAbbreviation = true;
    private boolean isApplyWeekEndColor = false;
    private boolean isDisplayMonthName = false;
    private boolean isSmoothScrolling;
    private boolean isScrolling;
    private boolean shouldDrawDaysHeader = true;
    private boolean shouldDrawIndicatorsBelowSelectedDays = true;
    private boolean displayOtherMonthDays = false;
    private boolean shouldSelectFirstDayOfMonthOnScroll = true;
    private CompactCalendarViewListener listener;
    private VelocityTracker velocityTracker = null;
    private Direction currentDirection = Direction.NONE;
    private Date currentDate = new Date();
    private Locale locale;
    private Calendar currentCalender;
    private Calendar todayCalender;
    private Calendar calendarWithFirstDayOfMonth;
    private Calendar eventsCalendar;
    private EventsContainer eventsContainer;
    private PointF accumulatedScrollOffset = new PointF();
    private OverScroller scroller;
    private Paint dayPaint = new Paint();
    private Paint background = new Paint();
    private Rect textSizeRect;
    private String[] dayColumnNames;
    // colors
    private int multiEventIndicatorColor;
    private int currentDayBackgroundColor;
    private int currentDayTextColor;
    private int calenderTextColor = Color.BLACK;
    private int selectedDayBackgroundColor;
    private int selectedDayTextColor;
    private int monthNameTextColor = Color.BLACK;
    private int calenderBackgroundColor = Color.WHITE;
    private int otherMonthDaysTextColor;
    private int weekEndDaysTextColor;
    private int weekDaysTextColor;
    private int weekNamesBackgroundColor;
    private int rowLineColor;
    private TimeZone timeZone;
    private Context mContext;
    private boolean isShowRowLine = false;
    private boolean isEventOverlap=false;
    private Typeface typefaceDateText, typefaceMonthText, typefaceWeekText;
    /**
     * Only used in onDrawCurrentMonth to temporarily calculate previous month days
     */
    private Calendar tempPreviousMonthCalendar;
    private Date userSelectedDate;
    private Date tempUserSelectedDate;


    CompactCalendarController(Paint dayPaint, OverScroller scroller, Rect textSizeRect, AttributeSet attrs,
                              Context context, int currentDayBackgroundColor, int calenderTextColor,
                              int currentSelectedDayBackgroundColor, VelocityTracker velocityTracker,
                              int multiEventIndicatorColor, EventsContainer eventsContainer,
                              Locale locale, TimeZone timeZone) {
        this.dayPaint = dayPaint;
        this.scroller = scroller;
        this.textSizeRect = textSizeRect;
        this.currentDayBackgroundColor = currentDayBackgroundColor;
        this.calenderTextColor = calenderTextColor;
        this.selectedDayBackgroundColor = currentSelectedDayBackgroundColor;
        this.otherMonthDaysTextColor = calenderTextColor;
        this.velocityTracker = velocityTracker;
        this.multiEventIndicatorColor = multiEventIndicatorColor;
        this.eventsContainer = eventsContainer;
        this.locale = locale;
        this.timeZone = timeZone;
        this.displayOtherMonthDays = false;
        this.mContext = context;
        loadAttributes(attrs, context);
        init(context);
    }

    private void loadAttributes(AttributeSet attrs, Context context) {
        if (attrs != null && context != null) {
            TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CompactCalendarView, 0, 0);
            try {
                currentDayBackgroundColor = typedArray.getColor(R.styleable.CompactCalendarView_compactCalendarCurrentDayBackgroundColor, currentDayBackgroundColor);
                calenderTextColor = typedArray.getColor(R.styleable.CompactCalendarView_compactCalendarTextColor, calenderTextColor);
                weekEndDaysTextColor = typedArray.getColor(R.styleable.CompactCalendarView_compactCalendarWeekEndDaysTextColor, calenderTextColor);

                weekDaysTextColor = typedArray.getColor(R.styleable.CompactCalendarView_compactCalendarWeekDaysTextColor, calenderTextColor);

                minYear = typedArray.getInt(R.styleable.CompactCalendarView_compactCalendarMinYear, minYear);
                maxYear = typedArray.getInt(R.styleable.CompactCalendarView_compactCalendarMaxYear, maxYear);
                weekNamesBackgroundColor = typedArray.getColor(R.styleable.CompactCalendarView_compactCalendarWeekNamesBackgroundColor, currentDayBackgroundColor);
                rowLineColor = typedArray.getColor(R.styleable.CompactCalendarView_compactCalendarRowLineColor, multiEventIndicatorColor);
                currentDayTextColor = typedArray.getColor(R.styleable.CompactCalendarView_compactCalendarCurrentDayTextColor, calenderTextColor);
                otherMonthDaysTextColor = typedArray.getColor(R.styleable.CompactCalendarView_compactCalendarOtherMonthDaysTextColor, otherMonthDaysTextColor);
                selectedDayBackgroundColor = typedArray.getColor(R.styleable.CompactCalendarView_compactCalendarSelectedDayBackgroundColor, selectedDayBackgroundColor);
                selectedDayTextColor = typedArray.getColor(R.styleable.CompactCalendarView_compactCalendarSelectedDayTextColor, calenderTextColor);
                calenderBackgroundColor = typedArray.getColor(R.styleable.CompactCalendarView_compactCalendarBackgroundColor, calenderBackgroundColor);
                multiEventIndicatorColor = typedArray.getColor(R.styleable.CompactCalendarView_compactCalendarMultiEventIndicatorColor, multiEventIndicatorColor);
                textSize = typedArray.getDimensionPixelSize(R.styleable.CompactCalendarView_compactCalendarDateTextSize,
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textSize, context.getResources().getDisplayMetrics()));

                textWeekNameSize = typedArray.getDimensionPixelSize(R.styleable.CompactCalendarView_compactCalendarWeekTextSize,
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textWeekNameSize, context.getResources().getDisplayMetrics()));

                textMonthNameSize = typedArray.getDimensionPixelSize(R.styleable.CompactCalendarView_compactCalendarMonthTextSize,
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textMonthNameSize, context.getResources().getDisplayMetrics()));


                targetHeight = typedArray.getDimensionPixelSize(R.styleable.CompactCalendarView_compactCalendarTargetHeight,
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, targetHeight, context.getResources().getDisplayMetrics()));
                eventIndicatorStyle = typedArray.getInt(R.styleable.CompactCalendarView_compactCalendarEventIndicatorStyle, SMALL_INDICATOR);
                currentDayIndicatorStyle = typedArray.getInt(R.styleable.CompactCalendarView_compactCalendarCurrentDayIndicatorStyle, FILL_LARGE_INDICATOR);
                selectedDayIndicatorStyle = typedArray.getInt(R.styleable.CompactCalendarView_compactCalendarSelectedDayIndicatorStyle, FILL_LARGE_INDICATOR);

                weekNamesBackgroundStyle = typedArray.getInt(R.styleable.CompactCalendarView_compactCalendarWeekBackgroundStyle, FILL);
                displayOtherMonthDays = typedArray.getBoolean(R.styleable.CompactCalendarView_compactCalendarDisplayOtherMonthDays, displayOtherMonthDays);
                shouldSelectFirstDayOfMonthOnScroll = typedArray.getBoolean(R.styleable.CompactCalendarView_compactCalendarShouldSelectFirstDayOfMonthOnScroll, shouldSelectFirstDayOfMonthOnScroll);
                circleScale = typedArray.getFloat(R.styleable.CompactCalendarView_compactCalendarCircleScale, circleScale);
                isShowRowLine = typedArray.getBoolean(R.styleable.CompactCalendarView_compactCalendarDrawRowLine, false);
                isApplyWeekEndColor = typedArray.getBoolean(R.styleable.CompactCalendarView_compactCalendarApplyWeekEndColor, false);
                isDisplayMonthName = typedArray.getBoolean(R.styleable.CompactCalendarView_compactCalendarDisplayMonthName, false);
                monthNameTextColor = typedArray.getColor(R.styleable.CompactCalendarView_compactCalendarMonthNameTextColor, calenderTextColor);
                eventIndicatiorRadius =typedArray.getFloat(R.styleable.CompactCalendarView_compactCalendarEventCircleRadius,eventIndicatiorRadius);
                isEventOverlap=typedArray.getBoolean(R.styleable.CompactCalendarView_compactCalendarEventCircleOverlap, false);
            } finally {
                typedArray.recycle();
            }
        }
    }

    private void init(Context context) {
        currentCalender = Calendar.getInstance(timeZone, locale);
        todayCalender = Calendar.getInstance(timeZone, locale);
        calendarWithFirstDayOfMonth = Calendar.getInstance(timeZone, locale);
        eventsCalendar = Calendar.getInstance(timeZone, locale);
        tempPreviousMonthCalendar = Calendar.getInstance(timeZone, locale);

        // make setMinimalDaysInFirstWeek same across android versions
        eventsCalendar.setMinimalDaysInFirstWeek(1);
        calendarWithFirstDayOfMonth.setMinimalDaysInFirstWeek(1);
        todayCalender.setMinimalDaysInFirstWeek(1);
        currentCalender.setMinimalDaysInFirstWeek(1);
        tempPreviousMonthCalendar.setMinimalDaysInFirstWeek(1);

        setFirstDayOfWeek(firstDayOfWeekToDraw);

        setUseWeekDayAbbreviation(false);
        dayPaint.setTextAlign(Paint.Align.CENTER);
        dayPaint.setStyle(Paint.Style.STROKE);
        dayPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        typefaceDateText = ResourcesCompat.getFont(context, R.font.montserrat_regular);
        typefaceWeekText = ResourcesCompat.getFont(context, R.font.montserrat_medium);
        typefaceMonthText = ResourcesCompat.getFont(context, R.font.montserrat_medium);
        dayPaint.setTypeface(typefaceDateText);
        dayPaint.setTextSize(textSize);
        dayPaint.setColor(calenderTextColor);
        dayPaint.getTextBounds("31", 0, "31".length(), textSizeRect);
        textHeight = textSizeRect.height() * 3;
        textWidth = textSizeRect.width() * 2;

        todayCalender.setTime(new Date());
        setToMidnight(todayCalender);

        currentCalender.setTime(currentDate);
        setCalenderToFirstDayOfMonth(calendarWithFirstDayOfMonth, currentDate, -monthsScrolledSoFar, 0);

        initScreenDensityRelatedValues(context);

        xIndicatorOffset = 2.5f * screenDensity;

        //scale small indicator by screen density
        smallIndicatorRadius = eventIndicatiorRadius * screenDensity;

        //just set a default growFactor to draw full calendar when initialised
        growFactor = Integer.MAX_VALUE;

        //Line Pan=int
        rowLinePaint.setColor(rowLineColor);
        rowLinePaint.setStrokeWidth(3);


    }

    private void initScreenDensityRelatedValues(Context context) {
        if (context != null) {
            screenDensity = context.getResources().getDisplayMetrics().density;
            final ViewConfiguration configuration = ViewConfiguration
                    .get(context);
            densityAdjustedSnapVelocity = (int) (screenDensity * SNAP_VELOCITY_DIP_PER_SECOND);
            maximumVelocity = configuration.getScaledMaximumFlingVelocity();

            final DisplayMetrics dm = context.getResources().getDisplayMetrics();
            multiDayIndicatorStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, dm);
        }
    }

    private void setCalenderToFirstDayOfMonth(Calendar calendarWithFirstDayOfMonth, Date currentDate, int scrollOffset, int monthOffset) {
        setMonthOffset(calendarWithFirstDayOfMonth, currentDate, scrollOffset, monthOffset);
        calendarWithFirstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);
    }

    private void setMonthOffset(Calendar calendarWithFirstDayOfMonth, Date currentDate, int scrollOffset, int monthOffset) {
        calendarWithFirstDayOfMonth.setTime(currentDate);
        calendarWithFirstDayOfMonth.add(Calendar.MONTH, scrollOffset + monthOffset);
        calendarWithFirstDayOfMonth.set(Calendar.HOUR_OF_DAY, 0);
        calendarWithFirstDayOfMonth.set(Calendar.MINUTE, 0);
        calendarWithFirstDayOfMonth.set(Calendar.SECOND, 0);
        calendarWithFirstDayOfMonth.set(Calendar.MILLISECOND, 0);
    }

    void setShouldSelectFirstDayOfMonthOnScroll(boolean shouldSelectFirstDayOfMonthOnScroll) {
        this.shouldSelectFirstDayOfMonthOnScroll = shouldSelectFirstDayOfMonthOnScroll;

    }

    void setDisplayOtherMonthDays(boolean displayOtherMonthDays) {
        this.displayOtherMonthDays = displayOtherMonthDays;
    }

    void shouldDrawIndicatorsBelowSelectedDays(boolean shouldDrawIndicatorsBelowSelectedDays) {
        this.shouldDrawIndicatorsBelowSelectedDays = shouldDrawIndicatorsBelowSelectedDays;
    }

    void setCurrentDayIndicatorStyle(int currentDayIndicatorStyle) {
        this.currentDayIndicatorStyle = currentDayIndicatorStyle;
    }

    void setEventIndicatorStyle(int eventIndicatorStyle) {
        this.eventIndicatorStyle = eventIndicatorStyle;
    }

    void setSelectedDayIndicatorStyle(int currentSelectedDayIndicatorStyle) {
        this.selectedDayIndicatorStyle = currentSelectedDayIndicatorStyle;
    }

    float getScreenDensity() {
        return screenDensity;
    }

    float getDayIndicatorRadius() {
        return bigCircleIndicatorRadius;
    }

    float getGrowFactorIndicator() {
        return growfactorIndicator;
    }

    void setGrowFactorIndicator(float growfactorIndicator) {
        this.growfactorIndicator = growfactorIndicator;
    }

    void setAnimationStatus(int animationStatus) {
        this.animationStatus = animationStatus;
    }

    int getTargetHeight() {
        return targetHeight;
    }

    void setTargetHeight(int targetHeight) {
        this.targetHeight = targetHeight;
    }

    int getWidth() {
        return width;
    }

    void setListener(CompactCalendarViewListener listener) {
        this.listener = listener;
    }

    void removeAllEvents() {
        eventsContainer.removeAllEvents();
    }

    void setFirstDayOfWeek(int day) {
        if (day < 1 || day > 7) {
            throw new IllegalArgumentException("Day must be an int between 1 and 7 or DAY_OF_WEEK from Java Calendar class. For more information please see Calendar.DAY_OF_WEEK.");
        }
        this.firstDayOfWeekToDraw = day;
        setUseWeekDayAbbreviation(useThreeLetterAbbreviation);
        eventsCalendar.setFirstDayOfWeek(day);
        calendarWithFirstDayOfMonth.setFirstDayOfWeek(day);
        todayCalender.setFirstDayOfWeek(day);
        currentCalender.setFirstDayOfWeek(day);
        tempPreviousMonthCalendar.setFirstDayOfWeek(day);
    }

    void setCurrentSelectedDayBackgroundColor(int currentSelectedDayBackgroundColor) {
        this.selectedDayBackgroundColor = currentSelectedDayBackgroundColor;
    }

    void setCurrentSelectedDayTextColor(int currentSelectedDayTextColor) {
        this.selectedDayTextColor = currentSelectedDayTextColor;
    }

    void setCalenderBackgroundColor(int calenderBackgroundColor) {
        this.calenderBackgroundColor = calenderBackgroundColor;
    }

    void setCurrentDayBackgroundColor(int currentDayBackgroundColor) {
        this.currentDayBackgroundColor = currentDayBackgroundColor;
    }

    void setCurrentDayTextColor(int currentDayTextColor) {
        this.currentDayTextColor = currentDayTextColor;
    }

    void showNextMonth() {
        handleClickHorizontalScrolling(-3000);
    }

    void showPreviousMonth() {
        handleClickHorizontalScrolling(3000);
    }

    void setLocale(TimeZone timeZone, Locale locale) {
        if (locale == null) {
            throw new IllegalArgumentException("Locale cannot be null.");
        }
        if (timeZone == null) {
            throw new IllegalArgumentException("TimeZone cannot be null.");
        }
        this.locale = locale;
        this.timeZone = timeZone;
        this.eventsContainer = new EventsContainer(Calendar.getInstance(this.timeZone, this.locale));
        // passing null will not re-init density related values - and that's ok
        init(null);
    }

    void setUseWeekDayAbbreviation(boolean useThreeLetterAbbreviation) {
        this.useThreeLetterAbbreviation = useThreeLetterAbbreviation;
        this.dayColumnNames = WeekUtils.getWeekdayNames(locale, firstDayOfWeekToDraw, this.useThreeLetterAbbreviation);
    }

    void setDayColumnNames(String[] dayColumnNames) {
        if (dayColumnNames == null || dayColumnNames.length != 7) {
            throw new IllegalArgumentException("Column names cannot be null and must contain a value for each day of the week");
        }
        this.dayColumnNames = dayColumnNames;
    }

    void setShouldDrawDaysHeader(boolean shouldDrawDaysHeader) {
        this.shouldDrawDaysHeader = shouldDrawDaysHeader;
    }

    void onMeasure(int width, int height, int paddingRight, int paddingLeft) {
        widthPerDay = (width) / DAYS_IN_WEEK;
        heightPerDay = targetHeight > 0 ? targetHeight / 7 : height / 7;
        this.width = width;
        this.distanceThresholdForAutoScroll = (int) (width * 0.50);
        this.height = height;
        this.paddingRight = paddingRight;
        this.paddingLeft = paddingLeft;

        //makes easier to find radius
        bigCircleIndicatorRadius = getInterpolatedBigCircleIndicator();

        // scale the selected day indicators slightly so that event indicators can be drawn below
        bigCircleIndicatorRadius = shouldDrawIndicatorsBelowSelectedDays && eventIndicatorStyle == SMALL_INDICATOR ? bigCircleIndicatorRadius * 0.85f : bigCircleIndicatorRadius;
    }

    //assume square around each day of width and height = heightPerDay and get diagonal line length
    //interpolate height and radius
    //https://en.wikipedia.org/wiki/Linear_interpolation
    private float getInterpolatedBigCircleIndicator() {
        float x0 = textSizeRect.height();
        float x1 = heightPerDay; // take into account indicator offset
        float x = (x1 + textSizeRect.height()) / 2f; // pick a point which is almost half way through heightPerDay and textSizeRect
        double y1 = 0.5 * Math.sqrt((x1 * x1) + (x1 * x1));
        double y0 = 0.5 * Math.sqrt((x0 * x0) + (x0 * x0));

        return (float) (y0 + ((y1 - y0) * ((x - x0) / (x1 - x0))));
    }

    void onDraw(Canvas canvas) {
        paddingWidth = widthPerDay / 2;
        paddingHeight = heightPerDay / 2;

        calculateXPositionOffset();

        if (animationStatus == EXPOSE_CALENDAR_ANIMATION) {
            drawCalendarWhileAnimating(canvas);
        } else if (animationStatus == ANIMATE_INDICATORS) {
            drawCalendarWhileAnimatingIndicators(canvas);
        } else {
            drawCalenderBackground(canvas);
            drawCalenderWeekNamesBackground(canvas);
            drawScrollableCalender(canvas);
        }
    }

    private void drawCalendarWhileAnimatingIndicators(Canvas canvas) {
        dayPaint.setColor(calenderBackgroundColor);
        dayPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0, 0, growFactor, dayPaint);
        dayPaint.setStyle(Paint.Style.STROKE);
        dayPaint.setColor(Color.WHITE);
        drawScrollableCalender(canvas);
    }

    private void drawCalendarWhileAnimating(Canvas canvas) {
        background.setColor(calenderBackgroundColor);
        background.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0, 0, growFactor, background);
        dayPaint.setStyle(Paint.Style.STROKE);
        dayPaint.setColor(Color.WHITE);
        drawScrollableCalender(canvas);
    }

    void onSingleTapUp(MotionEvent e) {
        // Don't handle single tap when calendar is scrolling and is not stationary
        if (isScrolling()) {
            return;
        }

        int dayColumn = Math.round((paddingLeft + e.getX() - paddingWidth - paddingRight) / widthPerDay);
        int dayRow = Math.round((e.getY() - paddingHeight) / heightPerDay);

        setCalenderToFirstDayOfMonth(calendarWithFirstDayOfMonth, currentDate, -monthsScrolledSoFar, 0);

        int firstDayOfMonth = getDayOfWeek(calendarWithFirstDayOfMonth);

        int dayOfMonth = ((dayRow - 1) * 7 + dayColumn) - firstDayOfMonth;

        if (dayOfMonth < calendarWithFirstDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
                && dayOfMonth >= 0) {
            calendarWithFirstDayOfMonth.add(Calendar.DATE, dayOfMonth);
            currentCalender.setTimeInMillis(calendarWithFirstDayOfMonth.getTimeInMillis());
            performOnDayClickCallback(currentCalender.getTime());
            if (todayCalender.get(Calendar.MONTH) == currentCalender.get(Calendar.MONTH)) {
                if (currentCalender.get(Calendar.YEAR) == todayCalender.get(Calendar.YEAR)) {
                    userSelectedDate = currentCalender.getTime();
                }
            }

        }
    }

    // Add a little leeway buy checking if amount scrolled is almost same as expected scroll
    // as it maybe off by a few pixels
    private boolean isScrolling() {
        float scrolledX = Math.abs(accumulatedScrollOffset.x);
        int expectedScrollX = Math.abs(width * monthsScrolledSoFar);
        return scrolledX < expectedScrollX - 5 || scrolledX > expectedScrollX + 5;
    }

    private void performOnDayClickCallback(Date date) {
        if (listener != null) {
            listener.onDayClick(date);
        }
    }

    public Date getSelectedDate() {
        return currentCalender.getTime();
    }

    public void setSelectedDate(int dayOfMonth) {
        calendarWithFirstDayOfMonth.add(Calendar.DATE, dayOfMonth);
        currentCalender.setTimeInMillis(calendarWithFirstDayOfMonth.getTimeInMillis());
    }

    boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        //ignore scrolling callback if already smooth scrolling
        if (isSmoothScrolling) {
            return true;
        }

        if (currentDirection == Direction.NONE) {
            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                currentDirection = Direction.HORIZONTAL;
            } else {
                currentDirection = Direction.VERTICAL;
            }
        }

        isScrolling = true;
        this.distanceX = distanceX;
        return true;
    }

    boolean onTouch(MotionEvent event) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }

        velocityTracker.addMovement(event);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            if (!scroller.isFinished()) {
                scroller.abortAnimation();
            }
            isSmoothScrolling = false;

        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            velocityTracker.addMovement(event);
            velocityTracker.computeCurrentVelocity(500);

        } else if (event.getAction() == MotionEvent.ACTION_UP) {

            handleHorizontalScrolling();
            velocityTracker.recycle();
            velocityTracker.clear();
            velocityTracker = null;
            isScrolling = false;
        }
        return false;
    }

    private void snapBackScroller() {
        float remainingScrollAfterFingerLifted1 = (accumulatedScrollOffset.x - (monthsScrolledSoFar * width));
        scroller.startScroll((int) accumulatedScrollOffset.x, 0, (int) -remainingScrollAfterFingerLifted1, 0);
    }

    private void handleHorizontalScrolling() {

        int velocityX = computeVelocity();
        tempUserSelectedDate = null;
        handleSmoothScrolling(velocityX);
        currentDirection = Direction.NONE;
        setCalenderToFirstDayOfMonth(calendarWithFirstDayOfMonth, currentDate, -monthsScrolledSoFar, 0);
        if (calendarWithFirstDayOfMonth.get(Calendar.MONTH) != currentCalender.get(Calendar.MONTH) && shouldSelectFirstDayOfMonthOnScroll) {
            setCalenderToFirstDayOfMonth(currentCalender, currentDate, -monthsScrolledSoFar, 0);
        }

    }


    private void handleClickHorizontalScrolling(int velocityX) {

        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker.clear();
            velocityTracker = null;
            isScrolling = false;
        }
        tempUserSelectedDate = null;

        handleSmoothScrolling(velocityX);
        currentDirection = Direction.NONE;
        setCalenderToFirstDayOfMonth(calendarWithFirstDayOfMonth, currentDate, -monthsScrolledSoFar, 0);
        if (calendarWithFirstDayOfMonth.get(Calendar.MONTH) != currentCalender.get(Calendar.MONTH) && shouldSelectFirstDayOfMonthOnScroll) {
            setCalenderToFirstDayOfMonth(currentCalender, currentDate, -monthsScrolledSoFar, 0);
        }

    }


    private boolean isCurrentMonthDaySelector() {
        if (isNotCurrentMonth()) {
            return false;
        } else {
            if (userSelectedDate == null) {
                return true;
            } else {
                if (tempUserSelectedDate == null || tempUserSelectedDate != userSelectedDate) {
                    int selectedDay = Integer.parseInt(DateFormat.format("dd", userSelectedDate).toString()); // 20
                    setSelectedDate(selectedDay - 1);
                    tempUserSelectedDate = userSelectedDate;
                }
                return false;
            }
        }
    }


    private boolean isNotCurrentMonth() {
        if (todayCalender.get(Calendar.MONTH) == calendarWithFirstDayOfMonth.get(Calendar.MONTH)) {
            if (calendarWithFirstDayOfMonth.get(Calendar.YEAR) == todayCalender.get(Calendar.YEAR)) {
                return false;
            }
        }
        return true;
    }


    private int computeVelocity() {

        velocityTracker.computeCurrentVelocity(VELOCITY_UNIT_PIXELS_PER_SECOND, maximumVelocity);
        return (int) velocityTracker.getXVelocity();
    }


    private int computeButtonVelocity(VelocityTracker velocityPreviousTracker) {

        velocityPreviousTracker.computeCurrentVelocity(VELOCITY_UNIT_PIXELS_PER_SECOND, maximumVelocity);
        return (int) velocityPreviousTracker.getXVelocity();
    }

    private void handleSmoothScrolling(int velocityX) {

        int distanceScrolled = (int) (accumulatedScrollOffset.x - (width * monthsScrolledSoFar));
        boolean isEnoughTimeElapsedSinceLastSmoothScroll = System.currentTimeMillis() - lastAutoScrollFromFling > LAST_FLING_THRESHOLD_MILLIS;
        if (velocityX > densityAdjustedSnapVelocity && isEnoughTimeElapsedSinceLastSmoothScroll) {
            scrollPreviousMonth();
        } else if (velocityX < -densityAdjustedSnapVelocity && isEnoughTimeElapsedSinceLastSmoothScroll) {
            scrollNextMonth();
        } else if (isScrolling && distanceScrolled > distanceThresholdForAutoScroll) {
            scrollPreviousMonth();
        } else if (isScrolling && distanceScrolled < -distanceThresholdForAutoScroll) {
            scrollNextMonth();
        } else {
            isSmoothScrolling = false;
            snapBackScroller();
        }
    }

    public void scrollNextMonth() {

        lastAutoScrollFromFling = System.currentTimeMillis();
        monthsScrolledSoFar = monthsScrolledSoFar - 1;
        performScroll();
        isSmoothScrolling = true;
        performMonthScrollCallback();
    }

    public void scrollPreviousMonth() {

        lastAutoScrollFromFling = System.currentTimeMillis();
        monthsScrolledSoFar = monthsScrolledSoFar + 1;
        performScroll();
        isSmoothScrolling = true;
        performMonthScrollCallback();
    }


    public void scrollClickNextMonth() {

        lastAutoScrollFromFling = System.currentTimeMillis();
        monthsScrolledSoFar = monthsScrolledSoFar - 1;
        performScroll();
        isSmoothScrolling = true;

        if (shouldSelectFirstDayOfMonthOnScroll) {
            setCalenderToFirstDayOfMonth(calendarWithFirstDayOfMonth, currentCalender.getTime(), 0, 1);
            setCurrentDate(calendarWithFirstDayOfMonth.getTime());
        }


        performMonthScrollCallback();
    }

    public void scrollClickPreviousMonth() {
        lastAutoScrollFromFling = System.currentTimeMillis();
        monthsScrolledSoFar = monthsScrolledSoFar + 1;
        performScroll();
        isSmoothScrolling = true;
        if (shouldSelectFirstDayOfMonthOnScroll) {
            setCalenderToFirstDayOfMonth(calendarWithFirstDayOfMonth, currentCalender.getTime(), 0, -1);
            setCurrentDate(calendarWithFirstDayOfMonth.getTime());
        }

        performMonthScrollCallback();
    }


    private void performMonthScrollCallback() {
        if (listener != null) {
            listener.onMonthScroll(getFirstDayOfCurrentMonth());
        }
    }

    private void performScroll() {
        int targetScroll = monthsScrolledSoFar * width;
        float remainingScrollAfterFingerLifted = targetScroll - accumulatedScrollOffset.x;
        scroller.startScroll((int) accumulatedScrollOffset.x, 0, (int) (remainingScrollAfterFingerLifted), 0,
                (int) (Math.abs((int) (remainingScrollAfterFingerLifted)) / (float) width * ANIMATION_SCREEN_SET_DURATION_MILLIS));
    }

    int getHeightPerDay() {
        return heightPerDay;
    }

    int getWeekNumberForCurrentMonth() {
        Calendar calendar = Calendar.getInstance(timeZone, locale);
        calendar.setTime(currentDate);
        return calendar.get(Calendar.WEEK_OF_MONTH);
    }

    Date getFirstDayOfCurrentMonth() {
        Calendar calendar = Calendar.getInstance(timeZone, locale);
        calendar.setTime(currentDate);
        calendar.add(Calendar.MONTH, -monthsScrolledSoFar);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        setToMidnight(calendar);
        return calendar.getTime();
    }

    Calendar getCalendarOfCurrentMonth() {
        Calendar calendar = Calendar.getInstance(timeZone, locale);
        calendar.setTime(currentDate);
        calendar.add(Calendar.MONTH, -monthsScrolledSoFar);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        setToMidnight(calendar);
        return calendar;
    }

    private HashSet<Integer> getWeekEndsFormMonthYear() {
        HashSet<Integer> hs = new HashSet<>();
        Calendar cal = getCalendarOfCurrentMonth();
        int month = cal.get(Calendar.MONTH);
        do {
            // get the day of the week for the current day
            int day = cal.get(Calendar.DAY_OF_WEEK);
            // check if it is a Saturday or Sunday
            if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
                hs.add(cal.get(Calendar.DAY_OF_MONTH));
                // print the day - but you could add them to a list or whatever
            }
            // advance to the next day
            cal.add(Calendar.DAY_OF_YEAR, 1);
        } while (cal.get(Calendar.MONTH) == month);

        return hs;
    }

    void setCurrentDate(Date dateTimeMonth) {
        distanceX = 0;
        monthsScrolledSoFar = 0;
        accumulatedScrollOffset.x = 0;
        scroller.startScroll(0, 0, 0, 0);
        currentDate = new Date(dateTimeMonth.getTime());
        currentCalender.setTime(currentDate);
        todayCalender = Calendar.getInstance(timeZone, locale);
        setToMidnight(currentCalender);
    }

    void setTodaySeverDate(Date dateTimeMonth) {
        userSelectedDate = null;
        todayCalender = Calendar.getInstance(timeZone, locale);
        todayCalender.setTime(dateTimeMonth);
        setToMidnight(todayCalender);
    }

    private void setToMidnight(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    void addEvent(Event event) {
        eventsContainer.addEvent(event);
    }

    void addEvents(List<Event> events) {
        eventsContainer.addEvents(events);
    }

    List<Event> getCalendarEventsFor(long epochMillis) {
        return eventsContainer.getEventsFor(epochMillis);
    }

    List<Event> getCalendarEventsForMonth(long epochMillis) {
        return eventsContainer.getEventsForMonth(epochMillis);
    }

    void removeEventsFor(long epochMillis) {
        eventsContainer.removeEventByEpochMillis(epochMillis);
    }

    void removeEvent(Event event) {
        eventsContainer.removeEvent(event);
    }

    void removeEvents(List<Event> events) {
        eventsContainer.removeEvents(events);
    }

    void setGrowProgress(float grow) {
        growFactor = grow;
    }

    float getGrowFactor() {
        return growFactor;
    }

    boolean onDown(MotionEvent e) {
        scroller.forceFinished(true);
        return true;
    }

    boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        scroller.forceFinished(true);
        return true;
    }

    boolean computeScroll() {
        if (scroller.computeScrollOffset()) {
            accumulatedScrollOffset.x = scroller.getCurrX();
            return true;
        }
        return false;
    }

    private void drawScrollableCalender(Canvas canvas) {
        drawPreviousMonth(canvas);

        drawCurrentMonth(canvas);

        drawNextMonth(canvas);

    }

    private void drawNextMonth(Canvas canvas) {
        setCalenderToFirstDayOfMonth(calendarWithFirstDayOfMonth, currentDate, -monthsScrolledSoFar, 1);
        drawMonth(canvas, calendarWithFirstDayOfMonth, (width * (-monthsScrolledSoFar + 1)));
    }

    private void drawCurrentMonth(Canvas canvas) {
        setCalenderToFirstDayOfMonth(calendarWithFirstDayOfMonth, currentDate, -monthsScrolledSoFar, 0);
        drawMonth(canvas, calendarWithFirstDayOfMonth, width * -monthsScrolledSoFar);
    }

    private void drawPreviousMonth(Canvas canvas) {

        setCalenderToFirstDayOfMonth(calendarWithFirstDayOfMonth, currentDate, -monthsScrolledSoFar, -1);
        drawMonth(canvas, calendarWithFirstDayOfMonth, (width * (-monthsScrolledSoFar - 1)));
    }

    private void calculateXPositionOffset() {
        if (currentDirection == Direction.HORIZONTAL) {
            accumulatedScrollOffset.x -= distanceX;
        }
    }

    private void drawCalenderWeekNamesBackground(Canvas canvas) {
        Paint weekBgPaint = dayPaint;
        weekBgPaint.setColor(weekNamesBackgroundColor);
        if (weekNamesBackgroundStyle == 1) {
            weekBgPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, 0, width, getHeightPerDay() - paddingHeight / 2, weekBgPaint);
        } else {
            weekBgPaint.setStrokeWidth(2.4f);
            weekBgPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(10, 10, width - 10, (getHeightPerDay() - paddingHeight / 2) - 10, weekBgPaint);
        }

        //canvas.drawRect(, 3, width - 15, getHeightPerDay() - 25, weekBgPaint);

    }

    private void drawCalenderBackground(Canvas canvas) {
        dayPaint.setColor(calenderBackgroundColor);
        dayPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, width, height, dayPaint);
        dayPaint.setStyle(Paint.Style.STROKE);
        dayPaint.setColor(calenderTextColor);
    }

    void drawEvents(Canvas canvas, Calendar currentMonthToDrawCalender, int offset) {
        int currentMonth = currentMonthToDrawCalender.get(Calendar.MONTH);
        List<Events> uniqEvents = eventsContainer.getEventsForMonthAndYear(currentMonth, currentMonthToDrawCalender.get(Calendar.YEAR));

        boolean shouldDrawCurrentDayCircle = currentMonth == todayCalender.get(Calendar.MONTH);
        boolean shouldDrawSelectedDayCircle = currentMonth == currentCalender.get(Calendar.MONTH);

        int todayDayOfMonth = todayCalender.get(Calendar.DAY_OF_MONTH);
        int currentYear = todayCalender.get(Calendar.YEAR);
        int selectedDayOfMonth = currentCalender.get(Calendar.DAY_OF_MONTH);
        float indicatorOffset = bigCircleIndicatorRadius / 2;
        if (uniqEvents != null) {
            for (int i = 0; i < uniqEvents.size(); i++) {
                Events events = uniqEvents.get(i);
                long timeMillis = events.getTimeInMillis();
                eventsCalendar.setTimeInMillis(timeMillis);

                int dayOfWeek = getDayOfWeek(eventsCalendar);

                int weekNumberForMonth = eventsCalendar.get(Calendar.WEEK_OF_MONTH);
                float xPosition = widthPerDay * dayOfWeek + paddingWidth + paddingLeft + accumulatedScrollOffset.x + offset - paddingRight;
                float yPosition = weekNumberForMonth * heightPerDay + paddingHeight;

                if (((animationStatus == EXPOSE_CALENDAR_ANIMATION || animationStatus == ANIMATE_INDICATORS) && xPosition >= growFactor) || yPosition >= growFactor) {
                    // only draw small event indicators if enough of the calendar is exposed
                    continue;
                } else if (animationStatus == EXPAND_COLLAPSE_CALENDAR && yPosition >= growFactor) {
                    // expanding animation, just draw event indicators if enough of the calendar is visible
                    continue;
                } else if (animationStatus == EXPOSE_CALENDAR_ANIMATION && (eventIndicatorStyle == FILL_LARGE_INDICATOR || eventIndicatorStyle == NO_FILL_LARGE_INDICATOR)) {
                    // Don't draw large indicators during expose animation, until animation is done
                    continue;
                }

                List<Event> eventsList = events.getEvents();
                int dayOfMonth = eventsCalendar.get(Calendar.DAY_OF_MONTH);
                int eventYear = eventsCalendar.get(Calendar.YEAR);
                boolean isSameDayAsCurrentDay = shouldDrawCurrentDayCircle && (todayDayOfMonth == dayOfMonth) && (eventYear == currentYear);
                boolean isCurrentSelectedDay = shouldDrawSelectedDayCircle && (selectedDayOfMonth == dayOfMonth);

                if (shouldDrawIndicatorsBelowSelectedDays || (!shouldDrawIndicatorsBelowSelectedDays && !isSameDayAsCurrentDay && !isCurrentSelectedDay) || animationStatus == EXPOSE_CALENDAR_ANIMATION) {
                    if (eventIndicatorStyle == FILL_LARGE_INDICATOR || eventIndicatorStyle == NO_FILL_LARGE_INDICATOR) {
                        Event event = eventsList.get(0);
                        drawEventIndicatorCircle(canvas, xPosition, yPosition, event.getColor());
                    } else {
                        yPosition += indicatorOffset - 5;
                        // offset event indicators to draw below selected day indicators
                        // this makes sure that they do no overlap
                        if (shouldDrawIndicatorsBelowSelectedDays && (isSameDayAsCurrentDay || isCurrentSelectedDay)) {
                            //change 2
                            //yPosition += indicatorOffset;
                        }

                        switch (eventsList.size()){
                            case 1:
                                drawSingleEvent(canvas, xPosition, yPosition, eventsList);
                            break;
                            case 2:
                                drawTwoEvents(canvas, xPosition, yPosition, eventsList);
                            break;

                            default:
                                drawEvents(canvas, xPosition, yPosition, eventsList);
                        }

                    }
                }
            }
        }
    }

    private void drawSingleEvent(Canvas canvas, float xPosition, float yPosition, List<Event> eventsList) {
        Event event = eventsList.get(0);
        drawEventIndicatorCircle(canvas, xPosition, yPosition, event.getColor());
    }

    private void drawTwoEvents(Canvas canvas, float xPosition, float yPosition, List<Event> eventsList) {
        //draw fist event just left of center
        drawEventIndicatorCircle(canvas, xPosition + (xIndicatorOffset * -1), yPosition, eventsList.get(0).getColor());
        //draw second event just right of center
        drawEventIndicatorCircle(canvas, xPosition + (xIndicatorOffset * 1), yPosition, eventsList.get(1).getColor());
    }

    //draw 2 eventsByMonthAndYearMap followed by plus indicator to show there are more than 2 eventsByMonthAndYearMap
    private void drawEvents(Canvas canvas, float xPosition, float yPosition, List<Event> eventsList) {
        // k = size() - 1, but since we don't want to draw more than 2 indicators, we just stop after 2 iterations so we can just hard k = -2 instead
        // we can use the below loop to draw arbitrary eventsByMonthAndYearMap based on the current screen size, for example, larger screens should be able to
        // display more than 2 evens before displaying plus indicator, but don't draw more than 3 indicators for now
        if(isEventOverlap){
            xIndicatorOffset=2.2f;
        }
        for (int j = 0, k = -(eventsList.size()-1); j < eventsList.size(); j++, k += 2) {
            Event event = eventsList.get(j);
            float xStartPosition = xPosition + (xIndicatorOffset * k);
            drawEventIndicatorCircle(canvas, xStartPosition, yPosition, event.getColor());
        }
    }


    // zero based indexes used internally so instead of returning range of 1-7 like calendar class
    // it returns 0-6 where 0 is Sunday instead of 1
    int getDayOfWeek(Calendar calendar) {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - firstDayOfWeekToDraw;
        dayOfWeek = dayOfWeek < 0 ? 7 + dayOfWeek : dayOfWeek;
        return dayOfWeek;
    }

    void setDateTextTypeface(Typeface typefaceRegular) {
        if (typefaceRegular != null) {
            this.typefaceDateText = typefaceRegular;
        }
    }


    void setWeekTextTypeface(Typeface typefaceRegular) {
        if (typefaceRegular != null) {
            this.typefaceWeekText = typefaceRegular;
        }
    }

    void setMonthTextTypeface(Typeface typefaceRegular) {
        if (typefaceRegular != null) {
            this.typefaceMonthText = typefaceRegular;
        }
    }

    void drawMonth(Canvas canvas, Calendar monthToDrawCalender, int offset) {


        HashSet<Integer> weekEndDays = getWeekEndsFormMonthYear();
        //offset by one because we want to start from Monday
        int firstDayOfMonth = getDayOfWeek(monthToDrawCalender);

        boolean isSameMonthAsToday = monthToDrawCalender.get(Calendar.MONTH) == todayCalender.get(Calendar.MONTH);
        boolean isSameYearAsToday = monthToDrawCalender.get(Calendar.YEAR) == todayCalender.get(Calendar.YEAR);
        boolean isSameMonthAsCurrentCalendar = monthToDrawCalender.get(Calendar.MONTH) == currentCalender.get(Calendar.MONTH) &&
                monthToDrawCalender.get(Calendar.YEAR) == currentCalender.get(Calendar.YEAR);
        int todayDayOfMonth = todayCalender.get(Calendar.DAY_OF_MONTH);
        boolean isAnimatingWithExpose = animationStatus == EXPOSE_CALENDAR_ANIMATION;

        int maximumMonthDay = monthToDrawCalender.getActualMaximum(Calendar.DAY_OF_MONTH);

        tempPreviousMonthCalendar.setTimeInMillis(monthToDrawCalender.getTimeInMillis());
        tempPreviousMonthCalendar.add(Calendar.MONTH, -1);
        int maximumPreviousMonthDay = tempPreviousMonthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int dayColumn = 0, dayRow = 0; dayColumn <= 6; dayRow++) {

            if (dayRow == 7) {
                dayRow = 0;
                if (dayColumn <= 6) {
                    dayColumn++;
                }
            }
            if (dayColumn == dayColumnNames.length) {
                break;
            }
            float xPosition = widthPerDay * dayColumn + paddingWidth + paddingLeft + accumulatedScrollOffset.x + offset - paddingRight;
            float yPosition = dayRow * heightPerDay + paddingHeight;
            if (xPosition >= growFactor && (isAnimatingWithExpose || animationStatus == ANIMATE_INDICATORS) || yPosition >= growFactor) {
                // don't draw days if animating expose or indicators
                continue;
            }
            int day = ((dayRow - 1) * 7 + dayColumn + 1) - firstDayOfMonth;
            if (dayRow == 0) {
                // first row, so draw the first letter of the day
                if (shouldDrawDaysHeader) {
                    dayPaint.setColor(calenderTextColor);
                    dayPaint.setStyle(Paint.Style.FILL);
                    dayPaint.setColor(weekDaysTextColor);
                    //change 1 : Week end day color
                    if (isApplyWeekEndColor) {
                        if (dayColumn == Calendar.SUNDAY - 1 || dayColumn == Calendar.SATURDAY - 1) {
                            dayPaint.setColor(weekEndDaysTextColor);
                        }
                    }
                    if (dayColumn == 6 && isDisplayMonthName) {
                        Paint monthPaint = dayPaint;
                        monthPaint.setTypeface(typefaceMonthText);
                        monthPaint.setColor(monthNameTextColor);
                        monthPaint.setTextSize(textMonthNameSize);
                        String monthName = currentCalender.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
                        canvas.drawText(monthName, xPosition, (dayRow * heightPerDay + paddingHeight) + heightPerDay, monthPaint);
                    }
                    dayPaint.setTextSize(textWeekNameSize);
                    dayPaint.setTypeface(typefaceWeekText);
                    canvas.drawText(dayColumnNames[dayColumn].toUpperCase(), xPosition, paddingHeight, dayPaint);

                }
            } else {

                int defaultCalenderTextColorToUse = calenderTextColor;
                //Draw Current
                if (isSameYearAsToday && isSameMonthAsToday && todayDayOfMonth == day && !isAnimatingWithExpose) {
                    drawDayCircleIndicator(currentDayIndicatorStyle, canvas, xPosition, yPosition, currentDayBackgroundColor);
                    defaultCalenderTextColorToUse = currentDayTextColor;
                } else if (currentCalender.get(Calendar.DAY_OF_MONTH) == day && isSameMonthAsCurrentCalendar && !isAnimatingWithExpose) {
                    //Draw Selected
                    if (!isCurrentMonthDaySelector()) {
                        drawDayCircleIndicator(selectedDayIndicatorStyle, canvas, xPosition, yPosition, selectedDayBackgroundColor);
                        defaultCalenderTextColorToUse = calenderTextColor;
                    }
                }
                /*else if(!isNotCurrentMonth() &&userSelectedDate!=null){

                    drawDayCircleIndicator(selectedDayIndicatorStyle, canvas, xPosition, yPosition, selectedDayBackgroundColor);
                    defaultCalenderTextColorToUse = calenderTextColor;
                }*/
                //Draw Events
                if (day <= 0) {

                    if (displayOtherMonthDays) {
                        // Display day month before
                        dayPaint.setStyle(Paint.Style.FILL);
                        dayPaint.setColor(otherMonthDaysTextColor);
                        dayPaint.setTypeface(typefaceDateText);
                        dayPaint.setTextSize(textSize);
                        canvas.drawText(String.valueOf(maximumPreviousMonthDay + day), xPosition, yPosition, dayPaint);
                    }
                } else if (day > maximumMonthDay) {

                    if (displayOtherMonthDays) {
                        // Display day month after
                        dayPaint.setStyle(Paint.Style.FILL);
                        dayPaint.setColor(otherMonthDaysTextColor);
                        dayPaint.setTypeface(typefaceDateText);
                        dayPaint.setTextSize(textSize);
                        canvas.drawText(String.valueOf(day - maximumMonthDay), xPosition, yPosition, dayPaint);
                    }
                } else {

                    dayPaint.setStyle(Paint.Style.FILL);
                    dayPaint.setColor(defaultCalenderTextColorToUse);
                    //Weekend color
                    if (isApplyWeekEndColor) {
                        for (Integer integer : weekEndDays) {
                            if (day == integer) {
                                if (isSameYearAsToday && isSameMonthAsToday && todayDayOfMonth == day && !isAnimatingWithExpose) {
                                    dayPaint.setColor(defaultCalenderTextColorToUse);
                                } else {
                                    dayPaint.setColor(weekEndDaysTextColor);
                                }

                            }

                        }
                    }
                    dayPaint.setTextSize(textSize);
                    dayPaint.setTypeface(typefaceDateText);
                    canvas.drawText(String.valueOf(day), xPosition, yPosition, dayPaint);
                    //Change 3 : Draw Line
                    if (isShowRowLine) {
                        if (dayRow != 1) {

                            canvas.drawLine(xPosition - paddingWidth, yPosition - paddingHeight - 10, xPosition + paddingWidth, yPosition - paddingHeight - 10, rowLinePaint);
                        }
                    }
                }

            }

        }

        //Bottom Line
        //canvas.drawLine(0, height-5, getWidth(),height-5, rowLinePaint);

        drawEvents(canvas, monthToDrawCalender, offset);
    }

    public void setSelectorDayCurrentMonth(boolean isCurrentMonthDaySelector) {

    }

    // set stroke so you can actually see the lines
    private void drawDayCircleIndicator(int indicatorStyle, Canvas canvas, float x, float y, int color) {
        // Log.d("bgcolor",String.valueOf(color));
        drawDayCircleIndicator(indicatorStyle, canvas, x, y, color, circleScale);
    }


    private void drawDayCircleIndicator(int indicatorStyle, Canvas canvas, float x, float y, int color, float circleScale) {
        float strokeWidth = dayPaint.getStrokeWidth();
        if (indicatorStyle == NO_FILL_LARGE_INDICATOR) {
            dayPaint.setStrokeWidth(2 * screenDensity);
            dayPaint.setStyle(Paint.Style.STROKE);
        } else {
            dayPaint.setStyle(Paint.Style.FILL);
        }
        drawCircle(canvas, x, y, color, circleScale);
        dayPaint.setStrokeWidth(strokeWidth);
        dayPaint.setStyle(Paint.Style.FILL);
    }

    // Draw Circle on certain days to highlight them
    private void drawCircle(Canvas canvas, float x, float y, int color, float circleScale) {
        dayPaint.setColor(color);
        if (animationStatus == ANIMATE_INDICATORS) {
            float maxRadius = circleScale * bigCircleIndicatorRadius * 1.4f;
            drawCircle(canvas, growfactorIndicator > maxRadius ? maxRadius : growfactorIndicator, x, y - (textHeight / 6));
        } else {
            drawCircle(canvas, circleScale * bigCircleIndicatorRadius, x, y - (textHeight / 6));
        }
    }

    private void drawEventIndicatorCircle(Canvas canvas, float x, float y, int color) {
        //Log.d("Color",String.valueOf(color));
        dayPaint.setColor(color);
        if (eventIndicatorStyle == SMALL_INDICATOR) {
            dayPaint.setStyle(Paint.Style.FILL);
            drawCircle(canvas, smallIndicatorRadius, x, y);
        } else if (eventIndicatorStyle == NO_FILL_LARGE_INDICATOR) {
            dayPaint.setStyle(Paint.Style.STROKE);
            drawDayCircleIndicator(NO_FILL_LARGE_INDICATOR, canvas, x, y, color);
        } else if (eventIndicatorStyle == FILL_LARGE_INDICATOR) {
            drawDayCircleIndicator(FILL_LARGE_INDICATOR, canvas, x, y, color);
        }
    }

    private void drawCircle(Canvas canvas, float radius, float x, float y) {

        canvas.drawCircle(x, y, radius, dayPaint);
    }

    private enum Direction {
        NONE, HORIZONTAL, VERTICAL
    }
}
