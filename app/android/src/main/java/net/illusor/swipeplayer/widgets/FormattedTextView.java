package net.illusor.swipeplayer.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Checkable;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.helpers.FontHelper;

public class FormattedTextView extends View implements Checkable
{
    private TextPaint headerPaint, linePaint;
    private CharSequence text = "";

    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
    private boolean isChecked;

    private int lineSpacing, charsToEllipsize;
    private boolean isWrapping;
    private ColorStateList colorStateList;

    public FormattedTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FormattedTextView);
        int fontName = a.getInt(R.styleable.FormattedTextView_Font, 0);
        this.lineSpacing = a.getDimensionPixelSize(R.styleable.FormattedTextView_LineSpacing, 5);
        this.charsToEllipsize = a.getInteger(R.styleable.FormattedTextView_CharsToEllipsize, 10);
        this.isWrapping = a.getBoolean(R.styleable.FormattedTextView_IsWrapping, false);
        this.colorStateList = a.getColorStateList(R.styleable.FormattedTextView_TextColor);
        int headerTextSize = a.getDimensionPixelSize(R.styleable.FormattedTextView_HeaderTextSize, 20);
        int lineTextSize = a.getDimensionPixelSize(R.styleable.FormattedTextView_LineTextSize, 20);
        int textColor = a.getColor(R.styleable.FormattedTextView_TextColor, Color.BLACK);
        a.recycle();

        Typeface font = FontHelper.font(fontName);
        this.headerPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        this.headerPaint.setTypeface(font);
        this.headerPaint.setTextSize(headerTextSize);
        this.headerPaint.setColor(textColor);
        this.headerPaint.density = getResources().getDisplayMetrics().density;

        this.linePaint = new TextPaint(this.headerPaint);
        this.linePaint.setTextSize(lineTextSize);

        this.updateTextColor(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int paddingV = this.getPaddingBottom() + this.getPaddingTop();
        int paddingH = this.getPaddingLeft() + this.getPaddingRight();

        int width, height;
        boolean needsWrap = false;

        int requestedWidth = (int)(this.headerPaint.measureText(this.text, 0, this.text.length())) + paddingH;
        switch (widthMode)
        {
            case MeasureSpec.UNSPECIFIED:
            {
                width = requestedWidth;
                break;
            }
            case MeasureSpec.AT_MOST:
            {
                width = widthSize < requestedWidth ? widthSize : requestedWidth;
                needsWrap = widthSize < requestedWidth;
                break;
            }
            default:
            {
                width = widthSize;
                needsWrap = widthSize < requestedWidth;
            }
        }

        Paint.FontMetricsInt metricsHeader = this.headerPaint.getFontMetricsInt();
        int requestedHeight = metricsHeader.descent - metricsHeader.ascent + paddingV;

        int maxTextWidth = width - this.getPaddingLeft() - this.getPaddingRight();
        int headerCharsCount = this.headerPaint.breakText(this.text, 0, this.text.length(), true, maxTextWidth, null);
        boolean ellipsize = (this.text.length() - headerCharsCount < this.charsToEllipsize) || !this.isWrapping;

        Log.d("Ellipsize-Measure", String.format("Text: %s || headerCharsCount: %d || length: %d || ellipsize: %b || maxTextWidth: %d", this.text, headerCharsCount, this.text.length(), ellipsize, maxTextWidth));

        if (needsWrap && !ellipsize && this.isWrapping)
        {
            Paint.FontMetricsInt metricsLine = this.linePaint.getFontMetricsInt();
            requestedHeight += metricsLine.descent - metricsLine.ascent - metricsHeader.descent + this.lineSpacing;
        }

        switch (heightMode)
        {
            case MeasureSpec.UNSPECIFIED:
            {
                height = requestedHeight;
                break;
            }
            case MeasureSpec.AT_MOST:
            {
                height = heightSize < requestedHeight ? heightSize : requestedHeight;
                break;
            }
            default:
            {
                height = heightSize;
            }
        }

        this.setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if (this.text.length() != 0)
        {
            int maxTextWidth = this.getWidth() - this.getPaddingLeft() - this.getPaddingRight();
            int headerCharsCount = this.headerPaint.breakText(this.text, 0, this.text.length(), true, maxTextWidth, null);

            if (headerCharsCount > 0)
            {
                Paint.FontMetricsInt metrics = this.headerPaint.getFontMetricsInt();

                int headerOffsetLeft = this.getPaddingLeft();
                int headerOffsetTop = this.getPaddingTop() - metrics.ascent;

                boolean ellipsize = (this.text.length() - headerCharsCount < this.charsToEllipsize) || !this.isWrapping;
                if (ellipsize)
                {
                    CharSequence smartText = TextUtils.ellipsize(this.text, this.headerPaint, maxTextWidth, TextUtils.TruncateAt.END);
                    canvas.drawText(smartText, 0, smartText.length(), headerOffsetLeft, headerOffsetTop, this.headerPaint);
                }
                else
                {
                    canvas.drawText(this.text, 0, headerCharsCount, headerOffsetLeft, headerOffsetTop, this.headerPaint);
                }

                if (!ellipsize && this.isWrapping && headerCharsCount < this.text.length())
                {
                    CharSequence lineText = this.text.subSequence(headerCharsCount, this.text.length());
                    lineText = TextUtils.ellipsize(lineText, this.linePaint, maxTextWidth, TextUtils.TruncateAt.END);

                    metrics = this.linePaint.getFontMetricsInt();

                    int lineOffsetTop = headerOffsetTop - metrics.ascent + this.lineSpacing;

                    canvas.drawText(lineText, 0, lineText.length(), headerOffsetLeft, lineOffsetTop, this.linePaint);
                }
            }
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace)
    {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (this.isChecked())
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);

        return drawableState;
    }

    @Override
    protected void drawableStateChanged()
    {
        super.drawableStateChanged();
        this.updateTextColor(true);
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        Parcelable superState = super.onSaveInstanceState();
        SavedState state = new SavedState(superState);
        state.text = this.text;
        state.isChecked = this.isChecked;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable superState)
    {
        if (!(superState instanceof SavedState))
        {
            super.onRestoreInstanceState(superState);
            return;
        }

        SavedState savedState = (SavedState)superState;
        super.onRestoreInstanceState(savedState.getSuperState());

        this.text = savedState.text;
        this.isChecked = savedState.isChecked;
    }

    @Override
    public String toString()
    {
        return this.text.toString();
    }

    //region Checkable

    @Override
    public boolean isChecked()
    {
        return this.isChecked;
    }

    @Override
    public void setChecked(boolean b)
    {
        this.isChecked = b;
        this.refreshDrawableState();
    }

    @Override
    public void toggle()
    {
        this.setChecked(!this.isChecked);
    }

    //endregion

    private void updateTextColor(boolean allowInvalidate)
    {
        if (this.colorStateList != null)
        {
            int color = this.colorStateList.getColorForState(this.getDrawableState(), 0);
            if (this.headerPaint.getColor() != color)
            {
                this.headerPaint.setColor(color);
                this.linePaint.setColor(color);
                if (allowInvalidate)
                    this.invalidate();
            }
        }
    }

    public void setText(CharSequence text)
    {
        if (text == null)
            text = "";

        this.text = text;

        this.requestLayout();
        this.invalidate();
    }

    public void setColor(int color)
    {
        this.colorStateList = null;
        this.headerPaint.setColor(color);
        this.linePaint.setColor(color);
        this.invalidate();
    }

    private class SavedState extends BaseSavedState
    {
        private CharSequence text;
        private boolean isChecked;

        private SavedState(Parcel source)
        {
            super(source);
            this.text = source.readString();
            this.isChecked = source.readByte() == 1;
        }

        private SavedState(Parcelable superState)
        {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);
            dest.writeString(this.text.toString());
            dest.writeByte(this.isChecked ? (byte)1 : 0);
        }
    }
}
