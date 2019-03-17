import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.*;
import org.opencv.videoio.VideoCapture;

public class ObjectTracking {
	
	static int SENSITIVITY_VALUE = 20;
	static int BLUR_SIZE = 10;
	static int WIDTH = 640;
	static int HEIGHT = 480;
	
	static CameraView thresholdWindow;
	static CameraView drawnWindow;

	public static Map tilemap = new Map(WIDTH, HEIGHT);
	
	public static void main(String[] args)
	{
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame("Localization Tilemap");
                frame.add(tilemap);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);    
                
            }
        });
        
		String opencvpath = System.getProperty("user.dir") + "\\files\\";
		System.load(opencvpath + Core.NATIVE_LIBRARY_NAME + ".dll");
	    
		drawnWindow = new CameraView(WIDTH,HEIGHT);
		readFromCamera(tilemap);
	}
	
	static void readFromCamera(Map map)
	{

		while (true)
		{
			VideoCapture camera = new VideoCapture(0);
			if (!camera.isOpened())
				System.out.println("Cannot open file");

			else
			{
				Mat currentFrame = new Mat();
				Mat gray = new Mat();
				Mat hsv = new Mat();
				Mat mask = new Mat();
				Mat mask2 = new Mat();
				Mat img_new = new Mat(); 
				boolean frameSuccess;
				
				while (true)
				{

					frameSuccess = camera.read(currentFrame);
					if (frameSuccess == true)
					{
						img_new.release();

						Imgproc.resize(currentFrame, currentFrame, new Size(WIDTH,HEIGHT));
						
						//Imgproc.cvtColor(currentFrame,hsv,Imgproc.COLOR_BGR2HSV);
						//Core.inRange(hsv, new Scalar(0,0,0),new Scalar(20,255,255),mask);
						//currentFrame.copyTo(img_new, mask);

						
						Imgproc.cvtColor(currentFrame, gray,
								Imgproc.COLOR_RGB2GRAY);
				        Imgproc.medianBlur(gray, gray, 5);
					
					}else
						break;
					
					detectCircles(gray);
					plotCircles(gray, map);
				}
				camera.release();
			}

		}
	}
	
	static void plotCircles(Mat image, Map map) {
        Mat circles = new Mat();
        Imgproc.HoughCircles(image, circles, Imgproc.HOUGH_GRADIENT, 1.0,
                (double)image.rows()/16, 
                100.0, 30.0, 1, 30); 
        
        double[][] points = new double[circles.cols()][];
        for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            if(center.x != 0 || center.y != 0) {
            	double[] point = {center.x, center.y};
            	points[x] = point;
            	map.addColor((int)Math.round(center.x) / 24, (int)Math.round(center.y) / 24);
            }
        }
	}
	
	static void detectCircles(Mat image) {
        Mat circles = new Mat();
        Imgproc.HoughCircles(image, circles, Imgproc.HOUGH_GRADIENT, 1.0,
                (double)image.rows()/16, 
                100.0, 30.0, 1, 30); 

        for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            Imgproc.circle(image, center, 1, new Scalar(0,100,100), 3, 8, 0 );
            int radius = (int) Math.round(c[2]);
            Imgproc.circle(image, center, radius, new Scalar(255,0,255), 3, 8, 0 );
        }
        drawnWindow.showImage(image);
	}
}
