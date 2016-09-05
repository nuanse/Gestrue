package com.example.gestdemo;

import android.content.Context;
import android.graphics.PointF;
import android.view.MotionEvent;

public class MoveGestureDetector extends BaseGestureDetector {
	
 
	public interface OnMoveGestureListener {
		public boolean onMove(MoveGestureDetector detector);
		public boolean onMoveBegin(MoveGestureDetector detector);
		public void onMoveEnd(MoveGestureDetector detector);
	}
	
	 
	public static class SimpleOnMoveGestureListener implements OnMoveGestureListener {
	    public boolean onMove(MoveGestureDetector detector) {
	        return false;
	    }

	    public boolean onMoveBegin(MoveGestureDetector detector) {
	        return true;
	    }

	    public void onMoveEnd(MoveGestureDetector detector) {
	    	// Do nothing   
	    }
	}

    private static final PointF FOCUS_DELTA_ZERO = new PointF();
    
    private final OnMoveGestureListener mListener;
    
    private PointF mCurrFocusInternal;
    private PointF mPrevFocusInternal;  
    private PointF mFocusExternal = new PointF();
    private PointF mFocusDeltaExternal = new PointF();
    

    public MoveGestureDetector(Context context, OnMoveGestureListener listener) {
    	super(context);
        mListener = listener;
    }

    @Override
    protected void handleStartProgressEvent(int actionCode, MotionEvent event){
        switch (actionCode) { 
            case MotionEvent.ACTION_DOWN: 
                resetState(); 
                
                mPrevEvent = MotionEvent.obtain(event);
                mTimeDelta = 0;

                updateStateByEvent(event);
                break;
            
            case MotionEvent.ACTION_MOVE:
                mGestureInProgress = mListener.onMoveBegin(this);
                break;
        }
    }
    
    @Override
    protected void handleInProgressEvent(int actionCode, MotionEvent event){ 	
        switch (actionCode) {
        	case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mListener.onMoveEnd(this);
                resetState();
                break;

            case MotionEvent.ACTION_MOVE:
                updateStateByEvent(event);

				 
                if (mCurrPressure / mPrevPressure > PRESSURE_THRESHOLD) {
                    final boolean updatePrevious = mListener.onMove(this);
                    if (updatePrevious) {
                        mPrevEvent.recycle();
                        mPrevEvent = MotionEvent.obtain(event);
                    }
                }
                break;
        }
	}
    
    protected void updateStateByEvent(MotionEvent curr) {
    	super.updateStateByEvent(curr);

    	final MotionEvent prev = mPrevEvent;
    	
        
        mCurrFocusInternal = determineFocalPoint(curr);
        mPrevFocusInternal = determineFocalPoint(prev);
        
        
        boolean mSkipNextMoveEvent = prev.getPointerCount() != curr.getPointerCount();
        mFocusDeltaExternal = mSkipNextMoveEvent ? FOCUS_DELTA_ZERO : new PointF(mCurrFocusInternal.x - mPrevFocusInternal.x,  mCurrFocusInternal.y - mPrevFocusInternal.y);
        
        
        mFocusExternal.x += mFocusDeltaExternal.x;
        mFocusExternal.y += mFocusDeltaExternal.y;        
    }

 
    private PointF determineFocalPoint(MotionEvent e){
    	// Number of fingers on screen
        final int pCount = e.getPointerCount(); 
        float x = 0f;
        float y = 0f;
        
        for(int i = 0; i < pCount; i++){
        	x += e.getX(i);
        	y += e.getY(i);
        }
        
        return new PointF(x/pCount, y/pCount);
    }

    public float getFocusX() {
        return mFocusExternal.x;
    }

    public float getFocusY() {
        return mFocusExternal.y;
    }

    public PointF getFocusDelta() {
		return mFocusDeltaExternal;
    }

}
