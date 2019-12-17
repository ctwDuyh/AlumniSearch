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
            conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/test","root", "");
            stmt = conn.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // read data in database.
    public List<String> read(String tableName, String selectProperty) {
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
            String sql = "INSERT INTO `test`.`" + tableName + "`(`collegename`, `website`)" + "VALUES (?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            preparedStatement.setString(1, college.getCollegeName());
            preparedStatement.setString(2, college.getWebsite());

            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }


    public int add(School school, String tableName) {
        try {
            String sql = "INSERT INTO `test`.`" + tableName + "`(`collegename`, `schoolname`, `website`)" + "VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            preparedStatement.setString(1, school.getCollegeName());
            preparedStatement.setString(2, school.getSchoolName());
            preparedStatement.setString(3, school.getWebsite());

            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public int add(TeacherSet teacherSet, String tableName) {
        try {
            String sql = "INSERT INTO `test`.`" + tableName + "`(`collegename`,`schoolname`,`teachersetname`,`website`)" + "VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            preparedStatement.setString(1, teacherSet.getCollegeName());
            preparedStatement.setString(2, teacherSet.getSchoolName());
            preparedStatement.setString(3, teacherSet.getTeacherSetName());
            preparedStatement.setString(4, teacherSet.getWebsite());

            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public int add(Teacher teacher, String tableName) {
        try {
            String sql = "INSERT INTO `test`.`" + tableName + "`(`collegename`,`schoolname`,`teachersetname`,`teachername`,`website`)" + "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            preparedStatement.setString(1, teacher.getCollegeName());
            preparedStatement.setString(2, teacher.getSchoolName());
            preparedStatement.setString(3, teacher.getTeacherSetName());
            preparedStatement.setString(4, teacher.getTeacherName());
            preparedStatement.setString(5, teacher.getWebsite());

            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public int add(Teacher teacher, String rank, String tableName) {
        try {
            String sql = "INSERT INTO `test`.`" + tableName + "`(`collegename`,`schoolname`,`teachersetname`,`teachername`,`rank`,`website`)" + "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            preparedStatement.setString(1, teacher.getCollegeName());
            preparedStatement.setString(2, teacher.getSchoolName());
            preparedStatement.setString(3, teacher.getTeacherSetName());
            preparedStatement.setString(4, teacher.getTeacherName());
            preparedStatement.setString(5, rank);
            preparedStatement.setString(6, teacher.getWebsite());

            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public int add(StructuredTeacher teacher) {
        try {
            String sql = "INSERT INTO `test`.`structuredteacher` (`collegename`,`teachername`,`major`,`field`,`position`,`website`)" + "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            preparedStatement.setString(1, teacher.getCname());
            preparedStatement.setString(2, teacher.getName());
            preparedStatement.setString(3, teacher.getMajor());
            preparedStatement.setString(4, teacher.getField());
            preparedStatement.setString(5, teacher.getPosition());
            preparedStatement.setString(6, teacher.getWebsite());

            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public int changeSig(String name)
    {
        try {
            String sql = "UPDATE test.college SET significant = \'1\' WHERE collegename = \'" + name + "\';";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public int removeBlank(String name)
    {
        try {
            String newName = name.replaceAll("\\s*", "");
            String sql = "UPDATE test.college SET collegename = \'" + newName + "\' WHERE collegename = \'" + name + "\';";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.executeUpdate();

            sql = "UPDATE test.school SET schoolname = \'" + newName + "\' WHERE schoolname = \'" + name + "\';";
            preparedStatement = conn.prepareStatement(sql);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }
}
