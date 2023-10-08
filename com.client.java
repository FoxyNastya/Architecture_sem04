package com.client;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
 
import com.model.Flight;
import com.model.Ticket;
/**
 * Клиент
 * @author XWZ
 *
 */
 / * Интерфейс постоянного протокола * /
interface Protocal {
	int QUERY = 1;
	int PURCHASE = 2;
	int ACCEPT = 3;
	int ERROR = 4;
	int EXIT = 5;
	int PAY = 6;
	int LOGIN = 7;
	int QUERYHISTORY = 8;
	int ISLOGIN = 9;
}
 
public class Client implements Protocal{
	static final int PORT = 8888;
	static final String HOST = "127.0.0.1";
	int count = 0;
	Socket socket = null;
 
	public Client() 
	{
		try 
		{
			socket = new Socket();
			socket.connect(new InetSocketAddress(HOST, PORT));
		} 
		catch (IOException e) 
		{
			//e.printStackTrace();
		}
	}
 
	public static void main(String[] args) 
	{
		Client client = new Client();
		if(client.socket.isConnected())
		{
			while(!client.login())
				;
			
			while (client.menu())
				;
			System.out.println("GoodBye");
		}
		else
		{
			System.out.println("503 Service Unavailable,Server error..");
		}
		try 
		{
			client.socket.close();
		} 
		catch (Exception e) {}
	}
 
	public boolean menu() 
	{
		if (count < 1) 
		{
			 System.out.println ("----------- Главный интерфейс системы продажи билетов Blue Sky ----------");
			 System.out.println ("1, запрос билета");
			 System.out.println ("2, запросить исторические записи полета");
			 System.out.println («3, выйти из системы»);
			count++;
		}
		 System.out.println («Пожалуйста, введите вариант операции:»);
 
		boolean flag = true;
		Scanner sc = new Scanner(System.in);
		Integer nChoice = sc.nextInt();
		switch (nChoice) {
		case 1:
			queryTicket(socket);
			break;
		case 2:
			queryHistory(socket);
			break;
		case 3:
			flag = false;
			exit(socket);
			break;
		default:
			 System.out.println («Нет такой опции, введите заново!»);
			break;
		}
		if (!flag)
			return false;
		return true;
	}
 
	 / * Запрос билета * /
	public void queryTicket(Socket socket) 
	{
		String start = null;
		String arrival = null;
		int flag = -1;
		try {
		
			ObjectOutputStream os1 = new ObjectOutputStream(socket.getOutputStream());
			os1.writeObject(new Integer(QUERY));
 
			 Scanner sc = new Scanner(System.in);
			while(flag<0)
			{
				 System.out.println («Введите город отправления:»);
				start = sc.nextLine();
				 System.out.println («Пожалуйста, укажите город прибытия:»);
				arrival = sc.nextLine();
				if(start.trim().equals("") || arrival.trim().equals(""))
				{
					 System.out.println («Пункт отправления или назначения не может быть пустым! Введите повторно:»);
				}
				else
				{
					flag = 1;
				}
			}
			 System.out.println («От:» + начало + «Место назначения:» + прибытие);
			os1.writeObject(start);
			os1.flush();
			os1.writeObject(arrival);
			
			ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
			Object obj = is.readObject();
			if (obj != null) 
			{
				@SuppressWarnings("unchecked")
				List<Flight> fList = (ArrayList<Flight>) obj;
				if (fList.size() < 1) 
				{
					 System.out.println («Нет подходящих рейсов!»);
				} 
				else 
				{
					 System.out.println («Номер рейса, номер рейса, вылет, пункт назначения, дата, время вылета, время прибытия»);
					for (int i = 0; i < fList.size(); i++) 
					{
						Flight flight = fList.get(i);
						System.out.println((i + 1) + "	" 
												+flight.getId() + "	" 
												+flight.getFrom() + "	"
												+flight.getTo() + "	" 
												+flight.getDate() + "	" 
												+flight.getStartTime()+ "		" 
												+flight.getArriveTime());
					}
 
					 System.out.println («Пожалуйста, выберите номер рейса или возврат (0 означает возврат)»);
					Integer nChoice = sc.nextInt();
					if (nChoice != 0) 
					{
						while ((nChoice < 0) || (nChoice > fList.size())) 
						{
							 System.out.println («Ошибка ввода, введите заново!»);
							nChoice = sc.nextInt();
						}
						 // Номер рейса введен правильно, вызываем функцию покупки билета
						bookTicket(socket, nChoice-1);
					} 
					else 
					{
						 // Завершаем этот запрос и возвращаемся в главное меню
					}
				}
			} 
			else 
			{
				 // System.out.println ("Ошибка передачи данных");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
 
	 // предварительное бронирование авиабилета
	public void bookTicket(Socket socket, Integer nChoice) 
	{
		try {
			ObjectOutputStream os;
			os = new ObjectOutputStream(socket.getOutputStream());
			os.writeObject(new Integer(PURCHASE));
			os.writeObject(nChoice);
			
			ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
			Integer result = (Integer) is.readObject();
			if (result == PAY)
			{
				 //Оплата
				 System.out.println ("Пожалуйста, оплатите:");
				Scanner sc = new Scanner(System.in);
				sc.nextLine();
				
				 // Получаем информацию о заказе
				
				Object obj = is.readObject();
				
				if(obj instanceof Ticket)
				{
					Ticket ticket = (Ticket)obj;
					if(ticket != null)
					{
						 System.out.println («Поздравляем, бронирование прошло успешно! Информация о билете есть»);
						 System.out.println ("Номер заказа Номер рейса Дата вылета Пункт отправления Пункт назначения Время вылета по прибытии
 Статус бронирования пассажирского места на время рейса »);
						System.out.print(ticket.getOrderId());
						System.out.print("	"+ticket.getFlight().getId());
						System.out.print("		"+ticket.getFlight().getDate());
						System.out.print("	"+ticket.getFlight().getFrom());
						System.out.print("	"+ticket.getFlight().getTo());
						System.out.print("	"+ticket.getFlight().getStartTime());
						System.out.print("	"+ticket.getFlight().getArriveTime());
						System.out.print("	"+ticket.getPassenger().getUsername());
						System.out.print("	"+ticket.getSeat());
						System.out.println("	"+ticket.getState());
					}
				}
				else
				{
					 System.out.println («Система занята! Бронирование не удалось!»);
				}
			}//if
		} //try
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	public void userInfoManage(Socket socket) {
	}
	
	 / * Запрос исторических записей бронирования * /
	public void queryHistory(Socket socket) 
	{
		try {
			ObjectOutputStream os;
			os = new ObjectOutputStream(socket.getOutputStream());
			os.writeObject(new Integer(QUERYHISTORY));
			
			ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
			@SuppressWarnings("unchecked")
			List<Ticket> tList = (ArrayList<Ticket>) is.readObject();
			if (tList.size()>0)
			{
					 System.out.println ("Номер заказа Номер рейса Дата вылета Пункт отправления Пункт назначения Время вылета Время прибытия	
 
 Статус бронирования номера пассажирского места »);
					for(int i = 0; i < tList.size(); i++)
					{
						Ticket ticket = tList.get(i);
						System.out.print(ticket.getOrderId());
						System.out.print("	"+ticket.getFlight().getId());
						System.out.print("		"+ticket.getFlight().getDate());
						System.out.print("	"+ticket.getFlight().getFrom());
						System.out.print("	"+ticket.getFlight().getTo());
						System.out.print("	"+ticket.getFlight().getStartTime());
						System.out.print("	"+ticket.getFlight().getArriveTime());
						System.out.print("	"+ticket.getPassenger().getUsername());
						System.out.print("	"+ticket.getSeat());
						System.out.println("	"+ticket.getState());
					}
			}
			else
			{
				 System.out.println («У вас еще нет записи о бронировании!»);
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
 
	 / * Выйти из функции, отключиться от сервера * /
	public void exit(Socket socket) 
	{
		ObjectOutputStream os = null;
		try {
			os = new ObjectOutputStream(socket.getOutputStream());
			os.writeObject(new Integer(EXIT));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	 / * Функция входа * /
	public boolean login()
	{
		boolean result = false;
		int flag = -1;
		String username = null;
		String password = null;
		Scanner sc = new Scanner(System.in);
		 System.out.println ("----------- Интерфейс входа в систему продажи билетов Blue Sky ----------");
		while(flag<0)
		{
			 System.out.print («Пожалуйста, введите имя пользователя:»);
			username = sc.nextLine();
			 System.out.print («Пожалуйста, введите пароль:»);
			password = sc.nextLine();
			if(username.trim().equals("") || password.trim().equals(""))
			{
				 System.out.println («Имя пользователя или пароль пуст! Пожалуйста, введите имя пользователя и пароль еще раз:»);
			}
			else
			{
				flag = 1;
			}
		}
		try {
			ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
			os.writeObject(new Integer(LOGIN));
			os.writeObject(username);
			os.writeObject(password);
			
			ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
			Integer nResult = (Integer)is.readObject();
			if(nResult != null)
			{
				if(nResult == ACCEPT)
				{
					result = true;
					 System.out.println («Вход выполнен успешно! Перейти к основному интерфейсу ...»);
				}
				else if (nResult == ISLOGIN)
				{
					 System.out.println («Вы уже вошли в систему и не можете войти снова!»);
				}
				else
				{
					 System.out.println («Неверное имя пользователя или пароль! Пожалуйста, войдите снова»);
				}
			}
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return result;
	}
}