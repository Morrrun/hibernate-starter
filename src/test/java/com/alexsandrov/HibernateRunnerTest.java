package com.alexsandrov;

import com.alexsandrov.entity.Chat;
import com.alexsandrov.entity.Company;
import com.alexsandrov.entity.LocaleInfo;
import com.alexsandrov.entity.Profile;
import com.alexsandrov.entity.User;
import com.alexsandrov.entity.UserChat;
import com.alexsandrov.util.HibernateUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Cleanup;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class HibernateRunnerTest {

    @Test
    void checkH2() {
        try (var sessionFactory = HibernateUtil.buildSessionFactory()) {
            try (var session = sessionFactory.openSession()) {
                session.beginTransaction();

                var company = Company.builder()
                        .name("Google")
                        .build();

                session.persist(company);

                session.getTransaction().commit();
            }
        }
    }
    @Test
    void localeInfo() {
        try (var sessionFactory = HibernateUtil.buildSessionFactory()) {
            try (var session = sessionFactory.openSession()) {
                session.beginTransaction();

                var company = session.get(Company.class, 1L);
                company.getLocales().add(LocaleInfo.of("ru", "Описание на русском"));
                company.getLocales().add(LocaleInfo.of("en", "English description"));


                session.getTransaction().commit();
            }
        }
    }

    @Test
    void checkManyToMany() {
        try (var sessionFactory = HibernateUtil.buildSessionFactory()) {
            try (var session = sessionFactory.openSession()) {
                session.beginTransaction();

                var user = session.get(User.class, 5L);
                var chat = session.get(Chat.class, 2L);

                var userChat = UserChat.builder()
                        .created_at(Instant.now())
                        .created_by(user.getUsername())
                        .build();

                userChat.setUser(user);
                userChat.setChat(chat);

                session.persist(chat);

                session.getTransaction().commit();
            }
        }
    }

    @Test
    void  checkOneToOne() {
        try (var sessionFactory = HibernateUtil.buildSessionFactory()) {
            try (var session = sessionFactory.openSession()) {
                session.beginTransaction();

                var user = session.get(User.class, 5L);
//                var user = User.builder()
//                        .username("test5@gmail.com")
//                        .company(Company.builder()
//                                .id(1)
//                                .name("Google")
//                                .build())
//                        .build();
//                var profile = Profile.builder()
//                        .languages("RU")
//                        .street("Kolasa 18")
//                        .build();
//                profile.setUser(user);

                session.persist(user);

                session.getTransaction().commit();
            }
        }
    }

    @Test
    void checkOrhanRemoval() {
        @Cleanup var sessionFactory = HibernateUtil.buildSessionFactory();
        @Cleanup var session = sessionFactory.openSession();

        session.beginTransaction();

        Company company = session.get(Company.class, 2L);

        company.getUsers().removeIf(user -> user.getId().equals(1L));
        session.remove(company);

        session.getTransaction().commit();
    }

    @Test
    void checkLazyInitialisation() {
//        Company company = null;
        try (var sessionFactory = HibernateUtil.buildSessionFactory()) {
            try (var session = sessionFactory.openSession()) {
                session.beginTransaction();

                User user = session.get(User.class,1L);
                System.out.println(user);
                Company company1 = user.getCompany();

                session.getTransaction().commit();
            }
        }
//        var users = company.getUsers();
//        System.out.println(users.size());
    }

    @Test
    void deleteCompany() {
        @Cleanup var sessionFactory = HibernateUtil.buildSessionFactory();
        @Cleanup var session = sessionFactory.openSession();

        session.beginTransaction();

        var company = session.get(Company.class, 2L);

        session.remove(company);

        session.getTransaction().commit();
    }

    @Test
    void addUserToNewCompany() {
        @Cleanup var sessionFactory = HibernateUtil.buildSessionFactory();
        @Cleanup var session = sessionFactory.openSession();

        session.beginTransaction();

        var company = Company.builder()
                .name("Facebook")
                .build();

        User user = User.builder()
                .username("sveta@gmail.com")
                .build();

        company.addUser(user);

        session.persist(company);

        session.getTransaction().commit();
    }

    @Test
    void oneToMany() {
        @Cleanup var sessionFactory = HibernateUtil.buildSessionFactory();
        @Cleanup var session = sessionFactory.openSession();

        session.beginTransaction();

        var company = session.get(Company.class, 1L);
        System.out.println();

        session.getTransaction().commit();

    }

    @Test
    void checkGetReflectionApi() throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.getString("username");
        resultSet.getString("firstname");
        resultSet.getString("lastname");

        Class<User> clazz = User.class;
        Constructor<User> constructor = clazz.getConstructor();
        User user = constructor.newInstance();
        Field usernameField = clazz.getDeclaredField("username");
        usernameField.setAccessible(true);
        usernameField.set(user, resultSet.getString("username"));
    }

    @Test
    void checkReflectionApi() throws SQLException, IllegalAccessException {
        User user = User.builder().build();

        String sql = """
                INSERT INTO %s (%s)
                VALUES (%s)
                """;

        String tableName = Optional.ofNullable(user.getClass().getAnnotation(Table.class))
                .map(tableAnnotation -> tableAnnotation.schema() + "." + tableAnnotation.name())
                .orElse(user.getClass().getName());

        Field[] declaredFields = user.getClass().getDeclaredFields();
        String columnsNames = Arrays.stream(declaredFields)
                .map(field -> Optional.ofNullable(field.getAnnotation(Column.class))
                        .map(Column::name)
                        .orElse(field.getName()))
                .collect(Collectors.joining(", "));

        String columnsValues = Arrays.stream(declaredFields)
                .map(field -> "?")
                .collect(Collectors.joining(", "));

        System.out.printf((sql) + "%n", tableName, columnsNames, columnsValues);


        Connection connection = null;

        PreparedStatement preparedStatement = connection.prepareStatement(sql.formatted(tableName, columnsNames, columnsValues));
        for(Field declaredField: declaredFields) {
            declaredField.setAccessible(true);
            preparedStatement.setObject(1, declaredField.get(user));
        }
    }

}