package controller;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Scanner;

import checkers.Checker;
import checkers.PrismThread;
import checkers.Property;
import enums.StateClass;
import image.Conditions;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.text.Font;
import javafx.stage.WindowEvent;
import model.*;
import model.Group;
import model.GroupTransition;
import model_ctrl.*;
import repair.Repairer;
import study.BugTracker;
import study.InteractionGenerator;
import study.GroupMBP;

/*
 * Class that controls the primary controls of the Interface
 *
 * Links to all the UI elements of the interface
 *
 * Holds the Undo and Redo stacks for the program
 */
public class MainController implements Initializable {

    private final int BUTTON_SELECT = 'A';
    private final int BUTTON_ADDGROUP = 'G';
    private final int BUTTON_ADDTRANS = 'T';
    // experiment stuff
    public String participantID;
    public String IP;
    ConsoleCT console;
    // context menus for deletions
    ContextMenu deleteMicrocol;
    ContextMenu deleteMicrointeraction;
    ContextMenu deleteTrans;
    ContextMenu deleteAnswer;
    // background thread
    //FIXME should be put in same place as verify() and "the PRISM thread"
    PrismThread backgroundThread;
    // for tracking conflicts
    ViolationsPane violationsPane = null;
    // Linking FXML elements to the class
    @FXML
    private ToggleButton selectButton, addGroup, addTransition;
    @FXML
    private SplitPane mainPane;
    @FXML
    private AnchorPane editorPaneAnchor;
    @FXML
    private AnchorPane parameterizer;
    @FXML
    private TabPane leftPane;
    @FXML
    private ScrollPane interactionScrollPane;
    @FXML
    private CheckMenuItem feedbackSwitch;
    @FXML
    private TreeView violations;
    @FXML
    private AnchorPane conditionalPane;


    // Flags for various conditions
    private int buttonFlag;
    //Declares the file path used to add microinteractions to the active project
    private String absFilePath;
    // global variable to hold the MainController object
    private MainController mainController;
    private ImportMicrosCT importMicrosCT;
    private InteractionPane interactionPane;
    // the current interactions, microinteraction and module in the editor
    private Interaction interaction;
    private ArrayList<Group> groups;
    private Module currModule;
    private GroupTransition currGroupTransition;
    private PrismInitController prismInitController;
    // Distance between mouse position and top left corner of group box. Used for group relocation
    private double deltaX = 0.0;
    private double deltaY = 0.0;
    // if not assisted (the control)
    private Boolean isNonAssisted;
    private HashMap<String, TooltipViz> staticTooltips;
    // for storing the categories of properties
    private ArrayList<String> propertyCategories;
    private boolean canStartExp;
    private boolean canStopExp;
    private boolean canClearDesign;
    private boolean locked;
    // properties file
    private File propsFile;
    private ArrayList<Property> graphProperties;
    // repairer
    private Repairer rep;

    public static void hackTooltipStartTiming(Tooltip tooltip) {
        try {
            Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
            fieldBehavior.setAccessible(true);
            Object objBehavior = fieldBehavior.get(tooltip);

            Field fieldTimer = objBehavior.getClass().getDeclaredField("activationTimer");
            fieldTimer.setAccessible(true);
            Timeline objTimer = (Timeline) fieldTimer.get(objBehavior);

            objTimer.getKeyFrames().clear();
            objTimer.getKeyFrames().add(new KeyFrame(new Duration(250)));

            fieldTimer = objBehavior.getClass().getDeclaredField("hideTimer");
            fieldTimer.setAccessible(true);
            objTimer = (Timeline) fieldTimer.get(objBehavior);

            objTimer.getKeyFrames().clear();
            objTimer.getKeyFrames().add(new KeyFrame(new Duration(20000)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Initialize Class variables
    @SuppressWarnings("restriction")
    @Override
    public void initialize(URL location, ResourceBundle resources) {


        // most importantly, set self
        mainController = this;

        //Set the current working directory
        absFilePath = System.getProperty("user.dir") + File.separator + "Interaction" + File.separator;

        // properties file
        propsFile = new File(absFilePath + "GraphProperties.xml");
        PropertyFileDecoder propFileDecoder = new PropertyFileDecoder();
        graphProperties = propFileDecoder.decode(propsFile);
        propertyCategories = propFileDecoder.getPropertyCategories();
        propertyCategories.add("Jams");
        propertyCategories.add("Speech Flubs");
        propertyCategories.add("Branching Errors");

        // Currently set to empty declarations
        interaction = new Interaction(graphProperties);
        currGroupTransition = null;

        /*
         * IMPORTANT FOR STUDIES: Determine whether in the 'assisted' or 'non-assisted' condition
         */
        isNonAssisted = new Boolean(false);
        interaction.setTutorial(false);
        interaction.setNonAssistedSwitch(isNonAssisted);
        setPid("1");
        setIP("10.130.229.214");

        deleteMicrocol = null;
        deleteMicrointeraction = null;
        deleteTrans = null;

        prismInitController = null; // the PrismInitController

        buttonFlag = 0;

        setControlTabs();

        //Set up the InteractionPane
        interactionPane = new InteractionPane(this);
        interactionScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        interactionScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        interactionScrollPane.setContent(editorPaneAnchor);

        // create canvas

        // we don't want the canvas on the top/left in this example => just
        // translate it a bit
        interactionPane.setTranslateX(100);
        interactionPane.setTranslateY(100);

        // create sample nodes which can be dragged
//		NodeGestures nodeGestures = new NodeGestures(interactionPane);

//		Label label1 = new Label("Draggable node 1");
//		label1.setTranslateX(10);
//		label1.setTranslateY(10);
//		label1.addEventFilter( MouseEvent.MOUSE_PRESSED, nodeGestures.getOnMousePressedEventHandler());
//		label1.addEventFilter( MouseEvent.MOUSE_DRAGGED, nodeGestures.getOnMouseDraggedEventHandler());
//
//		Circle circle1 = new Circle( 300, 300, 50);
//		circle1.setStroke(Color.ORANGE);
//		circle1.setFill(Color.ORANGE.deriveColor(1, 1, 1, 0.5));
//		circle1.addEventFilter( MouseEvent.MOUSE_PRESSED, nodeGestures.getOnMousePressedEventHandler());
//		circle1.addEventFilter( MouseEvent.MOUSE_DRAGGED, nodeGestures.getOnMouseDraggedEventHandler());
//
//		Rectangle rect1 = new Rectangle(100,100);
//		rect1.setTranslateX(450);
//		rect1.setTranslateY(450);
//		rect1.setStroke(Color.BLUE);
//		rect1.setFill(Color.BLUE.deriveColor(1, 1, 1, 0.5));
//		rect1.addEventFilter( MouseEvent.MOUSE_PRESSED, nodeGestures.getOnMousePressedEventHandler());
//		rect1.addEventFilter( MouseEvent.MOUSE_DRAGGED, nodeGestures.getOnMouseDraggedEventHandler());
//
//		canvas.getChildren().addAll(label1, label2, label3, circle1, rect1);

        //group.getChildren().add(canvas);

//		interactionScrollPane.setContent(interactionPane);

//		interactionPane.setPrefSize();

        // create scene which can be dragged and zoomed
        //Scene scene = new Scene(group, 1024, 768);

        SceneGestures sceneGestures = new SceneGestures(interactionPane);
        interactionPane.addEventFilter(MouseEvent.MOUSE_PRESSED, sceneGestures.getOnMousePressedEventHandler());
        interactionPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, sceneGestures.getOnMouseDraggedEventHandler());
        interactionPane.addEventFilter(ScrollEvent.ANY, sceneGestures.getOnScrollEventHandler());

        // init the repairer
        rep = new Repairer(interaction, this, importMicrosCT);

        readInteraction();

        addIPEventHandler(interactionPane);
        setEditorAnchor(interactionPane);

        staticTooltips = new HashMap<>();

        // initialize the booleans that control starting, stopping, and clearing the designs
        canStartExp = true;
        canStopExp = false;
        canClearDesign = false;
        locked = false;

        // finally, start the prism model checker
        // comment this out if starting prism immediately is not desired
        PrismThread pt = new PrismThread(console, interaction, this);
        Thread t = pt.getThread();
        t.setPriority(Thread.MAX_PRIORITY);  // I think this is completely unnecessary and doesn't result in a change of speed
        pt.start("");

        new Notifier(interaction, this, parameterizer, isNonAssisted);

        // make the conditionalPane
        ConditionBoard condBoard = new ConditionBoard();
        conditionalPane.getChildren().add(condBoard);
    }

    public Boolean getNonAssistedSwitch() {
        return isNonAssisted;
    }

    public void setPid(String pid) {
        this.participantID = pid;
    }

    public void setIP(String ip) {
        this.IP = ip;
    }

    // properties file
    public ArrayList<Property> getProperties() {
        return graphProperties;
    }

    public ConsoleCT getConsole() {
        return console;
    }

    public ArrayList<Microinteraction> getMicrointeractions() {
        return interaction.getMicros();
    }

    //Set the anchor for the editor pane which holds the console and microinteraction library viewer
    private void setEditorAnchor(Object obj) {
        AnchorPane.setBottomAnchor((Node) obj, 0.0);
        AnchorPane.setTopAnchor((Node) obj, 0.0);
        AnchorPane.setLeftAnchor((Node) obj, 0.0);
        AnchorPane.setRightAnchor((Node) obj, 0.0);
        editorPaneAnchor.getChildren().add((Node) obj);
    }

    //Add the tabs for the console area and microinteraction library
    private void setControlTabs() {
        //Initialize the console object
        console = new ConsoleCT(this);
        console.display();

        //Initialize library tab
        importMicrosCT = new ImportMicrosCT(this);
        //Add it to the tab pane
        Tab tab = new Tab("Library", importMicrosCT);
        leftPane.getTabs().set(0, tab);
        //Add console tab to the tabpane
    }

    // Executes the necessary steps to properly shutdown the application
    public boolean closeApplication(ActionEvent event) {
        ExitController ec = new ExitController();
        if (ec.display()) {
            Platform.exit();
            System.exit(0);
        }
        return false;
    }

    // the start button
    public void startDesign() {
        if (canStartExp) {

            selectButton.setSelected(true);

            interaction.startDesign(console, mainController);

            interaction.reinitializeBugTracker();

            ArrayList<Group> groupsToUpdate = new ArrayList<>(interaction.getGroups());

            realignTransitions();
            interaction.updateAllAndDisplayConditions();

            interaction.getNetworkPropagator().propagateSequentialChanges(groupsToUpdate, console, interaction, mainController, false);

            // lastly, set the positions of the groups!
            interaction.getInit().setLayoutX(100);
            interaction.getInit().setLayoutY(100);
            interaction.getInit().refresh();

            canStartExp = false;
            canStopExp = true;
        }
    }

    public void realignTransitions() {
        for (Group group : interaction.getGroups()) {

            // nudge back and forth to re-align transitions
            group.relocate(group.getLayoutX() + 1, group.getLayoutY() + 1);
            group.relocate(group.getLayoutX() - 1, group.getLayoutY() - 1);

        }
    }

    public void realignTransitions(ArrayList<Group> groups) {
        for (Group group : groups) {
            // nudge back and forth to re-align transitions
            group.relocate(group.getLayoutX() + 1, group.getLayoutY() + 1);
            group.relocate(group.getLayoutX() - 1, group.getLayoutY() - 1);

        }
    }

    // the stop button
    public void stopDesign() {
        if (canStopExp && !locked) {
            String name = exportDesign();

            interaction.killBugTracker(name);
            interaction.makeBugtrackerNull();
            selectButton.setSelected(false);
            addGroup.setSelected(false);
            addTransition.setSelected(false);
            interaction.makeBugtrackerNull();

            // move the file to the CurrInteraction folder
            File sourceFile = new File(name + ".xml");
            Path source = sourceFile.toPath();
            File targetFile = new File("CurrInteraction" + File.separator + name + ".xml");
            Path target = targetFile.toPath();

            try {
                Files.move(source, target, REPLACE_EXISTING);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            canStopExp = false;
            canClearDesign = true;
        }
    }

    public String exportDesign() {
        console.updateText("Exporting interaction.");
        Exporter exp = new Exporter(interaction, participantID);
        return exp.export();
    }

    public void importDesign() {
        console.updateText(" Importing interaction.");
        stopDesign();
        clearDesignHelper(false, false);

        Importer imp = new Importer(absFilePath, interaction, this);
        imp.importInteraction();
        for (Microinteraction micro : interaction.getMicros())
            micro.addParameterizer(new MicroParameterizer(micro.getGlobalVars(), this));

        // add everything to the pane
        for (Group group : interaction.getGroups()) {
            if (!interactionPane.getChildren().contains(group)) {
                interactionPane.getChildren().add(group);
                addMicroColEventHandler(interactionPane, group);
            }
        }

        for (GroupTransition mt : interaction.getMacroTransitions()) {
            mt.setLinked(true);
            addMacroTrans(mt);
        }

        interaction.nullifyChecker();
        (new PrismThread(console, interaction, this)).start("");
    }

    public void clearDesign() {
        clearDesignHelper(true, true);
    }

    public void clearDesignInitializeNew() {
        if (canClearDesign) {
            // get the values
            ConditionChooser cc = new ConditionChooser(interaction, this);
            isNonAssisted = !cc.display();

            resetNao();

            String currDesign = interaction.getCurrDesign();
            int currInstruction = interaction.getCurrInstruction();
            boolean wasTutorial = interaction.getTutorial();

            interaction.getMicros().clear();
            interaction.setInit(null);
            interaction.getMacroTransitions().clear();
            interaction.getGroups().clear();

            // clear the parameterizer
            parameterizer.getChildren().clear();

            deleteMicrocol = null;
            deleteMicrointeraction = null;
            deleteTrans = null;

            // Currently set to empty declarations
            interaction = new Interaction(graphProperties);
            interaction.setCurrInstruction(currInstruction);
            interaction.setTutorial(wasTutorial);
            interaction.setCurrDesign(currDesign);
            currGroupTransition = null;
            interactionPane.removeAllCollections();

            readInteraction();
            for (Group group : interaction.getGroups()) {
                for (Microinteraction micro : group.getMicrointeractions()) {
                    micro.build();
                }
            }
            if (true) {
                interaction.nullifyChecker();
                PrismThread pt = new PrismThread(console, interaction, this);
                Thread t = pt.getThread();
                pt.start("");
                try {
                    t.join();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            new Notifier(interaction, this, parameterizer, isNonAssisted);

            canStartExp = true;
            canClearDesign = false;
        }
    }

    // the clear screen button
    private void clearDesignHelper(boolean restartPrism, boolean initBlankInteraction) {
        resetNao();

        String currDesign = interaction.getCurrDesign();
        int currInstruction = interaction.getCurrInstruction();
        boolean wasTutorial = interaction.getTutorial();

        interaction.getMicros().clear();
        interaction.setInit(null);
        interaction.getMacroTransitions().clear();
        interaction.getGroups().clear();

        // clear the parameterizer
        parameterizer.getChildren().clear();

        deleteMicrocol = null;
        deleteMicrointeraction = null;
        deleteTrans = null;

        // Currently set to empty declarations
        interaction = new Interaction(graphProperties);
        interaction.setCurrInstruction(currInstruction);
        currGroupTransition = null;
        interactionPane.removeAllCollections();

        if (wasTutorial) {
            interaction.setCurrDesign(currDesign);
        } else {
            if (currDesign.equals("Delivery")) {
                interaction.setCurrDesign("Instruction-Action");
            } else
                interaction.setCurrDesign("Delivery");
        }

        if (initBlankInteraction)
            readInteraction();

        for (Group group : interaction.getGroups()) {
            for (Microinteraction micro : group.getMicrointeractions()) {
                micro.build();
            }
        }
        if (restartPrism) {
            interaction.nullifyChecker();
            PrismThread pt = new PrismThread(console, interaction, this);
            Thread t = pt.getThread();
            pt.start("");
            try {
                t.join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        new Notifier(interaction, this, parameterizer, isNonAssisted);

        canStartExp = true;
        canClearDesign = false;
    }

    public void interactionToJPG() {
        // the content of scrollPane is saved as a JPEG file.
        WritableImage img = interactionPane.snapshot(new SnapshotParameters(), null);
        File file = new File("interaction.png");

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", file);
        } catch (IOException e) {
            // TODO: handle exception here
        }
    }

    public void lock() {
        unselectAllButtons();
        locked = true;
        interaction.getBugTracker().addEvent("locked design");
    }

    public void unlock() {
        if (locked && canStopExp) {
            locked = false;
            if (feedbackSwitch.isSelected())
                switchFeedback();
            interaction.getBugTracker().addEvent("unlocked design");
        }
    }

    public void repairInteraction(int ID, Object obj, boolean preview) {
        // calculate the current dimensions of the interaction
        double lowX = Double.MAX_VALUE;
        double highX = 0;
        double lowY = Double.MAX_VALUE;
        double highY = 0;

        for (Group group : interaction.getGroups()) {
            double groupX = group.getLayoutX();
            double groupY = group.getLayoutY();

            if (groupX < lowX)
                lowX = groupX;
            if (groupY < lowY)
                lowY = groupY;

            if (groupX + group.getWidth() > highX)
                highX = groupX + group.getWidth();
            if (groupY + group.getHeight() > highY)
                highY = groupY + group.getHeight();
        }

        rep.repair(ID, obj, lowX, lowY, highX - lowX, highY - lowY, preview);
    }

    public void removeRepairPreview() {
        ArrayList<Object> previews = rep.getPreview();
        for (Object obj : previews) {
            Node node = (Node) obj;
            interactionPane.getChildren().remove(node);
        }
    }

    public void generateRandomInteraction() {

        InteractionGenerator randInteractGen = new InteractionGenerator(this, interaction.getNetworkPropagator(), importMicrosCT);
        randInteractGen.start();
    }

    // notification for initializing prism
    public void notifyInitPrism() {
        prismInitController = new PrismInitController();
        prismInitController.display();
    }

    public void notifyFinishedInitPrism() {
        if (prismInitController == null)
            return;
        else
            prismInitController.close();
    }

    public Interaction getInteraction() {
        return interaction;
    }

    public Module getModule() {
        return currModule;
    }

    public void importMicrointeraction(ActionEvent event) {
        // remove everything that has been drawn
        IMDController imdc = new IMDController();
        String src = imdc.importMicroInteraction();
        if (!src.equals("NaN")) {
            File file = new File(src);
            try {
                Files.copy(file.toPath(), (new File(absFilePath + "Microinteractions" + File.separator + file.getName()).toPath()),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                ErrDController erdc = new ErrDController();
                erdc.display("Error copying Microinteraction", e);
            }
        }
    }

    // read all files in the interaction and build them using supreme.xml
    // this method should ONLY be called during initialization OR when reading in
    // another project
    public void readInteraction() {
        // get all of the microinteractions from the microinteraction folder
        File dir = new File(absFilePath + "MicroInteractions" + File.separator);
        ArrayList<File> files = new ArrayList<>();
        for (File file : dir.listFiles()) {

            // this is clunky -- should probably be redone at some point
            if (!file.getName().contains("ignore") && !(file.getName().charAt(0) == '.')) {
                files.add(file);
            }
        }

        // iterate through the files and build new
        for (File f : files) {
            String filename = f.getAbsolutePath();
            Microinteraction micro = new Microinteraction();
            Decoder d = new Decoder(this, isNonAssisted);
            d.readMicrointeraction(f, filename, micro);
            micro.addParameterizer(new MicroParameterizer(micro.getGlobalVars(), this));
            if (!(micro.getName().contains("temp"))) {
                micro.build();
                if (isNonAssisted)
                    micro.setStaticTooltip(staticTooltips.get(micro.getName()));
                interaction.addMicro(micro);
            }
        }

        buildInteractionHelper();
    }

    public HashMap<String, TooltipViz> getStaticTooltips() {
        return staticTooltips;
    }

    private void buildInteractionHelper() {
        Decoder decoder = new Decoder(this, isNonAssisted);
        decoder.readSupreme(absFilePath + "Supreme.xml", interaction);

        // Read MicroCollections and transitions. Build the same way as a tab in
        // microinteraction
        groups = interaction.getGroups();
        for (Group group : groups) {
            for (Microinteraction micro : group.getMicrointeractions())
                micro.addParameterizer(new MicroParameterizer(micro.getGlobalVars(), this));
            addMicroColEventHandler(interactionPane, group);
            addMicroColGPEventHandler(interactionPane, group);
            interactionPane.addCollection(group, 0, 0);
        }
    }

    // temporary code for check reachability
    public void checkReachability(ActionEvent event) {
        (new PrismThread(console, interaction, this)).start("reachability");
    }

    // temporary code for check sequential
    public void checkSequential(ActionEvent event) {
        (new PrismThread(console, interaction, this)).start("sequential");
    }

    public void checkConcurrent(ActionEvent event) {
        (new PrismThread(console, interaction, this)).start("concurrent");
    }

    //FIXME: move to Interaction or maybe somewhere else
    public void verify() {
        if (canStopExp) { // means that the design session has started!
            // tell backgroundthread to finish if necessary!
            if (backgroundThread != null && backgroundThread.getThread().isAlive()) {
                try {
                    backgroundThread.getThread().join();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            forceNetworkPropagation(false);
            verifyConcurrentAndGraph();
        }
    }

    public void updateConflictPane() {
        if (violationsPane == null)
            violationsPane = new ViolationsPane(violations, interaction, propertyCategories, this);
        violationsPane.update();
    }

    public void verifyConcurrentAndGraph() {
        backgroundThread = new PrismThread(console, interaction, this, interaction.getInit());
        Thread t = backgroundThread.getThread();
        backgroundThread.start("concurrentAndGraph");
    }

    public void beginSimulate() {

        // tell backgroundthread to finish if necessary!
        if (backgroundThread != null && backgroundThread.getThread().isAlive()) {
            try {
                backgroundThread.getThread().join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        resetNao();

        if (interaction.getInit().getMicrointeractions().size() > 0) {
            verifyConcurrentAndGraph();
        } else {
            (new InfoPanel("Cannot simulate due to empty interaction!")).display();
            return;
        }

        try {
            Runtime.getRuntime().exec("ssh nao@" + IP + " python prepareExperiment.py");         // for UW Net

            Thread.sleep(2000);
            exportDesign();
            if (!interaction.getTutorial())
                Runtime.getRuntime().exec("mv " + participantID + "_" + interaction.getCurrDesign() + ".xml interaction.xml");
            else
                Runtime.getRuntime().exec("mv " + participantID + "_tutorial.xml interaction.xml");
            Thread.sleep(100);
            Runtime.getRuntime().exec("scp interaction.xml nao@" + IP + ":~/");
            System.out.println("scp interaction.xml nao@" + IP + ":~/");
            Thread.sleep(2000);
            Runtime.getRuntime().exec("ssh nao@" + IP + " python exec/try.py &");
            Thread.sleep(1000);

            StopSimulatingController ssc = new StopSimulatingController(this);
            ssc.display();

        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void endSimulate() {

        try {
            Runtime.getRuntime().exec("ssh nao@" + IP + " ps aux | grep -ie exec/try.py | awk '{print $2}' | xargs kill -15");
            Thread.sleep(2000);
            Runtime.getRuntime().exec("ssh nao@" + IP + " python prepareExperiment.py");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }         // for UW Net
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        resetNao();
    }

    // Undo the last action

    // Create and add a new Project to the working directory
    public void newProject(ActionEvent event) {
        System.out.println("Not complete yet...");
    }

    public void selectButton(ActionEvent event) {
        if (interaction.getBugTracker() == null || locked)
            selectButton.setSelected(false);
        else if (buttonFlag != BUTTON_SELECT) {
            buttonFlag = BUTTON_SELECT;
            addGroup.setSelected(false);
            addTransition.setSelected(false);
        } else {
            buttonFlag = BUTTON_SELECT;
            selectButton.setSelected(true);
        }
    }

    public void addGroupButton(ActionEvent event) {
        if (interaction.getBugTracker() == null || locked)
            addGroup.setSelected(false);
        else if (buttonFlag != BUTTON_ADDGROUP) {
            buttonFlag = BUTTON_ADDGROUP;
            selectButton.setSelected(false);
            addTransition.setSelected(false);
        } else {
            buttonFlag = BUTTON_ADDGROUP;
            addGroup.setSelected(true);
        }
    }

    public void addTransitionButton(ActionEvent event) {
        if (interaction.getBugTracker() == null || locked)
            addTransition.setSelected(false);
        else if (buttonFlag != BUTTON_ADDTRANS) {
            buttonFlag = BUTTON_ADDTRANS;
            selectButton.setSelected(false);
            addGroup.setSelected(false);
        } else {
            buttonFlag = BUTTON_ADDTRANS;
            addTransition.setSelected(true);
        }
    }

    //Used to resize the console/library pane
    public void resizeConsoleButton(ActionEvent event) {

        if (mainPane.getDividerPositions()[0] > 0.9) {
            mainPane.setDividerPositions(0.7);
        } else {
            mainPane.setDividerPositions(0.1);
        }

    }

    public void unselectAllButtons() {
        selectButton.setSelected(false);
        addGroup.setSelected(false);
        addTransition.setSelected(false);
        buttonFlag = BUTTON_SELECT;
    }

    // used to switch between feedback and no feedback
    public void switchFeedback() {
        if (locked) {
            if (feedbackSwitch.isSelected()) {
                if (isNonAssisted) {
                    isNonAssisted = false;
                    for (Microinteraction micro : interaction.getMicros())
                        micro.setIsStaticTooltip(false);
                    verify();
                }
            } else {
                if (!isNonAssisted) {
                    isNonAssisted = true;
                    for (Microinteraction micro : interaction.getMicros())
                        micro.setStaticTooltip(staticTooltips.get(micro.getName()));
                    verify();
                }
            }
        }
    }

    public void resetNao() { // IP is 192.168.1.202
        try {
            Runtime.getRuntime().exec("ssh nao@" + IP + " python prepareExperiment.py");

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public int getFlag() {
        return buttonFlag;
    }

    public void addMicroColGPEventHandler(Pane pane, Node node) {
        GridPane gp = ((Group) node).getGp();

        gp.addEventHandler(MouseDragEvent.MOUSE_DRAGGED, (MouseEvent me) -> {
            // set size of anchor pane to match the size of the window (scroll pane)

            // begin a new transition
            if (buttonFlag == BUTTON_ADDTRANS) {
                beginTransition(pane, node, me);
            }
        });
    }

    //Add the necessary event handlers for the microcollection
    public void addMicroColEventHandler(Pane pane, Node node) {

        node.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent me) -> {
            if (buttonFlag == BUTTON_SELECT && me.getButton().equals(MouseButton.PRIMARY)) {   // if select is selected

                GroupProperties groupProperties = new GroupProperties((Group) node);
                groupProperties.run();
                parameterizer.getChildren().clear();
                parameterizer.getChildren().addAll(groupProperties);

                for (Group group : interaction.getGroups()) {
                    if (((Group) node).getName().equals(group.getName()) && !((Group) node).equals(group)) {
                        ((Group) node).setName(((Group) node).getName() + "1");
                    }
                }

                if (((Group) node).getName().equalsIgnoreCase("Auth") && !interaction.getTutorial() && interaction.getCurrDesign().equals("Delivery")) {
                    for (Microinteraction micro : ((Group) node).getMicrointeractions()) {
                        if (micro.getName().equals("Ask")) {

                            if (backgroundThread != null && backgroundThread.getThread().isAlive()) {
                                try {
                                    backgroundThread.getThread().join();
                                } catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }

                            verifyConcurrentAndGraph();

                        }
                    }
                }

            } else if (buttonFlag == BUTTON_SELECT && me.getButton().equals(MouseButton.SECONDARY)) {
                if (!((Group) node).isInit()) {
                    deleteMicrocol = new ContextMenu();
                    MenuItem emptyGroup = new MenuItem("Delete group " + ((Group) node).getName());
                    deleteMicrocol.setAutoHide(true);
                    deleteMicrocol.setOnHidden(new EventHandler<WindowEvent>() {

                        @Override
                        public void handle(WindowEvent event) {
                            mainPane.setContextMenu(null);
                        }
                    });

                    deleteMicrocol.getItems().addAll(emptyGroup);
                    mainPane.setContextMenu(deleteMicrocol);
                    deleteMicrocol.show(node, me.getScreenX(), me.getScreenY());

                    emptyGroup.setOnAction((m) -> {
                        // remove all of the bugs associated with this microcollection
                        Group mdel = ((Group) node);
                        for (ArrayList<Microinteraction> pair : mdel.getBadPairs()) {
                            interaction.getBugTracker().removeBug("speech", pair);
                        }

                        ArrayList<GroupMBP> toRemove = new ArrayList<GroupMBP>();
                        for (GroupMBP mmbp : interaction.getBugTracker().getBehViolations()) {
                            if (mmbp.group.equals(mdel)) {
                                toRemove.add(mmbp);
                            }
                        }
                        for (GroupMBP mmbp : toRemove) {
                            interaction.getBugTracker().removeBug("behConflict", mmbp);
                        }

                        // remove all the microinteractions
                        for (Microinteraction toDelete : ((Group) node).getMicrointeractions()) {
                            interaction.getMicros().remove(toDelete);
                        }

                        ((Group) node).getMicrointeractions().clear();

                        // remove all macrotransitions
                        ArrayList<Group> groupsToUpdate = new ArrayList<Group>();
                        for (GroupTransition macro : ((Group) node).getAllMacroTransitions()) {
                            Group target = macro.getTarget();
                            if (!target.equals((Group) node))
                                groupsToUpdate.add(target);
                            deleteMacroTransition(macro);
                        }

                        // tell backgroundthread to finish if necessary!
                        if (backgroundThread != null && backgroundThread.getThread().isAlive()) {
                            try {
                                backgroundThread.getThread().join();
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        if (interaction.testIsCyclic()) {
                            // start over again by wiping the end states!
                            interaction.getNetworkPropagator().propagateSequentialChanges(groupsToUpdate, console, interaction, mainController, false);
                            forceNetworkPropagation(false);
                        } else
                            interaction.getNetworkPropagator().propagateSequentialChanges(groupsToUpdate, console, interaction, mainController, false);

                        verifyConcurrentAndGraph();

                        // remove graphics
                        interactionPane.getChildren().remove(node);
                        interactionPane.getChildren().remove(((Group) node).getEnderIndicatorLights());
                        interactionPane.getChildren().remove(((Group) node).getStarterIndicatorLights());

                        interaction.getGroups().remove((Group) node);

                        buttonFlag = BUTTON_SELECT;
                        selectButton.setSelected(true);
                    });
                } else {
                    InfoPanel infopanel;
                    if (((Group) node).isInit())
                        infopanel = new InfoPanel("Cannot delete initial group.");
                    else
                        infopanel = new InfoPanel("Cannot delete the entry/exit point of the method.");
                    infopanel.display();

                }
                me.consume();
            }
        });

        node.setOnDragOver(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {

                if (event.getGestureSource() != node && event.getDragboard().hasString()) {

                    if (event.getDragboard().getString().equals("TransferMicro")) {

                        event.acceptTransferModes(TransferMode.COPY);

                    }
                }

                event.consume();
            }
        });

        node.setOnDragEntered(new EventHandler<DragEvent>() {

            @Override
            public void handle(DragEvent event) {
                if (event.getDragboard().getString().equals("TransferMicro")) {
                    ((Group) node).setSelected(true);
                }
            }
        });

        node.setOnDragExited(new EventHandler<DragEvent>() {

            @Override
            public void handle(DragEvent event) {
                if (event.getDragboard().getString().equals("TransferMicro")) {
                    ((Group) node).setSelected(false);
                }
            }
        });

        //TODO URGENT: call the import microinteraction method to add the new microinteraction to the current project directory
        node.setOnDragDropped(new EventHandler<DragEvent>() {

            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString() && interaction.getBugTracker() != null) {
                    if (db.getString().equals("TransferMicro") && !locked) {
                        success = true;
                        File file = db.getFiles().get(0);
                        //import microinteraction method should be called here using the above file
                        addMicroToGroup((Group) node, file);

                    }
                }
                /*
                 * let the source (library tab) know whether the string was successfully transferred and used
                 */
                event.setDropCompleted(success);

                event.consume();

            }
        });


        node.addEventHandler(MouseDragEvent.MOUSE_PRESSED, (MouseEvent me) -> {
            if (buttonFlag == 'A') {
                Point2D locationInScene = new Point2D(me.getSceneX(), me.getSceneY());
                Point2D locationInParent = pane.sceneToLocal(locationInScene);
                double X = locationInParent.getX();
                double Y = locationInParent.getY();
                deltaX = X - node.getLayoutX();
                deltaY = Y - node.getLayoutY();
            }
        });

        //Used to move the collection around the editor.
        node.addEventHandler(MouseDragEvent.MOUSE_DRAGGED, (MouseEvent me) -> {
            // set size of anchor pane to match the size of the window (scroll pane)

            // begin a new transition
            if (buttonFlag == BUTTON_ADDTRANS) {
                beginTransition(pane, node, me);
            } else if (buttonFlag == BUTTON_SELECT) {


                // Used to get coordinates for the mouse relative to the draw area
                Point2D locationInScene = new Point2D(me.getSceneX(), me.getSceneY());
                Point2D locationInParent = pane.sceneToLocal(locationInScene);

                double X = locationInParent.getX();
                double Y = locationInParent.getY();

                // the current position of the node
                double currY = node.getLayoutY();

                double offsetW = ((Group) node).getWidth();
                double offsetH = ((Group) node).getHeight();

                double finalX = X - deltaX;
                double finalY = Y - deltaY;

                // determine if the node is overlapping another
                for (Group group : interaction.getGroups()) {
                    if (!group.equals((Group) node)) {

                        // check if any of the corners are in this group
                        String violation = overlappingCorners(group, finalX, finalY);
                        if (violation != null) {

                            if (violation.equals("top"))
                                finalY = group.getLayoutY() + offsetH;

                            if (violation.equals("bottom"))
                                finalY = group.getLayoutY() - offsetH;

                            if (violation.equals("right"))
                                finalX = group.getLayoutX() + offsetW;

                            if (violation.equals("left"))
                                finalX = group.getLayoutX() - offsetW;
                        }
                    }

                }

                // change size of interactionPane to make scroll bar work
                if (finalX < 0) {
                    finalX = 0;
                }
                if (finalY < 0) {
                    finalY = 0;
                }
                if (finalY > interactionPane.getHeight() - offsetH) {
                    interactionPane.setPrefHeight(currY + offsetH);
                    editorPaneAnchor.setPrefHeight(finalY + offsetH);
                }
                if (finalX > interactionPane.getWidth() - offsetW) {
                    interactionPane.setPrefWidth(finalX + offsetW);
                    editorPaneAnchor.setPrefWidth(finalX + offsetW);
                }

                if (X < interactionPane.getWidth() - offsetW && Y < interactionPane.getHeight() - offsetH) {
                    node.relocate(finalX, finalY);
                }

            }

        });

        node.addEventHandler(MouseDragEvent.MOUSE_RELEASED, (MouseEvent me) -> {
            if (currGroupTransition != null) {
                // check if mouse is over
                Point2D locationInScene = new Point2D(me.getSceneX(), me.getSceneY());
                Point2D locationInParent = pane.sceneToLocal(locationInScene);

                double X = locationInParent.getX();
                double Y = locationInParent.getY();

                Group group = interaction.isWithinMicroCollection(X, Y);
                if (group != null) {
                    if (((Group) node).getName().equals("EndProcedure")) {
                        InfoPanel infopanel = new InfoPanel("Cannot add a transition from the exit point of a procedure.");
                        infopanel.display();

                        ((Group) node).getOutputMacroTransitions().remove(currGroupTransition);
                        interactionPane.getChildren().remove(currGroupTransition);
                        currGroupTransition = null;
                    } else if (group.getName().equals("BeginProcedure")) {
                        InfoPanel infopanel = new InfoPanel("Cannot add a transition to the entry point of a procedure.");
                        infopanel.display();

                        ((Group) node).getOutputMacroTransitions().remove(currGroupTransition);
                        interactionPane.getChildren().remove(currGroupTransition);
                        currGroupTransition = null;
                    } else {
                        interaction.addTransition(currGroupTransition);
                        currGroupTransition.setTarget(group);
                        currGroupTransition.setLinked(true);
                        currGroupTransition.initWithOrientation();
                        addMacroTransitionEventHandler(currGroupTransition);
                        group.addInputMacroTrans(currGroupTransition);
                        currGroupTransition.updateAndDisplayConditions();

                        // tell backgroundthread to finish if necessary!
                        if (backgroundThread != null && backgroundThread.getThread().isAlive()) {
                            try {
                                backgroundThread.getThread().join();
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        PrismThread pt = new PrismThread(console, interaction, this, currGroupTransition);
                        Thread t = pt.getThread();
                        pt.start("sequential");
                        try {
                            t.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (interaction.testIsCyclic()) {
                            // start over again by wiping the end states!
                            forceNetworkPropagation(false);
                        } else {
                            ArrayList<Group> groupsToUpdate = new ArrayList<Group>();
                            groupsToUpdate.add(currGroupTransition.getSource());
                            groupsToUpdate.add(currGroupTransition.getTarget());
                            interaction.getNetworkPropagator().propagateSequentialChanges(groupsToUpdate, console, interaction, mainController, false);
                        }

                        verifyConcurrentAndGraph();

                        // update locations and stuff
                        group.refresh();
                        currGroupTransition.updateIndicatorLocation();

                        // get the indicator and add an event handler
                        Circle indicator = currGroupTransition.getIndicator();
                        addIndicatorEventHandler(indicator, currGroupTransition, interactionPane);
                    }
                } else {  // the macrotransition was dropped in blank space
                    ((Group) node).getOutputMacroTransitions().remove(currGroupTransition);
                    interactionPane.getChildren().remove(currGroupTransition);
                    interactionPane.getChildren().remove(currGroupTransition.getConditions());
                    interactionPane.getChildren().remove(currGroupTransition.getIndicator());
                    interactionPane.getChildren().remove(currGroupTransition.getIndicatorOutline());
                    currGroupTransition = null;
                }

                //interactionPane.getChildren().remove(currGroupTransition);
                currGroupTransition = null;

            }
        });
    }

    public void addMicroToGroup(Group node, File file) {
        MicroBox mb;
        if (file != null) {
            mb = importMicrosCT.getMBbyID(file.getAbsolutePath());
        } else {
            mb = importMicrosCT.getRandomMB();
            file = mb.getFile();
        }

        addMicroBoxEventHandler((Group) node, mb);

        Microinteraction newMicro = new Microinteraction();
        (new Decoder(this, isNonAssisted)).readMicrointeraction(file, file.getAbsolutePath(), newMicro, mb);

        // sets up a parameterizer specific to this GUI
        newMicro.addParameterizer(new MicroParameterizer(newMicro.getGlobalVars(), this));

        newMicro.build();
        if (isNonAssisted)
            newMicro.setStaticTooltip(staticTooltips.get(newMicro.getName()));

        /*
         * don't do anything if this microinteraction already exists!
         */
        boolean alreadyExists = false;
        int microCount = 0;
        for (Microinteraction micro : ((Group) node).getMicrointeractions()) {
            if (micro.getName().equals(newMicro.getName()))
                alreadyExists = true;
            microCount += 1;
        }

        if (alreadyExists) {
            System.out.println("Cannot add microinteraction " + newMicro.getName() + " because it already exists in this grouping.");
            console.updateText(" Cannot add microinteraction " + newMicro.getName() + " because it already exists in this grouping.");
            return;
        }

        /*
         * don't do anything if there are already 4 microinteractions in the group!
         */
        if (microCount > 3) {
            InfoPanel infopanel = new InfoPanel("Cannot add more than 4 microinteractions to a group.");
            infopanel.display();
            System.out.println("Cannot add microinteraction " + newMicro.getName() + " because there are already 4 in the group.");
            console.updateText(" Cannot add microinteraction " + newMicro.getName() + " because there are already 4 in the group.");
            return;
        }


        /*
         * done checking if it already exists
         */

        // stop the background thread if necessary
        if (backgroundThread != null && backgroundThread.getThread().isAlive())
            try {
                backgroundThread.getThread().join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        interaction.addMicro(newMicro);
        ((Group) node).addMicro(newMicro);
        new PrismThread(console, interaction, mainController, newMicro);

        /*
         * THIS IS WHERE WE CAN BEGIN NETWORK PROPAGATION INSTEAD OF USING THE CODE THAT IS COMMENTED OUT BELOW
         */

        // tell backgroundthread to finish if necessary!
        if (backgroundThread != null && backgroundThread.getThread().isAlive()) {
            try {
                backgroundThread.getThread().join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        System.out.println("Network propagation");
        if (interaction.testIsCyclic()) {
            // start over again by wiping the end states!
            forceNetworkPropagation(false);
        } else {
            ArrayList<Group> groupsToUpdate = new ArrayList<Group>();
            groupsToUpdate.add((Group) node);
            interaction.getNetworkPropagator().propagateSequentialChanges(groupsToUpdate, console, interaction, mainController, false);
        }
        System.out.println("Done with network propagation");

        verifyConcurrentAndGraph();

        setMicrointeractionTooltip(newMicro);
    }

    public void setMicrointeractionTooltip(Microinteraction micro) {
        // adding the tooltip
        String path = "images" + File.separator;
        Image image;
        MicroBox mb = micro.getMicroBox();
        try {
            image = new Image(new FileInputStream(path + "wip.jpg"));
            ImageView imageView = new ImageView(image);
            Tooltip tooltip = new Tooltip();

            tooltip.setGraphic(micro.getTooltipViz());
            tooltip.setPrefWidth(400);

            tooltip.setText(micro.getDescription());
            tooltip.setWrapText(true);
            tooltip.setWidth(100);

            tooltip.setContentDisplay(ContentDisplay.BOTTOM);
            tooltip.setFont(Font.font("Veranda", 16));

            Tooltip.install(mb, tooltip);
            micro.setTooltip(tooltip);
            hackTooltipStartTiming(tooltip);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public void beginTransition(Node pane, Node node, MouseEvent me) {
        Point2D locationInScene = new Point2D(me.getSceneX(), me.getSceneY());
        Point2D locationInParent = pane.sceneToLocal(locationInScene);
        if (currGroupTransition == null) {
            currGroupTransition = new GroupTransition((Group) node, locationInParent, interaction.getBugTracker(), isNonAssisted);
            interactionPane.getChildren().add(currGroupTransition);
            Polygon poly = new Polygon();
            interactionPane.getChildren().addAll(poly);
            currGroupTransition.setPoly(poly);

            ArrayList<Circle> indics = currGroupTransition.getIndicatorComponents();
            interactionPane.getChildren().addAll(indics.get(1), indics.get(0));
            interactionPane.getChildren().addAll(currGroupTransition.getConditions());
            addConditionEventHandler(currGroupTransition.getConditions());
            addIndicatorEventHandler(indics.get(0), currGroupTransition);
        } else {
            currGroupTransition.setTempTarget(locationInParent.getX(), locationInParent.getY());
            currGroupTransition.updateIndicatorLocation();
        }
    }

    public void forceNetworkPropagation(boolean concurrent) {
        // start over again by wiping the end states!
        for (Group group : interaction.getGroups()) {
            group.wipeEndStates();
            // mark all of the microcollections as needing an update!
            group.markForUpdate();
        }

        // find the init!
        Group init = interaction.getInit();
        ArrayList<Group> groupsToUpdate = new ArrayList<Group>();
        groupsToUpdate.add(init);

        // find any other disjoint "inits"


        interaction.getNetworkPropagator().propagateSequentialChanges(groupsToUpdate, console, interaction, mainController, concurrent);
    }

    public boolean[] getEndStates(Node node) {
        boolean readyExists = false;
        boolean busyExists = false;
        boolean ignoreExists = false;

        boolean noBreakdownExists = false;
        boolean breakdownExists = false;

        // obtain all combinations of end states from the scratch
        HashMap<Microinteraction, ArrayList<Integer>> endStateIdxs = ((Group) node).getEndStateIdxs();
        HashMap<Microinteraction, HashMap<Integer, ArrayList<State>>> micro2idx2state = ((Group) node).getMicro2Idx2State();

        if (endStateIdxs != null && micro2idx2state != null) {
            Iterator it = endStateIdxs.entrySet().iterator();
            while (it.hasNext()) {
                HashMap.Entry pair = (HashMap.Entry) it.next();
                Microinteraction micro = (Microinteraction) pair.getKey();
                ArrayList<Integer> idxs = (ArrayList<Integer>) pair.getValue();

                for (Integer i : idxs) {
                    ArrayList<State> sts = micro2idx2state.get(micro).get(i);
                    for (State st : sts) {
                        if (st.getStateClass().equals(StateClass.READY))
                            readyExists = true;
                        else if (st.getStateClass().equals(StateClass.BUSY))
                            busyExists = true;
                        else if (st.getStateClass().equals(StateClass.IGNORE))
                            ignoreExists = true;

                        if (st.isBreakdown())
                            breakdownExists = true;
                        else
                            noBreakdownExists = true;
                    }
                }
            }
        }

        boolean[] result = {readyExists, busyExists, ignoreExists, breakdownExists, noBreakdownExists};
        return result;
    }

    private String overlappingCorners(Group background, double finalX, double finalY) {

        double width = background.getWidth();
        double height = background.getHeight();

        String violation = null;

        // top left corner of the movable
        if (finalX > background.getLayoutX() && finalX < background.getLayoutX() + width) {
            if (finalY > background.getLayoutY() && finalY < background.getLayoutY() + height) {

                if ((background.getLayoutX() + width) - finalX > (background.getLayoutY() + height) - finalY)
                    violation = "top";
                else
                    violation = "right";
            }
        }

        // bottom left corner of the movable
        if (finalX > background.getLayoutX() && finalX < background.getLayoutX() + width) {
            if (finalY + height > background.getLayoutY() && finalY + height < background.getLayoutY() + height) {

                if ((background.getLayoutX() + width) - finalX > (background.getLayoutY()) - (finalY + height))
                    violation = "bottom";
                else
                    violation = "right";
            }
        }

        // top right corner of the movable
        if (finalX + width > background.getLayoutX() && finalX + width < background.getLayoutX() + width) {
            if (finalY > background.getLayoutY() && finalY < background.getLayoutY() + height) {

                if ((background.getLayoutX()) - (finalX + width) > (background.getLayoutY() + height) - finalY)
                    violation = "top";
                else
                    violation = "left";
            }
        }

        // bottom right corner of the movable
        if (finalX + width > background.getLayoutX() && finalX + width < background.getLayoutX() + width) {
            if (finalY + height > background.getLayoutY() && finalY + height < background.getLayoutY() + height) {

                if ((background.getLayoutX()) - (finalX + width) > (background.getLayoutY()) - (finalY + height))
                    violation = "bottom";
                else
                    violation = "left";
            }
        }

        return violation;
    }

    public void addIndicatorEventHandler(Node node, GroupTransition currMt, Pane pane) {
        node.addEventHandler(MouseDragEvent.MOUSE_DRAGGED, (MouseEvent me) -> {
            Point2D locationInScene = new Point2D(me.getSceneX(), me.getSceneY());
            Point2D locationInParent = pane.sceneToLocal(locationInScene);

            double X = locationInParent.getX();
            double Y = locationInParent.getY();

            double currX = node.getLayoutX();
            double currY = node.getLayoutY();

            double offsetW = ((Circle) node).getStrokeWidth();
            double offsetH = ((Circle) node).getStrokeWidth();

            double finalX = X;
            double finalY = Y;

            // change size of interactionPane to make scroll bar work
            if (finalX < 0) {
                finalX = 0;
            }
            if (finalY < 0) {
                finalY = 0;
            }
            if (finalY > interactionPane.getHeight() - offsetH) {
                interactionPane.setPrefHeight(currY + offsetH);
                editorPaneAnchor.setPrefHeight(finalY + offsetH);
            }
            if (finalX > interactionPane.getWidth() - offsetW) {
                interactionPane.setPrefWidth(finalX + offsetW);
                editorPaneAnchor.setPrefWidth(finalX + offsetW);
            }

            //if (X < interactionPane.getWidth() - offsetW && Y < interactionPane.getHeight() - offsetH) {
            Point2D midpoint = currMt.getMidpoint();

            Conditions conditions = currMt.getConditions();
            if (Math.abs(finalX - midpoint.getX()) > 10 || Math.abs(finalY - midpoint.getY()) > 10) {
                ((Circle) node).setCenterX(finalX);
                ((Circle) node).setCenterY(finalY);
                currMt.getIndicatorOutline().setCenterX(finalX);
                currMt.getIndicatorOutline().setCenterY(finalY);

                conditions.setLayoutX(finalX - 18);
                conditions.setLayoutY(finalY + 10);

                currMt.updateBreakpoint(finalX, finalY);
            } else {
                ((Circle) node).setCenterX(midpoint.getX());
                ((Circle) node).setCenterY(midpoint.getY());
                currMt.getIndicatorOutline().setCenterX(midpoint.getX());
                currMt.getIndicatorOutline().setCenterY(midpoint.getY());

                conditions.setLayoutX(midpoint.getX() - 18);
                conditions.setLayoutY(midpoint.getY() + 10);

                currMt.removeBreakpoint();
            }


        });

        addMacroTransitionDragDropHandlers(node, currMt);
    }

    public void addIndicatorEventHandler(Node indicator, Node node) {
        indicator.addEventHandler(MouseDragEvent.MOUSE_CLICKED, (MouseEvent me) -> {
            handleMacroTransEvent(me, node);
        });
    }

    public void addMacroTrans(GroupTransition mt) {
        addMacroTransitionEventHandler(mt);
        ArrayList<Circle> indics = mt.getIndicatorComponents();
        interactionPane.getChildren().add(mt);
        interactionPane.getChildren().addAll(indics.get(1), indics.get(0));
        interactionPane.getChildren().addAll(mt.getConditions());
        addConditionEventHandler(currGroupTransition.getConditions());
        interactionPane.getChildren().addAll(mt.getPoly());
        addIndicatorEventHandler(indics.get(0), mt);
        Circle indicator = mt.getIndicator();
        addIndicatorEventHandler(indicator, mt, interactionPane);
    }

    public void addMacroTransShell(GroupTransition mt) {
        ArrayList<Circle> indics = mt.getIndicatorComponents();
        interactionPane.getChildren().add(mt);
        interactionPane.getChildren().addAll(indics.get(1), indics.get(0));
        interactionPane.getChildren().addAll(mt.getConditions());
        interactionPane.getChildren().addAll(mt.getPoly());
    }

    public void addMacroTransitionEventHandler(Node node) {
        node.addEventHandler(MouseDragEvent.MOUSE_CLICKED, (MouseEvent me) -> {
            handleMacroTransEvent(me, node);
        });

        addMacroTransitionDragDropHandlers(node, node);
    }

    private void addMacroTransitionDragDropHandlers(Node node, Node mt) {
        node.setOnDragOver(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {

                if (event.getGestureSource() != node && event.getGestureSource() != mt && event.getDragboard().hasString()) {

                    if (event.getDragboard().getString().contains("TransferCondition")) {

                        event.acceptTransferModes(TransferMode.COPY);

                    }
                }

                event.consume();
            }
        });

        node.setOnDragEntered(new EventHandler<DragEvent>() {

            @Override
            public void handle(DragEvent event) {
                if (event.getDragboard().getString().contains("TransferCondition")) {
                    ((GroupTransition) mt).setSelected(true);
                }
            }
        });

        node.setOnDragExited(new EventHandler<DragEvent>() {

            @Override
            public void handle(DragEvent event) {
                if (event.getDragboard().getString().contains("TransferCondition")) {
                  //  ((GroupTransition) mt).setSelected(false);
                }
            }
        });

        node.setOnDragDropped(new EventHandler<DragEvent>() {

            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString() && interaction.getBugTracker() != null) {
                    String content = db.getString();
                    interaction.getBugTracker().addCommand("changed mt conditions");
                    if (content.contains("TransferCondition") && !locked) {
                        boolean[] humanBranching = ((GroupTransition) mt).getHumanBranching();
                        boolean[] vals = {humanBranching[0], humanBranching[1], humanBranching[2]};
                        if (content.contains("ready"))
                            vals[0] = true;
                        if (content.contains("busy"))
                            vals[1] = true;
                        if (content.contains("ignore"))
                            vals[2] = true;

                        boolean changed = ((GroupTransition) mt).getConditions().update(vals);
                        updateChangeOfConditions(mt, changed);

                    }
                }

                /*
                 * let the source (library tab) know whether the string was successfully transferred and used
                 */
                event.setDropCompleted(success);

                event.consume();

            }
        });
    }

    public void updateChangeOfConditions(Node node, boolean refresh) {
        if (refresh) {
            // tell backgroundthread to finish if necessary!
            if (backgroundThread != null && backgroundThread.getThread().isAlive()) {
                try {
                    backgroundThread.getThread().join();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        if (refresh && interaction.testIsCyclic()) {
            // start over again by wiping the end states!
            forceNetworkPropagation(false);
        } else if (refresh) {
            console.updateText(((GroupTransition) node).toString());
            PrismThread pt = new PrismThread(console, interaction, mainController, ((GroupTransition) node));
            Thread t = pt.getThread();
            pt.start("sequential");
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ArrayList<Group> groupsToUpdate = new ArrayList<Group>();
            groupsToUpdate.add(((GroupTransition) node).getTarget());
            interaction.getNetworkPropagator().propagateSequentialChanges(groupsToUpdate, console, interaction, mainController, false);
        }

        if (refresh)
            verifyConcurrentAndGraph();
    }

    private void handleMacroTransEvent(MouseEvent me, Node node) {
        if (buttonFlag == BUTTON_SELECT && !locked && me.getButton().equals(MouseButton.PRIMARY)) {

            TransitionProperties transitionProperties = new TransitionProperties((GroupTransition) node, interaction.getBugTracker());

            parameterizer.getChildren().clear();
            parameterizer.getChildren().add(transitionProperties);

            /*
             * determine the current branching conditions!
             */
            boolean[] oldHumanBranch = ((GroupTransition) node).getHumanBranching().clone();
            boolean refresh = transitionProperties.isChanged();
            /*
             * obtain the resulting branching conditions!
             */
            boolean[] newHumanBranch = ((GroupTransition) node).getHumanBranching().clone();


            if (refresh) {
                ((GroupTransition) node).updateAndDisplayConditions();
                // tell backgroundthread to finish if necessary!
                if (backgroundThread != null && backgroundThread.getThread().isAlive()) {
                    try {
                        backgroundThread.getThread().join();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

            if (refresh && interaction.testIsCyclic()) {
                // start over again by wiping the end states!
                forceNetworkPropagation(false);
            } else if (refresh) {
                console.updateText(((GroupTransition) node).toString());
                PrismThread pt = new PrismThread(console, interaction, mainController, ((GroupTransition) node));
                Thread t = pt.getThread();
                pt.start("sequential");
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                ArrayList<Group> groupsToUpdate = new ArrayList<Group>();
                groupsToUpdate.add(((GroupTransition) node).getTarget());
                interaction.getNetworkPropagator().propagateSequentialChanges(groupsToUpdate, console, interaction, mainController, false);
            }

            if (refresh)
                verifyConcurrentAndGraph();
        } else if (buttonFlag == BUTTON_SELECT && me.getButton().equals(MouseButton.SECONDARY)) {
            deleteTrans = new ContextMenu();
            MenuItem delete = new MenuItem("Delete transition from " + ((GroupTransition) node).getSource().getName() + " to " + ((GroupTransition) node).getTarget().getName());
            deleteTrans.setAutoHide(true);

            deleteTrans.setOnHidden(new EventHandler<WindowEvent>() {

                @Override
                public void handle(WindowEvent event) {
                    mainPane.setContextMenu(null);
                }
            });

            deleteTrans.getItems().addAll(delete);
            mainPane.setContextMenu(deleteTrans);
            deleteTrans.show(node, me.getScreenX(), me.getScreenY());

            delete.setOnAction((m) -> {
                ArrayList<Group> groupsToUpdate = new ArrayList<Group>();
                Group target = ((GroupTransition) node).getTarget();
                groupsToUpdate.add(target);

                deleteMacroTransition(node);

                // tell backgroundthread to finish if necessary!
                if (backgroundThread != null && backgroundThread.getThread().isAlive()) {
                    try {
                        backgroundThread.getThread().join();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (interaction.testIsCyclic()) {

                    // start over again by wiping the end states!
                    interaction.getNetworkPropagator().propagateSequentialChanges(groupsToUpdate, console, interaction, mainController, false);
                    forceNetworkPropagation(false);
                } else
                    interaction.getNetworkPropagator().propagateSequentialChanges(groupsToUpdate, console, interaction, mainController, false);

                verifyConcurrentAndGraph();

                buttonFlag = BUTTON_SELECT;
                selectButton.setSelected(true);
            });
        }
        me.consume();
    }

    public void addConditionEventHandler(Conditions cond) {
        ArrayList<Canvas> canvases = cond.getCanvases();
        ArrayList<Image> images = cond.getImages();
        ArrayList<String> types = cond.getTypes();

        for (int i = 0; i < canvases.size(); i++) {
            Node node = canvases.get(i);
            Image img = images.get(i);
            String type = types.get(i);

            node.setOnDragDetected(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event) {

                    boolean[] humanBranching = cond.getMt().getHumanBranching();
                    boolean[] vals = {humanBranching[0], humanBranching[1], humanBranching[2]};
                    if (type.equals("ready"))
                        vals[0] = false;
                    if (type.equals("busy"))
                        vals[1] = false;
                    if (type.equals("ignore"))
                        vals[2] = false;
                    boolean changed = cond.update(vals);

                    Dragboard db = node.startDragAndDrop(TransferMode.COPY);

                    Canvas dragCanvas = new Canvas(20, 20);
                    dragCanvas.getGraphicsContext2D().drawImage(img, 0, 0, 20, 20);
                    WritableImage snapshot = dragCanvas.snapshot(new SnapshotParameters(), null);
                    db.setDragView(snapshot);

                    ClipboardContent content = new ClipboardContent();
                    content.putString("TransferCondition_" + type);

                    db.setContent(content);

                    event.consume();
                    updateChangeOfConditions(cond.getMt(), changed);
                }

            });
        }
    }

    public void addIPEventHandler(Node node) {
        node.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent me) -> {
            if (me.getButton().equals(MouseButton.SECONDARY)) {
                if (deleteMicrointeraction != null)
                    deleteMicrointeraction.hide();
                if (deleteTrans != null)
                    deleteTrans.hide();
                if (deleteMicrocol != null)
                    deleteMicrocol.hide();
                me.consume();
            } else if (buttonFlag == BUTTON_ADDGROUP) {
                Point2D locationInScene = new Point2D(me.getSceneX(), me.getSceneY());
                Point2D locationInParent = node.sceneToLocal(locationInScene);

                double x = locationInParent.getX();
                double y = locationInParent.getY();

                Group group = new Group(false, interaction.getBugTracker());
                addGroupHelper(group, x, y);
            }
        });
    }

    public void addGroupHelper(Group group, double x, double y) {

        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            String name = "untitled" + i;
            boolean nameExists = false;
            for (Group mc : interaction.getGroups()) {
                if (mc.getName().equals(name)) {
                    nameExists = true;
                }
            }

            if (!nameExists) {
                group.setName(name);
                break;
            }
        }

        interaction.addGroup(group);
        addMicroColEventHandler(interactionPane, group);
        interactionPane.addCollection(group, x, y);
        addMicroColGPEventHandler(interactionPane, group);
    }

    public void addGroupShell(Group group, double x, double y) {
        group.setLayoutX(x);
        group.setLayoutY(y);
        interactionPane.getChildren().add(group);
    }

    public void addMicroBoxEventHandler(Node group, Node node) {
        node.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent me) -> {
            if (buttonFlag == BUTTON_SELECT && me.getButton().equals(MouseButton.PRIMARY)) {
                parameterizer.getChildren().clear();

                // display the correct parameters
                Microinteraction currMicro = ((MicroBox) node).getMicrointeraction();
                addGlobalParams(currMicro);

                // display the properties box
                if (((MicroBox) node).getMicrointeraction().getEndStates() != null) { }
                me.consume();
            } else if (buttonFlag == BUTTON_SELECT && me.getButton().equals(MouseButton.SECONDARY)) {
                if (false) ; // replace "true" with "false" if we want actual exporting abilities!
                else {

                    deleteMicrointeraction = new ContextMenu();
                    MenuItem delete = new MenuItem("Delete " + ((MicroBox) node).getMicrointeraction().getName() + " from group");
                    deleteMicrointeraction.setAutoHide(true);

                    deleteMicrointeraction.setOnHidden(new EventHandler<WindowEvent>() {

                        @Override
                        public void handle(WindowEvent event) {
                            mainPane.setContextMenu(null);
                        }
                    });

                    deleteMicrointeraction.getItems().addAll(delete);
                    mainPane.setContextMenu(deleteMicrointeraction);
                    deleteMicrointeraction.show(node, me.getScreenX(), me.getScreenY());

                    delete.setOnAction((m) -> {
                        deleteMicrointeraction(group, node);
                        buttonFlag = BUTTON_SELECT;
                        selectButton.setSelected(true);
                    });
                    me.consume();
                }
            }
        });

    }

    public void verifyUpdate(Node node, boolean[] oldEnds) {

        // tell backgroundthread to finish if necessary!
        if (backgroundThread != null && backgroundThread.getThread().isAlive()) {
            try {
                backgroundThread.getThread().join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (interaction.testIsCyclic()) {
            // start over again by wiping the end states!
            forceNetworkPropagation(false);
        } else {
            ArrayList<Group> groupsToUpdate = new ArrayList<Group>();
            groupsToUpdate.add((Group) node);
            interaction.getNetworkPropagator().propagateSequentialChanges(groupsToUpdate, console, interaction, mainController, false);
        }

        verifyConcurrentAndGraph();

    }

    private void addGlobalParams(Microinteraction micro) {
        HashMap<Variable, Node> params = micro.getParameterizer().getParams();

        double currY = 2.0;
        double x = 10.0;

        ArrayList<Variable> keys = new ArrayList<Variable>();

        Iterator it = params.entrySet().iterator();
        if (!it.hasNext())
            parameterizer.getChildren().add(new Text(x, currY + 15, micro.getName() + " has no parameters."));

        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) it.next();
            keys.add((Variable) pair.getKey());
        }

        Collections.sort(keys);

        for (Variable var : keys) {

            Text txt = new Text(x, currY + 15, ((Variable) var).getName() + ":");
            Label lab = new Label(((Variable) var).getName() + ":");
            lab.setLayoutX(x);
            lab.setLayoutY(currY);
            if (var.getDescription() != null) {
                Tooltip tool = new Tooltip(var.getDescription());
                Tooltip.install(lab, tool);
            }
            parameterizer.getChildren().add(lab);

            currY += 20;

            if (((Variable) var).getType().equals("array")) {

                TextField ta = new TextField();
                ta.setPrefWidth(170);
                ta.setPromptText("Enter answers here...");

                parameterizer.getChildren().add(ta);
                ta.setLayoutY(currY);
                ta.setLayoutX(x);

                // add list of current values
                ListView list = (ListView) params.get(var);
                list.setLayoutX(x);
                list.setLayoutY(currY + 30);
                list.setPrefWidth(200);
                list.setPrefHeight(100);
                list.setEditable(false);
                parameterizer.getChildren().add(list);

                ta.setOnKeyPressed(new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent keyEvent) {
                        if (keyEvent.getCode() == KeyCode.ENTER && !ta.getText().equals("")) {
                            Variable glob = (Variable) var;

                            // create an answer pane!
                            GridPane gp = new GridPane();
                            AnchorPane textPane = new AnchorPane();
                            AnchorPane comboboxPane = new AnchorPane();
                            gp.setColumnIndex(textPane, 0);
                            gp.setColumnIndex(comboboxPane, 1);
                            gp.getChildren().add(0, textPane);
                            gp.getChildren().add(1, comboboxPane);

                            addAnswerBoxEventHandler(gp, var, list);

                            textPane.setMaxHeight(500);
                            textPane.setMaxWidth(500);
                            textPane.setPrefHeight(20);
                            textPane.setPrefWidth(90);
                            comboboxPane.setPrefWidth(100);

                            Text txt = new Text(ta.getText());
                            textPane.getChildren().add(txt);
                            textPane.setTopAnchor(txt, 5.0);
                            textPane.setLeftAnchor(txt, 5.0);
                            textPane.setBottomAnchor(txt, 5.0);

                            ComboBox cmbo = new ComboBox();
                            cmbo.setValue("-");
                            cmbo.getItems().addAll("-", "ready", "suspended");
                            cmbo.valueProperty().addListener(new ChangeListener<String>() {
                                @Override
                                public void changed(ObservableValue ov, String t, String t1) {
                                    String strLinks = "";
                                    for (Object obj : list.getItems()) {
                                        strLinks += ((ComboBox) ((AnchorPane) ((GridPane) obj).getChildren().get(1)).getChildren().get(0)).getSelectionModel().getSelectedItem() + ";";
                                    }
                                    glob.setValueLinks(strLinks);
                                }
                            });
                            comboboxPane.getChildren().add(cmbo);
                            comboboxPane.setRightAnchor(cmbo, 5.0);

                            list.getItems().add(gp/*ta.getText()*/);
                            ta.clear();

                            String str = "";
                            String strLinks = "";
                            for (Object obj : list.getItems()) {
                                str += ((Text) (((AnchorPane) ((GridPane) obj).getChildren().get(0)).getChildren()).get(0)).getText() + ";";
                                strLinks += ((ComboBox) ((AnchorPane) ((GridPane) obj).getChildren().get(1)).getChildren().get(0)).getSelectionModel().getSelectedItem() + ";";
                            }

                            glob.setValues(str);
                            glob.setValueLinks(strLinks);
                        }
                    }
                });

                list.setOnKeyPressed(new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(final KeyEvent keyEvent) {
                        final GridPane selectedItem = (GridPane) list.getSelectionModel().getSelectedItem();

                        if (selectedItem != null) {
                            deleteAnswerHelper(var, list);
                        }
                    }
                });


                currY += 150;
            } else if (((Variable) var).getType().equals("str")) {
                TextArea ta = (TextArea) params.get(var);
                ta.setPrefWidth(200);
                ta.setWrapText(true);
                ta.setPrefHeight(70);

                parameterizer.getChildren().add(ta);
                ta.setLayoutY(currY);
                ta.setLayoutX(x);

                currY += 80;

            } else {
                parameterizer.getChildren().add((params.get(var)));
                (params.get(var)).setLayoutY(currY);
                (params.get(var)).setLayoutX(x);

                currY += 40;
            }

        }

    }

    public void addAnswerBoxEventHandler(Node node, Variable var, ListView list) {
        node.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent me) -> {
            if (me.getButton().equals(MouseButton.SECONDARY)) {
                deleteAnswer = new ContextMenu();
                MenuItem delete = new MenuItem("Delete answer");
                deleteAnswer.setAutoHide(true);

                deleteAnswer.setOnHidden(new EventHandler<WindowEvent>() {

                    @Override
                    public void handle(WindowEvent event) {
                        mainPane.setContextMenu(null);
                    }
                });

                deleteAnswer.getItems().addAll(delete);
                mainPane.setContextMenu(deleteAnswer);
                deleteAnswer.show(node, me.getScreenX(), me.getScreenY());

                delete.setOnAction((m) -> {
                    deleteAnswerHelper(var, list);
                });
                me.consume();
            }
        });
    }

    private void deleteAnswerHelper(Variable var, ListView list) {
        final GridPane selectedItem = (GridPane) list.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            //Delete or whatever you like:
            Variable glob = (Variable) var;
            list.getItems().remove(selectedItem);
            String str = "";
            String strLinks = "";
            for (Object obj : list.getItems()) {
                str += ((Text) (((AnchorPane) ((GridPane) obj).getChildren().get(0)).getChildren()).get(0)).getText() + ";";
                strLinks += ((ComboBox) ((AnchorPane) ((GridPane) obj).getChildren().get(1)).getChildren().get(0)).getSelectionModel().getSelectedItem() + ";";
            }

            glob.setValues(str);
            glob.setValueLinks(strLinks);

        }
    }

    // delete a macrotransition
    private void deleteMacroTransition(Node node) {
        // remove from source's list of outputs
        ((GroupTransition) node).getSource().getOutputMacroTransitions().remove(((GroupTransition) node));

        // remove from target's list of inputs
        ((GroupTransition) node).getTarget().getInputMacroTransitions().remove(((GroupTransition) node));

        // update the conditional branching on the source node
        Group group = ((GroupTransition) node).getSource();
        boolean[] goodPartition = group.checkBranchingPartition();
        ArrayList<GroupTransition> mtrans = group.getOutputMacroTransitions();

        Circle inner = ((GroupTransition) node).getIndicator();
        Circle outer = ((GroupTransition) node).getIndicatorOutline();
        Polygon arrow = ((GroupTransition) node).getPoly();

        interaction.getBugTracker().addCommand("removed mt");
        interaction.getMacroTransitions().remove((GroupTransition) node);

        // check whether is should SHOW as a good partition!
        // add or remove the branching bug
        if (goodPartition[0]) {
            for (GroupTransition mtran : group.getOutputMacroTransitions())
                mtran.unGray();
        }
        // else, change all macrotransitions to gray
        else {
            for (GroupTransition mtran : group.getOutputMacroTransitions())
                mtran.grayOut();
        }

        // check whether it IS a good partition!
        if (goodPartition[1]) {
            if (!group.getWasGoodPartition()) {
                interaction.getBugTracker().removeBug("branching", group);
                group.setGoodPartition(true);
            }
        } else {
            if (group.getWasGoodPartition()) {
                interaction.getBugTracker().addBug("branching", group);
                group.setGoodPartition(false);
            }
        }

        // add or remove the sequential bug
        if (!((GroupTransition) node).getIndicator().getFill().equals(Color.LIGHTGREEN)) {
            ArrayList<ArrayList<Microinteraction>> badConnections = ((GroupTransition) node).getBadConnections();
            for (ArrayList<Microinteraction> micros : badConnections) {
                interaction.getBugTracker().removeBug("sequential", micros);
            }
        }

        // remove graphical components
        interactionPane.getChildren().remove(((GroupTransition) node));
        interactionPane.getChildren().remove(inner);
        interactionPane.getChildren().remove(outer);
        interactionPane.getChildren().remove(arrow);
        interactionPane.getChildren().remove(((GroupTransition) node).getConditions());
    }

    // delete a microinteraction
    private void deleteMicrointeraction(Node group, Node node) {
        Microinteraction toDelete = ((MicroBox) node).getMicrointeraction();
        boolean[] oldEnds = ((Group) group).getHumanEndStates();
        ((Group) group).removeMicro(toDelete);  // delete the microinteraction from the group
        interaction.getMicros().remove(toDelete);

        verifyUpdate(group, oldEnds);
    }
}
