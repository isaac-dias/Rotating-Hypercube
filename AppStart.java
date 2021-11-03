import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import javax.swing.*;
import javax.swing.event.*;
import java.util.ArrayList;

/**
* A program to visualize a wireframe of hypercube (starting from square), and how it rotates in 2D Space.
*/
public class AppStart {
	//GLOBAL VARIABLES
	//Colours
	Color bckgrndClr = new Color(43, 51, 59),	//Background colour
		  xySlidersClr = new Color(0, 2, 186),	//Inner colour - Sliders that directly affect x or y axis
		  numSlidersClr = Color.darkGray,		//Inner colour - Sliders that don't directly affect x or y axis
		  txtClr = Color.white,					//Text colour
		  btnClr = new Color(73, 83, 92),		//Colour for button
		  drawClr = new Color(8, 255, 12);		//Colour of object drawn
	//GUI
	private static JButton but1;
	private static JRadioButton rad1;
	private static JRadioButton rad2;
	private ArrayList<JSlider> sliders;
	private JPanel sliderPanel;							//Global to increase amount of sliders on button click
	private ArtSpace workspace;							//Global for paint method, used for sliders listeners
	//Logic
	private final double CENTER=220,					//Center of drawing space
						POINTDISTANCE=100;				//1/2 the length of the object
	private final int CAP=15;							//maximum amount of dimensions
	private ArrayList<ArrayList<Double>> points;		//Collection of all points
	private ArrayList<ArrayList<Integer>> connections;	//Mapping to draw one line between correct points, via position in outer ArrayList with points
	private ArrayList<RotationAxis> rot;				//Mapping to correlate each slider to a specific 2D rotation, via position in ArrayList with sliders
	private int[] sliderModifier = new int[] {0, -1};	//Modifies output of sliders so that translation anywhere across the sliders equates to equal rotations

	/**
	* Starting Point, Runs the program.
	* @param args	
	*/
	public static void main(String[] args) {
		new AppStart();
	}

	/**
	* Constructor.
	*/
	public AppStart() {
		//Build square to start
		points = new ArrayList<ArrayList<Double>>();
		connections = new ArrayList<ArrayList<Integer>>();
		points.add(new ArrayList<Double>());
		connections.add(new ArrayList<Integer>());
		points.add(new ArrayList<Double>());
		connections.add(new ArrayList<Integer>());
		points.add(new ArrayList<Double>());
		connections.add(new ArrayList<Integer>());
		points.add(new ArrayList<Double>());
		connections.add(new ArrayList<Integer>());
		points.get(0).add(CENTER-POINTDISTANCE);
		points.get(0).add(CENTER-POINTDISTANCE);
		points.get(1).add(CENTER-POINTDISTANCE);
		points.get(1).add(CENTER+POINTDISTANCE);
		points.get(2).add(CENTER+POINTDISTANCE);
		points.get(2).add(CENTER+POINTDISTANCE);
		points.get(3).add(CENTER+POINTDISTANCE);
		points.get(3).add(CENTER-POINTDISTANCE);
		connections.get(0).add(1);
		connections.get(1).add(2);
		connections.get(2).add(3);
		connections.get(3).add(0);

		//Frame for app
		JFrame frame = new JFrame("Hypercube Viewer App");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		//Build panel for visuals
		workspace = new ArtSpace();
    	workspace.setPreferredSize(new Dimension((int)CENTER*2, (int)CENTER*2));
		workspace.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), BorderFactory.createTitledBorder("")));
		//Build panel for user input
		JPanel inputSpace = new JPanel();
		inputSpace.setLayout(new BorderLayout());  
		inputSpace.setPreferredSize(new Dimension((int)CENTER*2, 200));
		inputSpace.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), BorderFactory.createTitledBorder(null, "  Input", 0, 0, null, txtClr)));
		
		//User inputs
		//button - increase dimensions
		but1 = new JButton("2 Dimensions");
		but1.addActionListener(new AllListeners());
		//radio button - toggle boring sliders
		rad1 = new JRadioButton("only x/y sliders", true);
		rad1.addActionListener(new AllListeners());
		//radio button - toggle projection
		rad2 = new JRadioButton("projection", true);
		rad2.addActionListener(new AllListeners());
		//slider - first slider, for x-y axis
		sliders = new ArrayList<JSlider>();
		sliders.add(new JSlider(-180,180));
		rot = new ArrayList<RotationAxis>();
		rot.add(new RotationAxis(0,1));
		sliders.get(0).addChangeListener(new AllListeners());
		//slider info
		JTextField sldrInfo = new JTextField("X-Y axis");
		sldrInfo.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		sldrInfo.setFocusable(false);

		//User inputs - right side layout
		JPanel rightSidePanel = new JPanel();
		rightSidePanel.setLayout(new BoxLayout(rightSidePanel, BoxLayout.Y_AXIS));
		rightSidePanel.add(rad1);
		rightSidePanel.add(rad2);
		rightSidePanel.add(but1);
		inputSpace.add(rightSidePanel, BorderLayout.LINE_END);

		//User inputs - left side layout
		JPanel firstInnerSliderPanel = new JPanel(new FlowLayout());
		firstInnerSliderPanel.add(Box.createHorizontalStrut(10));
		firstInnerSliderPanel.add(sliders.get(0));
		firstInnerSliderPanel.add(sldrInfo);
		sliderPanel = new JPanel();
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.Y_AXIS));
		sliderPanel.add(firstInnerSliderPanel);
		JScrollPane leftSidePanel = new JScrollPane(sliderPanel);
		leftSidePanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		leftSidePanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		leftSidePanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		inputSpace.add(leftSidePanel, BorderLayout.LINE_START);

		//Colour scheme (most of it)
		workspace.setBackground(bckgrndClr);
		inputSpace.setBackground(bckgrndClr);
		rightSidePanel.setBackground(bckgrndClr);
		firstInnerSliderPanel.setBackground(bckgrndClr);
		rad1.setBackground(bckgrndClr);
		rad2.setBackground(bckgrndClr);
		sldrInfo.setBackground(bckgrndClr);
		sliders.get(0).setBackground(xySlidersClr);
		but1.setBackground(btnClr);
		but1.setForeground(txtClr);
		rad1.setForeground(txtClr);
		rad2.setForeground(txtClr);
		sldrInfo.setForeground(txtClr);

		//Display frame
		frame.getContentPane().add(workspace, BorderLayout.CENTER);
		frame.getContentPane().add(inputSpace, BorderLayout.SOUTH);
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
			SwingUtilities.updateComponentTreeUI(frame);
		} catch (Exception a) { System.out.println("ERROR - look and feel failed..." + a); }
		frame.pack();
		frame.setVisible(true);
	}

	/**
	* Combined class for all listener types. 
	*/
	private class AllListeners implements ActionListener, ChangeListener{
		/**
		* Button Events: (1)Jbutton event: Increase dimensions and modify GUI
		*				 (2)JRadioButton event: Toggle grey sliders
		*				 (3)JRadioButton event: Toggles 3D projection
		* @param	e	info generated describing the source that triggered the event
		*/
		public void actionPerformed(ActionEvent e){
			//(1)Jbutton event: Increase dimensions and modify GUI
			if (e.getSource() == but1  &&  points.get(0).size() < CAP){
				//Loop - for each dimension that currently exists
				for(int i=0; i<points.get(0).size(); i++){
					//Slider - set up new slider and map axis for it's rotation
					sliders.add(new JSlider(-180, 180));
					rot.add(new RotationAxis(i, points.get(0).size()));
					sliders.get(sliders.size()-1).addChangeListener(new AllListeners());
					//Slider info - generic
					JTextField newSldrInfo = new JTextField(" " +(i-1)+ "-" + (points.get(0).size()-1) + " axis ");
					sliders.get(sliders.size()-1).setBackground(numSlidersClr);
					newSldrInfo.setBorder(javax.swing.BorderFactory.createEmptyBorder());
					newSldrInfo.setFocusable(false);
					newSldrInfo.setBackground(bckgrndClr);
					newSldrInfo.setForeground(txtClr);
					//Slider info - for sliders on X or Y axis
					if(i==0){
						newSldrInfo.setText(" X-" + (points.get(0).size()-1) + " axis ");
						sliders.get(sliders.size()-1).setBackground(xySlidersClr);
					}else if(i==1){
						newSldrInfo.setText(" Y-" + (points.get(0).size()-1) + " axis ");
						sliders.get(sliders.size()-1).setBackground(xySlidersClr);
					}
					
					//Add new slider to GUI
					JPanel newInnerSliderPanel = new JPanel(new FlowLayout());
					newInnerSliderPanel.setBackground(bckgrndClr);
					newInnerSliderPanel.add(Box.createHorizontalStrut(10));
					newInnerSliderPanel.add(sliders.get(sliders.size()-1));
					newInnerSliderPanel.add(newSldrInfo);
					sliderPanel.add(newInnerSliderPanel);

					//toggle newest slider if necessary (greyed out sliders)
					if(sliders.get(sliders.size()-1).getBackground()==numSlidersClr) sliderPanel.getComponent(sliders.size()-1).setVisible(!rad1.isSelected());
				}
				//increase dimensions
				increaseDim();
				but1.setText(points.get(0).size() + " Dimensions");
				workspace.repaint();
			}

			//(2)JRadioButton event: Toggle grey sliders
			if (e.getSource() == rad1){
				for(int i=0; i<sliderPanel.getComponentCount(); i++){
					if(sliders.get(i).getBackground()==numSlidersClr) sliderPanel.getComponent(i).setVisible(!rad1.isSelected());
				}
			}

			//(3)JRadioButton event: Toggles 3D projection
			if (e.getSource() == rad2){
				workspace.repaint();
			}
		}

		/**
		* Slider Events: Sliding any slider rotates the hypercube across 2 dimensions (specified by slider)
		* @param	e	info generated describing the source that triggered the event
		*/
		public void stateChanged(ChangeEvent e){
			for(int i=0; i<sliders.size(); i++){
				if (e.getSource() == sliders.get(i)){
					//if slider is different than the last slider used, reset slider modifier
					if(i != sliderModifier[1]){
						sliderModifier[0]=sliders.get(i).getValue();
						sliderModifier[1]=i;
					}
					//rotation matrix
					double tempCos=Math.cos(Math.toRadians(sliders.get(i).getValue()-sliderModifier[0])),
						   tempSin=Math.sin(Math.toRadians(sliders.get(i).getValue()-sliderModifier[0]));
						for(int x=0; x<points.size(); x++){
							double tempX = points.get(x).get(rot.get(i).getAxis1()),
								   tempY = points.get(x).get(rot.get(i).getAxis2());
							points.get(x).set(rot.get(i).getAxis1(), (((tempX-CENTER) * tempCos) - ((tempY-CENTER) * tempSin)) + CENTER);
							points.get(x).set(rot.get(i).getAxis2(), (((tempX-CENTER) * tempSin) + ((tempY-CENTER) * tempCos)) + CENTER);
						}
					//update slider modifier
					sliderModifier[0] = sliders.get(i).getValue();
					//display
					workspace.repaint();
					break;	//no need to iterate
				}
			}
		}
	}

	/**
	* Special class to overide JPanel's paint function
	*/
	private class ArtSpace extends JPanel{
		/**
		* JPanel's paint function modified for the hypercube.
		* @param	gp	allow an application to draw onto components
		*/
		public void paint(Graphics gp) {
			super.paint(gp);
			Graphics2D graphics = (Graphics2D) gp;
			for(int x=0; x<connections.size(); x++){
				for(int y=0; y<connections.get(x).size(); y++){
					graphics.setColor(drawClr);
					Point2D.Double a = getProjection(points.get(x));
					Point2D.Double b = getProjection(points.get(connections.get(x).get(y)));
					if(rad2.isSelected())graphics.draw(new Line2D.Double(a.x, a.y, b.x, b.y));
					else graphics.draw(new Line2D.Double(((points.get(x).get(0)-CENTER)*0.5)+CENTER, ((points.get(x).get(1)-CENTER)*0.5)+CENTER, ((points.get(connections.get(x).get(y)).get(0)-CENTER)*0.5)+CENTER, ((points.get(connections.get(x).get(y)).get(1)-CENTER)*0.5)+CENTER ));
				}
			}
		}

		/**
		 * Creates a projection of given vector in 2d space
		 * @param objectPoint the vector to be projected in 2D space
		 * @return the projection
		 */
		private Point2D.Double getProjection(ArrayList<Double> objectPoint){
			if(objectPoint.size()==2) return new Point2D.Double(((objectPoint.get(0)-CENTER)*0.5)+CENTER, ((objectPoint.get(1)-CENTER)*0.5)+CENTER);

			ArrayList<Double> vectorAB = new ArrayList<Double>();
			ArrayList<Double> vectorAC = new ArrayList<Double>();
			ArrayList<Double> projPoint = new ArrayList<Double>();
			ArrayList<Double> viewPoint = new ArrayList<Double>();
			Point2D.Double proj = new Point2D.Double();
			double projV = 0,
					divideBy=0; 

			//Set up projPoint and viewPoint
			projPoint.add(objectPoint.get(0));
			projPoint.add(objectPoint.get(1));
			viewPoint.add(CENTER);
			viewPoint.add(CENTER);
			for(int i=2; i<objectPoint.size(); i++){
				viewPoint.add(POINTDISTANCE*10);
				projPoint.add(POINTDISTANCE*20);
			}
			
			//build vectors and projection vector
			for(int c=0; c<objectPoint.size(); c++){ //first two values of second vector are 0, done outside.
				vectorAC.add(objectPoint.get(c)-projPoint.get(c));
				vectorAB.add(objectPoint.get(c)-viewPoint.get(c));
				projV += (vectorAC.get(c)*vectorAB.get(c));
				divideBy += (vectorAB.get(c) * vectorAB.get(c));
			}
			projV /= Math.sqrt(divideBy);

			//change projV to a fraction of vectorAB's distance
			projV /= Math.sqrt(divideBy);

			proj.x = vectorAB.get(0) * projV + CENTER;
			proj.y = vectorAB.get(1) * projV + CENTER;
			return proj;
		}
	}

	/**
	* Function that increases by 1 the amount of dimensions for every point in the hypercube, and maps out connections for drawing lines. It makes a copy of current points, and offsets new and old points by 'POINTDISTANCE' about the origin.
	*/
	public void increaseDim(){
		ArrayList<ArrayList<Double>> points2 = new ArrayList<ArrayList<Double>>();
		//Double connections and offset
		for (int i1=0; i1<points.size(); i1++){
			points2.add(new ArrayList<Double>());
			for(int i2=0; i2<points.get(i1).size(); i2++) points2.get(i1).add(points.get(i1).get(i2));
			points.get(i1).add(CENTER+POINTDISTANCE);
			points2.get(i1).add(CENTER-POINTDISTANCE);
		}
		//Update mapping and combine points2 with points
		for(int x=0; x<points2.size(); x++){
			points.add(points2.get(x));
			connections.add(new ArrayList<Integer>());
			for (int y=0; y<connections.get(x).size(); y++) connections.get(connections.size()-1).add(connections.size()-1 + connections.get(x).get(y) - x);
			connections.get(x).add(points.size()-1);
		}
	}

	/**
	* Class that stores two integers as final variables, used to map a slider to 2 axis for rotations.
	*/
	private class RotationAxis{
		private int axis1, axis2;
		/**
		 * Constructor, only way to populate data
		 */
		public RotationAxis(int axisOne, int axisTwo){
			axis1=axisOne;
			axis2=axisTwo;
		}
		
		/**
		 * Getter for axis1
		 * @return returns the given axis, expressed as its position in the list of dimensions created, starting from 0
		 */
		public int getAxis1() {return axis1;}

		/**
		 * Getter for axis1
		 * @return returns the given axis, expressed as its position in the list of dimensions created, starting from 0
		 */
		public int getAxis2() {return axis2;}
	}
}