package whu.alumnispider.utilities;

public class Alumni {
    private String name;
    private String job;
    private boolean isIllegal;
    private String website;

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public boolean isIllegal() {
        return isIllegal;
    }

    public void setIllegal(boolean illegal) {
        isIllegal = illegal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

}
