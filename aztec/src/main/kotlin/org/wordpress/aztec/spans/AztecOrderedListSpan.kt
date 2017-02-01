/*
 * Copyright (C) 2016 Automattic
 * Copyright (C) 2015 Matthew Lee
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wordpress.aztec.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.os.Parcel
import android.text.Layout
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.LineHeightSpan
import android.text.style.UpdateLayout
import org.wordpress.aztec.formatting.BlockFormatter

class AztecOrderedListSpan : AztecListSpan, LineHeightSpan.WithDensity, UpdateLayout {

    override fun chooseHeight(p0: CharSequence?, p1: Int, p2: Int, p3: Int, p4: Int, p5: Paint.FontMetricsInt?) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun chooseHeight(text: CharSequence, start: Int, end: Int, spanstartv: Int, v: Int, fm: Paint.FontMetricsInt, paint: TextPaint) {
        val spanned = text as Spanned
        val spanStart = spanned.getSpanStart(this)
        val spanEnd = spanned.getSpanEnd(this)


        if (start === spanStart || start < spanStart) {
            fm.ascent -= 50
            fm.top -= 50
        }
        if (end === spanEnd || spanEnd < end) {
            fm.descent += 50
            fm.bottom += 50
        }

    }

    private val TAG = "ol"

    private var textColor: Int = 0
    private var textMargin: Int = 0
    private var textPadding: Int = 0
    private var bulletWidth: Int = 0 //we are using bullet width to maintain same margin with bullet list

    override var attributes: String = ""
    override var lastItem: AztecListItemSpan = AztecListItemSpan()

    //used for marking
    constructor() : super() {
    }

    constructor(attributes: String) : super() {
        this.attributes = attributes
    }

    constructor(listStyle: BlockFormatter.ListStyle, attributes: String, last: AztecListItemSpan) : super() {
        this.textColor = listStyle.indicatorColor
        this.textMargin = listStyle.indicatorMargin
        this.bulletWidth = listStyle.indicatorWidth
        this.textPadding = listStyle.indicatorPadding
        this.attributes = attributes
        this.lastItem = last
    }

    constructor(src: Parcel) : super(src) {
        this.textColor = src.readInt()
        this.textMargin = src.readInt()
        this.textPadding = src.readInt()
        this.attributes = src.readString()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeInt(textColor)
        dest.writeInt(textMargin)
        dest.writeInt(textPadding)
        dest.writeString(attributes)
    }

    override fun getStartTag(): String {
        if (TextUtils.isEmpty(attributes)) {
            return TAG
        }
        return TAG + attributes
    }

    override fun getEndTag(): String {
        return TAG
    }

    override fun getLeadingMargin(first: Boolean): Int {
        return textMargin + 2 * bulletWidth + textPadding
    }


    override fun drawLeadingMargin(c: Canvas, p: Paint, x: Int, dir: Int,
                                   top: Int, baseline: Int, bottom: Int,
                                   text: CharSequence, start: Int, end: Int,
                                   first: Boolean, l: Layout) {

        if (!first) return

        val spanStart = (text as Spanned).getSpanStart(this)
        val spanEnd = text.getSpanEnd(this)

        if (start !in spanStart..spanEnd || end !in spanStart..spanEnd) return

        val listText = text.subSequence(spanStart, spanEnd)

        val lineNumber = getLineNumber(listText, end - spanStart)

        var adjustment = 0

        val numberOfLines = text.substring(spanStart..spanEnd - 2).split("\n").count()

        if (numberOfLines > 1 && lineNumber == 1) {
            adjustment = 0
        } else if (numberOfLines > 1 && numberOfLines == lineNumber) {
            adjustment = -50
        } else if (numberOfLines == 1) {
            adjustment = -50
        }


        val style = p.style

        val oldColor = p.color

        p.color = textColor
        p.style = Paint.Style.FILL

        val textToDraw = lineNumber.toString() + "."

        val width = p.measureText(textToDraw)
        c.drawText(textToDraw, (textMargin + x + dir - width) * dir, (bottom - p.descent()) + adjustment, p)

        p.color = oldColor
        p.style = style

    }

}