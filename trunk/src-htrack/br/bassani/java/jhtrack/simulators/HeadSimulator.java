package br.bassani.java.jhtrack.simulators;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class HeadSimulator extends JPanel implements WindowListener, ChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4029117067678132465L;
	
	//Set up values parameters.
    static final int VALUE_MIN = -1000;
    static final int VALUE_MAX = 1000;
    static final int VALUE_INIT = 0;    //initial value
    
    float headX, headY, headDist;
    
    HeadSimulatorListener listiner;

    public HeadSimulator() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        addSlider("headX");
        addSlider("headY");
        addSlider("headDist");
        
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        
        listiner = null;
    }
    
    void addSlider(String name){
    	//Create the label.
        JLabel sliderLabel = new JLabel(name, JLabel.CENTER);
        sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        //Create the slider.
        JSlider framesPerSecond = new JSlider(JSlider.HORIZONTAL, VALUE_MIN, VALUE_MAX, VALUE_INIT);
        framesPerSecond.setName(name);
        framesPerSecond.addChangeListener(this);
        //Turn on labels at major tick marks.
        framesPerSecond.setMajorTickSpacing(500);
        framesPerSecond.setMinorTickSpacing(250);
        framesPerSecond.setPaintTicks(true);
        //framesPerSecond.setPaintLabels(true);
        framesPerSecond.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        Font font = new Font("Serif", Font.ITALIC, 15);
        framesPerSecond.setFont(font);

        //Put everything together.
        add(sliderLabel);
        add(framesPerSecond);
    }
    
    public HeadSimulatorListener getListiner() {
		return listiner;
	}

	public void setListiner(HeadSimulatorListener listiner) {
		this.listiner = listiner;
	}

	/** Add a listener for window events. */
    void addWindowListener(Window w) {
        w.addWindowListener(this);
    }

    //React to window events.
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowClosing(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}

    /** Listen to the slider. */
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
            int fps = (int)source.getValue();
            String name = source.getName();
            if(name.equals("headX")){
            	headX = (float)fps/(float)VALUE_MAX;
            }else if(name.equals("headY")){
            	headY = (float)fps/(float)VALUE_MAX;
            }else if(name.equals("headDist")){
            	headDist = (float)fps/(float)(VALUE_MAX/5);
            }
            if(listiner!=null){
            	listiner.onAlternativeHeadEvent(headX, headY, headDist);
            }
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI(HeadSimulatorListener listiner) {
        //Create and set up the window.
        JFrame frame = new JFrame("Head Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        HeadSimulator animator = new HeadSimulator();
        animator.setListiner(listiner);

        //Add content to the window.
        frame.add(animator, BorderLayout.CENTER);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void showHeadSimulator(final HeadSimulatorListener listiner) {
        /* Turn off metal's use of bold fonts */
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(listiner);
            }
        });
    }
    
    public static void main(String[] args) {
    	showHeadSimulator(null);
    }
}