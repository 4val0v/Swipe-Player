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

    private float heightRequestedRatio = 1.2f;
    private float lineSpacing = 5;
    private float headerTextSize;
    private float lineTextSize;
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
        int textSizeHeader = a.getDimensionPixelSize(R.styleable.FormattedTextView_HeaderTextSize, 15);
        int textSizeLine = a.getDimensionPixelSize(R.styleable.FormattedTextView_LineTextSize, 15);
        this.isWrapping = a.getBoolean(R.styleable.FormattedTextView_IsWrapping, false);
        this.textColor = a.getColor(R.styleable.FormattedTextView_TextColor, 0);
        this.colorStateList = a.getColorStateList(R.styleable.FormattedTextView_TextColor);
        a.recycle();

        this.textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        this.textPaint.setTypeface(FontHelper.font(fontName));
        this.textPaint.density = getResources().getDisplayMetrics().density;

        this.setRawHeaderTextSize(textSizeHeader);
        this.setRawLineTextSize(textSizeLine);
        this.updateTextColor(false);
    }

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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width, height;
        boolean needsWrap;

        switch (widthMode)
        {
            case MeasureSpec.UNSPECIFIED:
            {
                this.textPaint.setTextSize(this.headerTextSize);
                width = (int) Math.ceil(this.textPaint.measureText(this.text, 0, this.text.length() - 1));
                break;
            }
            case MeasureSpec.AT_MOST:
            {
                this.textPaint.setTextSize(this.headerTextSize);
                int textWidth = (int) Math.ceil(this.textPaint.measureText(this.text, 0, this.text.length() - 1));
                width = widthSize < textWidth ? widthSize : textWidth;
                break;
            }
            default:
                width = widthSize;
        }

        switch (heightMode)
        {
            case MeasureSpec.UNSPECIFIED:
            {
                this.textPaint.setTextSize(this.headerTextSize);
                Rect bounds = new Rect();
                this.textPaint.getTextBounds(this.text.toString(), 0, this.text.length() - 1, bounds);
                height = bounds.height();
                break;
            }
            case MeasureSpec.AT_MOST:
            {
                this.textPaint.setTextSize(this.headerTextSize);
                Rect bounds = new Rect();
                this.textPaint.getTextBounds(this.text.toString(), 0, this.text.length() - 1, bounds);
                int textHeight = bounds.height();
                height = heightSize < textHeight ? heightSize : textHeight;
                break;
            }
            default:
                height = heightSize;
        }

        this.setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        this.textPaint.setTextSize(this.headerTextSize);
        this.textPaint.setColor(this.textColor);
        CharSequence paintText = TextUtils.ellipsize(this.text, this.textPaint, this.getWidth(), TextUtils.TruncateAt.END);

        Rect bounds = new Rect();
        this.textPaint.getTextBounds(paintText.toString(), 0, paintText.length() - 1, bounds);

        canvas.drawText(paintText, 0, paintText.length(), -bounds.left, -bounds.top, this.textPaint);
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
        this.invalidate();
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

    private void setRawHeaderTextSize(float size)
    {
        if (this.headerTextSize != size)
        {
            this.headerTextSize = size;
            this.requestLayout();
            this.invalidate();
        }
    }

    private void setRawLineTextSize(float size)
    {
        if (this.lineTextSize != size)
        {
            this.lineTextSize = size;
            this.requestLayout();
            this.invalidate();
        }
    }
}
