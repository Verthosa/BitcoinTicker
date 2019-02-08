package be.verthosa.ticker.bitcointicker.Models;

public class NewsFact {
    private String _id1;
    private String _title;
    private String _description;
    private String _url;

    public NewsFact(String id, String title, String description, String url){
        _id1 = id;
        _title = title;
        _description = description;
        _url = url;
    }

    public String GetId(){
        return _id1;
    }

    public String GetTitle(){
        return _title;
    }

    public String GetDescription(){
        return _description;
    }

    public String GetUrl() {return _url;}

}
