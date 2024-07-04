package com.rutils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;;

public class Main{
    public static void main(String[] args){
        try{
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(Main.class.getClassLoader().getResourceAsStream("server.p12"), "ksserver7548".toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, "ksserver7548".toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);

            HttpsServer server = HttpsServer.create(new InetSocketAddress(8000), 0);
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
            server.createContext("/greet", new GreetHandler());
            server.setExecutor(null);
            server.start();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    static class GreetHandler implements HttpHandler{

        @Override
        public void handle(HttpExchange exchange) throws IOException{
            String response = "hello client";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

    }
}