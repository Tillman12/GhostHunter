/**
 * 
 */
package com.example.ghosthunter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;

public class money {

	private Bitmap bitmap;	// the actual bitmap
	private int x;			// the X coordinate
	private int y;			// the Y coordinate
	private int value; 		//value

	
	public money(Bitmap bitmap, int x, int y, int val) {
		this.bitmap = bitmap;
		this.x = x;
		this.y = y;
		this.setValue(val);

	}
	
	public Bitmap getBitmap() {
		return bitmap;
	}
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	
	public void draw(Canvas canvas) {
		canvas.drawBitmap(bitmap, x - (bitmap.getWidth() / 2), y - (bitmap.getHeight() / 2), null);
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	
}
