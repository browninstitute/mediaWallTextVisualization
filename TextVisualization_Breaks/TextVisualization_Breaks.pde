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
import java.util.Arrays;

// Data arrays and objects
JSONArray textData;
LetterObject [] letterObjects;
String [] listOfProjectTitles;
String selectedYear_1 = "2012-2013";
String selectedYear_2 = "2013-2014";
// String [] selectedYears = {"2016-2017","2017-2018"};
String [] selectedYears = {"2012-2013", "2013-2014", "2014-2015", "2015-2016", "2015-2017"};
JSONArray allData;

// Loading fonts
PFont lightFont;
PFont regularFont;

// Global visualization variables
int textStartingX = 1;
int textEndingX = 1700;
int break1Start, break1End, break2Start, break2End;
int middleSpace = 138;
int singleScreenDimension = 480;

// Global letter variables
int velocityOfChange = 60; // Inverse (smaller numbers == faster) 80 == cycle of 500 frames
int lettersOnDisplay = 5;
float letterWidthFactor = 9.5;
float lineHeightFactor = 21.5;
int fontSize = 16;
float minTextOpacity = 0.1;

// Global word variables
float rectOpacity = 0;
float textOpacity = 0;
float maxRectOpacity = 0.75;
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

void setup(){
    allData = loadJSONArray("data/projects.json");
    lightFont = createFont("RobotoMono-Light.ttf", fontSize);
    regularFont = createFont("RobotoMono-Regular.ttf", fontSize);
    textFont(regularFont);
    size(1716, 270);
    colorMode(HSB, 360, 100, 100, 1);
    println("Building objects...");
    // buildTextObjects();
    // println("Done building text objects...");
    buildLetterObjects();
    println("Done building letter objects...");
    pixelDensity(2);
    opacityRateChange = maxRectOpacity / (highlightDuration / 3);
    textOpacityChange = 1 / (highlightDuration / 3);
    break1Start = singleScreenDimension;
    break1End = break1Start + middleSpace;
    break2Start = break1End + singleScreenDimension;
    break2End = break2Start + middleSpace;
    // noLoop();
}

void buildLetterObjects(){
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
                    else{}
                    letterObjects[counter] = new LetterObject(thisTitle[k].toUpperCase(), xPos, yPos, k, 0.5, true, title);
                    xPos = xPos + 1;
                    counter += 1;
                }
                xPos = xPos + 2;
                getProjectTitle += 1;
            }
            else{}
            String projectTitle = thisProject.getString("title");
            if (nounphrases.size() > i){
                String thisPhrase[] = nounphrases.getString(i).split("");
                for (int k = 0; k < thisPhrase.length; ++k) {
                    if (xPos > (textEndingX / letterWidthFactor)){
                        xPos = textStartingX;
                        yPos += 1;
                    }
                    letterObjects[counter] = new LetterObject(thisPhrase[k].toUpperCase(), xPos, yPos, k, 0.5, false, projectTitle);
                    xPos = xPos + 1;
                    counter += 1;
                }
                xPos = xPos + 2;
            }
            else{}
            tempCounter += 1;         
        }
    }
}

void drawLetters(){
    fill(255);
    for (LetterObject letterObject : letterObjects) {
        // float opacityValue = cos(letterObject.letterPos);
        float opacityValue = cos(float(frameCount) / velocityOfChange - float(letterObject.letterPos) / lettersOnDisplay);
        opacityValue = map(opacityValue, -1, 1, -0.15, 1);
        if (opacityValue <= minTextOpacity){
            opacityValue = minTextOpacity;
        }
        else{}
        // println(opacityValue);
        letterObject.opacity = opacityValue;
        fill(0, 0, 100, letterObject.opacity);
        text(letterObject.letter, letterObject.xPos * letterWidthFactor, letterObject.yPos * lineHeightFactor);
    }
}

void drawWords(String projectTitle){
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

void draw(){
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
            highlightedProject = listOfProjectTitles[int(random(0, listOfProjectTitles.length))];
            println(highlightedProject);
            if (highlightedProject.equals(previouslyHighlightedProject)){
                sameAsPrevious = true;
                println("Same as previous!");
            }
            else {
                sameAsPrevious = false;
            }
            // int minXpos = 2000;
            // int maxXpos = 0;
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
                    // println(letterObject.letter);
                    // minXpos = min(minXpos, letterObject.xPos);
                    // maxXpos = max(maxXpos, letterObject.xPos);
                }
            }
            // println("break1Start:",break1Start,"break1End:",break1End,"break2Start:",break2Start,"break2End:",break2End);
            // println("firstXpos:",firstXpos * letterWidthFactor,"lastXpos:",lastXpos * letterWidthFactor);
            if ((firstXpos * letterWidthFactor < break1Start && lastXpos * letterWidthFactor > break1Start) || (firstXpos * letterWidthFactor < break1End && lastXpos * letterWidthFactor > break1End) || (firstXpos * letterWidthFactor < break2Start && lastXpos * letterWidthFactor > break2Start) || (firstXpos * letterWidthFactor < break2End && lastXpos * letterWidthFactor > break2End) || (firstXpos > lastXpos)){
                onEdge = true;
                println("On edge!");
            }
            else {
                onEdge = false;
            }
        }
        
        // highlightedProject = listOfProjectTitles[int(random(0, listOfProjectTitles.length))];
        println(highlightedProject);
        // while (highlightedProject.equals(previouslyHighlightedProject)){
        //     highlightedProject = listOfProjectTitles[int(random(0, listOfProjectTitles.length))];
        //     println(highlightedProject);
        // }
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
    // saveFrame("frames/####.png");
    PImage partialSave1 = get(0, 0, singleScreenDimension * 2, 270 * 2);
    PImage partialSave2 = get((singleScreenDimension + middleSpace) * 2, 0, singleScreenDimension * 2, 270 * 2);
    PImage partialSave3 = get((singleScreenDimension * 2 + middleSpace * 2) * 2, 0, singleScreenDimension * 2, 270 * 2);
    image(partialSave1, 0, 0, singleScreenDimension, 270);
    image(partialSave2, singleScreenDimension, 0, singleScreenDimension, 270);
    image(partialSave3, singleScreenDimension * 2, 0, singleScreenDimension, 270);
    PImage partialSave4 = get(0, 0, 1440 * 2, 270 * 2);
    String outputFileName = "frames/CutFrames_" + nf(frameCount, 4) + ".png";
    partialSave4.save(outputFileName);
    println("Saved frame:",frameCount);
    if (frameCount == 7200){
        exit();
    }
}


