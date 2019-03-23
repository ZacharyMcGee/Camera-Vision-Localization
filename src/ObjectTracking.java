import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.colorchooser.ColorSelectionModel;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.*;

public class ObjectTracking {
	static int WIDTH = 640;
	static int HEIGHT = 480;
	
	static CameraView thresholdWindow;
	static CameraView drawnWindow;

	public static Map tilemap = new Map(WIDTH, HEIGHT);
	public static Mat template = null;
	
	public static Robot robot = new Robot();
	
	public static void main(String[] args)
	{
        //obj.serialWrite('F');
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame("Localization Tilemap");
                
                JMenu menu, submenu;  
                JMenuItem i1, i2, i3, i4, i5;  
                JMenuBar mb=new JMenuBar();  
                menu=new JMenu("Menu");  
                submenu=new JMenu("Sub Menu");  
                i1=new JMenuItem("Start");  
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
                
            	i1.addActionListener(new ActionListener() {

            	    @Override
            	    public void actionPerformed(ActionEvent e) {
                        Thread hilo = new Thread(new Runnable() {

                            @Override
                            public void run() {

                               readFromCamera(tilemap);

                            }
                        });         
                        hilo.start();
            	    }
            	});
            }
        });
        
        
		String opencvpath = System.getProperty("user.dir") + "\\files\\";
		System.load(opencvpath + Core.NATIVE_LIBRARY_NAME + ".dll");
	    
		template = Highgui.imread(System.getProperty("user.dir") + "\\files\\" + "template.jpg", Highgui.CV_LOAD_IMAGE_COLOR);
		
		drawnWindow = new CameraView(WIDTH,HEIGHT);
		
		robot.CreateObj("COM7");
		if(robot.ConnectBluetooth()) {
			System.out.println("HJ");
		}
	}
	
	static void readFromCamera(Map map)
	{

		while (true)
		{	        
			VideoCapture camera = new VideoCapture(-1);
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
					System.out.println(frameSuccess);
					if (frameSuccess == true)
					{
				        Mat sceneImage = currentFrame;
				        
				        MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
				        FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.FAST);
				        featureDetector.detect(template, objectKeyPoints);
				        KeyPoint[] keypoints = objectKeyPoints.toArray();

				        MatOfKeyPoint objectDescriptors = new MatOfKeyPoint();
				        DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
				        descriptorExtractor.compute(template, objectKeyPoints, objectDescriptors);

				        // Create the matrix for output image.
				        Mat outputImage = new Mat(template.rows(), template.cols(), Highgui.CV_LOAD_IMAGE_COLOR);
				        Scalar newKeypointColor = new Scalar(255, 0, 0);

				        //Features2d.drawKeypoints(template, objectKeyPoints, outputImage, newKeypointColor, 0);

				        // Match object image with the scene image
				        MatOfKeyPoint sceneKeyPoints = new MatOfKeyPoint();
				        MatOfKeyPoint sceneDescriptors = new MatOfKeyPoint();
				        featureDetector.detect(sceneImage, sceneKeyPoints);
				        descriptorExtractor.compute(sceneImage, sceneKeyPoints, sceneDescriptors);

				        Mat matchoutput = new Mat(sceneImage.rows() * 2, sceneImage.cols() * 2, Highgui.CV_LOAD_IMAGE_COLOR);
				        Scalar matchestColor = new Scalar(0, 255, 0);

				        List<MatOfDMatch> matches = new LinkedList<MatOfDMatch>();
				        DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
				        descriptorMatcher.knnMatch(objectDescriptors, sceneDescriptors, matches, 2);

				        LinkedList<DMatch> goodMatchesList = new LinkedList<DMatch>();

				        float nndrRatio = 1f;

				        for (int i = 0; i < matches.size(); i++) {
				            MatOfDMatch matofDMatch = matches.get(i);
				            DMatch[] dmatcharray = matofDMatch.toArray();
				            DMatch m1 = dmatcharray[0];
				            DMatch m2 = dmatcharray[1];

				            if (m1.distance <= m2.distance * nndrRatio) {
				                goodMatchesList.addLast(m1);

				            }
				        }

				        if (goodMatchesList.size() >= 14) {

				            List<KeyPoint> objKeypointlist = objectKeyPoints.toList();
				            List<KeyPoint> scnKeypointlist = sceneKeyPoints.toList();

				            LinkedList<Point> objectPoints = new LinkedList<>();
				            LinkedList<Point> scenePoints = new LinkedList<>();

				            for (int i = 0; i < goodMatchesList.size(); i++) {
				                objectPoints.addLast(objKeypointlist.get(goodMatchesList.get(i).queryIdx).pt);
				                scenePoints.addLast(scnKeypointlist.get(goodMatchesList.get(i).trainIdx).pt);
				            }

				            MatOfPoint2f objMatOfPoint2f = new MatOfPoint2f();
				            objMatOfPoint2f.fromList(objectPoints);
				            MatOfPoint2f scnMatOfPoint2f = new MatOfPoint2f();
				            scnMatOfPoint2f.fromList(scenePoints);
				           
				            Mat homography = Calib3d.findHomography(objMatOfPoint2f, scnMatOfPoint2f, Calib3d.RANSAC, 3);

				            Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
				            Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

				            obj_corners.put(0, 0, new double[]{0, 0});
				            obj_corners.put(1, 0, new double[]{template.cols(), 0});
				            obj_corners.put(2, 0, new double[]{template.cols(), template.rows()});
				            obj_corners.put(3, 0, new double[]{0, template.rows()});

				            System.out.println("Transforming object corners to scene corners...");
				            Core.perspectiveTransform(obj_corners, scene_corners, homography);

				            Mat img = currentFrame;

				            Core.line(img, new Point(scene_corners.get(0, 0)), new Point(scene_corners.get(1, 0)), new Scalar(0, 255, 0), 4);
				            Core.line(img, new Point(scene_corners.get(1, 0)), new Point(scene_corners.get(2, 0)), new Scalar(0, 255, 0), 4);
				            Core.line(img, new Point(scene_corners.get(2, 0)), new Point(scene_corners.get(3, 0)), new Scalar(0, 255, 0), 4);
				            Core.line(img, new Point(scene_corners.get(3, 0)), new Point(scene_corners.get(0, 0)), new Scalar(0, 255, 0), 4);
				            
				            Point center = new Point(scene_corners.get(0, 0));
				            Point halfCenter = new Point(scene_corners.get(2, 0));
				            Point topCenter = new Point(scene_corners.get(0, 0));
				            Point halfTop = new Point(scene_corners.get(1, 0));
				            
				            
				            
				            topCenter.x = (topCenter.x + halfTop.x) / 2;
				            topCenter.y = (topCenter.y + halfTop.y) / 2; 
				            center.x = (center.x + halfCenter.x) / 2;
				            center.y = (center.y + halfCenter.y) / 2;

				            Core.circle(img, center, 15, new Scalar(0, 0, 255));
				            Core.circle(img, topCenter, 15, new Scalar(0, 0, 255));
				            
				            // y-axis line
				            Core.line(img, new Point(center.x, center.y + HEIGHT), new Point(center.x, center.y - HEIGHT), new Scalar(255, 0, 0), 4);
				            Core.line(img, new Point(topCenter.x + (topCenter.x - center.x) * 4, topCenter.y + (topCenter.y - center.y) * 4), new Point(center.x + (topCenter.x - center.x) * -4, center.y + (topCenter.y - center.y) * -4), new Scalar(0, 255, 0), 4);

				            System.out.println("Drawing matches image...");
				            MatOfDMatch goodMatches = new MatOfDMatch();
				            goodMatches.fromList(goodMatchesList);
				            //map.addColor(center);
				            float theta = 0;
				            
				            theta = getAngle(homography, theta);
				            
				            Core.putText(img, Math.round(theta) + " degrees", topCenter, Font.PLAIN, 1, new Scalar(0, 0, 255));
				            
				            //Features2d.drawMatches(template, objectKeyPoints, sceneImage, sceneKeyPoints, goodMatches, matchoutput, matchestColor, newKeypointColor, new MatOfByte(), 2);
				            drawnWindow.showImage(img);
				        } else {
				            System.out.println("Object Not Found");
				            Features2d.drawKeypoints(sceneImage, objectKeyPoints, sceneImage);
				            drawnWindow.showImage(sceneImage);
				        }

					
					//detectCircles(currentFrame);
					//plotCircles(currentFrame, map);
				}
				//camera.release();
			}

		}
	}
}
	public static float getAngle(Mat normalised_homography, float theta)
	{
	    double a = normalised_homography.get(0, 0)[0];
	    double b = normalised_homography.get(0, 1)[0];

	    theta = (float) (Math.atan2(b,a)*(180/Math.PI));
	    return theta;
	}
}
