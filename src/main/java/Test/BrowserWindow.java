package Test;

import javax.swing.*;
import java.awt.*;

public class BrowserWindow extends JFrame {
    private int width;
    private int height;

    public BrowserWindow(){
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screen = toolkit.getScreenSize();
        width = screen.width;
        height = screen.height;
        setBounds(0,0,width,height);
    }
}
