package br.bassani.java.jhtrack;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import wiiusej.WiiUseApiManager;
import wiiusej.Wiimote;
import wiiusej.values.IRSource;
import wiiusej.wiiusejevents.physicalevents.ExpansionEvent;
import wiiusej.wiiusejevents.physicalevents.IREvent;
import wiiusej.wiiusejevents.physicalevents.MotionSensingEvent;
import wiiusej.wiiusejevents.physicalevents.WiimoteButtonsEvent;
import wiiusej.wiiusejevents.utils.WiimoteListener;
import wiiusej.wiiusejevents.wiiuseapievents.ClassicControllerInsertedEvent;
import wiiusej.wiiusejevents.wiiuseapievents.ClassicControllerRemovedEvent;
import wiiusej.wiiusejevents.wiiuseapievents.DisconnectionEvent;
import wiiusej.wiiusejevents.wiiuseapievents.GuitarHeroInsertedEvent;
import wiiusej.wiiusejevents.wiiuseapievents.GuitarHeroRemovedEvent;
import wiiusej.wiiusejevents.wiiuseapievents.NunchukInsertedEvent;
import wiiusej.wiiusejevents.wiiuseapievents.NunchukRemovedEvent;
import wiiusej.wiiusejevents.wiiuseapievents.StatusEvent;

import br.bassani.java.jhtrack.simulators.HeadSimulator;
import br.bassani.java.jhtrack.simulators.HeadSimulatorListener;
import br.bassani.java.jhtrack.simulators.IRSimulator;
import br.bassani.java.jhtrack.simulators.IRSimulatorListener;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureIO;

/**
 * Java Head Track
 * 
 * This source code is based on Johnny Lee's demo (WiIDesktopVR.cs) 
 * This source code is based on <michaelwiibrew at gmail.com> Head Track ported to Wii GPU (htrack2.c)
 * 
 * @author Oswaldo Bassani < oswaldo.bassani at gmail.com >
 * @version 1.0
 */
public class JHTrack implements GLEventListener, WiimoteListener, IRSimulatorListener, HeadSimulatorListener {

	private static final int WIDTH = 1024; //800;
	private static final int HEIGHT = 768; //600;
	
	float headX, headY, headDist;
	float nearPlane;

	private static final int nTarget = 7;
	double[] targetX = new double[nTarget];
	double[] targetY = new double[nTarget];
	double[] targetZ = new double[nTarget];

	float TARGET_W = 0.15f;

	boolean DEBUG_WIIMOTE_IR_COMPLETO = false;
	boolean DEBUG_WIIMOTE_EVENTOS_EXTRAS = false;

	public static void main(String[] args) {
		new JHTrack().run(args);
	}

	private static JFrame frame;
	
	private static GLCanvas canvas;

	private void run(String[] args) {
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);

		frame = new JFrame("Java Head Tracker - JOGL + WiiUseJ");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas = new GLCanvas();

		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		JMenuItem item = new JMenuItem("Exit");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		menu.add(item);

		menuBar.add(menu);

		canvas.addGLEventListener(this);
		frame.getContentPane().add(canvas);
		frame.setJMenuBar(menuBar);
		frame.setSize(WIDTH, HEIGHT);
		
		frame.setVisible(true);

		try {
			Thread.sleep(1 * 1000);
		} catch (InterruptedException e) {
		}

		setTextureFile(new File("./data/target.png"));
		canvas.repaint();
	}

	private boolean newTexture;
	private boolean flushTexture;
	private File file;
	private Texture texture;
	private GLU glu = new GLU();

	public JHTrack() {
		super();

		headX = 0.0f;
		headY = 0.0f;
		headDist = 1f;
		nearPlane = 0.05f;

		rollTargets();

		/*
		 * Libs:
		 * + libbluethooth-dev (Ubuntu 9.04)
		 */

		Wiimote[] wiimotes = WiiUseApiManager.getWiimotes(1, true);
		if(wiimotes.length>0){
			System.out.println("Wiimote encontrados: "+wiimotes.length);
			Wiimote wiimote = wiimotes[0];
			wiimote.setSensorBarAboveScreen();
			//wiimote.setSensorBarBelowScreen();
			wiimote.activateIRTRacking();
			wiimote.activateMotionSensing();
			wiimote.addWiiMoteEventListeners(this);
		}else{
			System.out.println("Nenhum wiimote encontrado.");
			if(false){
				IRSimulator.showIRSimulator(this);
			}else{
				HeadSimulator.showHeadSimulator(this);
			}
		}
	}

	public void setTextureFile(File file) {
		this.file = file;
		newTexture = true;
	}

	public void flushTexture() {
		flushTexture = true;
	}

	public void init(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		drawable.setGL(new DebugGL(gl));

		gl.glClearColor(0, 0, 0, 0);
		gl.glEnable(GL.GL_DEPTH_TEST);
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL gl = drawable.getGL();

		boolean ortho = false;

		if(ortho){
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glLoadIdentity();
			glu.gluOrtho2D(0, 1, 0, 1);
			gl.glMatrixMode(GL.GL_MODELVIEW);
			gl.glLoadIdentity();
		}else{
			double upx = 0f, upy = 1f, upz = 0f;

			gl.glViewport(0, 0, width, height);
			gl.glMatrixMode(GL.GL_PROJECTION);
			gl.glLoadIdentity();
			glu.gluPerspective(30, (float) width / (float) height, 1.0, 100.0);
			gl.glMatrixMode(GL.GL_MODELVIEW);
			gl.glLoadIdentity();

			glu.gluLookAt(headX, headY, 1.0*headDist, //eye
					headX, headY, 0, //center
					upx, upy, upz); //up
		}
	}

	public void dispose(GLAutoDrawable drawable) {

	}

	public void display(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		if (flushTexture) {
			flushTexture = false;
			if (texture != null) {
				texture.dispose();
				texture = null;
			}
		}

		if (newTexture) {
			newTexture = false;

			if (texture != null) {
				texture.dispose();
				texture = null;
			}

			try {
				System.err.println("Loading texture...");
				texture = TextureIO.newTexture(file, true);
				System.err.println("Texture estimated memory size = " + texture.getEstimatedMemorySize());
			} catch (IOException e) {
				e.printStackTrace();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(bos));
				JOptionPane.showMessageDialog(null,
						bos.toString(),
						"Error loading texture",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		gl.glColor3f(1.0f, 1.0f, 1.0f);
		draw_grid(gl);

		drawTargets(gl);

		gl.glColor3f(1.0f, 0.0f, 0.0f);
		drawLines(gl);
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {

	}

	private void rollTargets(){
		int i;
		for(i=0; i<nTarget; i++){
			targetX[i] = (0.5f - (float)Math.random())/1.3f;
			targetY[i] = (0.5f - (float)Math.random())/1.3f;
			targetZ[i] = (-0.5f - 4 * (float)Math.random())/1.2f;
		}
	}

	private void drawTargets(GL gl){
		if (texture != null) {
			texture.enable();

			// do not draw the transparent parts of the texture
			gl.glEnable(GL.GL_BLEND);
			// don't show source alpha parts in the destination
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			// determine which areas of the polygon are to be rendered
			gl.glEnable(GL.GL_ALPHA_TEST);
			// only render if alpha > 0
			gl.glAlphaFunc(GL.GL_GREATER, 0);
			// enable texturing
			gl.glEnable(GL.GL_TEXTURE_2D);

			texture.bind();
			gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
			TextureCoords coords = texture.getImageTexCoords();

			int i;
			for(i=0; i<nTarget; i++){

				float x1, y1, x2, y2, z;

				boolean fixed = false;
				if(fixed){
					x1 = 0.25f;
					y1 = 0.25f;
					x2 = 0.75f;
					y2 = 0.75f;
					z = -0.5f;
				}else{
					x1 = (float)targetX[i];
					y1 = (float)targetY[i];
					x2 = (float)targetX[i] + TARGET_W;
					y2 = (float)targetY[i] + TARGET_W;

					z = (float)targetZ[i];
				}

				gl.glBegin(GL.GL_QUADS);
				gl.glTexCoord2f(coords.left(), coords.bottom());
				gl.glVertex3f(x1, y1, z);
				gl.glTexCoord2f(coords.right(), coords.bottom());
				gl.glVertex3f(x2, y1, z);
				gl.glTexCoord2f(coords.right(), coords.top());
				gl.glVertex3f(x2, y2, z);
				gl.glTexCoord2f(coords.left(), coords.top());
				gl.glVertex3f(x1, y2, z);
				gl.glEnd();
			}

			//gl.glDisable(GL.GL_ALPHA);  // switch off transparency
			gl.glDisable(GL.GL_BLEND);

			texture.disable();
		}
	}

	private void draw_grid(GL gl){
		float nearPlane = 0.05f;
		float farPlane = 5.0f;
		gl.glLineWidth(0.5f);
		gl.glBegin(GL.GL_LINES);
		for (float x = -0.5f; x < 1.0f; x+= 1.0f){
			for(float y = -0.5f; y < 0.51f; y+= 0.2f){
				gl.glVertex3f(x , y, -nearPlane);
				gl.glVertex3f(x , y, -farPlane);

				gl.glVertex3f(y , x, -nearPlane);
				gl.glVertex3f(y , x, -farPlane);
			}
		}
		for (float z = nearPlane; z<farPlane; z+= 0.2f)  
		{
			gl.glVertex3f(-0.5f, -0.5f, -z);
			gl.glVertex3f(-0.5f, 0.5f, -z);

			gl.glVertex3f(-0.5f, 0.5f, -z);
			gl.glVertex3f(0.5f, 0.5f, -z);

			gl.glVertex3f(0.5f, 0.5f, -z);
			gl.glVertex3f(0.5f, -0.5f, -z);

			gl.glVertex3f(0.5f, -0.5f, -z);
			gl.glVertex3f(-0.5f, -0.5f, -z);
		}
		gl.glEnd();
	}

	private void drawLines(GL gl){
		gl.glLineWidth(1.5f);
		gl.glBegin(GL.GL_LINES);
		for (int i=0; i<nTarget; i++)
		{
			float x, y, yBase, z;
			yBase = -0.5f;

			boolean fixed = false;
			if(fixed){
				x = 0.5f;
				y = 0.5f;
				z = 0f;
			}else{
				x = (float)targetX[i];
				y = (float)targetY[i];
				z = (float)targetZ[i];
			}
			gl.glVertex3f(x+TARGET_W/2, y+TARGET_W/2, z);
			gl.glVertex3f(x+TARGET_W/2, yBase, z);
		}
		gl.glEnd();
	}

	@Override
	public void onButtonsEvent(WiimoteButtonsEvent event) {
		System.out.println("onButtonsEvent - "+event);
		if(event.isButtonAPressed()){
			System.exit(0);
		}
	}

	@Override
	public void onClassicControllerInsertedEvent(ClassicControllerInsertedEvent event) {
		if(DEBUG_WIIMOTE_EVENTOS_EXTRAS) System.out.println("onClassicControllerInsertedEvent - "+event);
	}

	@Override
	public void onClassicControllerRemovedEvent(ClassicControllerRemovedEvent event) {
		if(DEBUG_WIIMOTE_EVENTOS_EXTRAS) System.out.println("onClassicControllerRemovedEvent - "+event);
	}

	@Override
	public void onDisconnectionEvent(DisconnectionEvent event) {
		if(DEBUG_WIIMOTE_EVENTOS_EXTRAS) System.out.println("onDisconnectionEvent - "+event);
	}

	@Override
	public void onExpansionEvent(ExpansionEvent event) {
		if(DEBUG_WIIMOTE_EVENTOS_EXTRAS) System.out.println("onExpansionEvent - "+event);
	}

	@Override
	public void onGuitarHeroInsertedEvent(GuitarHeroInsertedEvent event) {
		if(DEBUG_WIIMOTE_EVENTOS_EXTRAS) System.out.println("onGuitarHeroInsertedEvent - "+event);
	}

	@Override
	public void onGuitarHeroRemovedEvent(GuitarHeroRemovedEvent event) {
		if(DEBUG_WIIMOTE_EVENTOS_EXTRAS) System.out.println("onGuitarHeroRemovedEvent - "+event);
	}

	float radiansPerPixel = (float)(Math.PI) / 1024.0f; //45 degree field of view with a 1024x768 camera
	float movementScaling = 1.0f;
	float dotDistanceInMM = 37.5f; // 8.5f * 25.4f;//width of the wii sensor bar
	float screenHeightinMM = 37.5f;// 20 * 25.4f;
	int cameraIsAboveScreen = 1;
	float cameraVerticaleAngle = 0;
	
	float headDistMAX = 4.0f;

	@Override
	public void onIrEvent(IREvent irData) {
		IRSource[] points = irData.getIRPoints();

		int numvisible = points.length;
		System.out.println("onIrEvent - --------------- ");
		System.out.println("onIrEvent - points.length - "+numvisible);
		for(int i=0; i<numvisible; i++){
			System.out.println("onIrEvent - points["+i+"] - ("+points[i].getRx()+", "+points[i].getRy()+")");
		}
		System.out.println("onIrEvent - --------------- ");
		if (numvisible > 1) {

			if(DEBUG_WIIMOTE_IR_COMPLETO) System.out.println("onIrEvent - "+irData);

			float rx[] = new float[2];
			rx[0] = points[0].getRx();
			rx[1] = points[1].getRx();
			float ry[] = new float[2];
			ry[0] = points[0].getRy();
			ry[1] = points[1].getRy();

			float dx = rx[0] - rx[1];
			float dy = ry[0] - ry[1];
			float pointDist = (float)Math.sqrt(dx * dx + dy * dy);

			float angle = radiansPerPixel * pointDist / 2;
			//in units of screen height since the box is a unit cube and box hieght is 1
			headDist = movementScaling * (float)((dotDistanceInMM / 2) / Math.tan(angle)) / screenHeightinMM;
			
			if(headDist<0) headDist = 0.01f;
			if(headDist>headDistMAX) headDist = headDistMAX;

			float avgX = (rx[0] + rx[1]) / 2.0f;
			float avgY = (ry[0] + ry[1]) / 2.0f;

			//should calaculate based on distance

			headX = (float)(movementScaling *  Math.sin(radiansPerPixel * (avgX - 512)) * headDist);

			double relativeVerticalAngle = (avgY - 384) * radiansPerPixel;//relative angle to camera axis

			if(irData.isSensorBarBelow()) // cameraIsAboveScreen
				headY = 0.0f + (float)(movementScaling * Math.sin(relativeVerticalAngle + cameraVerticaleAngle) * headDist);
			else
				headY = -0.0f + (float)(movementScaling * Math.sin(relativeVerticalAngle + cameraVerticaleAngle) * headDist);
		}
		
		// if(headDist<0) headDist = 0.01f;
		// if(headDist>headDistMAX) headDist = headDistMAX;
		
		if(headX<-0.5f) headX = -0.5f;
		if(headX>0.5f) headX = 0.5f;
		
		if(headY<-1.0f) headY = -1.0f;
		if(headY>1.0f) headY = 1.0f;
		
		System.out.println("onIrEvent - ("+headX+","+headY+")/("+headDist+")");
		if(frame!=null){
			if(canvas!=null){
				canvas.repaint();
				canvas.reshape(0, 0, WIDTH, HEIGHT);
			}
		}
	}

	public void onAlternativeIrEvent(float rx1, float ry1, float rx2, float ry2, boolean cameraIsAboveScreen){
		System.out.println("onAlternativeIrEvent - ("+rx1+","+ry1+")-("+rx2+","+ry2+") / "+cameraIsAboveScreen);

		float dx = rx1 - rx2;
		float dy = ry1 - ry2;
		float pointDist = (float)Math.sqrt(dx * dx + dy * dy);

		float angle = radiansPerPixel * pointDist / 2;
		//in units of screen height since the box is a unit cube and box hieght is 1
		headDist = movementScaling * (float)((dotDistanceInMM / 2) / Math.tan(angle)) / screenHeightinMM;

		float avgX = (rx1 + rx2) / 2.0f;
		float avgY = (ry1 + ry2) / 2.0f;

		//should calaculate based on distance

		headX = (float)(movementScaling *  Math.sin(radiansPerPixel * (avgX - 512)) * headDist);

		double relativeVerticalAngle = (avgY - 384) * radiansPerPixel;//relative angle to camera axis

		if(cameraIsAboveScreen)
			headY = .5f+(float)(movementScaling * Math.sin(relativeVerticalAngle + 0)  *headDist);
		else
			headY = -.5f + (float)(movementScaling * Math.sin(relativeVerticalAngle + 0) * headDist);

		if(frame!=null){
			if(canvas!=null){
				canvas.repaint();
				canvas.reshape(0, 0, WIDTH, HEIGHT);
			}
		}
	}

	@Override
	public void onAlternativeHeadEvent(float headX, float headY, float headDist) {
		System.out.println("onAlternativeHeadEvent - ("+headX+","+headY+")/("+headDist+")");
		this.headX = headX;
		this.headY = headY;
		this.headDist = headDist;
		
		if(frame!=null){
			if(canvas!=null){
				canvas.repaint();
				canvas.reshape(0, 0, WIDTH, HEIGHT);
			}
		}
	}

	@Override
	public void onMotionSensingEvent(MotionSensingEvent event) {
		if(DEBUG_WIIMOTE_EVENTOS_EXTRAS) System.out.println("onMotionSensingEvent - "+event);
	}

	@Override
	public void onNunchukInsertedEvent(NunchukInsertedEvent event) {
		if(DEBUG_WIIMOTE_EVENTOS_EXTRAS) System.out.println("onNunchukInsertedEvent - "+event);
	}

	@Override
	public void onNunchukRemovedEvent(NunchukRemovedEvent event) {
		if(DEBUG_WIIMOTE_EVENTOS_EXTRAS) System.out.println("onNunchukRemovedEvent - "+event);
	}

	@Override
	public void onStatusEvent(StatusEvent event) {
		if(DEBUG_WIIMOTE_EVENTOS_EXTRAS) System.out.println("onStatusEvent - "+event);
	}

}
