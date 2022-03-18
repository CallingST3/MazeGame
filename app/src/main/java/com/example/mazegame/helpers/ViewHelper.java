package com.example.mazegame.helpers;

import android.view.View;
import android.view.ViewGroup;

public class ViewHelper {

    public static void setLpMargin(View view, int width, int height, Offsets margins) {
        ViewGroup.LayoutParams vglp = view.getLayoutParams();
        ViewGroup.MarginLayoutParams lp = vglp != null
                ? vglp instanceof ViewGroup.MarginLayoutParams ? (ViewGroup.MarginLayoutParams)vglp : null
                : new ViewGroup.MarginLayoutParams(width, height);

        if(lp != null) {
            lp.width = width;
            lp.height = height;
            lp.topMargin = margins.top;
            lp.bottomMargin = margins.bottom;
            lp.leftMargin = margins.left;
            lp.rightMargin = margins.right;
            view.setLayoutParams(lp);
        }
    }

    public static class Offsets {
        int top = 0, bottom = 0, left = 0, right = 0;

        Offsets() { }

        public Offsets(int left, int right, int top, int bottom) {
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
        }

        public Offsets(int hrz, int vrt) {
            this(hrz, hrz, vrt, vrt);
        }

        public static Offsets left(int offset) {
            return new Offsets().setLeft(offset);
        }

        public Offsets setLeft(int offset) {
            this.left = offset;
            return this;
        }

        public static Offsets right(int offset) {
            return new Offsets().setRight(offset);
        }

        public Offsets setRight(int offset) {
            this.right = offset;
            return this;
        }

        public static Offsets leftRight(int lOffset, int rOffset) {
            return new Offsets().setLeftRight(lOffset, rOffset);
        }

        public Offsets setLeftRight(int left, int right) {
            setLeft(left);
            return setRight(right);
        }

        public static Offsets top(int offset) {
            return new Offsets().setTop(offset);
        }

        public Offsets setTop(int offset) {
            this.top = offset;
            return this;
        }

        public static Offsets bottom(int offset) {
            return new Offsets().setBottom(offset);
        }

        public Offsets setBottom(int offset) {
            this.bottom = offset;
            return this;
        }

        public static Offsets topBottom(int tOffset, int bOffset) {
            return new Offsets().setTopBottom(tOffset, bOffset);
        }

        public Offsets setTopBottom(int top, int bottom) {
            setTop(top);
            return setBottom(bottom);
        }

        public static Offsets hrz(int offset) {
            return new Offsets().setHrz(offset);
        }

        public Offsets setHrz(int offset) {
            setLeft(offset);
            return setRight(offset);
        }

        public static Offsets vrt(int offset) {
            return new Offsets().setVrt(offset);
        }

        public Offsets setVrt(int offset) {
            setTop(offset);
            return setBottom(offset);
        }

        public static Offsets all(int offset) {
            return new Offsets().setAll(offset);
        }

        public Offsets setAll(int offset) {
            setVrt(offset);
            return setHrz(offset);
        }
    }
}
