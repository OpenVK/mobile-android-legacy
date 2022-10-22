package uk.openvk.android.legacy.api.attachments;


public class Attachment {
    public String type;
    public String status;
    private Object content;
    public Attachment(String type) {
        this.type = type;
        if(type.equals("photo")) {
            content = new PhotoAttachment();
        } else if(type.equals("poll")) {
            content = new PollAttachment();
        } else {
            content = null;
        }
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
