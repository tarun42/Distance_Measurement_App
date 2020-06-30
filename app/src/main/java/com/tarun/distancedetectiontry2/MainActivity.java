package com.tarun.distancedetectiontry2;
import com.tarun.distancedetectiontry2.global_variables;
import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.support.annotation.ColorInt;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private JavaCameraView javaCameraView;
    private Mat B,F;
    double t=0,area=3.14*4;
    public static int a=0,b=0,c=0,d=0,e=0,f=0;
    SeekBar h_min,h_max,v_min,v_max,s_min,s_max;
    static global_variables gv;
    Button red,blue,green;
    TextView text ;

    //te=findViewById(R.id.dist);
    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case BaseLoaderCallback.SUCCESS:
                    javaCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);



        red=(Button)findViewById(R.id.Red);
        blue=(Button)findViewById(R.id.Blue);
        green=(Button)findViewById(R.id.Green);
        text=findViewById(R.id.dist);
        text.setText(String.valueOf(t));

        red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                h_min.setProgress(0);
                h_max.setProgress(10);
                s_min.setProgress(150);
                s_max.setProgress(255);
                v_min.setProgress(150);
                v_max.setProgress(255);
            }
        });
        blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                h_min.setProgress(240);
                h_max.setProgress(255);
                s_min.setProgress(100);
                s_max.setProgress(255);
                v_min.setProgress(100);
                v_max.setProgress(255);
            }
        });
        green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                h_min.setProgress(120);
                h_max.setProgress(150);
                s_min.setProgress(100);
                s_max.setProgress(255);
                v_min.setProgress(50);
                v_max.setProgress(155);


            }
        });


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)==PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA},101);
        }

        javaCameraView=(JavaCameraView)findViewById(R.id.javaCameraView);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);

        h_min=(SeekBar)findViewById(R.id.h_min);
        h_max=(SeekBar)findViewById(R.id.h_max);
        v_min=(SeekBar)findViewById(R.id.v_min);
        v_max=(SeekBar)findViewById(R.id.v_max);
        s_min=(SeekBar)findViewById(R.id.s_min);
        s_max=(SeekBar)findViewById(R.id.s_max);

        h_max.setMax(255);
        h_min.setMax(255);
        v_max.setMax(255);
        v_min.setMax(255);
        s_max.setMax(255);
        s_min.setMax(255);

        h_min.setProgress(gv.getA());
        h_max.setProgress(gv.getD());
        s_min.setProgress(gv.getB());
        s_max.setProgress(gv.getE());
        v_min.setProgress(gv.getC());
        v_max.setProgress(gv.getF());

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        B = new Mat(width,height, CvType.CV_8UC4);
        F = new Mat(width,height,CvType.CV_8UC1);
        Log.d("Before", String.valueOf(B));
    }

    @Override
    public void onCameraViewStopped() {
        B.release();
        F.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        double distance=0;
        gv.a=h_min.getProgress();
        gv.b=v_min.getProgress();
        gv.c=s_min.getProgress();
        gv.d=h_max.getProgress();
        gv.e=v_max.getProgress();
        gv.f=s_max.getProgress();
        F=inputFrame.rgba();
            Imgproc.cvtColor( F, B, Imgproc.COLOR_RGB2HSV);
        Scalar lowerThreshold = new Scalar ( 0, 0,  0); // Blue color – lower hsv values
        Scalar upperThreshold = new Scalar( 0, 0, 255 ); // Blue color – higher hsv values
        //Core.inRange (B, lowerThreshold , upperThreshold, B );
        Core.inRange(B,new Scalar(gv.a,gv.c,gv.b), new Scalar(gv.d,gv.e,gv.f),B);
        Imgproc.dilate ( B, B, new Mat() );
        Imgproc.dilate ( F, F, new Mat() );
        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours ( B, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE );




        Mat current_contour,largest_contour = null;
        double largest_area =0;
        int largest_contour_index = 0;

        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            current_contour= contours.get(contourIdx);
            double contourArea = Imgproc.contourArea(current_contour);
            Imgproc.drawContours ( F, contours, contourIdx,new Scalar(255, 255, 0, 255),3);
            if (contourArea > largest_area) {
                largest_area = contourArea;
                largest_contour_index = contourIdx;
                largest_contour=current_contour;
            }
        }

        t=largest_area;
        //distance=422.3657139*Math.pow((3.14*4)/largest_area,0.5);
        distance=1301.73836155*Math.pow((area)/t,0.5);
        final double temp=largest_area;
        //distance=26*Math.pow(330/largest_area,0.5);
        //Log.d("TAG","pixel area "+temp);
        //Log.d("TAG", "Area  "+largest_contour);

        Log.d("TAG","DISTANCE   "+distance);
        t=distance;
        //Toast.makeText(getApplicationContext(), (int) distance,Toast.LENGTH_SHORT).show();
        final double finalDistance =(int) distance;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(String.valueOf(finalDistance));
            }
        });
        if(finalDistance==20) {
            Intent i = new Intent(getApplicationContext(), Main3Activity.class);
            Bundle v=new Bundle();
            /*v.putInt("a",a);
            v.putInt("b",b);
            v.putInt("c",c);
            v.putInt("d",d);
            v.putInt("e",e);
            v.putInt("f",f);
            i.putExtra("masking_values",v);
*/
            //startActivity(i);
        }



        return F;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(javaCameraView != null)
            javaCameraView.disableView();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(OpenCVLoader.initDebug()){
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else
        {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this,baseLoaderCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(javaCameraView != null)
            javaCameraView.disableView();
    }
}
/*Imgproc.cvtColor(inputFrame.rgba(), B, Imgproc.COLOR_RGB2GRAY, 3);
        final Size ksize = new Size(3, 3);
        Imgproc.GaussianBlur(B, B, ksize, 0);
        double high_threshold = Imgproc.threshold(B, B, 0, 255, 8);
        double low_threshold = high_threshold * 0.33;
        Imgproc.Canny(B, B, 30, 125);

        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();

        Imgproc.findContours(B, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat current_contour,largest_contour = null;
        double largest_area =0;
        int largest_contour_index = 0;

        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            current_contour= contours.get(contourIdx);
            double contourArea = Imgproc.contourArea(current_contour);
            if (contourArea > largest_area) {
                largest_area = contourArea;
                largest_contour_index = contourIdx;
                largest_contour=current_contour;
            }
        }
        Log.d("TAG", "Area"+String.valueOf(largest_contour));
*/