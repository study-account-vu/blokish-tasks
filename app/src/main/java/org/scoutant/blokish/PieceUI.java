
package org.scoutant.blokish;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import org.scoutant.blokish.model.Piece;
import org.scoutant.blokish.model.Square;

import java.util.Calendar;

import androidx.core.content.ContextCompat;

/**
 * Interactive rendering of one model piece, used both in a player's tray and on the board.
 * Touch gestures move, rotate, and flip the piece; placement validity is delegated to
 * the owning {@link GameView}'s game model.
 */
public class PieceUI extends FrameLayout implements OnTouchListener, OnLongClickListener, Comparable<PieceUI> {

  public static final int PADDING = 4;
  private static final String tag = "activity";
  private Drawable disc;
  private Drawable disc_ok;
  private ImageButton ok;

  private Resources resources;
  private Drawable square;
  private Drawable square_bold;
  private Canvas canvas;
  private int size;
  private int footprint;
  private int df;

  public Piece piece;
  public int i0;
  public int j0;

  public int i;
  public int j;

  private int localX=0;
  private int localY=0;

  public static int[] icons = { R.drawable.red, R.drawable.green, R.drawable.blue, R.drawable.orange };
  public static int[] icons_bold = { R.drawable.red_bold, R.drawable.green_bold, R.drawable.blue_bold, R.drawable.orange_bold };

  public boolean movable=true;
  public boolean moving=false;
  private boolean rotating = false;

  private Paint paint = new Paint();

  public int swipeX=0;
  private int downX;
  private int downY;
  private int angle=0;
  private int radius=0;
  private double rDown=0;

  private float rawX;
  private float rawY;
  private int oo;
  private Context context;
  private Vibrator vibrator;

  private Animation animation;
  private int statusBarHeight=-1;
  private Matrix m = new Matrix();
  private boolean isOk=false;

  private static int grey = 0x99999999;
  private static int green = 0x3333ee33;

  /**
   * Initializes gesture handling and display-dependent drawing dimensions.
   * @param context context used to access display and system services
   */
  protected PieceUI(Context context) {
    super(context);
    this.context = context;
    setWillNotDraw(false);
    setOnLongClickListener(this);
    setOnTouchListener(this);
    setOnClickListener( new DoubleTapListener());
    resources = context.getApplicationContext().getResources();
    Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    Point pointSize = new Point();
    display.getSize(pointSize);
    int width = pointSize.x;
    size = width/20;
    vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    animation = AnimationUtils.loadAnimation(context, R.anim.wave_scale);
    paint.setColor(grey);
  }

  /** Converts a quick second tap into activation of the shared confirm control. */
  private class DoubleTapListener implements OnClickListener {
    long time = SystemClock.currentThreadTimeMillis();
    /**
     * Activates confirmation when two taps occur within the gesture interval.
     * @param v view receiving the click
     */
    @Override
    public void onClick(View v) {
      long t = Calendar.getInstance().getTimeInMillis();
      long elapse = t-time;
      if (elapse<300) {
        Log.d("click", "TAP");
        if (ok!=null) ok.callOnClick();
        if (vibrator!=null) vibrator.vibrate(20);
      }
      time = t;
    }
  }

  /**
   * Creates a view for a model piece, loading color-specific artwork and shape metrics.
   *
   * @param context context used to access display services and piece resources
   * @param piece model piece represented by this view
   */
  public PieceUI(Context context, Piece piece) {
    this(context);
    this.piece = piece;
    footprint = piece.size;
    df = Math.max(footprint, 3);
    oo = ( footprint>2? 1 : 0);
    if (footprint==5) oo = 2;
    radius = PADDING*size + footprint*size/2;
    square = getDrawable( icons[piece.color]);
    square_bold = getDrawable( icons_bold[piece.color]);
    resetLocalXY();

    disc = getDrawable( R.drawable.disc);
    disc_ok = getDrawable( R.drawable.disc_ok);
  }

  /**
   * Resolves a color resource using this view's themed context.
   * @param id color resource identifier
   * @return resolved color value
   */
  protected int getColor( int id) {
    return ContextCompat.getColor( getContext(), id);
  }

  /**
   * Resolves a drawable resource using this view's themed context.
   * @param id drawable resource identifier
   * @return resolved drawable, or {@code null} if unavailable
   */
  protected Drawable getDrawable( int id) {
    return ContextCompat.getDrawable( getContext(), id);
  }


  /**
   * Creates a piece view and positions it at its initial tray origin.
   *
   * @param context context used to create the view
   * @param piece model piece to display
   * @param i initial horizontal tray coordinate in cells
   * @param j initial vertical tray coordinate in cells
   */
  public PieceUI( Context context, Piece piece, int i, int j){
    this(context, piece);
    i0=i;
    j0=j;
    replace();
    setVisibility(INVISIBLE);
  }

  /**
   * Creates a tray piece view connected to the game's shared confirmation button.
   *
   * @param context context used to create the view
   * @param piece model piece to display
   * @param i initial horizontal tray coordinate in cells
   * @param j initial vertical tray coordinate in cells
   * @param ok confirmation control activated by a double tap
   */
  public PieceUI( Context context, Piece piece, int i, int j, ImageButton ok){
    this(context, piece, i, j);
    this.ok = ok;
  }

    /** Moves the view to the supplied origin and marks the piece as placed on the board. */
    private void place(int i, int j){
    move(i, j);
    place();
  }

  /**
   * Places the view on the board and optionally plays its placement animation.
   * @param i horizontal board origin in cells
   * @param j vertical board origin in cells
   * @param animate whether to play the placement animation
   */
  public void place(int i, int j, boolean animate){
    place(i, j);
    if (animate) {
      this.startAnimation(animation);
    }
  }

  /** Marks the piece as used and makes its view visible on the board. */
  public void place(){
    movable=false;
    setVisibility(VISIBLE);
  }

  /** Restores the piece to its tray origin and clears its temporary rotation state. */
  public void replace(){
    rotating=false;
    move(i0, j0);
  }


  /**
   * Sets the piece origin in cell coordinates and schedules a redraw.
   * @param i horizontal origin in cells
   * @param j vertical origin in cells
   */
  public void move(int i, int j) {
    this.i=i;
    this.j=j;
    doLayout();
    invalidate();
  }

  /** Re-centers the pointer anchor for the piece's current footprint. */
  private void resetLocalXY(){
    localX=PADDING*size + footprint*size/2;
    localY=PADDING*size + footprint*size/2;
    if (footprint==4) localX += size;
    localY += 2*size;
  }

  /**
   * Offsets this tray piece by a pixel-based horizontal swipe amount.
   * @param x horizontal swipe offset in pixels
   */
  public void swipe(int x) {
    swipeX = (x+size/2)/size;
    bringToFront();
    doLayout();
    invalidate();
  }

  /** Converts board-cell or tray coordinates into this view's pixel frame and margins. */
  private void doLayout() {
    FrameLayout.LayoutParams layout;
    if (j>20) {
      layout = new FrameLayout.LayoutParams(df*size, df*size, Gravity.TOP);
      layout.leftMargin = (i-1)*size;
      if (!moving) layout.leftMargin -= swipeX*size;
      layout.topMargin  = (j-1)*size;
    } else {
      layout = new FrameLayout.LayoutParams( 2*radius, 2*radius, Gravity.TOP);
      layout.leftMargin = (i-PADDING-1)*size;
      layout.topMargin  = (j-PADDING-1)*size;
      if (footprint<=2) {
        layout.leftMargin = (i-PADDING)*size;
        layout.topMargin  = (j-PADDING)*size;
      }
      if (footprint==5) {
        layout.leftMargin = (i-PADDING-2)*size;
        layout.topMargin  = (j-PADDING-2)*size;
      }
    }
    setLayoutParams(layout);
  }


  /**
   * Draws the piece, its placement handles, and its color-specific board cells.
   * @param canvas drawing surface supplied by the view system
   */
  @Override
  protected void onDraw(Canvas canvas) {
    if (rotating) {
      m.setRotate(angle, radius, radius);
      canvas.concat(m);
    } else {
      doLayout();
    }
    gotCanvas(canvas);
    if (movable && j<20) {
      Drawable d = isOk ? disc_ok : disc;
      d.setBounds(0, 0, getWidth(), getHeight());
      d.draw(canvas);

      paint.setColor( isOk ? green : grey);
      canvas.drawCircle(radius, size, size, paint);
      canvas.drawCircle(radius, 2 * radius - size, size, paint);
      canvas.drawCircle(size, radius, size, paint);
      canvas.drawCircle(2 * radius - size, radius, size, paint);
    }
    if (j>20 && footprint==1) {
      for (Square s : piece.squares()) add( s.i+1, s.j+1);
    } else {
      for (Square s : piece.squares()) add( s.i, s.j);
    }
  }

  /** Retains the active drawing surface for the helper that renders individual piece cells. */
  /** Retains the active drawing surface for the helper that renders individual piece cells. */
  private void gotCanvas(Canvas canvas) {
    this.canvas = canvas;
  }
  

  /** Draws one local piece cell, highlighting the most recently placed piece when applicable. */
  private PieceUI add(int i, int j){
    GameView game = (GameView) this.getParent();
    if (game.lasts[piece.color] == this && this.j<=20) {
      square_bold.setBounds( new Rect((i+PADDING+oo)*size, (j+PADDING+oo)*size, (i+PADDING+oo+1)*size+1, (j+PADDING+oo+1)*size+1));
      square_bold.draw(canvas);
      return this;
    }
    if (this.j<=20) {
      square.setBounds( new Rect((i+PADDING+oo)*size+1, (j+PADDING+oo)*size+1, (i+PADDING+oo+1)*size, (j+PADDING+oo+1)*size));
    } else {
      square.setBounds( new Rect((i+oo)*size+1, (j+oo)*size+1, (i+oo+1)*size, (j+oo+1)*size));
    }
    square.draw(canvas);
    return this;
  }

  /**
   * Starts selection on a long press, or flips the already selected piece.
   * @param v view receiving the long press
   * @return {@code true} when the long press begins selection
   */
  public boolean onLongClick(View v) {
    if (!movable) return false;
    GameView game = (GameView) v.getParent();
    if (game.selected == null) {
      game.selected = this;
      return true;
    } else {
      if (!moving && !rotating) flip();
    }
    return false;
  }

  /**
   * Processes tray swipes, piece dragging, rotation gestures, and placement validation.
   * @param v view receiving the touch event
   * @param event pointer action and coordinates
   * @return {@code true} if the event is consumed by this listener
   */
  public boolean onTouch(View v, MotionEvent event) {
    if (statusBarHeight<0) {
      Rect decor = new Rect();
      ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(decor);
      statusBarHeight = decor.top;
      Log.i(tag, "status bar height is : " +  statusBarHeight);
    }

    GameView game = (GameView) getParent();
    int action = event.getAction();

    if (game.selected==null && !PreferenceManager.getDefaultSharedPreferences(context).getBoolean("ai", true) && piece.color!=game.ui.turn) {
      game.doTouch(event);
      return false;
    }

    if (game.selected==null) {
      if (action==MotionEvent.ACTION_DOWN) {
        rawX=event.getRawX();
        rawY=event.getRawY();
        rotating=false;
        game.doTouch(event);
        return false;
      }
      int dX = Float.valueOf( event.getRawX()-rawX).intValue();
      int dY = Float.valueOf( event.getRawY()-rawY).intValue();
      if ( movable==false || -dY < Math.abs(dX) ) {
        game.doTouch(event);
        return false;
      }
      game.selected = this;
      moving = true;
      return false;
    }
    if (action==MotionEvent.ACTION_DOWN) {
      localX = (int)event.getX();
      localY = (int)event.getY();
      downX = localX/size;
      downY = localY/size;
      if ( willRotate()) {
        rotating = true;
        rDown = Math.toDegrees( Math.atan2(event.getX()-radius, radius-event.getY()));
      } else {
        rotating=false;
      }
      bringToFront();
    }
    if (action==MotionEvent.ACTION_MOVE && game.selected==this) {
      bringToFront();


      if (rotating) {
        double r = Math.toDegrees( Math.atan2(event.getX()-radius, radius-event.getY()));
        int a = Double.valueOf( r-rDown).intValue();
        // Keep angle deltas on the shortest path when the pointer crosses the angular boundary.
        if (a>180) a-= 360;
        if (a<-180) a+= 360;
        if (angle==a) return false;
        angle = a;
        game.buttons.setOkState( false);
        setOkState( false);
      } else {
        int r = (footprint%2==0 ? radius-size/2 : radius);
        int newi = ((int) event.getRawX() - localX  + r)/size;
        int newj = ((int) event.getRawY() - statusBarHeight - localY + r)/size;
        if (i==newi && j==newj) return false;
        i=newi;
        j=newj;
        moving = true;

        boolean okState = game.game.valid(piece, i, j) && !game.thinking;
        game.buttons.setOkState( okState);
        setOkState( okState);
        if (okState && vibrator!=null) vibrator.vibrate(20);
      }
    }
    if (action==MotionEvent.ACTION_UP) {
      moving=false;
      rotating=false;
      rotateAgainstGrid();
      angle=0;
      resetLocalXY();
      if (j>20) {
        game.buttons.setVisibility( INVISIBLE);
        this.replace();
        game.selected=null;
      } else {
        game.buttons.setVisibility( VISIBLE);
        game.buttons.bringToFront();
        boolean okState = game.game.valid(piece, i, j) && !game.thinking;
        game.buttons.setOkState( okState);
        setOkState( okState);
      }
    }
    invalidate();
    return false;
  }

  /**
   * Updates the visual marker indicating whether the current placement can be confirmed.
   * @param value {@code true} to show a valid placement state
   */
  public void setOkState( boolean value) {
    this.isOk = value;
  }

  /** Snaps a completed free rotation to the nearest quarter turn. */
  private void rotateAgainstGrid(){
    if (angle>45) piece.rotate(1);
    if (angle>135) piece.rotate(1);
    if (angle<-45) piece.rotate(-1);
    if (angle<-135) piece.rotate(-1);
  }

  /**
   * Rotates the model shape by one quarter turn in the requested direction.
   * @param dir positive for clockwise, negative for counterclockwise
   */
  public void rotate(int dir) {
    piece.rotate(dir);
    invalidate();
  }

  /** Mirrors the model shape and refreshes the rendered view. */
  public void flip() {
    piece.flip();
    invalidate();
  }

  /** Identifies the corner handles reserved for rotating rather than dragging. */
  private boolean willRotate(){
    int r = radius/size;
    if (Math.abs( downX-r)<= 1 &&  Math.abs(downY-1) <= 1 ) return true;
    if (Math.abs( downX-1)<= 1 &&  Math.abs(downY-r) <= 1 ) return true;
    if (Math.abs( downX-2*r+1)<= 1 &&  Math.abs(downY-r) <= 1 ) return true;
    return false;
  }

  /**
   * Returns a diagnostic representation containing the current origin and shape.
   * @return textual description of this view and its piece
   */
  @Override
  public String toString() {
    return "<PieceUI> : (" + this.i + ", " + this.j + ") ; " + piece;
  }

  /**
   * Orders tray views by a size/count key so larger shapes can be laid out first.
   * @param that other piece view to compare with
   * @return negative, zero, or positive according to the tray ordering key
   */
  public int compareTo(PieceUI that) {
    return (2*this.piece.count + this.piece.size) - (2*that.piece.count + that.piece.size) ;
  }
}
