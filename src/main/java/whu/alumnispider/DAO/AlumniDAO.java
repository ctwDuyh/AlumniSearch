package whu.alumnispider.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import whu.alumnispider.utilities.*;

public class AlumniDAO {
    private Connection conn = null;
    private Statement stmt = null;

    public AlumniDAO() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/test?serverTimezone=UTC","root", "123456");
            stmt = conn.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // read data in database.
    public List<String> readFromGovWebsite(String tableName, String selectProperty) {
        try {
            List<String> rets = new ArrayList<String>();
            String sql = "SELECT " + selectProperty +" FROM " + tableName;
            ResultSet resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) {
                String ret = resultSet.getString(1);
                rets.add(ret);
            }

            return rets;
        } catch (SQLException e) {
            e.printStackTrace();
        };

        return null;
    }
    // 获取学校和学校网址信息
    public List<College> readFromColWebsite(String tableName) {
        try {
            List<College> colleges = new ArrayList<>();
            String sql = "SELECT * FROM " + tableName;
            ResultSet resultSet = stmt.executeQuery(sql);
            College college = new College();
            while (resultSet.next()) {
                String schoolName = resultSet.getString("schoolname");
                String website = resultSet.getString("website");
                college.setName(schoolName);
                college.setWebsite(website);
                colleges.add(college);
            }

            return colleges;
        } catch (SQLException e) {
            e.printStackTrace();
        };

        return null;
    }

    // write data in database.
    public int add(Person person, String tableName) {
        try {
            String sql = "INSERT INTO `test`.`"+ tableName +"`(`name`, `jobPosition`, `placeBirth`, `url`, `industry`, `graduatedSchool`)" +
                    "VALUES (?, ?, ?, ?, ?, ?);";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            preparedStatement.setString(1, person.getName());
            preparedStatement.setString(2, person.getJobPosition());
            preparedStatement.setString(3, person.getPlaceBirth());
            preparedStatement.setString(4, person.getUrl());
            preparedStatement.setString(5, person.getIndustry());
            preparedStatement.setString(6, person.getGraduatedSchool());

            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int add(GovSubpage govSubpage, String tableName) {
        try {
            String sql = "INSERT INTO `test`.`" + tableName + "`(`url`, `organizer`)" + "VALUES (?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            preparedStatement.setString(1, govSubpage.getUrl());
            preparedStatement.setString(2, govSubpage.getOrganizer());

            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public int add(College college, String tableName) {
        try {
            String sql = "INSERT INTO `test`.`" + tableName + "`(`schoolname`, `website`)" + "VALUES (?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            preparedStatement.setString(1, college.getName());
            preparedStatement.setString(2, college.getWebsite());

            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public int add(Alumni alumni, String tableName) {
        try {
            String sql = "INSERT INTO `test`.`" + tableName + "`(`name`, `job`,`illegal`,`website`,`picture`,`content`)" + "VALUES (?, ?,?,?,?,?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            preparedStatement.setString(1, alumni.getName());
            preparedStatement.setString(2, alumni.getJob());
            preparedStatement.setBoolean(3, alumni.isIllegal());
            preparedStatement.setString(4,alumni.getWebsite());
            preparedStatement.setString(5,alumni.getPicture());
            preparedStatement.setString(6,alumni.getContent());

            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }


    public int add(Academy academy, String tableName) {
        try {
            String sql = "INSERT INTO `test`.`" + tableName + "`(`schoolname`, `academywebsite`)" + "VALUES (?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            preparedStatement.setString(1, academy.getSchoolName());
            preparedStatement.setString(2, academy.getAcademyWebsite());

            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public List<String> getCandidate() {
        try {
            List<String> rets = new ArrayList<>();
            String sql = "SELECT name FROM `vipcandidate` WHERE state = 0";
            ResultSet resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) {
                String ret = resultSet.getString(1);
                rets.add(ret);
            }
            return rets;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int updateCandidate(String name) {
        try {
            String sql = "UPDATE `vipcandidate` SET state = 1 WHERE name = '"+name+"'";
            return stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<String> getWebsite(){
        try {
            List<String> rets = new ArrayList<>();
            String sql = "SELECT website FROM `alumnis` WHERE content is NULL";
            ResultSet resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) {
                String ret = resultSet.getString(1);
                rets.add(ret);
            }
            return rets;
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int updateContent(String website,String content){
        try {
            String sql = "UPDATE `alumnis` SET `content` = ? WHERE `website` = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            preparedStatement.setString(1, content);
            preparedStatement.setString(2, website);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int addName(String name,String table){
        try {
            String sql = "INSERT INTO `" + table + "`(`name`,`state`) values(?,1) ";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,name);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
