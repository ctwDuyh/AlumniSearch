package whu.alumnispider.utilities;

public class StructuredTeacher extends Website{
    private String name;
    private String cname;
    private String major;
    private String position;
    private String field;

    public StructuredTeacher(String name, String cname, String major, String position, String field, String website) {
        this.name = name;
        this.cname = cname;
        this.major = major;
        this.position = position;
        this.field = field;
        setWebsite(website);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}
