package com.mcg.util;

import com.jcraft.jsch.*;

import java.io.*;
import java.util.Properties;

public class SSH {
    private static Session getSession(String host, String userName, String password) {
        JSch jsch = new JSch();
        int port = 22;
        Session session = null;
        try {
            session = jsch.getSession(userName, host, port);
            session.setPassword(password);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.setTimeout(3000);
            session.connect();
        } catch (JSchException e) {
            System.out.println(e.getMessage());
        }
        return session;
    }

    private static   void execute(Session session, String cmd) {
        try {
            ChannelExec exec = (ChannelExec) session.openChannel("exec");
            System.out.println("executing : " + cmd);
            exec.setCommand(cmd);
            exec.setErrStream(System.err);
            exec.setInputStream(null);
            InputStream in = exec.getInputStream();
            BufferedReader bufferedInputStream = new BufferedReader(new InputStreamReader(in));
            exec.connect();
            try {
                String result;
                while ((result = bufferedInputStream.readLine()) != null) {
                    System.out.println(result);
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            } finally {
                bufferedInputStream.close();
            }
            exec.disconnect();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("command complete");
    }

    public static void upload(String host, String userName, String password, String Dir, String text) {
        File file = new File("/temp.c");
        if(!file.exists()){
            try {
                file.createNewFile();
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(text);
            bw.flush();
            bw.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        Session session = getSession(host, userName, password);
        if(session != null) {
            try {
                ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
                sftp.connect();
                sftp.cd(Dir);
                sftp.put(new FileInputStream(file), file.getName());
                sftp.disconnect();
                String exe = "dfcc ";
                execute(session, exe+Dir+"/"+file.getName());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            session.disconnect();
        }
        file.delete();
    }

    public static File download(String host, String userName, String password, String Dir, String saveDir, String file) throws Exception {
        Session session = getSession(host, userName, password);
        File remoteFile = new File(saveDir+file);
        if (session != null) {
            ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            channelSftp.cd(Dir);
            channelSftp.get(file , new FileOutputStream(remoteFile));
            channelSftp.disconnect();
            session.disconnect();
        }
        return remoteFile;
    }
}