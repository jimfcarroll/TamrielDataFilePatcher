import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

public class ProgressDisplayPanel extends JPanel {
	private int progress;
	private Font font;
	private Color lightBlue;
	
	public ProgressDisplayPanel() {
		progress = 0;
		font = new Font("Arial", Font.BOLD, 20);
		lightBlue = new Color(38, 112, 188);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setPaintMode();
		
		int currentHeight = getHeight();
		int currentWidth = getWidth();
		
		if (progress == 0) {
			g2d.setColor(Color.gray);
		} else {
			float percentageFactor = ((float)progress) / 100;
			g2d.setColor(lightBlue);
			g2d.fillArc(31, 31, currentWidth-65, currentHeight-65, 90, -Math.round(360*percentageFactor));
			
			g2d.setColor(Color.black);
		}
		
		g2d.setStroke(new BasicStroke(2));
		g2d.drawOval(21, 21, currentWidth-45, currentHeight-45);
		
		g2d.setFont(font);
		g2d.drawString(progress+" %", (currentWidth/2)-15, (currentHeight/2));
		
	}
	
	public void updateProgress(int progress) {
		this.progress = progress;
		repaint();
	}
}
