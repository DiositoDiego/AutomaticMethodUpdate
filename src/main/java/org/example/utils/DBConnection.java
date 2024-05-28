package org.example.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/reflexion", "root", "root");
    }

//    create database reflexion;
//    use reflexion;
//
//    create table metodos(
//            id int primary key auto_increment,
//            nombre varchar(255) not null,
//            f_creacion datetime default now()
//    );
//
//    select * from metodos;
}
