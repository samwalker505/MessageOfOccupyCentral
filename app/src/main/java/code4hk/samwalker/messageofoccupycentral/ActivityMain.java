package code4hk.samwalker.messageofoccupycentral;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.apache.http.Header;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

@EActivity(R.layout.activity_main)
public class ActivityMain extends ActionBarActivity {



    static final int REQUEST_TAKE_PHOTO = 1;

    String mCurrentPhotoPath;
    ImageLoader imageLoader;
    AsyncHttpClient client;
    ProgressDialog pd;

    @ViewById
    ImageView imgPreview;

    @ViewById
    EditText etxtContent;


    @Click(R.id.btnCamera)
    void onBtnCameraClicked() {
        dispatchTakePictureIntent();
    }

    @Click(R.id.btnSend)
    void onBtnSendClicked() {
        String content = etxtContent.getText().toString();
        postContent(content, mCurrentPhotoPath);

    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @AfterViews
    void onViewInjected(){
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
        client = new AsyncHttpClient();
        client.addHeader("Content-Type", "image/jpeg");
        pd = new ProgressDialog(this);
        pd.setMessage("Sending...");
    }



    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }



    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            galleryAddPic();
            imageLoader.displayImage("file:///"+mCurrentPhotoPath, imgPreview);

        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void postContent(String content, String photoPath) {
        RequestParams params = new RequestParams();
        params.put("message", content);
        File photo = new File(photoPath);
        try {
            params.put("photo", photo);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        params.put("key", "lkjgqe*@E&HSX(91r2cisO");
        client.post("http://chengfen-kiopa.rhcloud.com/upload", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (statusCode >= 201) {
                    pd.dismiss();
                    Toast.makeText(ActivityMain.this, "Post Success!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pd.dismiss();
                Toast.makeText(ActivityMain.this, "Post Failure!", Toast.LENGTH_SHORT).show();
            }
        });

        pd.show();
    }
}
