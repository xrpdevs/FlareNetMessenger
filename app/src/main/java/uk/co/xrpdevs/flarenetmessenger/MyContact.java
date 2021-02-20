package uk.co.xrpdevs.flarenetmessenger;

public class MyContact {

    public String tag;
    public String displayname;
    public String XRPAddr;
    public int id;

    public MyContact(String displayname, String XRPAddr, String tag, int id) {
        this.tag = tag;
        this.id = id;
        this.XRPAddr  = XRPAddr;
        this.displayname = displayname;
    }
}
