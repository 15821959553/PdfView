package com.android.pdfview;

import android.content.Context;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;

import com.android.pdfview.api.ApiManager;
import com.android.pdfview.listener.LoadCompleteListener;
import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class AndroidPdfView extends PDFView {
    /**
     * Construct the initial view
     *
     * @param context
     * @param set
     */
    public AndroidPdfView(Context context, AttributeSet set) {
        super(context, set);
    }

    /** Use a url as the pdf source */
    public void setUrl(String fileUrl,final LoadCompleteListener loadCompleteListener) {
        final String SDPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/pdf/";
        final String fileName="tsp.pdf";
        File parent =new File(SDPath);

        if(!parent.exists()){

            parent.mkdirs();

        }
        File file = new File(SDPath, fileName);
//        if (file.exists()){//文件已经存在，直接获取本地文件打开，否则从网络现在文件，文件下载成功之后再打开
//              listener.setFile(file);
//
//        }else {
        ApiManager.downloadPicFromNet(fileUrl).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody responseBody) {
                        InputStream is = null;
                        byte[] buf = new byte[2048];
                        int len = 0;
                        FileOutputStream fos = null;
                        try {
                            is = responseBody.byteStream();
                            long total = responseBody.contentLength();
                            File file1 = new File(SDPath);
                            if (!file1.exists()){
                                file1.mkdirs();
                            }
                            File fileN = new File(SDPath, fileName);

                            if (!fileN.exists()){
                                boolean mkdir = fileN.createNewFile();
                                Log.d("mkdir", "call: "+mkdir);
                            }
                            Log.d("h_bl", "文件下载成功==="+fileN.getPath());
                            fos = new FileOutputStream(fileN);
                            Log.d("h_bl", "文件下载成功==="+fileN.exists());
                            long sum = 0;
                            while ((len = is.read(buf)) != -1) {
                                fos.write(buf, 0, len);
                                sum += len;
                                int progress = (int) (sum * 1.0f / total * 100);
                                Log.d("h_bl", "progress=" + progress);
                            }
                            fos.flush();
                            Log.d("h_bl", "文件下载成功");
                            loadCompleteListener.loadComplete(fileN);
//                            listener.setFile(fileN);
                        } catch (Exception e) {

                            Log.d("h_bl", "文件下载失败");
                            Log.d("h_bl", e.getMessage());
                            Log.d("h_bl", e.toString());
                        } finally {
                            try {
                                if (is != null)
                                    is.close();
                            } catch (IOException e) {
                            }
                            try {
                                if (fos != null)
                                    fos.close();
                            } catch (IOException e) {
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d("h_bl", "文件下载失败");
                    }
                });
    }
}
