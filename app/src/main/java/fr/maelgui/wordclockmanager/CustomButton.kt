package fr.maelgui.wordclockmanager

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView

/**
 * TODO: document your custom view class.
 */
class CustomButton : CardView {

    private var colorActive = R.color.colorAccent
    private var colorInactive = R.color.lightColor
    private var backgroundActive = R.color.colorPrimaryDark
    private var backgroundInactive = R.color.colorAccent

    private var _cardView: MaterialCardView? = null
    private var _imageView: ImageView? = null
    private var _textView: TextView? = null

    private var _text: String? = null
    private var _image: Drawable? = null
    private var _active: Boolean = false

    var active: Boolean
        get() = _active
        set(value) {
            _active = value
            invalidateColor()
        }

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
        inflate(context, R.layout.item_mode, this)

        this.background = null

        _cardView = findViewById(R.id.customButtonCard)
        _imageView = findViewById(R.id.customButtonImage)
        _textView = findViewById(R.id.customButtonText)

        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.CustomButton, defStyle, 0
        )

        _text = a.getString(
            R.styleable.CustomButton_text
        )


        _image = a.getDrawable(
            R.styleable.CustomButton_image
        )

        _active = a.getBoolean(
            R.styleable.CustomButton_active,
            false
        )

        _imageView!!.setImageDrawable(_image)
        _textView!!.text = _text

        a.recycle()

        invalidateColor()
    }

    private fun invalidateColor() {
        if (_active) {
            _cardView?.setCardBackgroundColor(ContextCompat.getColor(context, backgroundActive))
            _cardView?.strokeColor = ContextCompat.getColor(context, backgroundActive)
            _textView?.setTextColor(ContextCompat.getColor(context, backgroundInactive))
            _imageView?.setColorFilter(ContextCompat.getColor(context, backgroundInactive))
        }
        else {
            _cardView?.setCardBackgroundColor(ContextCompat.getColor(context, backgroundInactive))
            _cardView?.strokeColor = ContextCompat.getColor(context, colorInactive)
            _textView?.setTextColor(ContextCompat.getColor(context, colorInactive))
            _imageView?.setColorFilter(ContextCompat.getColor(context, colorInactive))
        }
    }

}
