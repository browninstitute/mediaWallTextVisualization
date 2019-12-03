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
    void displayTitle(){
        fill(255);
        text(title, titlePos.x, titlePos.y);
    }
}