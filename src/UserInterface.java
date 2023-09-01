import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

import ecs100.UI;

public class UserInterface {

	HashMap<String, Station> stations = new HashMap<>();
	HashMap<String, TrainLine> trainLines = new HashMap<>();

	double pressedX = 0;
	double pressedY = 0;
	private String currentStationName = "Wellington";
	private String destinationStationName = "Waikanae";

	public UserInterface() {
		UI.initialise();
		UI.setWindowSize(1500, 750);
		UI.addButton("Clear", () -> {
			this.resetScreen();
		});
//		UI.addButton("Load All Data", () -> {
//			this.loadStationData();
//			this.loadTrainLineData();
//			this.loadServiceData();
//		});
//		UI.addButton("Load Station Data", this::loadStationData);
//		UI.addButton("Load TrainLine Data", this::loadTrainLineData);
//		UI.addButton("Load Service Data", this::loadServiceData);
		UI.addButton("All Stations in Region", this::showAllStations);
		UI.addButton("All Train Lines in Region", this::showAllTrainLines);
		UI.addButton("Show All Info For All Train Lines", this::showAllInfoAllLines);

		UI.addButton("Choose Current Station", this::setCurrentStation);
		UI.addButton("Choose Destination Station", this::setDestinationStation);

		UI.addButton("List All Lines through station", this::listAllLinesThroughStation);

		UI.addButton("Find a route from Current to Destination", this::findARoute);

		this.loadAllData();
		this.resetScreen();
		UI.setMouseListener(this::mouseListener);
	}

	public void findARoute() {
		UI.println("Finding route from " + currentStationName + " to " + destinationStationName);
	}

	public void listAllLinesThroughStation() {
		this.printLinesForStation(currentStationName);
	}

	public void resetScreen() {
		UI.clearText();
		UI.drawImage("data/system-map.png", 0, 0);
	}

	public void mouseListener(String action, double x, double y) {

//		UI.println(action);
		if (action.equals("pressed")) {
			pressedX = x;
			pressedY = y;
//			UI.println("X: " + x);
//			UI.println("y: " + y);

		}
		if (action.equals("released")) {
//			UI.printMessage("TL: " + pressedX + " " + pressedY + " BR: " + x + " " + y);
			UI.printMessage("(x > " + pressedX + ") && (x < " + x + ") && (y > " + pressedY + ") && (y < " + y + ")");

			if ((x > 16.0) && (x < 159.0) && (y > 417.0) && (y < 434.0)) {
//				UI.print(trainLines.get("Johnsonville_Wellington"));
				this.printStationsOnTrainLine(trainLines.get("Johnsonville_Wellington"));
				this.printStationsOnTrainLine(trainLines.get("Wellington_Johnsonville"));

			}
			if ((x > 123.0) && (x < 266.0) && (y > 284.0) && (y < 301.0)) {
				this.printStationsOnTrainLine(trainLines.get("Waikanae_Wellington"));
				this.printStationsOnTrainLine(trainLines.get("Wellington_Waikanae"));
			}

			if ((x > 238.0) && (x < 381.0) && (y > 251.0) && (y < 268.0)) {
				this.printStationsOnTrainLine(trainLines.get("Upper-Hutt_Wellington"));
				this.printStationsOnTrainLine(trainLines.get("Wellington_Upper-Hutt"));
			}

			if ((x > 256.0) && (x < 372.0) && (y > 428.0) && (y < 446.0)) {
				this.printStationsOnTrainLine(trainLines.get("Melling_Wellington"));
				this.printStationsOnTrainLine(trainLines.get("Wellington_Melling"));

			}

			if ((x > 308.0) && (x < 484.0) && (y > 16.0) && (y < 36.0)) {
				this.printStationsOnTrainLine(trainLines.get("Masterton_Wellington"));
				this.printStationsOnTrainLine(trainLines.get("Wellington_Masterton"));
			}

			if ((x > 40.0) && (x < 121.0) && (y > 528.0) && (y < 544.0)) {
//				stations.forEach((k, v) -> {
//					UI.println(k);
//				});
				this.printLinesForStation("Simla-Crescent");

				UI.drawRect(40, 528, 100, 15);
			}

			if ((x > 408.0) && (x < 468.0) && (y > 120.0) && (y < 139.0)) {
				this.printLinesForStation("Carterton");
			}

			if ((x > 248.0) && (x < 394.0) && (y > 656.0) && (y < 679.0)) {
				this.printLinesForStation("Wellington");
			}
		}
	}

	public void printStationsOnTrainLine(TrainLine trainLine) {
		UI.println("Stations on the " + trainLine.getName() + " line");
		trainLine.getStations().forEach((station) -> {
//		trainLines.get(trainLine).getStations().forEach((station) -> {
			UI.println("  " + station.getName());
		});
		UI.println();

	}

	public void printInstructions() {
		UI.println("Click on the train line header to list stations on that line");
	}

	public void printLinesForStation(String stationName) {
		Station s = stations.get(stationName);
		// distance from start?
		UI.println("In Zone " + s.getZone());
		UI.println("On Lines");
		for (TrainLine tl : s.getTrainLines()) {
			UI.println("- " + tl.getName());
			for (int i = 0; i < tl.getStations().size(); i++) {
				UI.print("   " + i + ". ");

				UI.print(tl.getStations().get(i).getName());

				if (tl.getStations().get(i).getName().equals(s.getName())) {
					UI.println(" ***");
				} else {
					UI.println();
				}
			}
		}
	}

	public void loadAllData() {
		this.loadStationData();
		this.loadTrainLineData();
		this.loadServiceData();
	}

	public void showAllInfoAllLines() {
		trainLines.forEach((k, v) -> {
			UI.println(k);
			for (TrainService service : v.getTrainServices()) {
				UI.println("ID: " + service.getTrainID());
				for (Integer time : service.getTimes()) {
					UI.println(time);
				}
//				UI.println("-end of service-");
			}
		});
		UI.println("====");
		UI.println();
	}

	/*
	 * Open the -services.data file for each line and build that data into the train
	 * line
	 *
	 * E.g. Masterton_Wellington-services.data
	 *
	 *
	 * 550 556 602 604 607 610 612 614 616 619 621 623 626 628 630 633 635
	 *
	 * 608 614 620 622 625 628 630 632 634 637 639 641 644 646 648 651 653
	 */
	public void loadServiceData() {

		trainLines.forEach((k, v) -> {
			try {

				Scanner sc = new Scanner(new File("data/" + k + "-services.data"));
				TrainLine tl = trainLines.get(k);

				TrainService tService;

				while (sc.hasNextLine()) {
					tService = new TrainService(v);
					String line = sc.nextLine();
					String[] times = line.split(" ");
//					UI.println("line " + line);
					boolean isFirstStop = false;
					for (int i = 0; i < times.length; i++) {

//						public void addTime(int time, boolean firstStop){

						if (i == 0) {
							isFirstStop = true;
						} else {
							isFirstStop = false;
						}
						tService.addTime(Integer.parseInt(times[i]), isFirstStop);
//						tService.addTime(Integer.parseInt(times[i]), !(i == 0));
					}
					tl.addTrainService(tService);

				}

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
		String dataFile = "data/stations.data";
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
					TrainLine trainLine = trainLines.get(trainLineKey);
					String stationName = sc.next();
					Station s = stations.get(stationName);
					trainLine.addStation(s);

					s.addTrainLine(trainLine);

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

	/**
	 * Ask the user for a station name and assign it to the currentStationName field
	 * Must pass a collection of the names of the stations to getOptionFromList
	 */
	public void setCurrentStation() {
		String name = getOptionFromList("Choose current station", stations.keySet());
		if (name == null) {
			return;
		}
		// Popup
		UI.println("Setting current station to " + name);
		currentStationName = name;
//		displayCurrentValues();
	}

	/**
	 * Ask the user for a destination station name and assign it to the
	 * destinationName field Must pass a collection of the names of the stations to
	 * getOptionFromList
	 */
	public void setDestinationStation() {
		String name = getOptionFromList("Choose destination station", stations.keySet());
		if (name == null) {
			return;
		}
		UI.println("Setting destination station to " + name);
		destinationStationName = name;
//	    displayCurrentValues();
	}

	/**
	 * Ask the user for a subway line and assign it to the currentLineName field
	 * Must pass a collection of the names of the lines to getOptionFromList
	 */
	public void setCurrentLine() {
		String name = getOptionFromList("Choose current subway line", trainLines.keySet());
		if (name == null) {
			return;
		}
		UI.println("Setting current subway line to " + name);
//	    currentLineName = name;
//	    displayCurrentValues();
	}

	//
	/**
	 * Method to get a string from a dialog box with a list of options
	 *
	 * Method written by VUW faculty for COMP102 assignment
	 *
	 * Just used for choosing station without typing.
	 *
	 * Can also select station from UI map (my code)
	 */
	public String getOptionFromList(String question, Collection<String> options) {
		Object[] possibilities = options.toArray();
		Arrays.sort(possibilities);
		return (String) javax.swing.JOptionPane.showInputDialog(UI.getFrame(), question, "",
				javax.swing.JOptionPane.PLAIN_MESSAGE, null, possibilities, possibilities[0].toString());
	}

	public static void main(String[] args) {
		new UserInterface();
	}

}
