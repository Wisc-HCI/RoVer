package study;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import model.Group;
import model.Interaction;
import model.Microinteraction;

import java.util.HashMap;
import java.util.Iterator;

import checkers.Property;
import image.BugStats;

public class BugTracker {

    private String buglistName;
    private PrintWriter writer;
    private PrintWriter commandWriter;
    private String commandListName;
    private Long startTime;
    private long endTime;
    private int currId;
    private HashMap<ArrayList<Microinteraction>, Integer> sequentialBugIDs;
    private HashMap<Group, Integer> branchingBugIDs;

    // graph and concurrent
    private HashMap<ArrayList<Microinteraction>, Integer> speechBugIDs;
    private HashMap<GroupMBP, Integer> modBehPairIDs;
    private HashMap<Group, Integer> greetingPropIDs;
    private int farewellPropID;
    private HashMap<Group, Integer> greetTwicePropIDs;
    private HashMap<Group, Integer> instHandleBusyPropIDs;
    private HashMap<Group, Integer> instHandleQuestionPropIDs;

    HashMap<Property, HashMap<Group, Integer>> groupGraphProperties;
    HashMap<Property, Integer> interactionGraphProperties;

    // interaction
    private Interaction ia;

    // exceptions
    private ArrayList<String> exceptions;

    public BugTracker(Interaction ia) {
        this.ia = ia;

        // sequential and branching
        sequentialBugIDs = new HashMap<ArrayList<Microinteraction>, Integer>();
        branchingBugIDs = new HashMap<Group, Integer>();

        // concurrent
        speechBugIDs = new HashMap<ArrayList<Microinteraction>, Integer>();
        modBehPairIDs = new HashMap<GroupMBP, Integer>();

        // graph
        greetingPropIDs = new HashMap<Group, Integer>();
        farewellPropID = -1;
        greetTwicePropIDs = new HashMap<Group, Integer>();
        instHandleBusyPropIDs = new HashMap<Group, Integer>();
        instHandleQuestionPropIDs = new HashMap<Group, Integer>();

        groupGraphProperties = new HashMap<Property, HashMap<Group, Integer>>();
        interactionGraphProperties = new HashMap<Property, Integer>();
        for (Property prop : ia.getGraphProperties()) {
            if (!prop.getTies().equals("interaction")) {
                groupGraphProperties.put(prop, new HashMap<Group, Integer>());
            }
        }

        // begin buglist
        endTime = -1;
        buglistName = "bugs.csv";
        currId = 0;
        try {
            writer = new PrintWriter(buglistName, "UTF-8");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        exceptions = new ArrayList<String>();

        startTime = System.nanoTime();
        writer.println("start,,,,,," + 0);


        commandListName = "commands.csv";
        try {
            commandWriter = new PrintWriter(commandListName, "UTF-8");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        commandWriter.println("start," + 0);
    }

    public void addBug(String type, Object bugToAdd) {

        if (type.equals("sequential")) {
            long currTime = System.nanoTime();
            int bugId = currId;
            currId++;
            ArrayList<Microinteraction> bug = (ArrayList<Microinteraction>) bugToAdd;
            sequentialBugIDs.put(bug, bugId);

            writer.println("add, sequential, " + bugId + ", " + ia.getMicros().size() + ", " + ia.getMacroTransitions().size() + ", " + ia.getGroups().size() + ", " + (currTime - startTime) / 1000000000.0);
        } else if (type.equals("branching")) {
            long currTime = System.nanoTime();
            int bugId = currId;
            currId++;
            Group bug = (Group) bugToAdd;
            branchingBugIDs.put(bug, bugId);

            writer.println("add, branching, " + bugId + ", " + ia.getMicros().size() + ", " + ia.getMacroTransitions().size() + ", " + ia.getGroups().size() + ", " + (currTime - startTime) / 1000000000.0);
        } else if (type.equals("speech")) {
            long currTime = System.nanoTime();
            int bugId = currId;
            currId++;
            ArrayList<Microinteraction> bug = (ArrayList<Microinteraction>) bugToAdd;
            speechBugIDs.put(bug, bugId);

            writer.println("add, speech, " + bugId + ", " + ia.getMicros().size() + ", " + ia.getMacroTransitions().size() + ", " + ia.getGroups().size() + ", " + (currTime - startTime) / 1000000000.0);
        } else if (type.equals("behConflict")) {
            long currTime = System.nanoTime();
            int bugId = currId;
            currId++;
            GroupMBP bug = (GroupMBP) bugToAdd;
            modBehPairIDs.put(bug, bugId);
            writer.println("add, behConflict, " + bugId + ", " + ia.getMicros().size() + ", " + ia.getMacroTransitions().size() + ", " + ia.getGroups().size() + ", " + (currTime - startTime) / 1000000000.0);
        }

        updateCurrBugCounts();
    }


    public void addGraphBug(Property prop, Group group) {

        long currTime = System.nanoTime();
        int bugId = currId;
        currId++;

        if (!prop.getTies().equals("interaction"))
            groupGraphProperties.get(prop).put(group, bugId);
        else
            interactionGraphProperties.put(prop, bugId);

        writer.println("add, " + prop.getBugtrackID() + ", " + bugId + ", " + ia.getMicros().size() + ", " + ia.getMacroTransitions().size() + ", " + ia.getGroups().size() + ", " + (currTime - startTime) / 1000000000.0);
    }


    public void removeBug(String type, Object bugToRemove) {

        try {
            if (type.equals("sequential")) {
                long currTime = System.nanoTime();
                int bugId = sequentialBugIDs.get((ArrayList<Microinteraction>) bugToRemove);
                sequentialBugIDs.remove((ArrayList<Microinteraction>) bugToRemove);
                writer.println("remove, sequential, " + bugId + ", " + ia.getMicros().size() + ", " + ia.getMacroTransitions().size() + ", " + ia.getGroups().size() + ", " + (currTime - startTime) / 1000000000.0);
            } else if (type.equals("branching")) {
                long currTime = System.nanoTime();
                int bugId = branchingBugIDs.get((Group) bugToRemove);
                branchingBugIDs.remove((Group) bugToRemove);

                writer.println("remove, branching, " + bugId + ", " + ia.getMicros().size() + ", " + ia.getMacroTransitions().size() + ", " + ia.getGroups().size() + ", " + (currTime - startTime) / 1000000000.0);
            } else if (type.equals("speech")) {
                long currTime = System.nanoTime();
                int bugId = speechBugIDs.get((ArrayList<Microinteraction>) bugToRemove);
                speechBugIDs.remove((ArrayList<Microinteraction>) bugToRemove);

                writer.println("remove, speech, " + bugId + ", " + ia.getMicros().size() + ", " + ia.getMacroTransitions().size() + ", " + ia.getGroups().size() + ", " + (currTime - startTime) / 1000000000.0);
            } else if (type.equals("behConflict")) {
                long currTime = System.nanoTime();
                int bugId = modBehPairIDs.get((GroupMBP) bugToRemove);
                modBehPairIDs.remove((GroupMBP) bugToRemove);
                writer.println("remove, behConflict, " + bugId + ", " + ia.getMicros().size() + ", " + ia.getMacroTransitions().size() + ", " + ia.getGroups().size() + ", " + (currTime - startTime) / 1000000000.0);
            }

            updateCurrBugCounts();
        } catch (Exception e) {
            long currTime = System.nanoTime();
            double totalTime = (currTime - startTime) / 1000000000.0;
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString(); // stack trace as a string
            exceptions.add(totalTime + sStackTrace);
        }
    }

    public void removeGraphBug(Property prop, Group group) {

        try {
            long currTime = System.nanoTime();
            int bugId;
            if (!prop.getTies().equals("interaction")) {
                bugId = groupGraphProperties.get(prop).get(group);
                groupGraphProperties.get(prop).remove(group);
            } else {
                bugId = interactionGraphProperties.get(prop);
                interactionGraphProperties.remove(prop);
            }

            writer.println("remove, " + prop.getBugtrackID() + ", " + bugId + ", " + ia.getMicros().size() + ", " + ia.getMacroTransitions().size() + ", " + ia.getGroups().size() + ", " + (currTime - startTime) / 1000000000.0);

        } catch (Exception e) {
            long currTime = System.nanoTime();
            double totalTime = (currTime - startTime) / 1000000000.0;
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString(); // stack trace as a string
            exceptions.add(totalTime + sStackTrace);
        }
    }


    public void addEvent(String str) {
        long currTime = System.nanoTime();
        double totalTime = (currTime - startTime) / 1000000000.0;

        writer.println("event - " + str + ",,,,,," + (currTime - startTime) / 1000000000.0);
    }

    public void addCommand(String str) {
        long currTime = System.nanoTime();
        double totalTime = (currTime - startTime) / 1000000000.0;

        commandWriter.println("command, " + str + ", " + (currTime - startTime) / 1000000000.0);
    }

    public void kill(String prefix) {
        endTime = System.nanoTime();
        writer.println("end,,,,,," + (endTime - startTime) / 1000000000.0);
        writer.flush();
        writer.close();

        File sourceFile = new File(buglistName);
        Path source = sourceFile.toPath();
        File targetFile = new File("CurrInteraction" + File.separator + prefix + "_" + buglistName);
        Path target = targetFile.toPath();

        try {
            Files.move(source, target, REPLACE_EXISTING);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        commandWriter.println("end," + (endTime - startTime) / 1000000000.0);
        commandWriter.flush();
        commandWriter.close();

        sourceFile = new File(commandListName);
        source = sourceFile.toPath();
        targetFile = new File("CurrInteraction" + File.separator + prefix + "_" + commandListName);
        target = targetFile.toPath();

        try {
            Files.move(source, target, REPLACE_EXISTING);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("Ended the bug and command trackers. " + exceptions.size() + " exceptions occurred.");

        PrintWriter writer;
        try {
            writer = new PrintWriter(prefix + "_exceptions.txt", "UTF-8");
            for (String except : exceptions)
                writer.println(except);
            writer.close();

            sourceFile = new File(prefix + "_exceptions.txt");
            source = sourceFile.toPath();
            targetFile = new File("CurrInteraction" + File.separator + prefix + "_exceptions.txt");
            target = targetFile.toPath();

            try {
                Files.move(source, target, REPLACE_EXISTING);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public ArrayList<GroupMBP> getBehViolations() {
        ArrayList<GroupMBP> groupmbps = new ArrayList<GroupMBP>();
        Iterator it = modBehPairIDs.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) it.next();
            groupmbps.add((GroupMBP) pair.getKey());
        }

        return groupmbps;
    }

    public void updateCurrBugCounts() {

        // do all else
        int count = 0;
        Iterator it = groupGraphProperties.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) it.next();
            HashMap<Group, Integer> groupMap = (HashMap<Group, Integer>) pair.getValue();

            Iterator it2 = groupGraphProperties.entrySet().iterator();
            while (it2.hasNext()) {
                HashMap.Entry pair2 = (HashMap.Entry) it2.next();
                count += 1;
            }
        }

        it = interactionGraphProperties.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) it.next();
            count += 1;
        }

        count += speechBugIDs.size();
        count += (farewellPropID != -1) ? 1 : 0;
    }

}
