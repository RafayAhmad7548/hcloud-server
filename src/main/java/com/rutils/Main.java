package com.rutils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;;

public class Main{

    static HttpsServer server;

    static{
        try{
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(Main.class.getClassLoader().getResourceAsStream("auth/ksserver.p12"), "server7548".toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, "server7548".toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            server = HttpsServer.create(new InetSocketAddress(25565), 0);
            server.setHttpsConfigurator(new HttpsConfigurator(sslContext){
                @Override
                public void configure(HttpsParameters params){
                    // InetSocketAddress remote = params.getClientAddress();
                    SSLContext c = getSSLContext();
                    SSLParameters sslParameters = c.getDefaultSSLParameters();
                    // if(remote.equals()){

                    // }
                    params.setSSLParameters(sslParameters);
                }
            });
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }
    public static void main(String[] args){
        System.out.println("Creating Contexts");
        server.createContext("/login", new LoginHandler());
        server.createContext("/register", new RegisterHandler());
        server.setExecutor(null);
        System.out.println("Starting Server...");
        server.start();
        System.out.println("Server Started");
    }

    static class RegisterHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) throws IOException{
            if("POST".equals(exchange.getRequestMethod())){
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(body);
                String username = jsonObject.getString("username");
                String password = jsonObject.getString("password");

                User user = new User(username, password);
                Session session = HibernateUtil.getSessionfactory().openSession();
                Transaction tx = null;
                String response = Integer.toString(1);

                try{
                    tx = session.beginTransaction();
                    User user2 = session.get(User.class, username);
                    if(user2 == null){
                        session.persist(user);
                        response = Integer.toString(1);
                    }
                    else response = Integer.toString(2);
                    
                    tx.commit();
                }
                catch(Exception e){
                    if(tx != null) tx.rollback();
                    e.printStackTrace();
                }
                finally{
                    session.close();
                }

                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();

            }
            else{
                exchange.sendResponseHeaders(405, -1);
            }

        }

    }

    static class LoginHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange exchange) throws IOException{
            if("POST".equals(exchange.getRequestMethod())){
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(body);
                String username = jsonObject.getString("username");           
                String password = jsonObject.getString("password");

                String response = Integer.toString(1);

                User user;
                Session session = HibernateUtil.getSessionfactory().openSession();
                Transaction tx = null;

                try{
                    tx = session.beginTransaction();
                    user = session.get(User.class, username);
                    if(user != null){
                        if(user.getPassword().equals(password)) response = Integer.toString(1); // successful verification
                        else response = Integer.toString(2); // incorrect password
                    }
                    else response = Integer.toString(0); // user not registered
                    tx.commit();
                }
                catch(Exception e){
                    if(tx != null) tx.rollback();
                    e.printStackTrace();
                }
                finally{
                    session.close();
                }

                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
            else{
                exchange.sendResponseHeaders(405, -1);
            }
        }

    }
}