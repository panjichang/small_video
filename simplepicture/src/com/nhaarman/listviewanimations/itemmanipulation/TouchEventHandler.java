package com.nhaarman.listviewanimations.itemmanipulation;

import android.view.MotionEvent;

import com.pan.simplepicture.annotations.NonNull;

public interface TouchEventHandler {

    boolean onTouchEvent(@NonNull MotionEvent event);

    boolean isInteracting();

}
