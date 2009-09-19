package br.bassani.java.jhtrack.simulators;

public interface IRSimulatorListener {

	public void onAlternativeIrEvent(float rx1, float ry1, float rx2, float ry2, boolean cameraIsAboveScreen);
	
}
