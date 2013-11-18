package net.illusor.swipeplayer.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.*;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.helpers.FontHelper;

public class FormattedTextView extends View implements Checkable
{
    private TextPaint textPaint;
    private CharSequence text = "";

    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
    private boolean isChecked;

    private int headerTextSize, lineTextSize, lineSpacing, charsToEllipsize;
    private boolean isWrapping;
    private int textColor;
    private ColorStateList colorStateList;

    public FormattedTextView(Context context)
    {
        this(context, null);
    }

    public FormattedTextView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public FormattedTextView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FormattedTextView, defStyle, 0);
        int fontName = a.getInt(R.styleable.FormattedTextView_Font, -1);
        this.headerTextSize = a.getDimensionPixelSize(R.styleable.FormattedTextView_HeaderTextSize, 20);
        this.lineTextSize = a.getDimensionPixelSize(R.styleable.FormattedTextView_LineTextSize, 20);
        this.lineSpacing = a.getDimensionPixelSize(R.styleable.FormattedTextView_LineSpacing, 20);
        this.charsToEllipsize = a.getInteger(R.styleable.FormattedTextView_CharsToEllipsize, 5);
        this.isWrapping = a.getBoolean(R.styleable.FormattedTextView_IsWrapping, false);
        this.textColor = a.getColor(R.styleable.FormattedTextView_TextColor, Color.BLACK);
        this.colorStateList = a.getColorStateList(R.styleable.FormattedTextView_TextColor);
        a.recycle();

        this.textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        this.textPaint.setTypeface(FontHelper.font(fontName));
        this.textPaint.density = getResources().getDisplayMetrics().density;

        this.updateTextColor(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int paddingV = this.getPaddingBottom() + this.getPaddingTop();
        int paddingH = this.getPaddingLeft() + this.getPaddingRight();

        int width, height;
        boolean needsWrap = false;

        this.textPaint.setTextSize(this.headerTextSize);

        int requestedWidth = (int)(this.textPaint.measureText(this.text, 0, this.text.length())) + paddingH;
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

        Paint.FontMetricsInt metrics = this.textPaint.getFontMetricsInt();
        int requestedHeight = metrics.descent - metrics.ascent + paddingV;
        if (needsWrap && this.isWrapping)
        {
            this.textPaint.setTextSize(this.lineTextSize);
            metrics = this.textPaint.getFontMetricsInt();
            requestedHeight += metrics.descent - metrics.ascent + this.lineSpacing;
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
            this.textPaint.setTextSize(this.headerTextSize);
            this.textPaint.setColor(this.textColor);

            int maxTextWidth = this.getWidth() - this.getPaddingLeft() - this.getPaddingRight();
            int headerCharsCount = this.textPaint.breakText(this.text, 0, this.text.length(), true, maxTextWidth, null);

            if (headerCharsCount > 0)
            {
                Rect bounds = new Rect();
                this.textPaint.getTextBounds(this.text.toString(), 0, headerCharsCount, bounds);
                Paint.FontMetricsInt metrics = this.textPaint.getFontMetricsInt();

                int headerOffsetLeft = this.getPaddingLeft() - bounds.left;
                int headerOffsetTop = this.getPaddingTop() - metrics.ascent;

                boolean ellipsize = (this.text.length() - headerCharsCount > this.charsToEllipsize) || !this.isWrapping;
                if (ellipsize)
                {
                    CharSequence smartText = TextUtils.ellipsize(this.text, this.textPaint, maxTextWidth, TextUtils.TruncateAt.END);
                    canvas.drawText(smartText, 0, smartText.length(), headerOffsetLeft, headerOffsetTop, this.textPaint);
                }
                else
                {
                    canvas.drawText(this.text, 0, headerCharsCount, headerOffsetLeft, headerOffsetTop, this.textPaint);
                }

                if (!ellipsize && this.isWrapping && headerCharsCount < this.text.length())
                {
                    this.textPaint.setTextSize(this.lineTextSize);
                    CharSequence lineText = this.text.subSequence(headerCharsCount, this.text.length());
                    lineText = TextUtils.ellipsize(lineText, this.textPaint, maxTextWidth, TextUtils.TruncateAt.END);

                    this.textPaint.getTextBounds(this.text.toString(), 0, headerCharsCount, bounds);
                    metrics = this.textPaint.getFontMetricsInt();

                    int lineOffsetTop = headerOffsetTop + this.lineSpacing - metrics.ascent;

                    canvas.drawText(lineText, 0, lineText.length(), headerOffsetLeft, lineOffsetTop, this.textPaint );
                }
            }
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace)
    {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (this.isChecked())
            this.mergeDrawableStates(drawableState, CHECKED_STATE_SET);

        return drawableState;
    }

    @Override
    protected void drawableStateChanged()
    {
        super.drawableStateChanged();
        this.updateTextColor(true);
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
            if (this.textColor != color)
            {
                this.textColor = color;
                if (allowInvalidate)
                    this.invalidate();
            }
        }
    }

    public CharSequence getText()
    {
        return text;
    }

    public void setText(CharSequence text)
    {
        if (text == null)
            text = "";

        this.text = text;

        this.requestLayout();
        this.invalidate();
    }

    public void setTextColorResource(int resourceId)
    {
        ColorStateList stateList = this.getResources().getColorStateList(resourceId);
        if (stateList != this.colorStateList)
        {
            this.colorStateList = stateList;
            this.updateTextColor(true);
        }
    }

    public void setTextColor(int color)
    {
        if (this.colorStateList != null || this.textColor != color)
        {
            this.colorStateList = null;
            this.textColor = color;
            this.updateTextColor(true);
        }
    }

}
