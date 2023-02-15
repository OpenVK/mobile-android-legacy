package uk.openvk.android.legacy.api.attachments;


public class Attachment {
    public String type;
    public String status;
    private Object content;
    public Attachment(String type) {
        this.type = type;
        switch (type) {
            case "photo":
                content = new PhotoAttachment();
                break;
            case "poll":
                content = new PollAttachment();
                break;
            default:
                content = null;
                break;
        }
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
