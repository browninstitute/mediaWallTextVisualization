import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.Arrays; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class TextVisualization_NoBreaks extends PApplet {

// Classes
class LetterObject {
    String letter, projectTitle;
    int xPos, yPos, letterPos;
    float opacity;
    boolean isTitle;
    LetterObject(String _letter, int _xPos, int _yPos, int _letterPos, float _opacity, boolean _isTitle, String _projectTitle){
        letter = _letter;
        xPos = _xPos;
        yPos = _yPos;
        letterPos = _letterPos;
        opacity = _opacity;
        isTitle = _isTitle;
        projectTitle = _projectTitle;
    }
}

// Import libraries


// Data arrays and objects
JSONArray textData;
LetterObject [] letterObjects;
String [] listOfProjectTitles;
String selectedYear_1 = "2012-2013";
String selectedYear_2 = "2013-2014";
String [] selectedYears = {"2016-2017","2017-2018"};
// String [] selectedYears = {"2012-2013", "2013-2014", "2014-2015", "2015-2016", "2015-2017"};
JSONArray allData;

// Loading fonts
PFont lightFont;
PFont regularFont;

// Global visualization variables
int textStartingX = 1;
int textEndingX = 1425;
int break1Start, break1End, break2Start, break2End;
int middleSpace = 138;
int singleScreenDimension = 480;

// Global letter variables
int velocityOfChange = 60; // Inverse (smaller numbers == faster) 80 == cycle of 500 frames
int lettersOnDisplay = 5;
float letterWidthFactor = 9.1f;
float lineHeightFactor = 17.5f;
int fontSize = 15;
float minTextOpacity = 0.1f;

// Global word variables
float rectOpacity = 0;
float textOpacity = 0;
float maxRectOpacity = 0.75f;
float opacityRateChange;
float textOpacityChange;
int startCounter = 0;
boolean drawWords = false;
boolean onEdge = true;
boolean sameAsPrevious = true;
float highlightInterval = 740;
float highlightDuration = 300;
int startingFrameHighlight = 10;
String highlightedProject;
String previouslyHighlightedProject = "";

public void setup(){
    allData = loadJSONArray("data/projects.json");
    lightFont = createFont("RobotoMono-Light.ttf", fontSize);
    regularFont = createFont("RobotoMono-Regular.ttf", fontSize);
    textFont(regularFont);
    
    colorMode(HSB, 360, 100, 100, 1);
    println("Building objects...");
    buildLetterObjects();
    println("Done building letter objects...");
    
    opacityRateChange = maxRectOpacity / (highlightDuration / 3);
    textOpacityChange = 1 / (highlightDuration / 3);
    break1Start = singleScreenDimension;
    break1End = break1Start + middleSpace;
    break2Start = break1End + singleScreenDimension;
    break2End = break2Start + middleSpace;
}

public void buildLetterObjects(){
    // Create subset based on year
    textData = new JSONArray();
    int newCounter = 0;
    for (int i = 0; i < allData.size(); i++){
        if (Arrays.asList(selectedYears).contains(allData.getJSONObject(i).getString("year"))){
            textData.setJSONObject(newCounter, allData.getJSONObject(i));
            newCounter += 1;
        }
    }

    int totalSize = 0;
    int maxLength = 0;
    int minLength = 200;
    listOfProjectTitles = new String[textData.size()];
    println("Number of projects:", textData.size());
    
    // Get min size of nounphrases
    for (int i = 0; i < textData.size(); i++) {
        JSONObject thisProject = textData.getJSONObject(i);
        JSONArray nounphrases = thisProject.getJSONArray("nounphrases");
        minLength = min(minLength, nounphrases.size());
    }
    println("Min size of nounphrases:", minLength);

    // Get total number of letters based on minLength of nounphrase
    for (int i = 0; i < textData.size(); ++i) {
        JSONObject thisProject = textData.getJSONObject(i);
        String title = thisProject.getString("title");
        println(title);
        listOfProjectTitles[i] = title;
        JSONArray nounphrases = thisProject.getJSONArray("nounphrases");
        totalSize += title.length();
        for (int j = 0; j < minLength; ++j) {
            totalSize += nounphrases.getString(j).length();
        }
    }
    println("Total number of letters:", totalSize);

    // Build letter objects based on minLength and totalSize
    letterObjects = new LetterObject[totalSize];
    int xPos = textStartingX;
    int yPos = 1;
    int counter = 0;
    int getProjectTitle = 0;
    int tempCounter = 0;
    for (int i = 0; i < minLength; i++){
        for (int j = 0; j < textData.size(); j++){
            JSONObject thisProject = textData.getJSONObject(j);
            JSONArray nounphrases = thisProject.getJSONArray("nounphrases");
            if (tempCounter % minLength == 0){
                String title = textData.getJSONObject(getProjectTitle).getString("title");
                String thisTitle[] = title.split("");
                for (int k = 0; k < thisTitle.length; ++k) {
                    if (xPos > (textEndingX / letterWidthFactor)){
                        xPos = textStartingX;
                        yPos += 1;
                    }
                    else{}letterObjects[counter] = new LetterObject(thisTitle[k].toUpperCase(), xPos, yPos, k, 0.5f, true, title);
                    xPos = xPos + 1;
                    counter += 1;
                }
                xPos = xPos + 2;
                getProjectTitle += 1;
            }
            else{}String projectTitle = thisProject.getString("title");
            if (nounphrases.size() > i){
                String thisPhrase[] = nounphrases.getString(i).split("");
                for (int k = 0; k < thisPhrase.length; ++k) {
                    if (xPos > (textEndingX / letterWidthFactor)){
                        xPos = textStartingX;
                        yPos += 1;
                    }
                    letterObjects[counter] = new LetterObject(thisPhrase[k].toUpperCase(), xPos, yPos, k, 0.5f, false, projectTitle);
                    xPos = xPos + 1;
                    counter += 1;
                }
                xPos = xPos + 2;
            }
            else{}tempCounter += 1;         
        }
    }
}

public void drawLetters(){
    fill(255);
    for (LetterObject letterObject : letterObjects) {
        float opacityValue = cos(PApplet.parseFloat(frameCount) / velocityOfChange - PApplet.parseFloat(letterObject.letterPos) / lettersOnDisplay);
        opacityValue = map(opacityValue, -1, 1, -0.15f, 1);
        if (opacityValue <= minTextOpacity){
            opacityValue = minTextOpacity;
        }
        else{}letterObject.opacity = opacityValue;
        fill(0, 0, 100, letterObject.opacity);
        text(letterObject.letter, letterObject.xPos * letterWidthFactor, letterObject.yPos * lineHeightFactor);
    }
}

public void drawWords(String projectTitle){
    fill(0, 0, 0, rectOpacity);
    rect(0, 0, width, height);
    if (startCounter < highlightDuration / 3){
        rectOpacity += opacityRateChange;
        textOpacity += textOpacityChange;
    }
    else if (startCounter > highlightDuration / 3 * 2){
        rectOpacity -= opacityRateChange;
        textOpacity -= textOpacityChange;
    }
    else {
        rectOpacity = maxRectOpacity;
        textOpacity = 1;
    }
    for (LetterObject letterObject : letterObjects) {
        if (letterObject.projectTitle == projectTitle){
            if (letterObject.isTitle == true){
                fill(32, 100, 100, textOpacity);
            }
            else {
                fill(0, 0, 100, textOpacity);
            }
            text(letterObject.letter, letterObject.xPos * letterWidthFactor, letterObject.yPos * lineHeightFactor);
        }
    }
    startCounter += 1;
}

public void draw(){
    background(0);
    drawLetters();
    if ((frameCount - startingFrameHighlight) % highlightInterval == 0){
        rectOpacity = 0;
        textOpacity = 0;
        startCounter = 0;
        drawWords = true;
        onEdge = true;
        sameAsPrevious = true;
        while (onEdge || sameAsPrevious){
            highlightedProject = listOfProjectTitles[PApplet.parseInt(random(0, listOfProjectTitles.length))];
            println(highlightedProject);
            if (highlightedProject.equals(previouslyHighlightedProject)){
                sameAsPrevious = true;
                println("Same as previous!");
            }
            else {
                sameAsPrevious = false;
            }
            int firstXpos = 0;
            int lastXpos = 0;
            boolean isFirstLetterPosSet = false;
            for (LetterObject letterObject : letterObjects) {
                if (letterObject.projectTitle.equals(highlightedProject)){
                    if (letterObject.isTitle == true){
                        lastXpos = letterObject.xPos;
                        if (isFirstLetterPosSet == false){
                            firstXpos = letterObject.xPos;
                        }
                        isFirstLetterPosSet = true;
                    }
                }
            }
            if ((firstXpos * letterWidthFactor < singleScreenDimension && lastXpos * letterWidthFactor > singleScreenDimension) || (firstXpos * letterWidthFactor < singleScreenDimension * 2 && lastXpos * letterWidthFactor > singleScreenDimension * 2) || (firstXpos > lastXpos)){
                onEdge = true;
                println("On edge!");
            }
            else {
                onEdge = false;
            }
        }
        println(highlightedProject);
        previouslyHighlightedProject = highlightedProject;
    }
    if (drawWords == true){
        drawWords(highlightedProject);
    }
    if (startCounter == highlightDuration){
        drawWords = false;
        rectOpacity = 0;
        textOpacity = 0;
    }
    saveFrame("frames/####.png");
    println("Saved frame:",frameCount);
    if (frameCount == 7200){
        exit();
    }
}


  public void settings() {  size(1440, 270);  pixelDensity(2); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "TextVisualization_NoBreaks" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
