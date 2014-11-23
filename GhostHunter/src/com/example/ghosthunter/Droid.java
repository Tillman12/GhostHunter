/**
 * 
 */
package com.example.ghosthunter;

import com.example.ghosthunter.Speed;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;

/**
 *
 */
public class Droid {

	private Bitmap bitmap;	// the actual bitmap
	private int x;			// the X coordinate
	private int y;			// the Y coordinate
	private boolean touched;	// if droid is touched/picked up
	private Speed speed;	// the speed with its directions
	private Rect space = new Rect();	//Rectangle to see what space it is occupying

	public Droid(Bitmap bitmap, int x, int y) {
		this.bitmap = bitmap;
		this.x = x;
		this.y = y;
		this.speed = new Speed();
		int xB = bitmap.getWidth();
		int yB = bitmap.getHeight();
		this.space.set(x-(xB/2), y+(yB/2), x+(xB/2), y-(yB/2));
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

	public Rect getSpace() {
		return space;
	}
	
	public void setSpace(int x, int y) {
		this.space.top = y + (bitmap.getHeight() / 2);
		this.space.bottom = y - (bitmap.getHeight() / 2);
		this.space.left = x - (bitmap.getWidth() / 2);
		this.space.right = x - (bitmap.getWidth() / 2);
	}

	public boolean isTouched() {
		return touched;
	}

	public void setTouched(boolean touched) {
		this.touched = touched;
	}

	public Speed getSpeed() {
		return speed;
	}

	public void setSpeed(Speed speed) {
		this.speed = speed;
	}

	public void draw(Canvas canvas) {
		canvas.drawBitmap(bitmap, x - (bitmap.getWidth() / 2), y - (bitmap.getHeight() / 2), null);
	}


	public void update() {

	}


//when screen is pressed
	public void handleActionDown(int eventX, int eventY) {
		if (eventX >= (x - bitmap.getWidth() / 2) && (eventX <= (x + bitmap.getWidth()/2))) {
			if (eventY >= (y - bitmap.getHeight() / 2) && (y <= (y + bitmap.getHeight() / 2))) {
				// droid touched
				setTouched(true);
			} else {
				setTouched(false);
			}
		} else {
			setTouched(false);
		}

	}
}
