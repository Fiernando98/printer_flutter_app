package com.flerma.printer_flutter_example

import android.os.Bundle

abstract class FontStylePanel {
    companion object {
        private const val FONT_NAME = "font-name"
        private const val FONT_SIZE = "font-size"
        private const val FONT_STYLE = "font-style"

        private const val FONT_STYLE_NULL = 0x0000
        private const val FONT_STYLE_BOLD = 0x0001
        private const val FONT_STYLE_ITALIC = 0x0002
        private const val FONT_STYLE_UNDERLINE = 0x0004
        private const val FONT_STYLE_REVERSE = 0x0008
        private const val FONT_STYLE_STRIKEOUT = 0x0010

        private var mFontName = "simsun"
        private var mFontSize = 24

        private var isTextBold = false
        private var isTextItalic = false
        private var isTextUnderline = false
        private var isTextStrikeout = false
        private var mFontStyle = FONT_STYLE_NULL

        fun getFontInfo(): Bundle {
            mFontStyle = FONT_STYLE_NULL
            if (isTextBold) {
                mFontStyle = mFontStyle or FONT_STYLE_BOLD
            }
            if (isTextItalic) {
                mFontStyle = mFontStyle or FONT_STYLE_ITALIC
            }
            if (isTextUnderline) {
                mFontStyle = mFontStyle or FONT_STYLE_UNDERLINE
            }
            if (isTextStrikeout) {
                mFontStyle = mFontStyle or FONT_STYLE_STRIKEOUT
            }
            val fontInfo = Bundle()
            fontInfo.putString(FONT_NAME, mFontName)
            fontInfo.putInt(FONT_SIZE, mFontSize)
            fontInfo.putInt(FONT_STYLE, mFontStyle)
            return fontInfo
        }
    }
}