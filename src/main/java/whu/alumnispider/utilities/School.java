package whu.alumnispider.utilities;


public class School extends College{
    private String schoolName = "#";

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolname) {
        this.schoolName = schoolname;
    }

    public School(String name, String sname, String website)
    {
        setCollegeName(name);
        setSchoolName(sname);
        setWebsite(website);
    }

    public School(String name, String website) {
        setCollegeName(name);
        setSchoolName(name);
        setWebsite(website);
    }

    public School()
    {

    }

}
