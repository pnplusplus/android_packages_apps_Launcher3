/*
 * Copyright (C) 2021 The Android Open Source Project
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
package com.android.launcher3.taskbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.launcher3.R;
import com.android.launcher3.util.TouchController;
import com.android.launcher3.views.BaseDragLayer;
import com.android.systemui.shared.system.ViewTreeObserverWrapper;
import com.android.systemui.shared.system.ViewTreeObserverWrapper.InsetsInfo;
import com.android.systemui.shared.system.ViewTreeObserverWrapper.OnComputeInsetsListener;

/**
 * Top-level ViewGroup that hosts the TaskbarView as well as Views created by it such as Folder.
 */
public class TaskbarDragLayer extends BaseDragLayer<TaskbarActivityContext> {

    private final Paint mTaskbarBackgroundPaint;

    private TaskbarDragLayerController.TaskbarDragLayerCallbacks mControllerCallbacks;

    private final OnComputeInsetsListener mTaskbarInsetsComputer = this::onComputeTaskbarInsets;

    public TaskbarDragLayer(@NonNull Context context) {
        this(context, null);
    }

    public TaskbarDragLayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TaskbarDragLayer(@NonNull Context context, @Nullable AttributeSet attrs,
            int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TaskbarDragLayer(@NonNull Context context, @Nullable AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, 1 /* alphaChannelCount */);
        mTaskbarBackgroundPaint = new Paint();
        mTaskbarBackgroundPaint.setColor(getResources().getColor(R.color.taskbar_background));
        mTaskbarBackgroundPaint.setAlpha(0);
    }

    public void init(TaskbarDragLayerController.TaskbarDragLayerCallbacks callbacks) {
        mControllerCallbacks = callbacks;
        recreateControllers();
    }

    @Override
    public void recreateControllers() {
        mControllers = new TouchController[] {mActivity.getDragController()};
    }

    private void onComputeTaskbarInsets(InsetsInfo insetsInfo) {
        if (mControllerCallbacks != null) {
            mControllerCallbacks.updateInsetsTouchability(insetsInfo);
            mControllerCallbacks.updateContentInsets(insetsInfo.contentInsets);
        }
    }

    protected void onDestroy() {
        ViewTreeObserverWrapper.removeOnComputeInsetsListener(mTaskbarInsetsComputer);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewTreeObserverWrapper.addOnComputeInsetsListener(getViewTreeObserver(),
                mTaskbarInsetsComputer);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        onDestroy();
    }

    @Override
    protected boolean canFindActiveController() {
        // Unlike super class, we want to be able to find controllers when touches occur in the
        // gesture area. For example, this allows Folder to close itself when touching the Taskbar.
        return true;
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        if (mControllerCallbacks != null) {
            mControllerCallbacks.onDragLayerViewRemoved();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.drawRect(0, canvas.getHeight() - mControllerCallbacks.getTaskbarBackgroundHeight(),
                canvas.getWidth(), canvas.getHeight(), mTaskbarBackgroundPaint);
        super.dispatchDraw(canvas);
    }

    /**
     * Sets the alpha of the background color behind all the Taskbar contents.
     * @param alpha 0 is fully transparent, 1 is fully opaque.
     */
    protected void setTaskbarBackgroundAlpha(float alpha) {
        mTaskbarBackgroundPaint.setAlpha((int) (alpha * 255));
        invalidate();
    }
}
