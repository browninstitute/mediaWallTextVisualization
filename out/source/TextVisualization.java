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

// Loading fonts
PFont lightFont;
PFont regularFont;

// Global letter variables
int velocityOfChange = 60;
int lettersOnDisplay = 5;

// Global word variables
float rectOpacity = 0;
float maxRectOpacity = 0.75f;
float opacityRateChange;
int startCounter = 0;
boolean drawWords = false;
float highlightInterval = 400;
float highlightDuration = 100;

public void setup(){
    textData = loadJSONArray("data/projects.json");
    lightFont = createFont("RobotoMono-Light.ttf", 14);
    regularFont = createFont("RobotoMono-Regular.ttf", 14);
    textFont(regularFont);
    
    colorMode(HSB, 360, 100, 100, 1);
    println("Building objects...");
    buildTextObjects();
    println("Done building text objects...");
    buildLetterObjects();
    println("Done building letter objects...");
    
    opacityRateChange = maxRectOpacity / (highlightDuration / 3);
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
    for (int i = 0; i < textData.size(); ++i) {
        JSONObject thisProject = textData.getJSONObject(i);
        String title = thisProject.getString("title");
        JSONArray nounphrases = thisProject.getJSONArray("nounphrases");
        minLength = min(minLength, nounphrases.size());
        maxLength = max(maxLength, nounphrases.size());
        totalSize += title.length();
        for (int j = 0; j < nounphrases.size(); ++j) {
            totalSize += nounphrases.getString(j).length();
        }
    }
    println("Min size: ", minLength);
    println("Total size: ", totalSize);
    letterObjects = new LetterObject[totalSize];
    int xPos = -50;
    int yPos = 0;
    int counter = 0;
    for (int i = 0; i < maxLength; i++){
        for (int j = 0; j < textData.size(); j++){
            JSONObject thisProject = textData.getJSONObject(j);
            JSONArray nounphrases = thisProject.getJSONArray("nounphrases");
            String title = thisProject.getString("title");
            if (nounphrases.size() > i){
                String thisPhrase[] = nounphrases.getString(i).split("");
                xPos = xPos + 2;
                for (int k = 0; k < thisPhrase.length; ++k) {
                    if (xPos > 230){
                        xPos = 0;
                        yPos += 1;
                    }
                    letterObjects[counter] = new LetterObject(thisPhrase[k].toUpperCase(), xPos, yPos, k, 0.5f);
                    xPos = xPos + 1;
                    counter += 1;
                }
            }
            println(title);
            String thisTitle[] = title.split("");
            xPos = xPos + 2;
            for (int k = 0; k < thisTitle.length; ++k) {
                if (xPos > 230){
                    xPos = 0;
                    yPos += 1;
                }
                letterObjects[counter] = new LetterObject(thisTitle[k].toUpperCase(), xPos, yPos, k, 0.5f);
                xPos = xPos + 1;
                counter += 1;
            }
            println(counter);
        }
    }
}

public void drawLetters(){
    fill(255);
    for (LetterObject letterObject : letterObjects) {
        float opacityValue = sin(PApplet.parseFloat(frameCount) / velocityOfChange - PApplet.parseFloat(letterObject.letterPos) / lettersOnDisplay);
        if (opacityValue <= 0){
            opacityValue = 0;
        }
        letterObject.opacity = opacityValue;
        fill(0, 0, 100, letterObject.opacity);
        text(letterObject.letter, letterObject.xPos * 8, letterObject.yPos * 16);
    }
}

public void drawWords(){
    fill(0, 0, 0, rectOpacity);
    rect(0, 0, width, height);
    println(rectOpacity);
    if (startCounter < highlightDuration / 3){
        rectOpacity += opacityRateChange;
    }
    else if (startCounter > highlightDuration / 3 * 2){
        rectOpacity -= opacityRateChange;
    }
    else {
        rectOpacity = maxRectOpacity;
    }
    startCounter += 1;
}

public void draw(){
    background(0);
    drawLetters();
    if (frameCount % highlightInterval == 0){
        rectOpacity = 0;
        startCounter = 0;
        drawWords = true;
    }
    if (drawWords == true){
        drawWords();
    }
    if (startCounter == highlightDuration){
        drawWords = false;
        rectOpacity = 0;
    }
    // println(frameCount % 10);
    // int textLength = -50;
    // int rowNumber = 10;
    // for (int i = 0; i < 2; ++i) {
    //     for (TextObject textObject : textObjects){
    //         fill(textObject.textFill);
    //         if (textObject.nounphrases.size() > i){
    //             String thisText[] = textObject.nounphrases.getString(i).split("");
    //             text(textObject.nounphrases.getString(i).toUpperCase(), textLength, rowNumber);
    //             textLength += textObject.nounphrases.getString(i).length() * 9;
    //             if (textLength > 1000){
    //                 textLength = 0;
    //                 rowNumber += 12;
    //             }
    //         }
    //         else {}
    //     }
    // }    
    // if ((frameCount % 100) == 0){
    //     int counter = 0;
    //     for (TextObject textObject : textObjects){
    //         if (counter == (frameCount/100)){
    //             textObject.textFill = 255;
    //         }
    //         else {
    //             textObject.textFill = 100;
    //         }
    //         counter += 1;
    //     }
    // }
    // saveFrame("frames/####.png");
    if (frameCount == 5000){
        exit();
    }
}
class LetterObject {
    String letter;
    int xPos, yPos, letterPos;
    float opacity;
    LetterObject(String _letter, int _xPos, int _yPos, int _letterPos, float _opacity){
        letter = _letter;
        xPos = _xPos;
        yPos = _yPos;
        letterPos = _letterPos;
        opacity = _opacity;
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
  public void settings() {  size(1700, 500);  pixelDensity(2); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "TextVisualization" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
