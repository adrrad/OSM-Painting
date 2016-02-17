
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;
 
public class View extends JFrame implements Observer {
	public static final long serialVersionUID = 20160216;
	Canvas canvas;
	JPanel leftPanel = new JPanel();
	boolean antia = false;
	Model model;
	static AffineTransform trans = new AffineTransform();

	public void toggleAA() {
		antia = !antia;
		repaint();
	}

	public Point2D inverse(double x, double y) {
		try {
			return trans.inverseTransform(new Point2D.Double(x, y), null);
		} catch (NoninvertibleTransformException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void zoom(double s, double cx, double cy) {
		pan(-cx, -cy);
		trans.preConcatenate(AffineTransform.getScaleInstance(s, s));
		pan(cx, cy);
	}

	public void pan(double dx, double dy) {
		trans.preConcatenate(AffineTransform.getTranslateInstance(dx, dy));
		model.dirty();
	}

	public void update(Observable obs, Object obj) {
		repaint();
	}

	public View(Model m) {
		model = m;
		model.addObserver(this);
		canvas = new Canvas();
		setLayout(new BorderLayout());
		getContentPane().add(canvas, BorderLayout.CENTER);
		getContentPane().add(leftPanel, BorderLayout.WEST);
		leftPanel.add(new Button("hueue"));
		pack();
		setSize(512, 512);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
		pan(-model.minlon, -model.maxlat);
		zoom(canvas.getWidth()/Math.max(model.maxlon-model.minlon, model.minlat-model.maxlat),0,0);
	}

	class Canvas extends JComponent {
		public static final long serialVersionUID = 20160216;

		public void paint(Graphics _g) {
			Graphics2D g = (Graphics2D) _g;
			g.setTransform(trans);
			Rectangle2D bbox = new Rectangle2D.Float(model.minlon, model.maxlat, model.maxlon-model.minlon, model.minlat-model.maxlat);
			g.setClip(bbox);
			if (antia) g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setStroke(new BasicStroke(Float.MIN_VALUE));
			g.setColor(new Color(127, 127, 255));
			for (Shape s : model.water) g.fill(s);
			g.setColor(Color.BLACK);
			for (Shape s : model.road) g.draw(s);
		}
	}
}