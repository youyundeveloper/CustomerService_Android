package com.customerservice.chat.imagemask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.view.View;

/**
 *	demo:MaskView imgView = new MaskView(
 *		mContext,loadedImage,
 *		(NinePatchDrawable)mContext.getResources().getDrawable(R.drawable.chat_img_right_mask),
 *		Function_Utility.mScreenWidth/3,Function_Utility.mScreenWidth/3,Function_Utility.mScreenWidth/4,Function_Utility.mScreenWidth/4);
 *		parentView.removeAllViews();
 *		parentView.addView(imgView);
 *		ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams)imgView.getLayoutParams();
 *		layoutParams.height = imgView.getViewHeight();
 *		layoutParams.width = imgView.getViewWidth();
 */
public class MaskView extends View {
	
	private int maxWidth;
	private int maxHeight;
	private int minWidth;
	private int minHeight;
	
	private MaskViewSize maskViewSize;
	private Bitmap bmp;
	private NinePatchDrawable mask;
	
	/**
	 * 
	 * @param context
	 * @param bmp
	 * @param mask
	 * @param maxWidth
	 * 			- maxWidth will be not less than mask's minWidth
	 * @param maxHeight
	 * 			- maxHeight will be not less than mask's minHeight
	 * @param minWidth
	 * 			- minWidth will be not less than mask's minWidth
	 * @param minHeight
	 * 			- minHeight will be not less than mask's minHeight
	 */
	public MaskView(Context context, Bitmap bmp, NinePatchDrawable mask, int maxWidth, int maxHeight, int minWidth, int minHeight) {
		super(context);
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		this.minWidth = minWidth;
		this.minHeight = minHeight;
		this.bmp = bmp;
		this.mask = mask;
		caculateAndResizeBmp();
		setBackgroundColor(Color.TRANSPARENT);
	}
	
	public static MaskViewSize caculateMaskViewSize(String imgPath, NinePatchDrawable mask, int maxWidth, int maxHeight, int minWidth, int minHeight){
		MaskViewSize maskViewSize = null;
		if(imgPath != null && mask != null){
			try {
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(imgPath, opts);
				maskViewSize = caculateMaskViewSize(opts.outWidth, opts.outHeight, mask, maxWidth, maxHeight, minWidth, minHeight);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return maskViewSize;
	}
	
	private void caculateAndResizeBmp(){
		if(bmp == null || mask == null){
			return;
		}
		maskViewSize = caculateMaskViewSize(bmp.getWidth(), bmp.getHeight(), mask, maxWidth, maxHeight, minWidth, minHeight);
		
		//resize bmp  待优�?原图长宽比例和view不同的情况需要做截取
		float scale = maskViewSize.targetBmpWidth * 1.0f / bmp.getWidth();
		
		//caculate cut x、y、width、height
		int cutX,cutY,cutWidth,cutHeight;
		if(maskViewSize.targetBmpWidth > maskViewSize.viewWidth){
			cutX = (int)((maskViewSize.targetBmpWidth - maskViewSize.viewWidth) / 2.0 / scale);
			cutWidth = (int)(maskViewSize.viewWidth / scale);
		} else{
			cutX = 0;
			cutWidth = bmp.getWidth();
		}
		if(maskViewSize.targetBmpHeight > maskViewSize.viewHeight){
			cutY = (int)((maskViewSize.targetBmpHeight - maskViewSize.viewHeight) / 2.0 / scale);
			cutHeight = (int)(maskViewSize.viewHeight / scale);
		} else{
			cutY = 0;
			cutHeight = bmp.getHeight();
		}
		
		bmp = getResizeBmp(bmp, scale,cutX,cutY,cutWidth,cutHeight);
		
		maskViewSize.viewWidth = bmp.getWidth();
		maskViewSize.viewHeight = bmp.getHeight();
	}
	
	public static MaskViewSize caculateMaskViewSize(int bmpWidth, int bmpHeight, NinePatchDrawable mask, int maxWidth, int maxHeight, int minWidth, int minHeight){
		MaskViewSize maskViewSize = new MaskViewSize();
		int a1 = maxWidth;				//max width
		int b1 = maxHeight;				//max height
		int a2 = minWidth;				//min width
		int b2 = minHeight;				//min height
		final int a3 = bmpWidth;		//source bmp width
		final int b3 = bmpHeight;		//source bmp width
		int a4 = a3;					//target view width
		int b4 = b3;					//target view height
		int a5 = a3;					//target bmp width
		int b5 = b3;					//target bmp height
		
		int maskMinWidth = mask.getMinimumWidth();
		int maskMinHeight = mask.getMinimumHeight();
		
		//decide maxWidth and maxHeight
		if(a1 < maskMinWidth){
			a1 = maskMinWidth;
		}
		if(b1 < maskMinHeight){
			b1 = maskMinHeight;
		}
		
		//decide minWidth and minHeight
		if(a2 < maskMinWidth){
			a2 = maskMinWidth;
		}
		if(b2 < maskMinHeight){
			b2 = maskMinHeight;
		}
		
		//caculate a4、b4 by max size
		if(a3 > a1 && b3 > b1){
			if(a3 * 1.0 / b3 < a1 * 1.0 / b1){
				a4 = (int)(b1 * a3 * 1.0 / b3);
				b4 = b1;
			}else{
				b4 = (int)(a1 * b3 * 1.0 / a3);
				a4 = a1;
			}
		}else if(a3 > a1 && b3 <= b1){
			b4 = (int)(a1 * b3 * 1.0 / a3);
			a4 = a1;
		}else if(b3 > b1 && a3 <= a1){
			a4 = (int)(b1 * a3 * 1.0 / b3);
			b4 = b1;
		}
		
		//caculate a4、b4 by min size
		if(a4 < a2){
			b4 = (int)(a2 * 1.0 / a4 * b4);
			if(b4 > b1){
				b4 = b1;
			}
			a4 = a2;
		}
		if(b4 < b2){
			a4 = (int)(b2 * 1.0 / b4 * a4);
			if(a4 > a1){
				a4 = a1;
			}
			b4 = b2;
		}
		
		//caculate a5、b5
		if(a3 != a4 || b3 != b4){
			if((a3 >= a4 && b3 >= b4) || (a4 <= a4 && b3 <= b4)){
				if(a3 * 1.0 / b3 > a4 * 1.0 / b4){
					a5 = (int)(b4 * a3 * 1.0 / b3);
					b5 = b4;
				}else{
					b5 = (int)(a4 * b3 * 1.0 / a3);
					a5 = a4;
				}
			}else if(a3 >= a4 && b3 <= b4){
				a5 = (int)(b4 * a3 * 1.0 / b3);
				b5 = b4;
			}else if(a3 <= a4 && b3 >= b4){
				b5 = (int)(a4 * b3 * 1.0 / a3);
				a5 = a4;
			}
		}
		maskViewSize.viewWidth = a4;
		maskViewSize.viewHeight = b4;
		maskViewSize.targetBmpWidth = a5;
		maskViewSize.targetBmpHeight = b5;
		return maskViewSize;
	}
	
	public MaskViewSize getMaskViewSize() {
		return maskViewSize;
	}

	public void setMaskViewSize(MaskViewSize maskViewSize) {
		this.maskViewSize = maskViewSize;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if(bmp == null || mask == null || maskViewSize == null){
			return;
		}
		//begin draw
        canvas.saveLayer(0, 0, maskViewSize.viewWidth, maskViewSize.viewHeight, null,
			  Canvas.MATRIX_SAVE_FLAG |
			  Canvas.CLIP_SAVE_FLAG |
			  Canvas.HAS_ALPHA_LAYER_SAVE_FLAG |
			  Canvas.FULL_COLOR_LAYER_SAVE_FLAG |
			  Canvas.CLIP_TO_LAYER_SAVE_FLAG);
        Paint paint = new Paint();
		mask.setBounds(new Rect(0,0,maskViewSize.viewWidth,maskViewSize.viewHeight));
		mask.draw(canvas);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
		canvas.drawBitmap(bmp, 0, 0, paint);
		paint.setXfermode(null);
	}

	private Bitmap getResizeBmp(Bitmap bmp, float scale, int cutX, int cutY, int cutWidth, int cutHeight){
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		Bitmap resizeBitmap = Bitmap.createBitmap(bmp, cutX, cutY, cutWidth, cutHeight, matrix,true);
		return resizeBitmap;
	}
}






