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
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


@SuppressLint("ClickableViewAccessibility") public class MainGamePanel extends SurfaceView implements
SurfaceHolder.Callback {

	private static final String TAG = MainGamePanel.class.getSimpleName();

	private MainThread thread;
	private Droid droid;
	private button b1;
	private button b2;
	private button startB;
	private ArrayList<ghost> ghosts;
	private ArrayList<bullet> bullets;
	private ArrayList<money> moneys;
	private ArrayList<barrier> barriers;
	private long shootTime; //time between bullet fires
	private long ghostTime;  //time between ghost regen
	private long barrierTime;
	private long barrierHit;  //time between barrier hit
	private int State; //0 paused, 1 running
	private int kills; //ghost kills
	private int moneyCount; //money collected
	private int score; //money collected
	
	private boolean start;

	public MainGamePanel(Context context) {
		super(context);
		State=0; //start @ paused state
	//	kills=0; 
	//	score=0;
	//	moneyCount = 0; 
		ghostTime=0;

		barriers=new ArrayList<barrier>();
		// adding the callback (this) to the surface holder to intercept events
		getHolder().addCallback(this);

		barrier d=new barrier(BitmapFactory.decodeResource(getResources(), R.drawable.barrier), 400, 400);
		barriers.add(d);

		// create droid and load bitmap
		droid = new Droid(BitmapFactory.decodeResource(getResources(), R.drawable.hunter_right), 400, 200);
		ghosts=new ArrayList<ghost>();
		bullets=new ArrayList<bullet>();
		moneys=new ArrayList<money>();
 
		//to be implemented?
		int width=getWidth();
		int height= getHeight();
		
		Log.d("ADebugTag", "Value: " + Integer.toString(width));
		b1=new button(BitmapFactory.decodeResource(getResources(), R.drawable.fire_button), 700, 1120); 
		b2=new button(BitmapFactory.decodeResource(getResources(), R.drawable.barrier_button), 560, 1120);
		startB=new button(BitmapFactory.decodeResource(getResources(), R.drawable.newbutton), 400, 200); 



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
					b2.handleActionDown((int)event.getX(i), (int)event.getY(i));

				}

				// (aka button press while moving guy)
				if(event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN)
				{
					b1.handleActionDown((int)event.getX(i), (int)event.getY(i));
					b2.handleActionDown((int)event.getX(i), (int)event.getY(i));

				}

				//TOUCH OFF
				if (event.getAction() == MotionEvent.ACTION_UP) {
					// touch was released
					if (droid.isTouched()) 
						droid.setTouched(false);

					if(b1.isTouched())
						b1.setTouched(false);
					if(b2.isTouched())
						b2.setTouched(false);
				}
				//SECOND TOUCH OFF
				if(event.getActionMasked() == MotionEvent.ACTION_POINTER_UP)
				{
					if(b1.isTouched())
						b1.setTouched(false);
					if(b2.isTouched())
						b2.setTouched(false);
				}
			}
		}
		if(State==0)
		{
			//set difficulty
			//start game
			int pointerCount = event.getPointerCount();

			for (int i = 0; i < pointerCount; i++)
			{	
				if (event.getAction() == MotionEvent.ACTION_DOWN) 
					startB.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.newbutton_tchd));

				if (event.getAction() == MotionEvent.ACTION_UP) 
				{
					startB.handleActionDown((int)event.getX(i), (int)event.getY(i));
					startB.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.newbutton));
				}
			}

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
			b2.draw(canvas);

			for(bullet b : bullets)
				b.draw(canvas);

			for(barrier c : barriers)
				c.draw(canvas);

			for(money p : moneys)
				p.draw(canvas);

			//text!
			Paint paint = new Paint(); 

			paint.setColor(Color.WHITE); 
			paint.setTextSize(30); 
			canvas.drawText("Kills: " + Integer.toString(kills), 10, 25, paint); 
			canvas.drawText("Money: $" + Integer.toString(moneyCount), 600, 25, paint); 
			paint.setTextSize(40);
			canvas.drawText("Score: " + Integer.toString(score), 250, 40, paint); 
		}
		if(State==0)
		{
			canvas.drawColor(Color.BLACK);
			startB.draw(canvas);
			Paint paint = new Paint(); 

			paint.setColor(Color.WHITE); 
			paint.setTextSize(40); 
			canvas.drawText("Ghosts killed: " + Integer.toString(kills), 250, 350, paint); 
			canvas.drawText("Total Score: " + Integer.toString(score), 250, 450, paint); 
			paint.setTextSize(25); 
			canvas.drawText("Instructions: Kill ghosts, grab some $, and stay alive to score big.", 40, 550, paint);
			canvas.drawText("You begin with $50, and can collect coins worth $5 to $15. ", 40, 580, paint);
			canvas.drawText("Bullets cost $1, barriers cost $20.  Ghost kills are worth 300 points.", 40, 610, paint);
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
			startB.setTouched(false);
			score++;
			
			if(start) //setting the initial money
			{
				moneyCount=50;
				start=false;
			}

			long now=System.currentTimeMillis();

			//add ghost every 3 sec
			if(ghostTime < now-2000 || now < 300)
			{

				double r1= Math.random();
				double r2= Math.random();
				int randY= (int)r2*1000;
				ghost a=new ghost(BitmapFactory.decodeResource(getResources(), R.drawable.ghost_1), 2000 ,randY);
				if(r1>.5)
					a.setX(-1000); //left

				a.setSpeed(new Speed((float) r1*3 + 1,(float) r1*3 + 1));
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
						double r1= Math.random();
						double rand=  r1*10;
						g.setSpeed(new Speed(0,0));
						money m=new money(BitmapFactory.decodeResource(getResources(), R.drawable.money), g.getX(), g.getY(), 
								(int) rand + 5);
						moneys.add(m);
						g.setX(2000); //ghost killed

						b.setY(3000); //bullet killed
						kills++; //add to kills
						score+=300;  //add to score
					}

				}
			}


			//barrier collision with ghosts  **UNDER CONSTRUCTION**

			for(barrier c: barriers)
			{
				for(ghost g: ghosts)
				{
					if ((c.getX() + c.getBitmap().getWidth() / 2)>(g.getX()- g.getBitmap().getWidth() / 2 ) &&
							( c.getX() - c.getBitmap().getWidth() / 2)<(g.getX()+ g.getBitmap().getWidth() / 2 )&&
							(10+c.getY() + c.getBitmap().getHeight() / 2)>(g.getY()- g.getBitmap().getHeight() / 2 )  &&
							(-10+c.getY() - c.getBitmap().getHeight() / 2)<(g.getY()+ g.getBitmap().getHeight() / 2 ) &&
							now-150 > barrierHit)
					{
						if((c.getY() + c.getBitmap().getHeight() / 2)<(-3+g.getY()- g.getBitmap().getHeight() / 2 )  ||
								(c.getY() - c.getBitmap().getHeight() / 2)>(3+g.getY()+ g.getBitmap().getHeight() / 2 ))
						{
							g.getSpeed().flipYDirection();
							barrierHit=now;
						}
						else
						{
							g.getSpeed().flipXDirection();
							barrierHit=now;
						}

					}

				}
			}

			//person collision with ghosts
			for(ghost g : ghosts)
			{
				if ((g.getX() + g.getBitmap().getWidth() / 2)>(droid.getX()- droid.getBitmap().getWidth() / 2) &&
						(g.getX() - g.getBitmap().getWidth() / 2)<(droid.getX()+ droid.getBitmap().getWidth() / 2)  &&
						(g.getY() + g.getBitmap().getWidth() / 2)>(droid.getY()- droid.getBitmap().getHeight() / 2)  &&
						(g.getY() - g.getBitmap().getWidth() / 2)<(droid.getY()+ droid.getBitmap().getHeight() / 2)) {
					State=0;
					droid.setX(400);
					droid.setY(200);
				}
			}


			//person collision with money
			for(money g : moneys)
			{
				if ((g.getX() + g.getBitmap().getWidth() / 2)>(droid.getX()- droid.getBitmap().getWidth() / 2) &&
						(g.getX() - g.getBitmap().getWidth() / 2)<(droid.getX()+ droid.getBitmap().getWidth() / 2)  &&
						(g.getY() + g.getBitmap().getWidth() / 2)>(droid.getY()- droid.getBitmap().getHeight() / 2)  &&
						(g.getY() - g.getBitmap().getWidth() / 2)<(droid.getY()+ droid.getBitmap().getHeight() / 2)) {
					g.setX(2000);
					moneyCount+=g.getValue();
				}
			}


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

			//shooting bullets
			if(b1.isTouched()  && shootTime<now-250 &&  moneyCount>0) 
			{
				b1.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.fire_button_tchd));
				bullet a=new bullet(BitmapFactory.decodeResource(getResources(), R.drawable.bullet));
				a.setY(droid.getY()-15);
				a.setSpeed(new Speed(15, 0));
				a.setFired(true);
				shootTime=System.currentTimeMillis();
				moneyCount-=1;

				if(droid.getSpeed().getxDirection()==1)
					a.setX(droid.getX()+30);
				if(droid.getSpeed().getxDirection()==-1)
				{
					a.setX(droid.getX()-30);
					a.getSpeed().flipXDirection();
				}
				bullets.add(a);
			}
			else if(shootTime< now-300)
				b1.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.fire_button));

			//dropping barriers
			if(b2.isTouched() && barrierTime < now-1000 && moneyCount>19)
			{
				b2.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.barrier_button_tchd));
				barrier d=new barrier(BitmapFactory.decodeResource(getResources(), R.drawable.barrier), droid.getX(), 
						droid.getY());
				barriers.add(d);
				barrierTime=System.currentTimeMillis();
				moneyCount-=20;
			}
			else if(barrierTime< now-300)
				b2.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.barrier_button));

			for(bullet e : bullets)
			{
				e.update();
				if(e.getX()>getWidth() || e.getX()<0 ) //kill if offscreen
					e.setY(3000);
			}

		}

		if(State==0)
		{
			if(startB.isTouched())
			{
				State=1;
				moneyCount=0;
				kills=0;
				score=0;
				b1.setTouched(false);
			}
			if(!startB.isTouched())
				State=0;
			ghosts.clear();
			moneys.clear();
			bullets.clear();
			barriers.clear();
			start=true;


		}
	}
}


