package com.example.simplegameengine;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class MainActivity extends Activity {

    GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initialize gameBiew and set it as the view
        gameView = new GameView(this);
        setContentView(gameView);
    }

    class GameView extends SurfaceView implements Runnable {
        //get device screen size in pixel
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

        //this is threat
        Thread gameThread = null;

        //i don't understand this
        SurfaceHolder ourHolder;

        //boolean for game is running/not
        volatile boolean playing;

        //Canvas and Paint object
        Canvas canvas;
        Paint paint;

        //this variable tracks game fps
        long fps;
        //this help calculating fps
        private long timeThisFrame;

        //declare an object type Bitmap
        Bitmap bitmapDoraemon;
        //declare the final bitmap object
        Bitmap finalBitmapDoraemon;
        //for double checking direction bitmap/image
        boolean isAlreadyRight = true;

        //boolean if slime move or not
        boolean isMoving = false;
        //boolean for moving direction
        boolean isGoToRight = true; //mean slime is moving to right

        //slime walking speed
        float walkSpeedPerSecond = 150;

        //starting position
        float doraemonXPosition = 10;

        //new sprite sheet animation

        //this is frame size
        private int frameWidth = 300;
        private int frameHeight = 300;

        //how many frame is sprite sheet
        private int frameCount = 6;

        //start fisrt frame
        private int currentFrame = 1;

        //what time was it when we last changed frmaes
        private long lastFrameChangeTime = 0;

        //how should each frame last
        private int frameLengthInMilliSeconds = 150;

        //a rectangle to define area of the sprite sheet
        //that represents 1 frame
        private Rect frameToDraw = new Rect(0,0,frameWidth,frameHeight);

        //a rect that defines an area of the screen
        //on which to draw
        private int theTop = 500;
        RectF whereToDraw = new RectF (doraemonXPosition,theTop , doraemonXPosition + frameWidth, frameHeight + theTop);

        //Constructor
        public GameView(Context context){
            //asking survace class to setup our object
            //How kind
            super(context);

            //initialize this two object
            ourHolder = getHolder();
            paint = new Paint();

            //load png file
            bitmapDoraemon = BitmapFactory.decodeResource(this.getResources(),R.drawable.doraemon);
            //scale bitmap to correct size
            bitmapDoraemon = Bitmap.createScaledBitmap(bitmapDoraemon, frameWidth*frameCount,frameHeight, false);
            //flip bitmap
            finalBitmapDoraemon = flipImage(bitmapDoraemon);

            //setboolean to true - game start
            playing = true;
        }

        @Override
        public void run(){
            while (playing == true){
                //capture the time in millisecond in startFrameTime
                long startFrameTime = System.currentTimeMillis();

                //update frame
                update();
                //draw the updated frame
                draw();

                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if(timeThisFrame > 0){
                    fps = 1000 / timeThisFrame;
                }
            }
        }

        //everything that needs to be updated goes here, same like unity void update()
        public void update(){
            //changing slime moving direction
            if(doraemonXPosition + frameWidth >= screenWidth){
                //change slime direction to go left
                isGoToRight = false;
                //flip bitmap to the left
                finalBitmapDoraemon = flipImage(bitmapDoraemon);
            }
            else if(doraemonXPosition <= 0){
                //change slime direction to go right
                isGoToRight = true;
                //flip bitmap to the right
                finalBitmapDoraemon = flipImage(bitmapDoraemon);
            }

            //if player touch screen, slime moving
            if(isMoving == true && isGoToRight == true){
                doraemonXPosition += (walkSpeedPerSecond / fps);
            }
            else if (isMoving == true && isGoToRight == false){
                doraemonXPosition -= (walkSpeedPerSecond / fps);
            }
        }

        public void getCurrentFrame(){
            long time = System.currentTimeMillis();
            //if moving
            if(isMoving == true){
                if(time > lastFrameChangeTime + frameLengthInMilliSeconds){
                    lastFrameChangeTime = time;
                    currentFrame++;
                    if(currentFrame >= frameCount){
                        currentFrame = 0;
                    }
                }
            }
            //if not moving change character to idle animation
            else if(isMoving == false){
                currentFrame = 1;
            }
            //update the left and right values of the source
            //of the next frmae on the sprite sheet
            frameToDraw.left = currentFrame * frameWidth;
            frameToDraw.right = frameToDraw.left + frameWidth;
        }

        //drawing screen in here
        public void draw(){
            //checking drawing surface is valid or will crash
            if(ourHolder.getSurface().isValid()){
                //lock the canvas ready to draw
                canvas = ourHolder.lockCanvas();

                //draw the bacnkground color
                canvas.drawColor(Color.argb(255,172,228,100));

                //choose the brush color for drawing and writing
                paint.setColor(Color.argb(255,249,129,0));

                //change text size
                paint.setTextSize((50));

                //display current fps
                canvas.drawText("FPS : " + fps, 20, 50, paint);
                //display current screen size
                canvas.drawText("Screen Width  : " + screenWidth, 20, 100, paint);
                canvas.drawText("Screen Height : " + screenHeight, 20, 150, paint);
                //display slime direction
                if(isGoToRight == true){
                    canvas.drawText("Direction : Right", 20, 200, paint);
                }
                else if(isGoToRight == false){
                    canvas.drawText("Slime Direction : Left", 20, 200, paint);
                }

                //draw slime at (slimeXPosition, y px)
                whereToDraw.set((int)doraemonXPosition, theTop, (int)doraemonXPosition + frameWidth, frameHeight + theTop);
                getCurrentFrame();
                canvas.drawBitmap(finalBitmapDoraemon, frameToDraw, whereToDraw, paint);

                //draw everything in the screen
                ourHolder.unlockCanvasAndPost((canvas));
            }
        }

        //for fliping bitmap
        public Bitmap flipImage(Bitmap source){
            //declare matrix for fliping/Rotate Bitmap
            Matrix matrix = new Matrix();
            //flip right
            if(isGoToRight == true && isAlreadyRight == false){
                matrix.preScale(1.0f,1.0f);
                isAlreadyRight = true;
            }
            //flip left
            else if(isGoToRight == false && isAlreadyRight == true){
                matrix.preScale(-1.0f,1.0f);
                isAlreadyRight = false;
            }
            else{
                return source;
            }
           return Bitmap.createBitmap(source, 0, 0,source.getWidth(),source.getHeight(), matrix,true);
        }

        //if this activity is Started then
        //start our thread
        public void pause(){
            playing = false;
            try{
                gameThread.join();
            } catch (InterruptedException e){
                Log.e("Error: ", "Joining thread");
            }
        }

        //if this activity is started then
        //start our thread
        public void resume(){
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        //detect user touch, same like unity
        @Override
        public boolean onTouchEvent(MotionEvent motionEvent){
            switch (motionEvent.getAction() & motionEvent.ACTION_MASK){
                //player touch screen
                case MotionEvent.ACTION_DOWN:
                    isMoving = true;
                    break;

                //player untouch the screen
                case MotionEvent.ACTION_UP:
                    isMoving = false;
                    break;
            }
            return true;
        }
    }
    //end of GameView inner class


    //This method executes when the player start the game
    @Override
    protected void onResume(){
        super.onResume();
        //tell the gameView to resume method to execute
        gameView.resume();
    }

    //this method executes when player quits the game
    @Override
    protected void onPause(){
        super.onPause();
        //tell the gameView to pause method execute
        gameView.pause();
    }
}












