package object;

public class TextData {

    private String text;
    private String hint;
    private String contentDescription;
    private String tooltipText;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getContentDescription() {
        return contentDescription;
    }

    public void setContentDescription(String contentDescription) {
        this.contentDescription = contentDescription;
    }

    public String getTooltipText() {
        return tooltipText;
    }

    public void setTooltipText(String tooltipText) {
        this.tooltipText = tooltipText;
    }

    public TextData(String text, String hint, String contentDescription, String tooltipText) {
        this.text = text;
        this.hint = hint;
        this.contentDescription = contentDescription;
        this.tooltipText = tooltipText;
    }
}
