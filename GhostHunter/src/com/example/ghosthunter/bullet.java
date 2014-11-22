package com.example.ghosthunter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;
import com.example.ghosthunter.Speed;
public class bullet {

	private Bitmap bitmap;	// the actual bitmap
	private int x;			// the X coordinate
	private int y;			// the Y coordinate
	private boolean fired;	// if bullet is fired
	private Speed speed;	// the speed with its directions

	public bullet(Bitmap bitmap) {
		this.bitmap = bitmap;
		this.speed = new Speed();

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

	public boolean isTouched() {
		return false;
	}


	public Speed getSpeed() {
		return speed;
	}

	public void setSpeed(Speed speed) {
		this.speed = speed;
	}
	
	public void setFired(boolean a) {
		this.fired = a;
		
	}

	public void draw(Canvas canvas) {
		canvas.drawBitmap(bitmap, x - (bitmap.getWidth() / 2), y - (bitmap.getHeight() / 2), null);
	}

	/**
	 * Method which updates the ghosts's internal state every tick
	 */
	public void update() {
		if (fired) {
			x += (speed.getXv() * speed.getxDirection()); 

		}
	}



}

