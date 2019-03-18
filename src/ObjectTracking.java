import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
                
                JMenu menu, submenu;  
                JMenuItem i1, i2, i3, i4, i5;  
                JMenuBar mb=new JMenuBar();  
                menu=new JMenu("Menu");  
                submenu=new JMenu("Sub Menu");  
                i1=new JMenuItem("Item 1");  
                i2=new JMenuItem("Item 2");  
                i3=new JMenuItem("Item 3");  
                i4=new JMenuItem("Item 4");  
                i5=new JMenuItem("Item 5");
                menu.add(i1); menu.add(i2); menu.add(i3);  
                submenu.add(i4); submenu.add(i5);  
                menu.add(submenu);  
                mb.add(menu);  
                frame.setJMenuBar(mb);  
                
                frame.setLayout(new BorderLayout());
                frame.add(tilemap, BorderLayout.NORTH);
                //frame.add(drawnWindow, BorderLayout.SOUTH);
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
        
        if(circles.cols() > 0) {
        	boolean found = false;
        	int[][] points = new int[circles.cols()][];
        	for (int x = 0; x < circles.cols(); x++) {
            	double[] c = circles.get(0, x);
            	Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            	if(center.x != 0 || center.y != 0) {
            		found = true;
            		int[] point = {(int)Math.round(center.x) / 24, (int)Math.round(center.y) / 24};
            		points[x] = point;
            	}
        	}
        	if(found) {
        		map.addColor(points);
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
