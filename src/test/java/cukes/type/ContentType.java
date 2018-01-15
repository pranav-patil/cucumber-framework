package cukes.type;

import org.springframework.http.MediaType;

public enum ContentType {

    JSON(MediaType.APPLICATION_JSON, "json"),
    XML(MediaType.APPLICATION_XML, "xml"),
    FORM(MediaType.APPLICATION_FORM_URLENCODED, "form");

    private MediaType mediaType;
    private String extension;

    ContentType(MediaType mediaType, String extension) {
        this.mediaType = mediaType;
        this.extension = extension;
    }

    public String value() {
        return this.mediaType.toString();
    }

    public MediaType mediaType() {
        return this.mediaType;
    }

    public String extension() {
        return this.extension;
    }

    public static ContentType getContentType(String type) {
        for(ContentType contentType : ContentType.values()){
            if(contentType.name().equalsIgnoreCase(type) || contentType.mediaType.toString().equalsIgnoreCase(type)) {
                return contentType;
            }
        }
        return null;
    }
}
