package com.rutils;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil{
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory(){
        try{
            return new Configuration().configure().buildSessionFactory();
        }
        catch(HibernateException e){
            e.printStackTrace();
        }
        return null;
    }

    public static SessionFactory getSessionfactory(){
        return sessionFactory;
    }

    public static void shutdown(){
        getSessionfactory().close();
    }

}