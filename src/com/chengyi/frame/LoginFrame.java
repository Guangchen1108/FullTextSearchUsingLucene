package com.chengyi.frame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFileChooser;

public class LoginFrame extends Frame{
		MenuBar menubar;  
	    Menu menu;  
	    MenuItem item1, item2;  
	    public LoginFrame(String s)  
	    {  
	        setTitle(s);  
	        Toolkit tool = getToolkit();  
	        Dimension dim = tool.getScreenSize();  
	        setBounds(0, 0, dim.width, dim.height);  
	        menubar = new MenuBar();  
	        menu = new Menu("�ļ�");  
	        item1 = new MenuItem("��");  
	        item2 = new MenuItem("����");
	        menu.add(item1);  
	        menu.add(item2);  
	        menubar.add(menu);
	        setMenuBar(menubar);
	        setSize(800, 600);
	        setResizable(false);
	        final Label pathLabel  = new Label("����·��:");
	        
	        add(BorderLayout.NORTH,pathLabel);
	        this.addWindowListener(new WindowAdapter(){ 
	            public void windowClosing(WindowEvent e) { 
	                dispose(); 
	                System.exit(0); 
	            } 
	        }); 
	        item1.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser();
					int ret = chooser.showOpenDialog(LoginFrame.this);
					if (ret == JFileChooser.APPROVE_OPTION) {
						pathLabel.setText("����·��:" + chooser.getCurrentDirectory().toString());
					}
					
				}
	        	
	        });
	        setVisible(true);
	    }
}
