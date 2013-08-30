package com.example.circlescrolllistview;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout.LayoutParams;

public class CircleScrollListView extends SurfaceView implements
		SurfaceHolder.Callback, OnGestureListener {
	private GestureDetector mGestureDetector;
	private Thread mThread;
	private ArrayList<CircleDrawItem> datas = new ArrayList<CircleDrawItem>();
	int[] playerDrawableResourceIds = new int[] { R.drawable.ronaldo,
			R.drawable.zindance, R.drawable.congvinh, R.drawable.huynhduc,
			R.drawable.gerrard, R.drawable.nagatomo, R.drawable.messi,
			R.drawable.minhphuong, R.drawable.neymar, R.drawable.ronaldo_beo,
			R.drawable.ronaldinho, R.drawable.xavi };
	public int mCenterX;
	public int mCenterY;
	public int mRadius;
	public double mStartAngleInRadian = Math.PI / 4;
	private boolean isStop = false;

	public CircleScrollListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mGestureDetector = new GestureDetector(context, this);
		getHolder().addCallback(this);
		this.setFocusable(true);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mGestureDetector.onTouchEvent(event);
		return true;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromResource(Resources res,
			int resId, int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	private double calculateScrollAngle(float px1, float py1, float px2,
			float py2) {
		double radian1 = Math.atan2(py1, px1);
		double radian2 = Math.atan2(py2, px2);
		double diff = radian2 - radian1;
		return diff;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		Global.density = getResources().getDisplayMetrics().density;
		Global.dw = getResources().getDisplayMetrics().widthPixels;
		Global.dh = getResources().getDisplayMetrics().heightPixels;
		Global.dp = Global.density / 1.5f;

		// For circle data
		mCenterX = (int) (-Global.dp * 200);
		mCenterY = (int) (Global.dh / 2.0f);
		mRadius = (int) (450 * Global.dp);
		mStartAngleInRadian = Math.PI / 4;

		for (int i = 0; i < playerDrawableResourceIds.length; i++) {
			CircleDrawItem circleDrawItem = new CircleDrawItem();
			circleDrawItem.mIconBitmap = decodeSampledBitmapFromResource(
					getResources(), playerDrawableResourceIds[i], 50, 50);
			circleDrawItem.mAngle = mStartAngleInRadian - i * Math.PI / 10;
			datas.add(circleDrawItem);
		}

		mThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!isStop) {
					draw();
				}
			}
		});

		mThread.start();
	}

	protected void draw() {
		Canvas canvas = getHolder().lockCanvas();
		if (canvas == null) {
			return;
		}
		canvas.save();
		canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setAntiAlias(true);

		for (int i = 0; i < datas.size(); i++) {
			canvas.save();
			CircleDrawItem item = datas.get(i);
			double x = mCenterX + Math.cos(item.mAngle) * mRadius;
			double y = mCenterY - Math.sin(item.mAngle) * mRadius;
			canvas.drawBitmap(item.mIconBitmap,
					(int) x - item.mIconBitmap.getWidth() / 2, (int) y
							- item.mIconBitmap.getHeight() / 2, paint);
			canvas.restore();
		}
		canvas.restore();
		getHolder().unlockCanvasAndPost(canvas);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		isStop = true;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		float tpx = e2.getX();
		float tpy = e2.getY();
		float disx = (int) distanceX;
		float disy = (int) distanceY;
		double scrollAngle = calculateScrollAngle(tpx, tpy, tpx + disx, tpy
				+ disy);
		for (int i = 0; i < datas.size(); i++) {
			datas.get(i).mAngle += scrollAngle;
		}
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
}
