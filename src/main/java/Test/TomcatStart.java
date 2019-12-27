package Test;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

public class TomcatStart {
    private Tomcat tomcat;

    //function
    private  void startTomcat(int port, String contextPath, String baseDir) throws LifecycleException{
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
        }catch (LifecycleException e){}
    }

    private static class shutdownThread implements Runnable{
        private TomcatStart tomcat;

        public shutdownThread(TomcatStart tomcat){this.tomcat = tomcat;}

        @Override
        public void run(){
            tomcat.stop();
        }
    }

    //jar run
    private TomcatStart(){}

    public static void main(String[] args) {// TODO: 2019/12/24 9:44 invoke embed browser window
        try{
            int port = 8081;
            String contextPath = "/mcg";
            String baseDir = new File("src/main/webapp").getAbsolutePath();
            TomcatStart tomcat = new TomcatStart();
            Runtime.getRuntime().addShutdownHook(new Thread(new shutdownThread(tomcat)));
            tomcat.startTomcat(port, contextPath, baseDir);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    //mvn dependency
    public TomcatStart(int port, String contextPath, String baseDir)throws LifecycleException{
        startTomcat(port,contextPath, baseDir);
    }

    public void Stop()throws LifecycleException {
        tomcat.stop();
    }
}
