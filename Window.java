import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.application.Platform;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import javafx.scene.control.Label;

import javafx.geometry.Insets;
import javafx.scene.text.Font;

import javafx.scene.paint.*;
import javafx.scene.canvas.*;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;

import java.io.*;


public class Window extends Application {
	public static final int X = 4;
	public static final int Y = 4;
	private char[][] grid = new char[X][Y];
	private GraphicsContext gc;
	private Coords[] currentWord;

	private void generateGrid() {
		String abc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random rd = new Random();
		for (int x = 0; x < X; x++) {
			for (int y = 0; y < Y; y++) {
				grid[x][y] = abc.charAt(rd.nextInt(abc.length()));
			}
		}
	}

	@Override
	public void start(Stage primaryStage) {
		generateGrid();

		GridPane pane = new GridPane();
		pane.setPadding(new Insets(27));

		for (int x = 0; x < X; x++) {
			for (int y = 0; y < Y; y++) {
				Label label = new Label(grid[x][y] + "");
				GridPane.setConstraints(label, x, y);
				label.setFont(new Font(30));
				label.setPadding(new Insets(15, 25, 15, 25));
				pane.getChildren().add(label);
			}
		}

		StackPane root = new StackPane();
		root.getChildren().add(pane);

		final Canvas canvas = new Canvas(331,331);
		gc = canvas.getGraphicsContext2D();
		gc.setStroke(Color.RED);
		gc.setLineWidth(3.0);

		root.getChildren().add(canvas);

		Scene scene = new Scene(root, 331, 331);

		primaryStage.setTitle("Boggle");
		primaryStage.setScene(scene);

		primaryStage.show();

		startSearch();
	}

	public void startSearch() {
		Task<Void> runTask = new Task<Void>() {
			@Override
			public Void call() throws Exception {
				BufferedReader br = new BufferedReader(new FileReader("dict.txt"));
				String line;
				while((line = br.readLine()) != null) {
					line = line.toUpperCase();

					currentWord = findWord(line);
					if (currentWord != null) {
						Platform.runLater( () -> updateDisplay() );
						Thread.sleep(2000);
					}
				}

				br.close();

				return null;
			}
		};

		Thread th = new Thread(runTask);
		th.setDaemon(true);
		th.start();
	}

	public void updateDisplay() {
		gc.clearRect(0, 0, 400, 400);
		if (currentWord == null) {
			return;
		}
		for (int i = 1; i <= currentWord.length-1; i++) {
			Coords l = currentWord[i-1];
			Coords c = currentWord[i];

			int base = 60;
			int mult = 69;

			gc.strokeLine(
				base + l.x*mult,
				base + l.y*mult,
				base + c.x*mult,
				base + c.y*mult
			);
		}
	}

	public Coords[] findWord(String word) {
		List<Coords> options = findLetter(word.charAt(0));
		
		for (int i = 0; i < options.size(); i++) {
			List<Coords> chain = new ArrayList<Coords>();

			chain.add(options.get(i));

			chain = findChain(chain, word, 1);

			if (chain != null && chain.size() == word.length()) {
				Coords[] r = new Coords[chain.size()];
				return chain.toArray(r);
			}
		}

		return null;
	}

	public List<Coords> findChain(List<Coords> chain, String word, int c) {
		if (c >= word.length()) {
			return chain;
		}

		List<Coords> options = findNext(chain, word.charAt(c));
		c++;

		for (int i = 0; i < options.size(); i++) {
			List<Coords> next = new ArrayList<Coords>(chain);
			next.add(options.get(i));

			next = findChain(next, word, c);
			if (next != null) {
				return next;
			}
		}

		return null;
	}

	public List<Coords> findLetter(char letter) {
		List<Coords> options = new ArrayList<Coords>();

		for (int x = 0; x < X; x++) {
			for (int y = 0; y < Y; y++) {
				if (grid[x][y] == letter) {
					options.add(new Coords(x, y));
				}
			}
		}

		return options;
	}

	public List<Coords> findNext(List<Coords> chain, char letter) {
		Coords last = chain.get(chain.size()-1);

		List<Coords> options = new ArrayList<Coords>();

		for (int x = last.x-1; x < last.x+1; x++) {
			if (x < 0 || x > X) continue;

			for (int y = last.y-1; y < last.y+1; y++) {
				if (y < 0 || y > Y) continue;
				if (x == last.x && y == last.y) continue;

				if (grid[x][y] == letter) {
					Coords next = new Coords(x, y);

					// TODO: check if the coordinates are already in the chain

					options.add(next);
				}
			}
		}
		return options;
	}
}
