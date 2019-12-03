import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class TextVisualization extends PApplet {

// Data arrays and objects
JSONArray textData;
TextObject[] textObjects;
LetterObject [] letterObjects;
String [] listOfProjectTitles;

// Loading fonts
PFont lightFont;
PFont regularFont;

// Global visualization variables
int textStartingX = -10;
int textEndingX = 1440;

// Global letter variables
int velocityOfChange = 60; // Inverse (smaller numbers == faster) 80 == cycle of 500 frames
int lettersOnDisplay = 5;
float letterWidthFactor = 7.5f;
float lineHeightFactor = 16;
float minTextOpacity = 0.1f;

// Global word variables
float rectOpacity = 0;
float textOpacity = 0;
float maxRectOpacity = 0.75f;
float opacityRateChange;
float textOpacityChange;
int startCounter = 0;
boolean drawWords = false;
float highlightInterval = 1000;
float highlightDuration = 180;
int startingFrameHighlight = 150;
String highlightedProject;

public void setup(){
    textData = loadJSONArray("data/projects.json");
    lightFont = createFont("RobotoMono-Light.ttf", 13);
    regularFont = createFont("RobotoMono-Regular.ttf", 13);
    textFont(regularFont);
    
    colorMode(HSB, 360, 100, 100, 1);
    println("Building objects...");
    // buildTextObjects();
    // println("Done building text objects...");
    buildLetterObjects();
    println("Done building letter objects...");
    
    opacityRateChange = maxRectOpacity / (highlightDuration / 3);
    textOpacityChange = 1 / (highlightDuration / 3);
    // noLoop();
}

public void buildTextObjects(){
    textObjects = new TextObject[textData.size()];
    for (int i = 0; i < textData.size(); ++i) {
        JSONObject thisProject = textData.getJSONObject(i);
        String title = thisProject.getString("title");
        JSONArray team = thisProject.getJSONArray("team");
        String year = thisProject.getString("year");
        String description = thisProject.getString("description");
        JSONArray nounphrases = thisProject.getJSONArray("nounphrases");
        textObjects[i] = new TextObject(title, team, year, description, nounphrases);
    }
}

public void buildLetterObjects(){
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
    int yPos = 0;
    int counter = 0;
    int getProjectTitle = 0;
    for (int i = 0; i < minLength; i++){
        for (int j = 0; j < textData.size(); j++){
            JSONObject thisProject = textData.getJSONObject(j);
            JSONArray nounphrases = thisProject.getJSONArray("nounphrases");
            if (j % 4 == 0){
                String title = textData.getJSONObject(getProjectTitle).getString("title");
                String thisTitle[] = title.split("");
                for (int k = 0; k < thisTitle.length; ++k) {
                    if (xPos > (textEndingX / letterWidthFactor)){
                        xPos = textStartingX;
                        yPos += 1;
                    }
                    letterObjects[counter] = new LetterObject(thisTitle[k].toUpperCase(), xPos, yPos, k, 0.5f, true, title);
                    xPos = xPos + 1;
                    counter += 1;
                }
                xPos = xPos + 2;
                getProjectTitle += 1;
            }
            String projectTitle = thisProject.getString("title");
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
        }
    }
}

public void drawLetters(){
    fill(255);
    for (LetterObject letterObject : letterObjects) {
        // float opacityValue = cos(letterObject.letterPos);
        float opacityValue = cos(PApplet.parseFloat(frameCount) / velocityOfChange - PApplet.parseFloat(letterObject.letterPos) / lettersOnDisplay);
        opacityValue = map(opacityValue, -1, 1, -0.15f, 1);
        if (opacityValue <= minTextOpacity){
            opacityValue = minTextOpacity;
        }
        // println(opacityValue);
        letterObject.opacity = opacityValue;
        fill(0, 0, 100, letterObject.opacity);
        text(letterObject.letter, letterObject.xPos * letterWidthFactor, letterObject.yPos * lineHeightFactor);
    }
}

public void drawWords(String projectTitle){
    // println(projectTitle);
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
    float testValue = sin(PApplet.parseFloat(frameCount) / velocityOfChange);
    if (testValue < minTextOpacity){
        testValue = minTextOpacity;
    }
    // println(testValue);
    // println(frameCount);
    background(0);
    drawLetters();
    if ((frameCount - startingFrameHighlight) % highlightInterval == 0){
        rectOpacity = 0;
        textOpacity = 0;
        startCounter = 0;
        drawWords = true;
        highlightedProject = listOfProjectTitles[PApplet.parseInt(random(0, listOfProjectTitles.length))];
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
    if (frameCount == 3600){
        exit();
    }
    // println(frameRate);
}
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
class TextObject {
    String title;
    JSONArray team;
    String year;
    PVector titlePos;
    JSONArray nounphrases;
    String description;
    int textFill;
    // Constructor
    TextObject(String _title, JSONArray _team, String _year, String _description, JSONArray _nounphrases){
        title = _title;
        team = _team;
        year = _year;
        nounphrases = _nounphrases;
        description = _description;
        titlePos = new PVector(random(0, width), random(0, height));
        textFill = 100;
    }
    public void displayTitle(){
        fill(255);
        text(title, titlePos.x, titlePos.y);
    }
}
  public void settings() {  size(1440, 270);  pixelDensity(2); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "TextVisualization" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
