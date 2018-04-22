package klienti;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class Client extends JFrame{
	
	private JTextArea chatArea;
	private JTextField inputField;
	
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private Socket connection;
	
	private String message = "";
	
	private String IP=null;
	private int port=0;
	private String username = "CLIENT_DEFAULT";
	
	//konstruktors
	public Client(){
		super("Client");
		inputField = new JTextField();
		inputField.setEditable(true);
		//kas notiek kad nospiež enter. inputs ir inputField+enter
		inputField.addActionListener(
				new ActionListener(){
				public void actionPerformed(ActionEvent event){
					if(username == "CLIENT_DEFAULT") {
						username = event.getActionCommand();
						show("lietotājvārds nomainīts uz "+username);
						inputField.setText("");
					}
				else if(IP==null) {
						IP=event.getActionCommand();
						inputField.setText("");
					}else if(!(port>0)){
						port=Integer.parseInt(event.getActionCommand());
						inputField.setText("");
					}else {
					msg(event.getActionCommand());
					System.out.println(event.getActionCommand());
					inputField.setText("");}
				}
			}
		);
		add(inputField, BorderLayout.SOUTH);
		chatArea = new JTextArea();
		Font font = new Font("Verdana", Font.BOLD, 12); //smukāks fonts
		chatArea.setFont(font);
		chatArea.setEditable(false);
		add(new JScrollPane(chatArea));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(500, 300);
		setResizable(false);
		setVisible(true);
	}
	//pirms izveido savienojumu ir jānoskaidro ip,ports,username
	public void prieksdarbi() {
		if(username=="CLIENT_DEFAULT") {
			show("Ievadiet lietotājvārdu");
		}
		while(username=="CLIENT_DEFAULT") {
			System.out.println(username);
		}
		if(IP==null) {
			show("Ievadi IP adresi");
		}
		while(IP==null) {
			System.out.println("IP = "+IP);
		}
		show("IP = "+IP);
		if(!(port>0)) {
			show("Ievadi savienojuma portu");
		}
		while(!(port>0)) {
			System.out.println("port = "+port);
		}
		show("Ports = "+port);
	}
	//Visa galvenā darbība notiek šeit
	public void START(){
		while(true) {try{
			prieksdarbi();
			connect();
			streams();
			chitChat();
		}catch(EOFException eofException){
			show("Klients pārtrauca savienojumu");
		}catch(IOException ioException){
			ioException.printStackTrace();
		}finally{
			show("Pārtrauc savienojumu");
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
	
	//atkārtoti mēģina savienoties ar serveri
	private void connect() throws IOException{
		show("Gaida savienojumu \n");
		do { try{connection = new Socket(InetAddress.getByName(IP), port);
		show("Savienots ar : " + connection.getInetAddress().getHostName());
		System.out.println(connection);}catch(ConnectException e) {};
		}
		while(connection==null);
	}
	
	//izveido input, output kanalus/streamus
	private void streams() throws IOException{
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		show("Savienojums izveidots! \n");
	}
	
	//tiek sanemtas zinas un padotas metodei show();
	private void chitChat() throws IOException{
		inputField.setEditable(true);
		do{
			try{
				message = (String) input.readObject();
				show(message);
			}catch(ClassNotFoundException e){
				e.printStackTrace();
			}
		}while(true);	
	}
	
	//tiek nosūtītas ziņas serverim (outputs)
	private void msg(String message){
		try{
			output.writeObject(username+" : " + message);
			output.flush();
			show(username+" : " + message);
		}catch(IOException ioException){
			chatArea.append("\n ERROR - ziņa nenosūtījās");
		}
	}
	
	//tiek attēlotas ziņas uz chatArea
	private void show(final String message){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm"); //21.04.18 22:04
					LocalDateTime now = LocalDateTime.now();
					String laiks = dtf.format(now); //21.04.18 22:04
					chatArea.append("\n"+laiks+"    "+message);
				}
			}
		);
	}

}