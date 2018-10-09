package controller;

import javafx.scene.control.ScrollPane;

public class InteractionScrollPane extends ScrollPane{

	public InteractionScrollPane(InteractionPane ip) {
		setContent(ip);
		setHbarPolicy(ScrollBarPolicy.ALWAYS);
		setVbarPolicy(ScrollBarPolicy.ALWAYS);
	}
	
	

}
