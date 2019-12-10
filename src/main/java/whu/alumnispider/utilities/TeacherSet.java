package whu.alumnispider.utilities;

public class TeacherSet extends School{
    private String teacherSetName = "#";

    public String getTeacherSetName() {
        return teacherSetName;
    }

    public void setTeacherSetName(String teacherSetName) {
        this.teacherSetName = teacherSetName;
    }

    public TeacherSet(String cname, String sname, String tname, String website)
    {
        setCollegeName(cname);
        setSchoolName(sname);
        setTeacherSetName(tname);
        setWebsite(website);
    }

    public TeacherSet(String cname, String sname, String website)
    {
        setCollegeName(cname);
        setSchoolName(sname);
        setWebsite(website);
    }

    public TeacherSet(String name, String website) {
        setCollegeName(name);
        setSchoolName(name);
        setWebsite(website);
    }

    public TeacherSet()
    {

    }
}
