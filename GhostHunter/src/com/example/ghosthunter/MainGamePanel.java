package com.example.ghosthunter;

import java.util.ArrayList;


import com.example.ghosthunter.MainThread;
import com.example.ghosthunter.R;
import com.example.ghosthunter.R.drawable;
import com.example.ghosthunter.Droid;
import com.example.ghosthunter.ghost;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


@SuppressLint("ClickableViewAccessibility") public class MainGamePanel extends SurfaceView implements
SurfaceHolder.Callback {

	private static final String TAG = MainGamePanel.class.getSimpleName();

	private MainThread thread;
	private Droid droid;
	private button b1;
	private ArrayList<ghost> ghosts;
	private ArrayList<bullet> bullets;
	private ArrayList<barrier> barriers;
	private long shootTime; //time between bullet fires
	private long ghostTime;  //time between ghost regen
	private int State; //0 paused, 1 running

	public MainGamePanel(Context context) {
		super(context);
		State=1; //running
		ghostTime=0;

		barriers=new ArrayList<barrier>();
		// adding the callback (this) to the surface holder to intercept events
		getHolder().addCallback(this);

		barrier d=new barrier(BitmapFactory.decodeResource(getResources(), R.drawable.barrier), 400, 400);
		barriers.add(d);

		// create droid and load bitmap
		droid = new Droid(BitmapFactory.decodeResource(getResources(), R.drawable.hunter_right), 30, 50);
		ghosts=new ArrayList<ghost>();
		bullets=new ArrayList<bullet>();


		b1=new button(BitmapFactory.decodeResource(getResources(), R.drawable.fire_button), 730, 1150); 



		ghost a=new ghost(BitmapFactory.decodeResource(getResources(), R.drawable.ghost_1), 400,  300);
		a.setSpeed(new Speed(3, 5));
		a.getSpeed().flipXDirection();
		ghost b=new ghost(BitmapFactory.decodeResource(getResources(), R.drawable.ghost_1), 400,  10);
		b.setSpeed(new Speed(7,4));
		ghost c=new ghost(BitmapFactory.decodeResource(getResources(), R.drawable.ghost_1), 400,  200);
		ghosts.add(a);
		ghosts.add(b);
		ghosts.add(c);

		// create the game loop thread
		thread = new MainThread(getHolder(), this);

		// make the GamePanel focusable so it can handle events
		setFocusable(true);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// at this point the surface is created and
		// we can safely start the game loop
		thread.setRunning(true);
		thread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "Surface is being destroyed");
		// tell the thread to shut down and wait for it to finish
		// this is a clean shutdown
		boolean retry = true;
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
				// try again shutting down the thread
			}
		}
		Log.d(TAG, "Thread was shut down cleanly");
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(State==1)
		{
			int pointerCount = event.getPointerCount();

			for (int i = 0; i < pointerCount; i++)
			{	
				//droid movement, upper panel
				if((int)event.getY(i)<getHeight()-120)
				{
					//FIRST TOUCH
					if(i==0)
					{
						if (event.getAction() == MotionEvent.ACTION_DOWN) {
							// delegating event handling to the droid
							droid.handleActionDown((int)event.getX(i), (int)event.getY(i));

						}

						//MOVE GUY
						if (event.getAction() == MotionEvent.ACTION_MOVE)
						{

							//hes grabbed
							if (droid.isTouched()) {

								//being pulled right far enough to make him turn right
								if((int)event.getX(i)>droid.getX()+5)
								{
									droid.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.hunter_right));
									droid.getSpeed().setxDirection(1);
								}
								//being pulled left far enough to make him turn left
								else if((int)event.getX(i)<droid.getX()-5)
								{
									droid.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.hunter_left));
									droid.getSpeed().setxDirection(-1);
								}

								//updates rectangle for intersection, MUST be before update x and y
								//droid.setSpace((int)event.getX(i),(int)event.getY(i)-60);

								//update his coords
								droid.setX((int)event.getX(i));
								droid.setY((int)event.getY(i)-60);  //the Y is less so is is above your finger
							}

						}

					}
				}
				//button press AKA LOWER PANEL
				if((int)event.getY(i)>getHeight()-120)
				{
					b1.handleActionDown((int)event.getX(i), (int)event.getY(i));

				}

				// (aka button press while moving guy)
				if(event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN)
				{
					b1.handleActionDown((int)event.getX(i), (int)event.getY(i));

				}

				//TOUCH OFF
				if (event.getAction() == MotionEvent.ACTION_UP) {
					// touch was released
					if (droid.isTouched()) 
						droid.setTouched(false);

					if(b1.isTouched())
						b1.setTouched(false);
				}
				//SECOND TOUCH OFF
				if(event.getActionMasked() == MotionEvent.ACTION_POINTER_UP)
				{
					if(b1.isTouched())
						b1.setTouched(false);

				}
			}
		}
		if(State==0)
		{
			//set difficulty
			//start game

		}

		return true;
	}

	public void render(Canvas canvas) {
		if(State==1)
		{
			canvas.drawColor(Color.BLACK);
			droid.draw(canvas);
			for(ghost g : ghosts)
				g.draw(canvas);
			b1.draw(canvas);

			for(bullet b : bullets)
				b.draw(canvas);

			for(barrier c : barriers)
				c.draw(canvas);
		}
		if(State==0)
		{
			canvas.drawColor(Color.BLACK);
		}
	}

	/**
	 * This is the game update method. It iterates through all the objects
	 * and calls their update method if they have one or calls specific
	 * engine's update method.
	 */
	public void update() {
		if(State==1)
		{
			long now=System.currentTimeMillis();

			//add ghost every 3 sec
			if(ghostTime < now-3000)
			{

				double r1= Math.random();
				double r2= Math.random();
				int randY= (int)r2*1000;
				ghost a=new ghost(BitmapFactory.decodeResource(getResources(), R.drawable.ghost_1), 2000 ,randY); //offscreen
				a.setSpeed(new Speed((float) r1*6 + 2,(float) r1*4 + 2));
				ghosts.add(a);
				ghostTime=System.currentTimeMillis();
			}

			//bullet collisions with ghosts
			for(bullet b: bullets)
			{
				for(ghost g : ghosts)
				{
					if ((g.getX() + g.getBitmap().getWidth() / 2)>b.getX() &&
							(g.getX() - g.getBitmap().getWidth() / 2)<b.getX() &&
							(g.getY() + g.getBitmap().getWidth() / 2)>b.getY() &&
							(g.getY() - g.getBitmap().getWidth() / 2)<b.getY()) {
						g.setSpeed(new Speed(0,0));
						g.setX(2000); //ghost killed

						b.setY(3000); //bullet killed
					}

				}
			}

			//barrier collision with ghosts  **UNDER CONSTRUCTION**
			for(barrier c: barriers)
			{
				for(ghost g: ghosts)
				{
					if ((c.getX() + c.getBitmap().getWidth() / 2)>(g.getX()- g.getBitmap().getWidth() / 2 ) &&
							(c.getX() - c.getBitmap().getWidth() / 2)<(g.getX()+ g.getBitmap().getWidth() / 2 )&&
							(c.getY() + c.getBitmap().getWidth() / 2)>(g.getY()- g.getBitmap().getHeight() / 2 )  &&
							(c.getY() - c.getBitmap().getWidth() / 2)<(g.getY()+ g.getBitmap().getHeight() / 2 ))
					{
						if((g.getY() + g.getBitmap().getHeight() / 2)>(c.getY() - c.getBitmap().getHeight() / 2) ||
								(g.getY() - g.getBitmap().getHeight() / 2)<(c.getY() + c.getBitmap().getHeight() / 2))
						{
							if(g.getSpeed().getYv()>0)
								g.setY(c.getY() - c.getBitmap().getWidth() / 2 - g.getBitmap().getWidth());

							else
								g.setY(c.getY() + c.getBitmap().getWidth() / 2 + g.getBitmap().getWidth());

							g.getSpeed().flipYDirection();
						}
						if((g.getX() + g.getBitmap().getWidth() / 2)<(c.getX() - c.getBitmap().getWidth() / 2 ) ||
								(g.getX() - g.getBitmap().getWidth() / 2)>(c.getX() + c.getBitmap().getWidth() / 2 ))
						{

							g.getSpeed().flipXDirection();
						}
					}

				}
			}

			//person collision with ghosts
			/*for(ghost g : ghosts)
			{
				if ((g.getX() + g.getBitmap().getWidth() / 2)>(droid.getX()- droid.getBitmap().getWidth() / 2) &&
						(g.getX() - g.getBitmap().getWidth() / 2)<(droid.getX()+ droid.getBitmap().getWidth() / 2)  &&
						(g.getY() + g.getBitmap().getWidth() / 2)>(droid.getY()- droid.getBitmap().getHeight() / 2)  &&
						(g.getY() - g.getBitmap().getWidth() / 2)<(droid.getY()+ droid.getBitmap().getHeight() / 2)) {

					State=0;
				}

			}
			 */

			// commands for each ghost
			for(ghost g : ghosts)
			{
				//set of if statements to bounce ghosts off walls
				//going right
				if (g.getSpeed().getxDirection() == Speed.DIRECTION_RIGHT
						&& g.getX() + g.getBitmap().getWidth() / 2 >= getWidth()) {
					g.getSpeed().flipXDirection();
				}
				// left
				if (g.getSpeed().getxDirection() == Speed.DIRECTION_LEFT
						&& g.getX() - g.getBitmap().getWidth() / 2 <= 10) {
					g.getSpeed().flipXDirection();
				}
				// down
				if (g.getSpeed().getyDirection() == Speed.DIRECTION_DOWN
						&& g.getY() + g.getBitmap().getHeight() / 2 >= getHeight()-120) {
					g.getSpeed().flipYDirection();
				}
				// up
				if (g.getSpeed().getyDirection() == Speed.DIRECTION_UP
						&& g.getY() - g.getBitmap().getHeight() / 2 <= 10) {
					g.getSpeed().flipYDirection();
				}

				//changing direction and speed randomly
				double r=Math.random();
				double r2=Math.random();
				if(r>.995)
				{
					g.getSpeed().flipXDirection();
					if(r2>.75&&g.getSpeed().getXv()<4)
						g.getSpeed().setXv((float) r*3+g.getSpeed().getXv());
					else if(r2<.25&&g.getSpeed().getXv()>3)
						g.getSpeed().setXv((float) -r*3 + g.getSpeed().getXv());	
				}

				if(r<.005)
				{
					g.getSpeed().flipYDirection();
					if(r2>.75&&g.getSpeed().getYv()<4)
						g.getSpeed().setYv((float) r*3+g.getSpeed().getYv());
					else if(r2<.25&&g.getSpeed().getYv()>4)
						g.getSpeed().setYv((float) r*-3 + g.getSpeed().getYv());
				}

				if(g.getSpeed().getYv()<1) //avoiding ghosts stuck at top/side glitch, keeps speed above 1
					g.getSpeed().setYv(1);
				if(g.getSpeed().getXv()<1) //sides
					g.getSpeed().setXv(1);


				//continue motion
				g.update();
			}


			if(b1.isTouched()) //FIRE button press
			{
				b1.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.fire_button_tchd));
				if(droid.getSpeed().getxDirection()==1 && shootTime<now-250)
				{
					bullet a=new bullet(BitmapFactory.decodeResource(getResources(), R.drawable.bullet));
					a.setX(droid.getX()+30);
					a.setY(droid.getY()-15);
					a.setSpeed(new Speed(15, 0));
					a.setFired(true);
					bullets.add(a);
					shootTime=System.currentTimeMillis();

				}
				if(droid.getSpeed().getxDirection()==-1 && shootTime<now-250)
				{
					bullet a=new bullet(BitmapFactory.decodeResource(getResources(), R.drawable.bullet));
					a.setX(droid.getX()-30);
					a.setY(droid.getY()-15);
					a.setSpeed(new Speed(15, 0));
					a.getSpeed().flipXDirection();
					a.setFired(true);
					bullets.add(a);
					shootTime=System.currentTimeMillis();
				}
			}
			else
				b1.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.fire_button));

			for(bullet e : bullets)
			{
				e.update();
				if(e.getX()>getWidth() || e.getX()<0 ) //kill if offscreen
					e.setY(3000);
			}

		}

		if(State==0)
		{


		}
	}
}


