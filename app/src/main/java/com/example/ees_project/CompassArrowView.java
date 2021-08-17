package com.example.ees_project;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

//public class CompassArrowView extends View {
//    public CompassArrowView(Context context, AttributeSet att, int re) { super(context, att, re); }
//    public CompassArrowView(Context context, AttributeSet att)         { super(context, att); }
//    public CompassArrowView(Context context)                           { super(context); }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    protected void onDraw(Canvas canvas) {
//        Resources res = getResources();
//
//        BitmapDrawable bd1 = null;
//        bd1 = (BitmapDrawable)res.getDrawable(R.drawable.compass_border, null);
//        Bitmap bit1 = bd1.getBitmap();
//        canvas.drawBitmap(bit1, null,  new Rect(getWidth()/2-510, getHeight()/2 + -500, getWidth()/2+490    , getHeight()/2+500), null);
//
//        Matrix matrix = new Matrix();
//        matrix.postRotate(90);  //회전각도
//        BitmapDrawable bd2 = null;
//        bd2 = (BitmapDrawable)res.getDrawable(R.drawable.compass_arrow, null);
//        Bitmap bmp = bd2.getBitmap();
//        Bitmap bit2 = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
//        canvas.drawBitmap(bit2, null,  new Rect(getWidth()/2-200, getHeight()/2 + -350, getWidth()/2+200, getHeight()/2+250), null);
//    }
//}
