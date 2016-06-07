package com.ruddell.moviestudio.util;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class FileHelper {
    private static final String TAG = "FileHelper";

    //get image (drawable) from cache...
    public static Drawable getDrawableFromCache(String fileName, Context context){
        Debug.LOGD(TAG,"getDrawableFromCache(" + fileName + ")");
        Drawable d = null;
        try{
            FileInputStream fin = context.openFileInput(fileName);
            if(fin != null){
                d = Drawable.createFromStream(fin, null);
                fin.close();
            }else{

            }
        }catch(Exception c){

        }
        return d;

    }

    //save image to cache
    public static void saveImageToCache(Bitmap theImage, String saveAsFileName, Context context){
        Debug.LOGD(TAG,"saveImageToCache(" + saveAsFileName + ")");
        try{
            if(saveAsFileName.length() > 3 && theImage != null){
                FileOutputStream fos = context.openFileOutput(saveAsFileName, Context.MODE_PRIVATE);
                theImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            }
        }catch (Exception e) {

        }
    }


}