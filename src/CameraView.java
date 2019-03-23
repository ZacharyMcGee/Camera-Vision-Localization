import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;

public class CameraView extends JFrame{
	
	private static final long serialVersionUID = 1L;
	Display display;
	int height, width;


	public CameraView ( int length, int breadth)
	{
		width = length;
		height = breadth;
		display = new Display(breadth,length);
		
		this.setSize(new Dimension(length, breadth));
		this.add(display);

		this.setFocusable(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		
	}
	


	public void showImage(Mat m)
	{
		MatOfByte matOfByte = new MatOfByte();
		Highgui.imencode(".jpg", m, matOfByte);
		
		byte[] byteArray = matOfByte.toArray();
		try
		{

			InputStream in = new ByteArrayInputStream(byteArray);
			display.paintSheet(ImageIO.read(in));
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
