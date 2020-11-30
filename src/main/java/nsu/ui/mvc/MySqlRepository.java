package nsu.ui.mvc;

import nsu.ui.Student;
import nsu.ui.MessageRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MySqlRepository implements MessageRepository {
    public static final String url = "jdbc:mysql://localhost:3306/groups";
    public static final String user = "root";
    public static final String pwd = "32232";
    private final Connection connection;
    private Long id;

    public MySqlRepository() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection(url, user, pwd);
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public Iterable<Student> findAll() {
        String query = "SELECT student.id, student.group_id, student.first_name, student.second_name, student.last_name, " +
                "student.birthday_date, `group`.group_name FROM student " +
                "INNER JOIN `group` ON `group`.id = student.group_id";
        ArrayList<Student> students = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                Student student = new Student();
                student.setId(rs.getLong(1));
                student.setName(rs.getString(3));
                student.setSecondName(rs.getString(4));
                student.setLastName(rs.getString(5));
                student.setBirthdayDate(rs.getString(6));
                student.setGroupName(rs.getString(7));
                students.add(student);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return students;
    }

    @Override
    public Student save(Student student) throws SQLException {
        String name = "'" + student.getName() + "'";
        String lastName = "'" + student.getLastName() + "'";
        String birthdayDate = "'" + student.getBirthdayDate() + "'";
        try (Statement st = connection.createStatement()) {
            st.executeUpdate(String.format("INSERT into `group`(group_name) VALUES ('%s')", student.getGroupName()));
            try (ResultSet rs = st.executeQuery(String.format("select id from `group` where group_name = '%s'", student.getGroupName()))) {
                while (rs.next()) {
                    String groupId = rs.getString(1);
                    st.executeUpdate(String.format("INSERT into student(group_id, first_name, second_name, last_name, birthday_date) " +
                            "VALUES ('%s', '%s', '%s', '%s', '%s')", groupId, student.getName(), student.getSecondName(),
                            student.getLastName(), student.getBirthdayDate()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try (ResultSet rs2 = st.executeQuery("select id from student where first_name = " + name + " and last_name = " + lastName + " and birthday_date = " + birthdayDate)) {
                Long id = student.getId();
                if (id == null) {
                    while (rs2.next()) {
                        student.setId(rs2.getLong(1));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return student;
    }

    @Override
    public Student findStudent(Long id) {
        String query = "SELECT student.id, student.group_id, student.first_name, student.second_name, student.last_name, " +
                "student.birthday_date, `group`.group_name FROM student " +
                "INNER JOIN `group` ON `group`.id = student.group_id";
        Student student = new Student();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                if (id == rs.getLong(1)) {
                    student.setId(rs.getLong(1));
                    student.setName(rs.getString(3));
                    student.setSecondName(rs.getString(4));
                    student.setLastName(rs.getString(5));
                    student.setBirthdayDate(rs.getString(6));
                    student.setGroupName(rs.getString(7));
                    break;
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return student;
    }

    public HashMap<String, ArrayList<Student>> findGroups() throws SQLException {
        HashMap<String, ArrayList<Student>> mapOfGroups = new HashMap<String, ArrayList<Student>>();
        ArrayList<String> groups = new ArrayList<>();
        Iterable<Student> all = findAll();
        ArrayList<Student> groupStudents = new ArrayList<>();
        for (Student student : all) {
            if (!groups.contains(student.getGroupName())) {
                groups.add(student.getGroupName());
            }
        }
        try (Statement st = connection.createStatement()) {
            for (String group : groups) {
                try (ResultSet rs = st.executeQuery(String.format("SELECT student.id, student.group_id, student.first_name, " +
                        "student.second_name, student.last_name, student.birthday_date, `group`.group_name FROM student " +
                        "INNER JOIN `group` ON `group`.id = student.group_id where group_name = '%s'", group))) {
                    while (rs.next()) {
                        Student student = new Student();
                        student.setId(rs.getLong(1));
                        student.setName(rs.getString(3));
                        student.setSecondName(rs.getString(4));
                        student.setLastName(rs.getString(5));
                        student.setBirthdayDate(rs.getString(6));
                        student.setGroupName(rs.getString(7));
                        groupStudents.add(student);
                    }
                    ArrayList<Student> arr = (ArrayList<Student>) groupStudents.clone();
                    mapOfGroups.put(group, arr);
                    groupStudents.clear();
                }
            }
        }
        return mapOfGroups;
    }

    public void saveId(Student student) {
        this.id = student.getId();
    }

    public Long getId() {
        return this.id;
    }

    public Student update(Student student) throws SQLException {
        String name = "'" + student.getName() + "'";
        String lastName = "'" + student.getLastName() + "'";
        String birthdayDate = "'" + student.getBirthdayDate() + "'";
        try (Statement st = connection.createStatement()) {
            try (ResultSet rs = st.executeQuery(String.format("select id from `group` where group_name = '%s'", student.getGroupName()))) {
                if (!rs.next()) {
                    st.executeUpdate(String.format("INSERT into `group`(group_name) VALUES ('%s')", student.getGroupName()));
                }
                try (ResultSet rs2 = st.executeQuery(String.format("select id from `group` where group_name = '%s'", student.getGroupName()))) {
                    while (rs2.next()) {
                        String groupId = rs2.getString(1);
                        st.executeUpdate(String.format("update student set group_id = '%s', " +
                                        "first_name = '%s', second_name = '%s', last_name = '%s', birthday_date = '%s' where id = '%s'",
                                groupId, student.getName(), student.getSecondName(), student.getLastName(), student.getBirthdayDate(), student.getId()));
                        break;
                    }
                }
            }
            try (ResultSet rs3 = st.executeQuery("select id from student where first_name = " + name + " and last_name = " + lastName + " and birthday_date = " + birthdayDate)) {
                Long id = student.getId();
                if (id == null) {
                    while (rs3.next()) {
                        student.setId(rs3.getLong(1));
                    }
                }
            }
        }
        return student;
}
}
