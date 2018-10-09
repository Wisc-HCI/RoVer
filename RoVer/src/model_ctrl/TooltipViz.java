package model_ctrl;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import enums.StateClass;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;

import model.Microinteraction;
import model.Module;
import model.State;

public class TooltipViz extends Canvas{
	
	Microinteraction micro;
	
	// human-readiness icons
	Image ready;
	Image busy;
	Image ignore;
	
	//arrow icons
	Image rightLine;
	Image leftLine;
	Image rightLines3;
	Image leftLines3;
	Image rightLines2;
	Image leftLines2;
	Image arrowheadLeftRight;
	Image arrowheadRight2;
	Image arrowheadRight3;
	
	@SuppressWarnings("restriction")
	public TooltipViz(double width, double height, Microinteraction micro) {
		super(width, height);
		this.micro = micro;
		
		String path = "Icons" + File.separator;
		//System.out.println(path + "Icon_HumReady_gray.png");
		try {
			ready = new Image(new FileInputStream(path + "Icon_HumReady.png"));
			busy = new Image(new FileInputStream(path + "Icon_HumBusy.png"));
			ignore = new Image(new FileInputStream(path + "Icon_HumIgnore.png"));
			
			path += "arrows" + File.separator;
			rightLine = new Image(new FileInputStream(path + "rightlines_1.png"));
			leftLine = new Image(new FileInputStream(path + "leftlines_1.png"));
			rightLines3 = new Image(new FileInputStream(path + "rightlines_3.png"));
			leftLines3 = new Image(new FileInputStream(path + "leftlines_3.png"));
			rightLines2 = new Image(new FileInputStream(path + "rightlines_2.png"));
			leftLines2 = new Image(new FileInputStream(path + "leftlines_2.png"));
			arrowheadLeftRight = new Image(new FileInputStream(path + "arrowhead_leftright.png"));
			arrowheadRight2 = new Image(new FileInputStream(path + "arrowhead_right_topbottom_2.png"));
			arrowheadRight3 = new Image(new FileInputStream(path + "arrowhead_right_topbottom_3.png"));
		} catch (Exception e) {
			System.out.println("Error: indicator images did not load.");
		}
	}
	
	@SuppressWarnings("restriction")
	public Boolean[] draw(boolean isNonAssisted) {
		boolean startReady = false;
		boolean startBusy = false;
		boolean startIgnore = false;
		int numStarters = 0;
		
		boolean endReady = false;
		boolean endBusy = false;
		boolean endIgnore = false;
		int numEnders = 0;
		
		for (Module mod : micro.getModules()) {
			for (State st : mod.getEnabledInits())  {
				if (st.getStateClass().equals(StateClass.READY)) {
					startReady = true;
					numStarters += 1;
				}
				if (st.getStateClass().equals(StateClass.BUSY)) {
					startBusy = true;
					numStarters += 1;
				}
				if (st.getStateClass().equals(StateClass.IGNORE)) {
					startIgnore = true;
					numStarters += 1;
				}
			}
		}
		
		for (ArrayList<State> states : micro.getEndStates()) {
			for (State st : states) {
				if (st.getStateClass().equals(StateClass.READY) && !endReady) {
					endReady = true;
					numEnders += 1;
				}
				if (st.getStateClass().equals(StateClass.BUSY) && !endBusy) {
					endBusy = true;
					numEnders += 1;
				}
				if (st.getStateClass().equals(StateClass.IGNORE) && !endIgnore) {
					endIgnore = true;
					numEnders += 1;
				}
			}
		}
		
		Boolean[] enders = {endReady, endBusy, endIgnore};
		
		/*
		 * obtain list of
		 */
		
		GraphicsContext gc = this.getGraphicsContext2D();
		gc.clearRect(0, 0, 450, 350);
		
		// set the fill color
		gc.setFill(Color.LIGHTBLUE);
		
		// set the center circle
		gc.fillOval(175, 150, 100, 50);
		
		int currHeight;
		
		if (numStarters == 3) {
			currHeight = 25;
			gc.drawImage(leftLines3, 104, 76);
		}
		else if (numStarters == 2) {
			currHeight = 75;
			gc.drawImage(leftLines2, 104, 126);
		}
		else {
			currHeight = 125;
			gc.drawImage(leftLine, 104, 176);
		}
		gc.drawImage(arrowheadLeftRight, 165, 170);
		
		gc.setFill(Color.WHITE);
		gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.CENTER);
        if (isNonAssisted)
        	gc.fillText("Available initial\nstates:", 25, currHeight);
        else
        	gc.fillText("Activated initial\nstates:", 25, currHeight);
		currHeight += 25;
		gc.setFill(Color.BLACK);
		gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(
            micro.getName(), 
            225, 
            175);
						
		if (startReady) {
			gc.setFill(Color.LIGHTGREY);
			gc.fillRect(25, currHeight, 80, 50);
			gc.setFill(Color.BLACK);
			gc.fillText("Human\nready", 25 + 50, currHeight + 25);
			gc.drawImage(ready, 25 + 5, currHeight + 17, 15, 15);
			currHeight += 100;
		}
		if (startBusy) {
			gc.setFill(Color.LIGHTGREY);
			gc.fillRect(25, currHeight, 80, 50);
			gc.setFill(Color.BLACK);
			gc.fillText("Human\nbusy", 25 + 50, currHeight + 25);
			gc.drawImage(busy, 25 + 5, currHeight + 17, 15, 15);
			currHeight += 100;
		}
		if (startIgnore) {
			gc.setFill(Color.LIGHTGREY);
			gc.fillRect(25, currHeight, 80, 50);
			gc.setFill(Color.BLACK);
			gc.fillText("Human\nsuspended", 25 + 50, currHeight + 25);
			gc.drawImage(ignore, 25 + 5, currHeight + 17, 15, 15);
			currHeight += 100;
		}
		
		if (numEnders == 3) {
			currHeight = 25;
			gc.drawImage(rightLines3, 274, 76);
			gc.drawImage(arrowheadRight3, 335, 73);
			gc.drawImage(arrowheadLeftRight, 335, 170);
		}
		else if (numEnders == 2) {
			currHeight = 75;
			gc.drawImage(rightLines2, 274, 126);
			gc.drawImage(arrowheadRight2, 335, 122);
		}
		else {
			currHeight = 125;
			gc.drawImage(rightLine, 274, 176);
			gc.drawImage(arrowheadLeftRight, 335, 170);
		}
		
		gc.setFill(Color.WHITE);
		gc.setTextAlign(TextAlignment.RIGHT);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFill(Color.WHITE);
        if (isNonAssisted)
        	gc.fillText("Possible end\nstates:", 425, currHeight);
        else
        	gc.fillText("Resulting end\nstates:", 425, currHeight);
		currHeight += 25;
		gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
		
		if (endReady) {
			gc.setFill(Color.LIGHTGREY);
			gc.fillRect(345, currHeight, 80, 50);
			gc.setFill(Color.BLACK);
			gc.fillText("Human\nready", 345 + 50, currHeight + 25);
			gc.drawImage(ready, 345 + 5, currHeight + 17, 15, 15);
			currHeight += 100;
		}
		if (endBusy) {
			gc.setFill(Color.LIGHTGREY);
			gc.fillRect(345, currHeight, 80, 50);
			gc.setFill(Color.BLACK);
			gc.fillText("Human\nbusy", 345 + 50, currHeight + 25);
			gc.drawImage(busy, 345 + 5, currHeight + 17, 15, 15);
			currHeight += 100;
		}
		if (endIgnore) {
			gc.setFill(Color.LIGHTGREY);
			gc.fillRect(345, currHeight, 80, 50);
			gc.setFill(Color.BLACK);
			gc.fillText("Human\nsuspended", 345 + 50, currHeight + 25);
			gc.drawImage(ignore, 345 + 5, currHeight + 17, 15, 15);
			currHeight += 100;
		}
		
		return enders;
	}
	
}
