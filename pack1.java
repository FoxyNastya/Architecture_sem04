package pack1;
 
import java.io.*;
import java.lang.Thread.State;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
 
import com.dao.FlightManageDAO;
import com.dao.LoginDAO;
import com.dao.TicketManageDAO;
import com.model.Flight;
import com.model.Passenger;
import com.model.Ticket;
/**
   * Сервис-Терминал
 * @author XWZ
 *
 */
 / * Интерфейс постоянного протокола * /
interface Protocal {
	 int QUERY = 1; // Запросить запрос
	 int PURCHASE = 2; // Запрос на покупку билета
	 int ACCEPT = 3; // Подтверждаем
	 int ERROR = 4; // ошибка
	 int EXIT = 5; // Выйти из запроса
	 int PAY = 6; // Запрос на оплату
	 int LOGIN = 7; // Запрос на вход
	 int QUERYHISTORY = 8; // История запросов
	 int ISLOGIN = 9; // Авторизовались
}
 
public class Server {
	static final int PORT = 8888;
	 static int m_count = 0; // Текущее количество подключений
	public Server() {}
 
	public static void main(String[] args) 
	{
		Object object = new Object();
		ServerSocket server_socket = null;
		 // Используем пул потоков, устанавливаем максимальное количество подключений на 100
		ExecutorService threadPool = Executors.newFixedThreadPool(100);
		try 
		{
			server_socket = new ServerSocket(PORT);
			System.out.println("Server is running");
			while (true) 
			{
				Socket client_socket = server_socket.accept();
				m_count = ((ThreadPoolExecutor)threadPool).getActiveCount();
				 System.out.println ("Текущий номер подключения:" + (m_count + 1));
				Thread thread = new Thread(new ProcessThread(client_socket,object));
				threadPool.submit(thread);
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		threadPool.shutdown();
	}
}
 
 / * Класс серверного потока * /
class ProcessThread implements Runnable,Protocal{
	
	 Socket client; // Клиент, подключенный в данный момент
	 BookTicket bt; // Объект бизнес-обработки
 
	ProcessThread() {}
 
	 / * Конструктор * /
	ProcessThread(Socket client,Object object) 
	{
		this.client = client;
		this.bt = new BookTicket(object);
	}
	
	 / * Функция потока * /
	public void run() 
	{
			try 
			{
				System.out.println("get connect from:" + client.getInetAddress() + ",port:" + client.getPort());
				while (onReceive())
					;
				System.out.println ("Клиент:" + client.getInetAddress () + ", порт:" + client.getPort () + "Выход");
				client.close();
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
	}
 
	 // Функция анализа протокола
	public boolean onReceive() {
		 int flag = 0; // Пользователь отслеживает, отключен ли клиент, -1 означает отключение
		try {
			 // Обработка аварийного отключения клиента
			client.sendUrgentData(0); 
			
			ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(client.getInputStream()));
			Integer nChoice = (Integer)is.readObject();
			ObjectOutputStream os = new ObjectOutputStream(client.getOutputStream());
	
			if(nChoice != null) 
			{
				switch (nChoice) 
				{
					case QUERY:
						bt.queryFlight(is,os);
						break;
					case PURCHASE:
						bt.bookTickt(is,os);
						break;
					case LOGIN:
						bt.login(is,os);
						break;
					case QUERYHISTORY:
						bt.queryHistory(is,os);
						break;
					case EXIT:
						bt.logout();
						flag = -1;
						break;
					default:
						flag = -1;
						break;
				}//switch
			}//if
		}//try
		catch (Exception e) 
		{
			flag = -1;
			bt.logout();
			e.printStackTrace();
		}
		
		if((flag < 0) || (client.isClosed()))
		{
			return false;
		}
		return true;
	}
}
 
 / * Класс оформления билетов бизнес-класса * /
class BookTicket implements Protocal{
	 FlightManageDAO fdao; // объект доступа к базе данных
	 TicketManageDAO tdao; // Объект доступа к базе данных билетов
	 LoginDAO ldao; // Вход в объект доступа к базе данных
	 List <Flight> fList = null; // Список рейсов
	 Passengersenger = null; // Текущий пользователь
	
	 static int i = 10; Количество оставшихся голосов, используемых для моделирования одновременных
	 Object object = null; // Общий объект блокировки		
	
	 / * Конструктор * /
	BookTicket(Object object) 
	{
		this.fdao = new FlightManageDAO();
		this.tdao = new TicketManageDAO();
		this.ldao = new LoginDAO();
		this.object = object;
	}
 
	 / * Функция запроса билетов * /
	public void queryFlight(ObjectInputStream is,ObjectOutputStream os) throws ClassNotFoundException, IOException 
	{
   		Object obj1 = is.readObject();
		Object obj2 = is.readObject();
		//Object obj3 = is.readObject();
		
		if (!(obj1 == null) && !(obj2 == null)) 
		{
			String start = (String) obj1;
			String arrival = (String) obj2;
			//String date = (String) obj3;
			fList = fdao.queryFilght(start, arrival);
			os.writeObject(fList);
		}
	}
	
	 / * Функция бронирования билетов * /
	public synchronized void bookTickt(ObjectInputStream is,ObjectOutputStream os) throws IOException, ClassNotFoundException 
	{
		 Integer nChoice = (Integer) is.readObject (); // Получить рейс, выбранный заказчиком
		if(nChoice != null)
		{
			 if (i> 0) Есть еще голоса
			{
				if((fList.size()>nChoice) && (nChoice>=0))
				{
					os.writeObject(new Integer(PAY));
					{
						 // Обработка платежа
					}
					
					 // Формируем заказы
					if((this.passenger != null))
					{
						Flight flight = fList.get(nChoice);
						Ticket ticket = new Ticket(flight, passenger, i--);
						int result = tdao.saveTicketInfo(ticket);
						if(result > 0)
							os.writeObject(ticket);
						else
							os.writeObject(ERROR);
					}
				} 
			}
			else
			{
				os.writeObject(new Integer(ERROR));
			}
		}//if
	}
	
	 / * Запрос истории покупок билетов * /
	public void queryHistory(ObjectInputStream is,ObjectOutputStream os) throws IOException
	{
		List<Ticket> tList =  tdao.queryHistoryTicket(passenger.getUsername());
		os.writeObject(tList);
	}
	
	 / * Функция входа * /
	public void login(ObjectInputStream is,ObjectOutputStream os) throws ClassNotFoundException, IOException
	{
		String username = (String)is.readObject();
		String password = (String)is.readObject();
		if((username != null) && (password != null))
		{
			Passenger p = ldao.checkPermission(username, password);
			if(p != null)
			{
				this.passenger = p;
				if(!p.isLogin())
				{
					os.writeObject(ACCEPT);
				}
				else 
				 {// Вы вошли
					os.writeObject(ISLOGIN);
				}
			}
			else
			 {//Нет такого пользователя
				os.writeObject(ERROR);
			}
		}
		else
		 {// имя пользователя или пароль пустые
			os.writeObject(ERROR);
		}
	}
	
	public void logout()
	{
		ldao.logOut(passenger);
	}
}
 