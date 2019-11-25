package whu.alumnispider.utilities;

import java.util.Objects;

public class School {
    private String name;
    private String website;

    public School(String name, String website) {
        this.name = name;
        this.website = website;
    }

    public School() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
        School college = (School) o;
        return Objects.equals(website, college.website);
    }

    @Override
    public int hashCode() {
        return Objects.hash(website);
    }
}
