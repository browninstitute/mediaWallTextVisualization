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