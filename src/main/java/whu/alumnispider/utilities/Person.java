package whu.alumnispider.utilities;

public class Person {
    public String name; // 姓名
    public String jobPosition; // 职务
    public String placeBirth; // 籍贯
    public String url;
    public String graduatedSchool;
    public String industry; // 行业分类：“Government”，“Business”, "Academic"

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name =name;
    }

    public String getJobPosition() {
        return this.jobPosition;
    }

    public void setJobPosition(String jobPosition) {
        this.jobPosition = jobPosition;
    }

    public String getPlaceBirth() {
        return this.placeBirth;
    }

    public void setPlaceBirth(String placeBirth) {
        this.placeBirth = placeBirth;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getGraduatedSchool() {
        return this.graduatedSchool;
    }

    public void setGraduatedSchool(String graduatedSchool) {
        this.graduatedSchool = graduatedSchool;
    }

    public String getIndustry() {
        return this.industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }
}
