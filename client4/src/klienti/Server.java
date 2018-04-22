package klienti;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
public class Server extends JFrame {
	
	private JTextArea chatArea;
	private JTextField inputField;
	
	private ObjectOutputStream output;
	private ObjectInputStream input;
	
	private ServerSocket server;
	private Socket connection;
	
	private String username="SERVER_DEFAULT";
	private int port=0;
	
	public Server(){
		super("Servera puse");
		inputField = new JTextField();
		inputField.setEditable(true);
		
		inputField.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent event){
					if(username == "SERVER_DEFAULT") {
						username = event.getActionCommand();
						show("lietotājvārds nomainīts uz "+username);
						inputField.setText("");
					}
					else if(!(port>0)) {
						port=Integer.parseInt(event.getActionCommand());
						inputField.setText("");
						
					}else {
					msg(event.getActionCommand());
					inputField.setText("");}
				}
			}
		);
		
		add(inputField, BorderLayout.SOUTH);
		chatArea = new JTextArea();
		Font font = new Font("Verdana", Font.BOLD, 12);
		chatArea.setFont(font);
		chatArea.setEditable(false);
		add(new JScrollPane(chatArea));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(600, 300);
		setResizable(false);
		setVisible(true);
			prieksdarbi();
			System.out.println(port);
			try {
				server = new ServerSocket(port, 100);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			while(true){
				try{
					serverize();
				}catch(EOFException eofException){
					show("\n "+username+" pārtrauca savienojumu! ");
				}catch(IOException ioException){
					try {
						clean();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ioException.printStackTrace();
				}finally{
					inputField.setEditable(false);
					try{
						output.close(); 
						input.close(); 
						connection.close(); 
					}catch(IOException ioException){
						ioException.printStackTrace();
					}
				}
			}
}
	
	//aiztaisa streamus
	private void clean() throws IOException {
		output.close(); 
		input.close(); 
		connection.close(); 
	}
	//
	public void prieksdarbi() {
		if(username=="SERVER_DEFAULT") {
			show("Ievadiet lietotājvārdu");
		}
		while(username=="SERVER_DEFAULT") {
			System.out.println(username);
		}
		if(!(port>0)) {
			show("Ievadiet savienojuma portu");
		}
		while(!(port>0)) {
			System.out.println("ports = "+port);
		}show("Ports = "+port);
		inputField.setEditable(false);
	}
	//vajag l
	private void serverize() throws IOException {
		connect();
		streams();
		chitChat();
	}
	private void connect() throws IOException{
		show(" Gaida savienojumu! \n");
		connection = server.accept();
		show(" Savienots ar " + connection.getInetAddress().getHostName());
	}
	
	private void streams() throws IOException{
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		System.out.println("streams");
	}
	
	//tiek sanemtas zinas un padotas metodei show();
	private void chitChat(){
		try {
		String message = " Savienojums izveidots! ";
		msg(message);
		inputField.setEditable(true);
		do{
			try{
				message = (String) input.readObject();
				show(message);
			}catch(ClassNotFoundException classNotFoundException){
				classNotFoundException.printStackTrace();
			}catch (SocketTimeoutException e) {
                e.printStackTrace();
            }
		}while(true);
		}catch(IOException e) {
			System.out.println("ha?");
		}
	}
	//Nosūta ziņas klientam (output)
	private void msg(String message){
		try{
			output.writeObject(username+" : " + message);
			output.flush();
			show(username+" : " + message);
		}catch(IOException ioException){
			chatArea.append("\n ERROR - ziņa nenosūtījās");
		}
	}
	
	//attēlo saņemtās ziņas chatArea
	private void show(final String text){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");//21.04.18 22:04
					LocalDateTime now = LocalDateTime.now();
					String laiks = dtf.format(now); //21.04.18 22:04
					chatArea.append("\n"+laiks+"    "+text);
				}
			}
		);
	}
}