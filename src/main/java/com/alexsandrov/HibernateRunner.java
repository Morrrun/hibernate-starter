package com.alexsandrov;

import com.alexsandrov.entity.Birthday;
import com.alexsandrov.entity.Company;
import com.alexsandrov.entity.PersonalInfo;
import com.alexsandrov.entity.User;
import com.alexsandrov.util.HibernateUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.sql.SQLException;
import java.time.LocalDate;

@Slf4j
public class HibernateRunner {

    public static void main(String[] args) throws SQLException {

        Company company = Company.builder()
                .name("Google")
                .build();

        User user = User.builder()
                .username("petr3@gmail.com")
                .personalInfo(PersonalInfo.builder()
                        .firstname("Petr")
                        .lastname("Petrov")
                        .birthDate(new Birthday(LocalDate.of(2000, 1, 19)))
                        .build())
                .company(company)
                .build();

        log.info("User entity is in transient state, object: {}", user);

        try (SessionFactory sessionFactory = HibernateUtil.buildSessionFactory()) {
            Session session1 = sessionFactory.openSession();
            try (session1){
                Transaction transaction = session1.beginTransaction();

                User user1 = session1.get(User.class, 1L);

                session1.getTransaction().commit();
            }
            log.warn("User is in detached state: {}, session is closed{}", user, session1);
        } catch (Exception exception) {
            log.error("Exception occurred", exception);
            throw exception;
        }
    }
}