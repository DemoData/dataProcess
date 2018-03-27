package com.example.demo.common.ssh;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class SSHLocalForward {

    private static final Object LOCK = new Object();

    private String sshHost,user,password,forwordHost;

    private int port;
    
    public int localPort;

    private SSHClient client = null;

    public SSHLocalForward(String sshHost,String user,String password,String forwordHost,int port){
        this.sshHost = sshHost;
        this.user = user;
        this.password = password;
        this.forwordHost = forwordHost;
        this.port = port;
        localPort = port;
    }

    /**
     * 连接到SSH
     * @throws IOException
     */
    public void connectSSH() throws IOException {
        new Thread(new Runnable() {
            public void run() {
                connectToSSH();
            }
        }).start();

        synchronized (LOCK) {
            try {
                LOCK.wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("如果没有报错，则SSH已连接，可连接localhost:"+localPort+"...");
        Thread.yield();

    }

    public void closeSSH(){
        try {
            while (client != null){
                client.close();
                client = null;
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connectToSSH(){
        try {
            if (client != null) {
                throw new RuntimeException("不能重复连接ssh...");
            }
            client = new SSHClient();

            client.loadKnownHosts();
            client.addHostKeyVerifier("34:89:41:c6:92:66:27:00:34:ab:b5:95:55:10:45:6d");
            client.connect(sshHost);
            try {

                // client.authPublickey(System.getProperty("user.name"));
                client.authPassword(user, password);

	            /*
	            * _We_ listen on localhost:8080 and forward all connections on to server, which then forwards it to
	            * google.com:80
	            */
                final LocalPortForwarder.Parameters params
                        = new LocalPortForwarder.Parameters("0.0.0.0", localPort, forwordHost, port);
                final ServerSocket ss = new ServerSocket();
                ss.setReuseAddress(true);
                ss.bind(new InetSocketAddress(params.getLocalHost(), params.getLocalPort()));
                try {
                    synchronized (LOCK) {
                        LOCK.notify();
                    }
                    client.newLocalPortForwarder(params, ss).listen();
                } finally {
                    ss.close();
                }

            } finally {
                client.disconnect();
                client = null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static  void main(String[] args){
        /*SSHLocalForward forward = new SSHLocalForward("121.199.8.159", "root", "jakt7ax2qOx.dyFb",
                "dds-bp1baff8ad4002a42.mongodb.rds.aliyuncs.com", 3717);*/
        SSHLocalForward forward = new SSHLocalForward("121.199.26.56", "root", "drm9joUtrzvPzf1~",
                "dds-bp1f7b56b50093041.mongodb.rds.aliyuncs.com", 3717);
        forward.localPort = 3718;
        try {
            forward.connectSSH();
        }catch (Exception e){
           e.printStackTrace();
        }
//        var forwardNlp = SSHLocalForward("114.55.11.178", "root", "mqa%54ovhtvgYWvm", "121.199.7.19", 1000)
//        forwardNlp.connectSSH()

//        SSHLocalForward forward = new  SSHLocalForward("114.55.11.178", "root", "mqa%54ovhtvgYWvm", "121.199.7.19", 1000);
//        try {
//            forward.connectSSH();
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }


    }

}