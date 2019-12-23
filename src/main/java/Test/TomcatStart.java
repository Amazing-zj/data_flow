package Test;

import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

public class TomcatStart {
    private Tomcat tomcat;


    private  void startTomcat(int port, String contextPath, String baseDir) throws Exception{
        tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.setBaseDir(".");
        StandardServer server = (StandardServer) tomcat.getServer();
        AprLifecycleListener listener = new AprLifecycleListener();
        server.addLifecycleListener(listener);
        tomcat.addWebapp(contextPath,baseDir);
        tomcat.start();
    }

    private void stop()   {
        try {
            tomcat.stop();
        }catch (Exception e){}
    }

    public Tomcat getTomcat(){
        return tomcat;
    }

    public static void main(String[] args) {
        try{
            int port = 8081;
            String contextPath = "/mcg";
            String baseDir = new File("src/main/webapp").getAbsolutePath();
            TomcatStart tomcat = new TomcatStart();
            tomcat.startTomcat(port, contextPath, baseDir);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }
}
