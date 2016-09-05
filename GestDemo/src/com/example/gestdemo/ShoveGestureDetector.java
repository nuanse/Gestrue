package com.example.gestdemo;

import android.content.Context;
import android.view.MotionEvent;
 
public class ShoveGestureDetector extends TwoFingerGestureDetector {

 
	public interface OnShoveGestureListener {
		public boolean onShove(ShoveGestureDetector detector);
		public boolean onShoveBegin(ShoveGestureDetector detector);
		public void onShoveEnd(ShoveGestureDetector detector);
	}
	
	/**
	 * Helper class which may be extended and where the methods may be
	 * implemented. This way it is not necessary to implement all methods
	 * of OnShoveGestureListener.
	 */
	public static class SimpleOnShoveGestureListener implements OnShoveGestureListener {
	    public boolean onShove(ShoveGestureDetector detector) {
	        return false;
	    }

	    public boolean onShoveBegin(ShoveGestureDetector detector) {
	        return true;
	    }

	    public void onShoveEnd(ShoveGestureDetector detector) {
	    	// Do nothing 
	    }
	}

	private float mPrevAverageY;
	private float mCurrAverageY;
	
    private final OnShoveGestureListener mListener;
    private boolean mSloppyGesture;

    public ShoveGestureDetector(Context context, OnShoveGestureListener listener) {
    	super(context);
        mListener = listener;
    }

    @Override
    protected void handleStartProgressEvent(int actionCode, MotionEvent event){
        switch (actionCode) {
            case MotionEvent.ACTION_POINTER_DOWN:
            	
                resetState();  
                mPrevEvent = MotionEvent.obtain(event);
                mTimeDelta = 0;
                
                updateStateByEvent(event);
                
                
                mSloppyGesture = isSloppyGesture(event);
                if(!mSloppyGesture){
                	 
                    mGestureInProgress = mListener.onShoveBegin(this);
                } 
            	break;
            
            case MotionEvent.ACTION_MOVE:
                if (!mSloppyGesture) {
                	break;
                }
                
                mSloppyGesture = isSloppyGesture(event);
                if(!mSloppyGesture){
                	 
                    mGestureInProgress = mListener.onShoveBegin(this);
                }
    
                break;
                
            case MotionEvent.ACTION_POINTER_UP:
                if (!mSloppyGesture) {
                	break;
                }
           
                break; 
        }
    }

    
    @Override
    protected void handleInProgressEvent(int actionCode, MotionEvent event){ 	
        switch (actionCode) {
            case MotionEvent.ACTION_POINTER_UP:
               
                updateStateByEvent(event);

                if (!mSloppyGesture) {
                    mListener.onShoveEnd(this);
                }

                resetState();
                break;

            case MotionEvent.ACTION_CANCEL:
                if (!mSloppyGesture) {
                    mListener.onShoveEnd(this);
                }

                resetState();
                break;

            case MotionEvent.ACTION_MOVE:
                updateStateByEvent(event);

                if (mCurrPressure / mPrevPressure > PRESSURE_THRESHOLD
                		&& Math.abs(getShovePixelsDelta()) > 0.5f) {
                    final boolean updatePrevious = mListener.onShove(this);
                    if (updatePrevious) {
                        mPrevEvent.recycle();
                        mPrevEvent = MotionEvent.obtain(event);
                    }
                }
                break;
        }
    }

    @Override
    protected void resetState() {
        super.resetState();
        mSloppyGesture = false;
        mPrevAverageY = 0.0f;
        mCurrAverageY = 0.0f;
    }
    
    @Override
    protected void updateStateByEvent(MotionEvent curr){
		super.updateStateByEvent(curr);
		
		final MotionEvent prev = mPrevEvent;
		float py0 = prev.getY(0);
		float py1 = prev.getY(1);
		mPrevAverageY = (py0 + py1) / 2.0f;
		
		float cy0 = curr.getY(0);
		float cy1 = curr.getY(1);
		mCurrAverageY = (cy0 + cy1) / 2.0f;
	}
    
    @Override
    protected boolean isSloppyGesture(MotionEvent event){
    	boolean sloppy = super.isSloppyGesture(event);
    	if (sloppy)
    		return true;
    	
    	double angle = Math.abs(Math.atan2(mCurrFingerDiffY, mCurrFingerDiffX));
    	//about 20 degrees, left or right
    	return !(( 0.0f < angle && angle < 0.35f)
    			|| 2.79f < angle && angle < Math.PI); 
    }


    /**
     * Return the distance in pixels from the previous shove event to the current
     * event. 
     * 
     * @return The current distance in pixels.
     */
	public float getShovePixelsDelta() {
		return mCurrAverageY - mPrevAverageY;
	}
}
