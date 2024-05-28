package org.example;

import org.example.utils.DBConnection;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Main {

    static {
        double totalTime = System.currentTimeMillis();
        double time = System.currentTimeMillis();
        System.out.println("Preparando listas...");
        List<String> metodosDB = new LinkedList<>();
        List<String> metodosClases = new LinkedList<>();
        List<String> metodosAInsertar = new LinkedList<>();
        List<String> metodosAEliminar = new LinkedList<>();
        System.out.println("Listas preparadas! (" + (System.currentTimeMillis() - time) + " ms)");
        try {
            time = System.currentTimeMillis();
            System.out.println("Conectando a la base de datos...");
            Connection con = DBConnection.getConnection();
            System.out.println("Conectado a la base de datos! (" + (System.currentTimeMillis() - time) + " ms)");

            time = System.currentTimeMillis();
            System.out.println("Obteniendo metodos guardados en la base de datos...");
            PreparedStatement ps = con.prepareStatement("SELECT nombre FROM metodos");
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                metodosDB.add(rs.getString(1));
            }
            System.out.println("Metodos obtenidos! (" + (System.currentTimeMillis() - time) + " ms)");

        } catch (SQLException e) {
            System.err.println("ERROR AL CONECTAR A LA BASE DE DATOS!!!!!!!");
            System.err.println("ERROR -> " + e.getMessage());
            System.exit(e.getErrorCode());
        }

        time = System.currentTimeMillis();
        System.out.println("Cargando clases...");
        Reflections reflections = new Reflections("org.example.methods", new SubTypesScanner(false));
        Set<Class<?>> clases = reflections.getSubTypesOf(Object.class);
        System.out.println("Clases cargadas! (" + (System.currentTimeMillis() - time) + " ms)");

        time = System.currentTimeMillis();
        System.out.println("Obteniendo todos los métodos de las clases...");
        System.out.println("\n------------------- MÉTODOS -----------------------");
        int mayor = 0;
        for (Class clase: clases){
            for (Method metodo: clase.getDeclaredMethods()){
                StringBuilder methodName = new StringBuilder();
                methodName.append(metodo.getName());
                methodName.append("(");
                Class<?>[] tiposParametros = metodo.getParameterTypes();
                for (int i = 0; i < metodo.getParameterCount(); i++){
                    methodName.append(tiposParametros[i].getName());
                    if (i < tiposParametros.length - 1){
                        methodName.append(", ");
                    }
                }
                methodName.append(")");
                metodosClases.add(clase.getName()+"."+methodName);
                String name = clase.getName()+"."+methodName+" : "+metodo.getReturnType();
                System.out.println(name + " (size = " + name.length() + ")");
                if(name.length() > mayor){
                    mayor = name.length();
                }
            }
        }
        System.out.println("MAX_LENGTH: " + mayor);
        System.out.println("---------------------------------------------------\n");
        System.out.println("Metodos obtenidos! (" + (System.currentTimeMillis() - time) + " ms)");

        time = System.currentTimeMillis();
        System.out.println("Comparando metodos...");
        for (String metodo : metodosClases){
            if (!metodosDB.contains(metodo)){
                metodosAInsertar.add(metodo);
            }
        }
        for (String metodo: metodosDB) {
            if (!metodosClases.contains(metodo)){
                metodosAEliminar.add(metodo);
            }
        }
        System.out.println("Metodos comparados! (" + (System.currentTimeMillis() - time) + " ms)");

        if (metodosAInsertar.isEmpty() && metodosAEliminar.isEmpty()){
            System.out.println("Proceso terminado! (" + (System.currentTimeMillis() - totalTime) + " ms)");
        } else {
            if (!metodosAEliminar.isEmpty()) {
                time = System.currentTimeMillis();
                System.out.println("Preparando metodos a eliminar...");
                String metodos = "(";
                for (String metodo : metodosAEliminar) {
                    metodos += "'" + metodo + "',";
                }
                metodos = metodos.substring(0, metodos.length() - 1);
                metodos += ")";
                String sql = "DELETE FROM metodos WHERE nombre IN " + metodos;
                System.out.println("Metodos preparados! (" + (System.currentTimeMillis() - time) + " ms)");
                try {
                    time = System.currentTimeMillis();
                    System.out.println("Conectando a la base de datos...");
                    Connection con = DBConnection.getConnection();
                    System.out.println("Conectado a la base de datos! (" + (System.currentTimeMillis() - time) + " ms)");

                    time = System.currentTimeMillis();
                    System.out.println("Eliminando métodos de la base de datos...");
                    PreparedStatement ps = con.prepareStatement(sql);
                    int deletedMethods = ps.executeUpdate();
                    if (deletedMethods >= 1) {
                        System.out.println(deletedMethods + " metodos eliminados! (" + (System.currentTimeMillis() - time) + " ms)");
                    } else {
                        System.err.println("No se insertó ningún método (" + (System.currentTimeMillis() - time) + " ms)");
                    }

                } catch (SQLException e) {
                    System.err.println("ERROR AL CONECTAR A LA BASE DE DATOS!!!!!!!");
                    System.err.println("ERROR -> " + e.getMessage());
                    System.err.println("SQL -> " + sql);
                    System.exit(e.getErrorCode());
                }
            }

            if (!metodosAInsertar.isEmpty()) {
                time = System.currentTimeMillis();
                System.out.println("Preparando metodos a insertar...");
                String metodos = "";
                for (String metodo : metodosAInsertar) {
                    metodos += "('" + metodo + "'),";
                }
                metodos = metodos.substring(0, metodos.length() - 1);
                String sql = "INSERT INTO metodos(nombre) VALUES " + metodos;
                System.out.println("Metodos preparados! (" + (System.currentTimeMillis() - time) + " ms)");
                try {
                    time = System.currentTimeMillis();
                    System.out.println("Conectando a la base de datos...");
                    Connection con = DBConnection.getConnection();
                    System.out.println("Conectado a la base de datos! (" + (System.currentTimeMillis() - time) + " ms)");

                    time = System.currentTimeMillis();
                    System.out.println("Insertando métodos en la base de datos...");
                    PreparedStatement ps = con.prepareStatement(sql);
                    int insertedMethods = ps.executeUpdate();
                    if (insertedMethods >= 1) {
                        System.out.println(insertedMethods + " metodos insertados! (" + (System.currentTimeMillis() - time) + " ms)");
                    } else {
                        System.err.println("No se insertó ningún método (" + (System.currentTimeMillis() - time) + " ms)");
                    }

                } catch (SQLException e) {
                    System.err.println("ERROR AL CONECTAR A LA BASE DE DATOS!!!!!!!");
                    System.err.println("ERROR -> " + e.getMessage());
                    System.err.println("SQL -> " + sql);
                    System.exit(e.getErrorCode());
                }
            }
            System.out.println("Proceso terminado! (" + (System.currentTimeMillis() - totalTime) + " ms)");
        }
    }

    public static void main(String[] args) {
        System.out.println("Hola mundo");
    }
}