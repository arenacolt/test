package hello;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SessionController {

	private final AtomicLong counter = new AtomicLong();
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private List<Session> sessions;
	private List<Reservation> reservs;

	SessionController() throws IOException, ClassNotFoundException {
		
		sessions = new LinkedList<>();
		reservs = new LinkedList<>();
		in = new ObjectInputStream(new FileInputStream("schedule.dat"));
		sessions = (List<Session>) in.readObject();

		File file = new File("reservation.dat");

		if (file.exists()) {
			in = new ObjectInputStream(new FileInputStream(file));
			reservs = (List<Reservation>) in.readObject();
			counter.set(reservs.get(reservs.size() - 1).getNumber());
		}

		in.close();
	}

	@RequestMapping(value = "/schedule", method = RequestMethod.GET)
	public String viewSchedule() throws IOException {

		String result = new String();

		for (int index = 0; index < sessions.size(); index++) {
			result += index + " " + sessions.get(index).getName() + " " + sessions.get(index).getDate().getTime() + " "
					+ sessions.get(index).getCost() + " руб\n";
		}

		return result;

	}

	@RequestMapping(value = "/reservation", method = RequestMethod.GET)
	public String viewReservation() throws IOException {

		String result = new String();

		for (int index = 0; index < reservs.size(); index++) {
			result += " " + reservs.get(index).getNumber() + " " + reservs.get(index).getPlace() + " "
					+ reservs.get(index).getSession().getName() + "\n";
		}

		return result;

	}

	@RequestMapping(value = "/add", method = RequestMethod.GET)
	public String addReservation(@RequestParam(value = "film", defaultValue = "0") int film,
	@RequestParam(value = "place", defaultValue = "0") int place) throws IOException {
		try {

			Session session = sessions.get(film);
			Reservation reserv = new Reservation(counter.incrementAndGet(), place, session);
			Reservation temp = reserv;
			boolean isEmpty = true;

			for (int index = 0; index < reservs.size(); index++) {

				temp = reservs.get(index);
				if (temp.getPlace() == place && temp.getSession().equals(session)) {
					System.out.println("Невозможно забронировать");
					isEmpty = false;
					break;
				}

			}
			if (isEmpty) {
				reservs.add(reserv);

				out = new ObjectOutputStream(new FileOutputStream("reservation.dat"));
				out.writeObject(reservs);

				return "Забронировано место " + reserv.getPlace() + " с номеров бронирования " + reserv.getNumber()
						+ " На фильм :" + reserv.getSession().getName();
			} else
				return " место " + reserv.getPlace() + " На фильм :" + reserv.getSession().getName()
						+ "нельзя забронировать";
		} catch (Exception e) {
			return "Ошибка бронирования ";
		}

		finally {
			in.close();
			out.close();
		}
	}

	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public String viewReservation(@RequestParam(value = "number", defaultValue = "0") int number) throws IOException {
		try {
			Reservation reserv;
			for (int index = 0; index < reservs.size(); index++) {
				reserv = reservs.get(index);
				if (reserv.getNumber() == number)

					return "По номеру " + number + " заказано место " + reserv.getPlace() + " на фильм "
							+ reserv.getSession().getName() + " Время " + reserv.getSession().getDate().getTime();

			}

			return "Ничего не найдено";

		} catch (Exception ex) {
			return "Невозможно выполнить";
		}

	}

	@RequestMapping(value = "/cancel", method = RequestMethod.GET)
	public String cancelReservation(@RequestParam(value = "number", defaultValue = "0") int number) throws IOException {
		try {

			Reservation reserv;
			for (int index = 0; index < reservs.size(); index++) {
				reserv = reservs.get(index);
				if (reserv.getNumber() == number) {

					reservs.remove(index);

					out = new ObjectOutputStream(new FileOutputStream("reservation.dat"));
					out.writeObject(reservs);
					out.close();

					return "Бронирование по номеру " + number + "отменено с  заказаным местом " + reserv.getPlace()
							+ " на фильм " + reserv.getSession().getName() + " Время "
							+ reserv.getSession().getDate().getTime();
				}

			}

			return "Ничего не найдено";

		} catch (Exception ex) {
			return "Невозможно выполнить";
		}

	}

}
