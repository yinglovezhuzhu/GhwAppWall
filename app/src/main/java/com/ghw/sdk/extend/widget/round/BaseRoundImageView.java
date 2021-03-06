/*
 * Copyright (C) 2015 The Android Open Source Project.
 *
 *        yinglovezhuzhu@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ghw.sdk.extend.widget.round;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * {@link RoundImageView}基类，这里集合了所有子类可以共用的方法
 * 
 * @author yinglovezhuzhu@gmail.com
 * 
 * @version 1.0
 * 
 */
public class BaseRoundImageView extends ImageView {

	protected final int DEFAULT_CORNER_RADIUS = 0;
	
	protected final int DEFAULT_CORNER_RATE = 1;

	// 默认边框颜色，透明色
	protected final int DEFAULT_COLOR = 0xFFFFFFFF;
	protected final int DEFAULT_BORDER_THICKNESS = 0;
	
	// 边框宽度
	protected int mBorderThickness = DEFAULT_BORDER_THICKNESS;
	protected int mBorderInsideThickness = DEFAULT_BORDER_THICKNESS;
	protected int mBorderOutsideThickness = DEFAULT_BORDER_THICKNESS;

	// 边框颜色
	protected int mBorderColor = DEFAULT_COLOR;
	protected int mBorderInsideColor = DEFAULT_COLOR;
	protected int mBorderOutsideColor = DEFAULT_COLOR;
	protected int mFillColor = DEFAULT_COLOR;

	// ImageView的尺寸
	protected int mViewWidth;
	protected int mViewHeight;
	
	/** 圆角半径值 **/
	protected int mCornerRadius = DEFAULT_CORNER_RADIUS;
	
	/** 圆角比率（和最小边的比率） **/
	protected int mCornerRate = DEFAULT_CORNER_RATE;
	
	/** 绘制的区域 **/
	protected final RectF mDrawRect = new RectF();
	/** 临时保存变量的RectF对象 **/
	protected final RectF mTempRect = new RectF();

	protected final Resources mResources;

	protected final String mPackageName;

	public BaseRoundImageView(Context context) {
		super(context);
		mResources = context.getResources();
		mPackageName = context.getPackageName();
	}

	public BaseRoundImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mResources = context.getResources();
		mPackageName = context.getPackageName();
	}

	public BaseRoundImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mResources = context.getResources();
		mPackageName = context.getPackageName();
	}




	/**
	 * 设置圆角半径，这个半径值会和圆角比率相冲突，只能有一个生效（最后设置的那个优先，xml中比率优先）<br>
	 * 另外需要注意的是，这个半径会在绘制过程中进行修正，最大的半径是1/2图形，也就是圆形
	 * 
	 * @param cornerRadius
	 * 
	 * @see {@link #setCornerRate(int)}} 设置圆角比率方法
	 */
	public void setCornerRadius(int cornerRadius) {
		this.mCornerRadius = cornerRadius;
		this.mCornerRate = DEFAULT_CORNER_RATE;
		postInvalidate();
 	}
	
	/**
	 * 设置圆角比率，这个比率值会和圆角半径相冲突，只能有一个生效（最后设置的那个优先，xml中比率优先）<br>
	 * <p>圆角比率说明：这个圆角比率的原理是以图形的最小边为基准，最小边值除以这个比率，就得到了<br>
	 * 圆角半径（即r = min(width, height) / rate），当值为2时是一个圆形。
	 * @param cornerRate
	 * 
	 * @see {@link #setCornerRadius(int)}} 设置圆角半径方法
	 */
	public void setCornerRate(int cornerRate) {
		this.mCornerRate = cornerRate;
		this.mCornerRadius = DEFAULT_CORNER_RADIUS;
		postInvalidate();
	}

	/**
	 * 设置边框宽度
	 * @param borderThickness
	 */
	public void setBorderThickness(int borderThickness) {
		this.mBorderThickness = borderThickness;
		if(mBorderThickness > DEFAULT_BORDER_THICKNESS) {
			mBorderOutsideThickness = DEFAULT_BORDER_THICKNESS;
			mBorderInsideThickness = DEFAULT_BORDER_THICKNESS;
		}
		postInvalidate();
	}

	/**
	 * 设置内边框宽度
	 * @param borderInsideThickness
	 */
	public void setBorderInsideThickness(int borderInsideThickness) {
		this.mBorderInsideThickness = borderInsideThickness;
		if(mBorderInsideThickness > DEFAULT_BORDER_THICKNESS) {
			mBorderThickness = DEFAULT_BORDER_THICKNESS;
		}
		postInvalidate();
	}

	/**
	 * 设置外边框宽度
	 * @param borderOutsideThickness
	 */
	public void setBorderOutsideThickness(int borderOutsideThickness) {
		this.mBorderOutsideThickness = borderOutsideThickness;
		if(mBorderOutsideThickness > DEFAULT_BORDER_THICKNESS) {
			mBorderThickness = DEFAULT_BORDER_THICKNESS;
		}
		postInvalidate();
	}

	/**
	 * 设置边框颜色
	 * @param borderColor
	 */
	public void setBorderColor(int borderColor) {
		this.mBorderColor = borderColor;
		this.mBorderOutsideColor = borderColor;
		this.mBorderInsideColor = borderColor;
		postInvalidate();
	}

	/**
	 * 设置内边框颜色
	 * @param borderInsideColor
	 */
	public void setBorderInsideColor(int borderInsideColor) {
		this.mBorderInsideColor = borderInsideColor;
		postInvalidate();
	}

	/**
	 * 设置外边框颜色
	 * @param borderOutsideColor
	 */
	public void setBorderOutsideColor(int borderOutsideColor) {
		this.mBorderOutsideColor = borderOutsideColor;
		postInvalidate();
	}
	
	/**
	 * 设置空白区域填充颜色<br>
	 * 如果图片不能铺满整个控件，那么空白区域将会被这个填充颜色填充
	 * @param fillColor
	 */
	public void setFillColor(int fillColor) {
		this.mFillColor = fillColor;
		postInvalidate();
	}

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {  
        super.onSizeChanged(w, h, oldw, oldh);  
        if ((w != oldw) || (h != oldh)) {
        	mViewWidth = w;
        	mViewHeight = h;
        }
    }  
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}


	protected int getIdentifier(String name, String defType) {
		int resId = mResources.getIdentifier(name, defType, mPackageName);
		if(0 == resId) {
			throw new IllegalStateException("'" + name  + "'"  + defType
					+ "resource dismissed, please put it to res/layout folder");
		}
		return resId;
	}

}
