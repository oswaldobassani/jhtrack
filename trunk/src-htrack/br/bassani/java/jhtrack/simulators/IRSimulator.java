/*
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

package br.bassani.java.jhtrack.simulators;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/*
 * SliderDemo.java requires all the files in the images/doggy
 * directory.
 */
public class IRSimulator extends JPanel implements WindowListener, ChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8218947718335641279L;
	
	//Set up values parameters.
    static final int VALUE_MIN = -640;
    static final int VALUE_MAX = 640;
    static final int VALUE_INIT = 0;    //initial value
    
    int rx1, ry1, rx2, ry2;
    
    IRSimulatorListener listiner;

    public IRSimulator() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        addSlider("RX1");
        addSlider("RY1");
        addSlider("RX2");
        addSlider("RY2");
        
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
        framesPerSecond.setMajorTickSpacing(100);
        framesPerSecond.setMinorTickSpacing(10);
        framesPerSecond.setPaintTicks(true);
        framesPerSecond.setPaintLabels(true);
        framesPerSecond.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        Font font = new Font("Serif", Font.ITALIC, 15);
        framesPerSecond.setFont(font);

        //Put everything together.
        add(sliderLabel);
        add(framesPerSecond);
    }
    
    public IRSimulatorListener getListiner() {
		return listiner;
	}

	public void setListiner(IRSimulatorListener listiner) {
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
            if(name.equals("RX1")){
            	rx1 = fps;
            }else if(name.equals("RY1")){
            	ry1 = fps;
            }else if(name.equals("RX2")){
            	rx2 = fps;
            }else if(name.equals("RY2")){
            	ry2 = fps;
            }
            if(listiner!=null){
            	listiner.onAlternativeIrEvent(rx1, ry1, rx2, ry2, false);
            }
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI(IRSimulatorListener listiner) {
        //Create and set up the window.
        JFrame frame = new JFrame("IR Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        IRSimulator animator = new IRSimulator();
        animator.setListiner(listiner);

        //Add content to the window.
        frame.add(animator, BorderLayout.CENTER);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void showIRSimulator(final IRSimulatorListener listiner) {
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
    	showIRSimulator(null);
    }
}