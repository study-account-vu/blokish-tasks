package org.scoutant.blokish;

import org.scoutant.blokish.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.RotateAnimation;

/**
 * Controls an animated spinner on a view while background work is in progress.
 * Visibility changes are posted to the UI thread through the associated handler.
 */
public class BusyIndicator {
	private View view;
	private Handler uiHandler;
	private boolean visible=false;
	private Drawable drawable;
	private RotateAnimation animation;
	
	/**
	 * Creates an initially hidden indicator for the supplied view.
	 *
	 * @param ctx context used to load the spinner drawable
	 * @param view view whose background and visibility represent the indicator
	 */
	public BusyIndicator(Context ctx, View view){
		this.view = view;
		view.setVisibility(View.INVISIBLE);
		uiHandler = new Handler();
		this.drawable =  ctx.getResources().getDrawable(R.drawable.spinner_blue_76);	
		animation = new RotateAnimation(0, 360, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setRepeatCount(Animation.INFINITE);
		final int cycles = 12;
		animation.setInterpolator(new Interpolator(){
			/**
			 * Converts linear animation progress into fixed visual rotation steps.
			 * @param input normalized animation progress
			 * @return stepped progress value
			 */
			public float getInterpolation(float input) {
				return ((int)(input * cycles)) / (float) cycles;
			}
		});
		animation.setDuration(1800);
		animation.setStartTime(RotateAnimation.START_ON_FIRST_FRAME);
		animation.setStartOffset(0);
	}
	
	/** Makes the indicator visible and starts its repeating rotation if needed. */
	public void show(){
		this.visible = true;
		uiHandler.post(new Runnable(){
			/** Applies the latest visible request and starts rotation when appropriate. */
			public void run() {
				view.setVisibility( View.VISIBLE);
				if(BusyIndicator.this.visible){
					view.setBackground(drawable);
					if(view.getAnimation() == null){
						view.startAnimation(animation);
					}
				}
			}
		});
	}
	
	/** Hides the indicator and clears its current animation. */
	public void hide(){
		this.visible = false;
		uiHandler.post(new Runnable(){
			/** Hides the indicator and clears its animation on the UI thread. */
			public void run() {
				view.setVisibility(View.INVISIBLE);
				if(view.getAnimation() != null){
					view.clearAnimation();
				}
			}
		});
	}
}
