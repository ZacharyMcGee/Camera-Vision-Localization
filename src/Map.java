import javax.swing.*;
import java.awt.*;

public class Map extends JPanel {

	public static final Color BACKGROUND = new Color (226, 226, 226);
    public static final Color ROBOT = new Color(0, 0, 0);

    public static final Color[] TERRAIN = {
    	BACKGROUND,
        ROBOT
    };

    public int NUM_ROWS = 25;
    public int NUM_COLS = 25;

    public static final int PREFERRED_GRID_SIZE_PIXELS = 25;

    private Color[][] terrainGrid;

    public Map(int rows, int cols){
    	NUM_ROWS = rows / 24;
    	NUM_COLS = cols / 24;
    	this.terrainGrid = new Color[NUM_ROWS][NUM_COLS];
        int preferredWidth = NUM_ROWS * 24;
        int preferredHeight = NUM_COLS * 24;
        setPreferredSize(new Dimension(preferredWidth, preferredHeight));
    }
    
    public void addColor(int[][] points) {
        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLS; j++) {
                Color randomColor = TERRAIN[0];
                this.terrainGrid[i][j] = randomColor;
            }
        }
        for(int i = 0; i < points.length; i++) {
        	this.terrainGrid[points[i][0]][points[i][1]] = TERRAIN[1];
        }
        
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.clearRect(0, 0, getWidth(), getHeight());
        int rectWidth = getWidth() / NUM_ROWS;
        int rectHeight = getHeight() / NUM_COLS;
        
        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLS; j++) {
                int x = i * rectWidth;
                int y = j * rectHeight;
                Color terrainColor = terrainGrid[i][j];
                g.setColor(terrainColor);
                g.fillRect(x, y, rectWidth, rectHeight);
            }
        }
        
        for(int i = 1; i < NUM_ROWS; i++) {
                g.setColor(Color.getHSBColor(135, 135, 135));
                g.drawLine(0, i * 24, i * NUM_ROWS * 24, i * 24);
                for(int j = 1; j < NUM_COLS; j++) {
                	g.drawLine(i * 24, 0, i * 24, i * NUM_ROWS * 24);
                	
                }

        }
        this.repaint();
    }

}