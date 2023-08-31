import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

import ecs100.UI;

public class UserInterface {

	HashMap<String, Station> stations = new HashMap<>();
	HashMap<String, TrainLine> trainLines = new HashMap<>();

	public UserInterface() {
		UI.initialise();
		UI.addButton("Clear", () -> UI.clearText());
		UI.addButton("Load Station Data", this::loadStationData);
		UI.addButton("Load TrainLine Data", this::loadTrainLineData);
		UI.addButton("Load Service Data", this::loadServiceData);
		UI.addButton("Show All Stations", this::showAllStations);
		UI.addButton("Show All Train Lines", this::showAllTrainLines);
	}

	public void loadServiceData() {
//		Masterton_Wellington-services.data

		trainLines.forEach((k, v) -> {
			try {
				Scanner sc = new Scanner(new File("data/" + k + "-services.data"));

				UI.println(sc.next());

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		});

	}

	public void showAllTrainLines() {
		UI.println("All Train Lines");
		UI.println("---");
		trainLines.forEach((k, v) -> {
			UI.println(v.getName());
		});
	}

	public void showAllStations() {
		UI.println("All Stations");
		UI.println("---");
		stations.forEach((k, v) -> {
			UI.println(v.getName() + " in zone " + v.getZone());
		});
	}

	public void loadStationData() {
//		Uncomment for hand in
//		String dataFile = UIFileChooser.open("Choose a file");
//		UI.println(dataFile);
//		./data/stations.data
		String dataFile = "data/stations.data";
//		if (dataFile == null) {
//		return;
//	}

		Scanner sc;

		try {
			sc = new Scanner(new File(dataFile));

			while (sc.hasNextLine()) {
				if (!sc.hasNext()) {
					break;
				}
				String name = sc.next();
				int zone = sc.nextInt();
				double distance = sc.nextDouble();
				stations.put(name, new Station(name, zone, distance));
//				UI.println(name + " " + " zone: " + zone + " / dist: " + distance);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		UI.println("Stations loaded");

	}

	public void loadTrainLineData() {
		String dataFile = "data/train-lines.data";
		Scanner sc;

		try {
			sc = new Scanner(new File(dataFile));

			while (sc.hasNextLine()) {
				if (!sc.hasNext()) {
					break;
				}
				String name = sc.next();
				trainLines.put(name, new TrainLine(name));
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		UI.println("Train Lines Loaded");

		this.loadStationsOnLine();
	}

	public void loadStationsOnLine() {
		trainLines.forEach((trainLineKey, trainLineVal) -> {
			Scanner sc;
			try {
				sc = new Scanner(new File("data/" + trainLineKey + "-stations.data"));
				while (sc.hasNext()) {

					// 1. add station to the trainLine
					TrainLine tl = trainLines.get(trainLineKey);
					String stationName = sc.next();
					Station s = stations.get(stationName);
					tl.addStation(s);

					s.addTrainLine(tl);

//					Station station = stations.get(trainLineVal);
//					station.addTrainLine(trainLineVal);

//					UI.print("**" + trainLineKey + "** ");
//					UI.println(sc.next());
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		});
	}

	public static void main(String[] args) {
		new UserInterface();
	}

}
