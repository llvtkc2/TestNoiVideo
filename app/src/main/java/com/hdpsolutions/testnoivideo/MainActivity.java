package com.hdpsolutions.testnoivideo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.googlecode.mp4parser.util.Matrix.ROTATE_270;

public class MainActivity extends AppCompatActivity {
    TextView tv2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv2 = findViewById(R.id.tv2);
        XinQuyen();
        getAllMedia();
        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MergeVideos(getAllMedia()).execute();
            }
        });
        tv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


    }


    public void XinQuyen() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED))
        {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        2);;
            }
        } else {
        }

    }

    public ArrayList<String> getAllMedia() {
        TextView tv=  findViewById(R.id.tv1);
        ArrayList<String> vd =  new ArrayList<>();
        String[] projection = { MediaStore.Video.VideoColumns.DATA ,MediaStore.Video.Media.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
        try {
            cursor.moveToFirst();
            do{
                vd.add(cursor.getString(0));
                tv.append(cursor.getString(0)+"\n");
            }while(cursor.moveToNext());

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vd;
    }

    private class MergeVideos extends AsyncTask<String, Integer, String> {
        //The file names to merge
        private ArrayList<String> videosToMerge = getAllMedia();
        //Dialog to show to the user
        private ProgressDialog progressDialog;
        private String outputVideo = Environment.getExternalStorageDirectory() + "audio_output.mp4";

        private MergeVideos(ArrayList<String> videosToMerge) {
            this.videosToMerge = videosToMerge;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Merging videos", "Please wait...", true);
        };

        @Override
        protected String doInBackground(String... params) {
            int count = videosToMerge.size();
            Log.d("hoang","size : "+videosToMerge.size()+"");
            try {

                Movie[] inMovies = new Movie[count];
                for (int i = 0; i < count; i++) {
                    inMovies[i] = MovieCreator.build(new File(videosToMerge.get(i)).getAbsolutePath());
                }

                List<Track> videoTracks  = new LinkedList<Track>();
                for (Movie m : inMovies) {
                    for (Track t : m.getTracks()) {
                        videoTracks.add(t);
                    }
                }


                Movie result = new Movie();

                if (videoTracks.size() > 0) {
                    result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
                }

                Container mContainer =  new DefaultMp4Builder().build(result);
                File file = new File(outputVideo);
                WritableByteChannel wbc = new FileOutputStream(file).getChannel();
                mContainer.writeContainer(wbc);


            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                Log.d("hoang","FileNotFoundException : "+e.getMessage());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.d("hoang","IOException : "+e.getMessage());
            }


            return outputVideo;
        }

        @Override
        protected void onPostExecute(String value) {
            super.onPostExecute(value);
            progressDialog.dismiss();
            tv2.setText(outputVideo);
        }

    }


}
