package com.alexsandrov.util;

import com.alexsandrov.converter.BirthdayConverter;
import com.alexsandrov.entity.User;
import lombok.experimental.UtilityClass;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

@UtilityClass
public class HibernateUtil {

    public static SessionFactory buildSessionFactory() {
        Configuration configuration = new Configuration();
//        configuration.addAttributeConverter(new BirthdayConverter());
        configuration.addAnnotatedClass(User.class);
        configuration.configure();

        return configuration.buildSessionFactory();
    }
}
