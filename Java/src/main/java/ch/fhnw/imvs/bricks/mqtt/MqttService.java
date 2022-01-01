// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package ch.fhnw.imvs.bricks.mqtt;

import java.security.cert.X509Certificate;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/* package */ class InsecureTrustManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {}

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {}

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}

/* package */ final class MqttService {
    public MqttService() {}

    private IMqttClient client = null;
    private String username = null;
    private String password = null;

    public void init(String host, String username, String password) {
        try {
            String hostURI = "ssl://" + host;
            String clientID = MqttClient.generateClientId();
            this.username = username;
            this.password = password;
            client = new MqttClient(hostURI, clientID, new MemoryPersistence());
        } catch (MqttException e) {
            e.printStackTrace();
        }    
    }

    public void connect() {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            options.setKeepAliveInterval(30); // s
            if (username != null) {
                options.setUserName(username);
            }
            if (password != null) {
                options.setPassword(password.toCharArray());
            }
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, 
                new TrustManager[] { new InsecureTrustManager() }, 
                new java.security.SecureRandom());
            options.setSocketFactory(sslContext.getSocketFactory());

            System.out.println("connect, " + client.getServerURI() + ", " + username + ", " + password);

            client.connect(options);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (MqttSecurityException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic, IMqttMessageListener listener) {
        try {
            System.out.printf("subscribe to \"%s\"\n", topic);
            client.subscribe(topic, 1, listener);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publish(String topic, byte[] payload) {
        try {
            System.out.printf("publish to \"%s\"\n", topic);
            client.publish(topic, payload, 1, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
