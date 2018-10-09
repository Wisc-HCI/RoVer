package controller;

import javafx.animation.Timeline;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import model.*;
import model.Group;
import javafx.animation.KeyFrame;
import javafx.scene.paint.Color;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;

import javafx.animation.Animation;

public class Notifier {

	ArrayList<Microinteraction> micros;
	ArrayList<Group> groups;
	ArrayList<GroupTransition> macros;
	private ArrayList<Double> gradient;
	private ArrayList<Double> blinker;
	private ArrayList<Double> paramGradient;
	private String direction;
	private String paramDirection;
	private int currIdx;
	private int paramCurrIdx;
	private Boolean isNonAssisted;
	private MainController mc;
	private Interaction ia;
	private AnchorPane parameterizer;

	public Notifier(Interaction ia, MainController mc, AnchorPane parameterizer, Boolean isNonAssisted) {
		this.mc = mc;
		this.ia = mc.getInteraction();
		this.parameterizer = parameterizer;
		gradient = new ArrayList<Double>();
		blinker = new ArrayList<Double>();
		paramGradient = new ArrayList<Double>();
		this.micros = ia.getMicros();
		this.groups = ia.getGroups();
		this.macros = ia.getMacroTransitions();
		initGradient();
		initBlinker();
		currIdx = 0;
		paramCurrIdx = 1;
		direction = "up";
		paramDirection = "up";
		this.isNonAssisted = isNonAssisted;
		start();
	}

	private void initGradient() {
		/*gradient.add("#000000");
		gradient.add("#170403");
		gradient.add("#330A07");
		gradient.add("#4F0F0C");
		gradient.add("#6B1410");
		gradient.add("#851913");
		gradient.add("#9C1E17");
		gradient.add("#B5221A");
		gradient.add("#D1281E");
		gradient.add("#ED2D23");
		gradient.add("#FF3025");*/
		gradient.add(0.0);
		gradient.add(0.05);
		gradient.add(0.1);
		gradient.add(0.15);
		gradient.add(0.2);
		gradient.add(0.25);
		gradient.add(0.3);
		gradient.add(0.35);
		gradient.add(0.4);
		gradient.add(0.45);
		gradient.add(0.5);

		paramGradient.add(0.0);
		paramGradient.add(0.2);
		paramGradient.add(0.4);

	}

	private void initBlinker() {
		blinker.add(0.0);
		blinker.add(0.0);
		blinker.add(0.0);
		blinker.add(0.0);
		blinker.add(0.0);
		blinker.add(0.0);
		blinker.add(0.0);
		blinker.add(0.1);
		blinker.add(0.5);
		blinker.add(0.1);
		blinker.add(0.0);
	}

	private void start() {
		Timeline timeline = new Timeline(new KeyFrame(
				Duration.millis(50),
				ae -> update()));
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
	}

	private void update() {
		if (currIdx == 10) {
			direction = "down";
			currIdx--;
		}
		else if (currIdx == 0) {
			direction = "up";
			currIdx++;
		}
		else if (direction.equals("up"))
			currIdx++;
		else
			currIdx--;

		for (Microinteraction m : micros) {
			// determine if the associated microinteraction has required variables
			boolean required = false;
			for (Variable var : m.getGlobalVars()) {
				if (var.getRequired())
					required = true;
			}

			if (required) {
				MicroBox mb = m.getMicroBox();
				Rectangle rect = mb.getFlashingRect();
				//Rectangle rect = mb.getOutline();
				//Rectangle rectInner = mb.getRect();
				//Text text = mb.getText();
				//text.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
				rect.setFill(Color.web("#000000", gradient.get(currIdx)));
				//text.setFill(Color.web(gradient.get(currIdx)));
			}
			else if (!m.getMicroBox().getFlashingRect().getFill().equals(Color.TRANSPARENT)) {
				m.getMicroBox().getFlashingRect().setFill(Color.TRANSPARENT);
			}
		}

		if (!isNonAssisted) {
			for (Group group : groups) {
				if (group.getNegativeFeedbackAllowance()) {
					if (group.getIsViolatingFlub()) {
						//group.getMicroList().setStyle("-fx-background-color: rgba(255, 0, 0, " + gradient.get(currIdx) + ");");
						//group.getScrollPane().setStyle("-fx-background-color: rgba(255, 0, 0, " + gradient.get(currIdx) + ")");
						group.getTitlebox().setStyle("-fx-background-color: rgba(255, 0, 0, " + gradient.get(currIdx) + ")");
						group.getTitlerect().setFill(Color.TRANSPARENT);
					}
					else if (group.getIsViolatingBehaviorConflict()) {
						//group.getMicroList().setStyle("-fx-background-color: rgba(255, 150, 100, " + blinker.get(currIdx) + ");");
						//group.getScrollPane().setStyle("-fx-background-color: rgba(255, 150, 100, " + blinker.get(currIdx) + ")");
						group.getTitlebox().setStyle("-fx-background-color: rgba(255, 150, 100, " + blinker.get(currIdx) + ")");
						group.getTitlerect().setFill(Color.TRANSPARENT);
					}
				}
				else if (group.getBlinker()) {
					//group.getMicroList().setStyle("-fx-background-color: rgba(255, 150, 100, " + blinker.get(currIdx) + ");");
					//group.getScrollPane().setStyle("-fx-background-color: rgba(255, 150, 100, " + blinker.get(currIdx) + ")");
					group.getTitlebox().setStyle("-fx-background-color: rgba(255, 150, 100, " + blinker.get(currIdx) + ")");
					group.getTitlerect().setFill(Color.TRANSPARENT);
				}
				else {
					//group.getMicroList().setStyle("-fx-background-color: rgba(255, 0, 0, " + 0 + ");");
					//group.getScrollPane().setStyle("-fx-background-color: rgba(255, 0, 0, " + 0 + ")");
					group.getTitlebox().setStyle("-fx-background-color: rgba(255, 0, 0, " + 0 + ")");
					//group.getTitlerect().setFill(Color.TRANSPARENT);
				}

			}

			for (GroupTransition macro : macros) {
				if (macro.getNegativeFeedbackAllowance()) {
					macro.setStroke(Color.color(1, 0, 0, gradient.get(currIdx)));
					macro.getPoly().setFill(Color.color(1, 0, 0, gradient.get(currIdx)));
					macro.getIndicatorOutline().setFill(Color.color(1, 0, 0, gradient.get(currIdx)));
				}
				else {
					macro.setSelected(false);
				}
			}
		}
		else {
			for (Group group : groups) {
				if (group.getNegativeFeedbackAllowance()) {
					if (group.getIsViolatingBehaviorConflict()) {
						//group.getMicroList().setStyle("-fx-background-color: rgba(255, 150, 100, " + blinker.get(currIdx) + ");");
						//group.getScrollPane().setStyle("-fx-background-color: rgba(255, 150, 100, " + blinker.get(currIdx) + ")");
						group.getTitlebox().setStyle("-fx-background-color: rgba(255, 150, 100, " + blinker.get(currIdx) + ")");
						group.getTitlerect().setFill(Color.TRANSPARENT);
					}
				}
				else {
					//group.getMicroList().setStyle("-fx-background-color: rgba(255, 0, 0, " + 0 + ");");
					//group.getScrollPane().setStyle("-fx-background-color: rgba(255, 0, 0, " + 0 + ")");
					group.getTitlebox().setStyle("-fx-background-color: rgba(255, 0, 0, " + 0 + ")");
					//group.getTitlerect().setFill(Color.TRANSPARENT);
				}
			}
		}

	}
}
