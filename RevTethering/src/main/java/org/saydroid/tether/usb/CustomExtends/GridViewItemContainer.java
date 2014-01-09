/*
 * Copyright (C) 2013, sayDroid.
 *
 * Copyright 2013 The sayDroid Project
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

package org.saydroid.tether.usb.CustomExtends;


import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.Arrays;

/**
 * This class makes sure that all items in a GridView row are of the same height.
 * (Could extend FrameLayout, LinearLayout etc as well, RelativeLayout was just my choice here)
 * @author Anton Spaans
 *
 */
public class GridViewItemContainer extends RelativeLayout {
    private View[] viewsInRow;

    public GridViewItemContainer(Context context) {
        super(context);
    }

    public GridViewItemContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public GridViewItemContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(11)
    public void setViewsInRow(View[] viewsInRow) {
        if  (viewsInRow != null) {
            if (this.viewsInRow == null) {
                this.viewsInRow = Arrays.copyOf(viewsInRow, viewsInRow.length);
            }
            else {
                System.arraycopy(viewsInRow, 0, this.viewsInRow, 0, viewsInRow.length);
            }
        }
        else if (this.viewsInRow != null){
            Arrays.fill(this.viewsInRow, null);
        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (viewsInRow == null) {
            return;
        }

        int measuredHeight = getMeasuredHeight();
        int maxHeight      = measuredHeight;
        for (View siblingInRow : viewsInRow) {
            if  (siblingInRow != null) {
                maxHeight = Math.max(maxHeight, siblingInRow.getMeasuredHeight());
            }
        }

        if (maxHeight == measuredHeight) {
            return;
        }

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        switch(heightMode) {
            case MeasureSpec.AT_MOST:
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(maxHeight, heightSize), MeasureSpec.EXACTLY);
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                break;

            case MeasureSpec.EXACTLY:
                // No debate here. Final measuring already took place. That's it.
                break;

            case MeasureSpec.UNSPECIFIED:
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.EXACTLY);
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                break;

        }
    }
}
