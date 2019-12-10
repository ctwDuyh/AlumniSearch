package whu.alumnispider.utilities;

import java.util.Objects;

public class Website {
    private String website;

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Website website = (Website) o;
        return Objects.equals(this.website, website.website)||Objects.equals(this.website +"/", website.website)||Objects.equals(this.website, website.website+"/");
    }

    @Override
    public int hashCode() {
        return Objects.hash(website);
    }
}
