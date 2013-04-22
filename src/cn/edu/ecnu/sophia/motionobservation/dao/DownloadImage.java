package cn.edu.ecnu.sophia.motionobservation.dao;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Matrix;
import android.os.Environment;

public class DownloadImage {
	public static Bitmap downloadImage(String myurl) throws IOException {
		InputStream is = null;
		try {
			URL url = new URL(myurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);/* milliseconds */
			conn.setConnectTimeout(15000);/* milliseconds */
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			// Starts the query
			conn.connect();
			// int response = conn.getResponseCode();
			is = conn.getInputStream();

			// Convert the InputStream into a Bitmap
			Bitmap bitmap = BitmapFactory.decodeStream(is);
			is.close();
			//bitmap = imageZoom(bitmap, 20);
			return bitmap;
			// Makes sure that the InputStream is closed after the app is
			// finished using it.
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	public static void saveBitmapToExternal(Bitmap bm, String directory,
			String fileName) throws IOException {
		File file = new File(directory);
		if (!file.exists()) {
			file.mkdirs();
		}
		File myImgFile = new File(directory + "/" + fileName);
		BufferedOutputStream fos = new BufferedOutputStream(
				new FileOutputStream(myImgFile));
		bm.compress(CompressFormat.PNG, 80, fos);
		fos.flush();
		fos.close();
	}

	public static Bitmap readImageFromExternal(String fileName) throws FileNotFoundException {
		Bitmap bm = null;
		FileInputStream fin = new FileInputStream(fileName);
		bm = BitmapFactory.decodeStream(fin);
		return bm;
	}

	/**
	 * 将大小超过最大允许空间的图片压缩到允许范围内
	 * @param bitmap 原图片资源
	 * @param maxSize 原图片大小（单位为KB）
	 * @return
	 */
	public static Bitmap imageZoom(Bitmap bitmap, double maxSize) {
		// 图片允许最大空间 单位：KB
		// 将bitmap放至数组中，意在bitmap的大小（与实际读取的原文件要大）
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		byte[] b = baos.toByteArray();
		// 将字节换成KB
		double mid = b.length / 1024;
		// 判断bitmap占用空间是否大于允许最大空间 如果大于则压缩 小于则不压缩
		if (mid > maxSize) {
			// 获取bitmap大小 是允许最大大小的多少倍
			double i = mid / maxSize;
			// 开始压缩 此处用到平方根 将宽带和高度压缩掉对应的平方根倍
			// （1.保持刻度和高度和原bitmap比率一致，压缩后也达到了最大大小占用空间的大小）
			bitmap = zoomImage(bitmap, bitmap.getWidth() / Math.sqrt(i),
					bitmap.getHeight() / Math.sqrt(i));
		}
		return bitmap;
	}

	/***
	 * 图片的缩放方法
	 * 
	 * @param bgimage
	 *            ：源图片资源
	 * @param newWidth
	 *            ：缩放后宽度
	 * @param newHeight
	 *            ：缩放后高度
	 * @return
	 */
	public static Bitmap zoomImage(Bitmap bgimage, double newWidth,
			double newHeight) {
		// 获取这个图片的宽和高
		float width = bgimage.getWidth();
		float height = bgimage.getHeight();
		// 创建操作图片用的matrix对象
		Matrix matrix = new Matrix();
		// 计算宽高缩放率
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 缩放图片动作
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) newWidth,
				(int) newHeight, matrix, true);
		return bitmap;
	}

	
	public static Bitmap scaleCompress(Bitmap image) {  
	      
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();         
	    image.compress(Bitmap.CompressFormat.JPEG, 100, baos);  
	    if( baos.toByteArray().length / 1024> 10) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出    
	        baos.reset();//重置baos即清空baos  
	        image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中  
	    }  
	    ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());  
	    BitmapFactory.Options newOpts = new BitmapFactory.Options();  
	    //开始读入图片，此时把options.inJustDecodeBounds 设回true了  
	    newOpts.inJustDecodeBounds = true;  
	    Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);  
	    newOpts.inJustDecodeBounds = false;  
	    int w = newOpts.outWidth;  
	    int h = newOpts.outHeight;  
	    //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为  
	    float hh = 72f;//这里设置高度为800f  
	    float ww = 72f;//这里设置宽度为480f  
	    //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可  
	    int be = 1;//be=1表示不缩放  
	    if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放  
	        be = (int) (newOpts.outWidth / ww);  
	    } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放  
	        be = (int) (newOpts.outHeight / hh);  
	    }  
	    if (be <= 0)  
	        be = 1;  
	    newOpts.inSampleSize = be;//设置缩放比例  
	    //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了  
	    isBm = new ByteArrayInputStream(baos.toByteArray());  
	    bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);  
	    return bitmap;//压缩好比例大小后再进行质量压缩  
	}
}
