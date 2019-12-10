package whu.alumnispider.utilities;

public class Teacher extends TeacherSet{
    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public Teacher(String cname, String sname, String tname, String tname2, String website)
    {
        setCollegeName(cname);
        setSchoolName(sname);
        setTeacherSetName(tname);
        setTeacherName(tname2);
        setWebsite(website);
    }

    public Teacher(String cname, String sname, String tname, String website)
    {
        setCollegeName(cname);
        setSchoolName(sname);
        setTeacherSetName(tname);
        setWebsite(website);
    }

    public Teacher(String cname, String sname, String website)
    {
        setCollegeName(cname);
        setSchoolName(sname);
        setWebsite(website);
    }

    public Teacher(String name, String website) {
        setCollegeName(name);
        setSchoolName(name);
        setWebsite(website);
    }

    public Teacher()
    {

    }

    private String teacherName = "#";

}
