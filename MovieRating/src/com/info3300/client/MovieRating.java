package com.info3300.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class MovieRating implements EntryPoint {

	private FlexTable moviesFlexTable = new FlexTable();
	private FlexTable topFiveFlexTable = new FlexTable();
	private FlexTable categoryFlexTable = new FlexTable();
	private DockPanel superTable = new DockPanel();
	private VerticalPanel leftPanel = new VerticalPanel();
	private VerticalPanel rightPanel = new VerticalPanel();
	private Label notEnoughMoviesLabel = new Label();
	private ArrayList<String[]> movies = new ArrayList<String[]>();

	private HorizontalPanel addCategoryPanel = new HorizontalPanel();
	private HorizontalPanel addTitlePanel = new HorizontalPanel();
	private HorizontalPanel addRatingPanel = new HorizontalPanel();
	private TextBox newMovieCategoryTextBox = new TextBox();
	private TextBox newMovieTitleTextBox = new TextBox();
	private TextBox newMovieRatingTextBox = new TextBox();
	private Label newMovieCategoryLabel = new Label();
	private Label newMovieTitleLabel = new Label();
	private Label newMovieRatingLabel = new Label();
	private Label addMovieErrorLabel = new Label();
	private Label successLabel = new Label();
	private Label movieLabel = new Label();
	private Label topsLabel = new Label();
	private Label catsLabel = new Label();
	
	private Button addMovieButton = new Button("Add");
	
	private Button sortCatAscButton = new Button("Sort");
	private Button sortTitleAscButton = new Button("Sort");
	private Button sortRatAscButton = new Button("Sort");
	
	private compareRatings compareRating = new compareRatings();
	private compareTitles compareTitle = new compareTitles();
	private compareCategories compareCategory = new compareCategories();

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		// Create table for main movie data.
		moviesFlexTable.setText(0, 0, "Category");
		moviesFlexTable.setText(0, 1, "Title");
		moviesFlexTable.setText(0, 2, "Rating");
		moviesFlexTable.setText(0, 3, "Delete");
		
		moviesFlexTable.setWidget(1, 0, sortCatAscButton);
		moviesFlexTable.setWidget(1, 1, sortTitleAscButton);
		moviesFlexTable.setWidget(1, 2, sortRatAscButton);
		sortCatAscButton.addStyleDependentName("sort");
		sortTitleAscButton.addStyleDependentName("sort");
		sortRatAscButton.addStyleDependentName("sort");

		// Add style elements
		moviesFlexTable.setCellPadding(6);
		moviesFlexTable.getRowFormatter().addStyleName(0, "tableHeader");
		moviesFlexTable.addStyleName("flexTable");

		// Assemble Add Movie mini-table.
		addCategoryPanel.add(newMovieCategoryLabel);
		newMovieCategoryLabel.setText("Category: ");
		newMovieCategoryLabel.setStyleName("inputLabel");
		addCategoryPanel.add(newMovieCategoryTextBox);


		addTitlePanel.add(newMovieTitleLabel);
		newMovieTitleLabel.setText("Title: ");
		newMovieTitleLabel.setStyleName("inputLabel");
		addTitlePanel.add(newMovieTitleTextBox);

		addRatingPanel.add(newMovieRatingLabel);
		newMovieRatingLabel.setText("Rating: ");		
		newMovieRatingLabel.setStyleName("inputLabel");
		addRatingPanel.add(newMovieRatingTextBox);

		// Create table for top 5 movie data.
		topFiveFlexTable.setText(0, 0, "Category");
		topFiveFlexTable.setText(0, 1, "Title");
		topFiveFlexTable.setText(0, 2, "Rating");
		
		// Add style elements
		topFiveFlexTable.setCellPadding(6);
		topFiveFlexTable.getRowFormatter().addStyleName(0, "tableHeader");
		topFiveFlexTable.addStyleName("flexTable");
		
		// Create table for movie category summary data.
		categoryFlexTable.setText(0, 0, "Category");
		categoryFlexTable.setText(0, 1, "Top Movie");
		categoryFlexTable.setText(0, 2, "Average Score");

		// Add style elements
		categoryFlexTable.setCellPadding(6);
		categoryFlexTable.getRowFormatter().addStyleName(0, "tableHeader");
		categoryFlexTable.addStyleName("flexTable");
		
		movieLabel.addStyleName("tableTitle");
		topsLabel.addStyleName("tableTitle");
		catsLabel.addStyleName("tableTitle");
		movieLabel.setText("Manage Movies");
		topsLabel.setText("Top 5 Movies");
		catsLabel.setText("Catagory Stats");
		
		successLabel.addStyleName("infoLabel");
		notEnoughMoviesLabel.addStyleName("infoLabel");

		// Put it all together
		leftPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		leftPanel.add(movieLabel);
		leftPanel.add(moviesFlexTable);
		leftPanel.add(successLabel);
		leftPanel.add(addCategoryPanel);
		leftPanel.add(addTitlePanel);
		leftPanel.add(addRatingPanel);
		leftPanel.add(addMovieButton);
		leftPanel.add(addMovieErrorLabel);
		superTable.add(leftPanel, DockPanel.WEST);

		rightPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		rightPanel.add(topsLabel);
		rightPanel.add(topFiveFlexTable);
		rightPanel.add(notEnoughMoviesLabel);
		rightPanel.add(catsLabel);
		rightPanel.add(categoryFlexTable);
		superTable.add(rightPanel, DockPanel.EAST);
		
		superTable.setSpacing(7);

		// Associate the Main panel with the HTML host page.
		RootPanel.get("movieRating").add(superTable);    

		// Preload table, and set up extra tables
		this.preload1();

		// Move cursor focus to the top input box.
		newMovieCategoryTextBox.setFocus(true);

		// Listen for mouse events on the Add button.
		addMovieButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addMovie();
			}
		});

		// Listen for keyboard events in the input boxes.
		newMovieCategoryTextBox.addKeyDownHandler(new KeyDownHandler() {
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					addMovie();
				}
			}
		});

		newMovieTitleTextBox.addKeyDownHandler(new KeyDownHandler() {
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					addMovie();
				}
			}
		});

		newMovieRatingTextBox.addKeyDownHandler(new KeyDownHandler() {
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					addMovie();
				}
			}
		});
		
		sortCatAscButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				sortCatAsc();
			}
		});
		
		sortTitleAscButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				sortTitleAsc();
			}
		});
		
		sortRatAscButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				sortRatAsc();
			}
		});

	}



	/**
	 * 
	 * 
	 */
	private void addMovie() {
		final String category = newMovieCategoryTextBox.getText().trim();
		final String title = newMovieTitleTextBox.getText().trim();
		final String rating = newMovieRatingTextBox.getText().trim();
		newMovieCategoryTextBox.setFocus(true);

		// Movie category must be between 1 and 20 chars that are numbers, letters, or spaces.
		if (!category.matches("^[0-9A-Za-z :,-]{1,20}$")) {
			Window.alert("'" + category + "' is not a valid category.");
			newMovieCategoryTextBox.selectAll();
			return;
		}

		// Movie title must be between 1 and 45 chars that are numbers, letters, or spaces.
		if (!title.matches("^[0-9A-Za-z :,-]{1,45}$")) {
			Window.alert("'" + title + "' is not a valid title.");
			newMovieTitleTextBox.selectAll();
			return;
		}

		// Movie rating must be a number between 0 and 10.
		if ( !rating.equals("1") && !rating.equals("2") && !rating.equals("3") && !rating.equals("4") &&
				!rating.equals("5") && !rating.equals("6") && !rating.equals("7") && !rating.equals("8") &&
				!rating.equals("9") && !rating.equals("10") && !rating.equals("0")) {
			Window.alert("'" + rating + "' is not a valid rating.");
			newMovieRatingTextBox.selectAll();
			return;
		}

		newMovieCategoryTextBox.setText("");
		newMovieTitleTextBox.setText("");
		newMovieRatingTextBox.setText("");

		// Don't add the movie if it's already in the table.
		for (String[] movie: movies) {
			if ((movie[0].equalsIgnoreCase(category)) && (movie[1].equalsIgnoreCase(title))) {
				Window.alert("A '" + category + "' movie titled '" + title + "' is already listed.");
				return;
			}
		}

		// Add the movie to the table.
		int row = moviesFlexTable.getRowCount();
		final String[] newMovie = {category, title, rating};
		movies.add(newMovie);
		moviesFlexTable.setText(row, 0, category);
		moviesFlexTable.setText(row, 1, title);
		moviesFlexTable.setText(row, 2, rating);

		Button removeMovieButton = new Button("x");
		removeMovieButton.addStyleDependentName("delete");
		removeMovieButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				int removedIndex = movies.indexOf(newMovie);
				movies.remove(removedIndex);       
				moviesFlexTable.removeRow(removedIndex + 2);
				
				// update dependent tables
				setTopFive();
				setCatAvgs();
				resetMainTable();
			}
		});
		moviesFlexTable.setWidget(row, 3, removeMovieButton);
		
		// update dependent tables
		setTopFive();
		setCatAvgs();

	}

	/**
	 * 
	 * @param movie
	 * @return
	 */
	private int addMovieFromArray(String[] movieIn) {
		final String[] movie = movieIn;

		// Must have all 3 entries
		if (movie.length != 3) {
			return 0;
		}

		// Movie category must be between 1 and 20 chars that are numbers, letters, or spaces.
		if ((movie[0].length() == 0) || (!movie[0].matches("^[0-9A-Za-z :,-]{1,20}$"))) {
			return 0;
		}

		// Movie title must be between 1 and 45 chars that are numbers, letters, or spaces.
		if ((movie[1].length() == 0) || (!movie[1].matches("^[0-9A-Za-z :,-]{1,45}$"))) {
			return 0;
		}

		// Movie rating must be a number between 0 and 10.
		if ((movie[2].length() == 0) || ( !movie[2].equals("1") && !movie[2].equals("2") && !movie[2].equals("3") && !movie[2].equals("4") &&
				!movie[2].equals("5") && !movie[2].equals("6") && !movie[2].equals("7") && !movie[2].equals("8") &&
				!movie[2].equals("9") && !movie[2].equals("10") && !movie[2].equals("0"))) {
			return 0;
		}

		newMovieCategoryTextBox.setText("");
		newMovieTitleTextBox.setText("");
		newMovieRatingTextBox.setText("");

		// Don't add the movie if it's already in the table.
		for (String[] oldMovie: movies) {
			if ((oldMovie[0].equalsIgnoreCase(movie[0])) && (oldMovie[1].equalsIgnoreCase(movie[1]))) {
				return 0;
			}
		}

		// Add the movie to the table.
		int row = moviesFlexTable.getRowCount();
		movies.add(movie);
		moviesFlexTable.setText(row, 0, movie[0]);
		moviesFlexTable.setText(row, 1, movie[1]);
		moviesFlexTable.setText(row, 2, movie[2]);

		Button removeMovieButton = new Button("x");
		removeMovieButton.addStyleDependentName("delete");
		removeMovieButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				int removedIndex = movies.indexOf(movie);  
				moviesFlexTable.removeRow(removedIndex + 2);
				movies.remove(removedIndex);     
				
				// update dependent tables
				setTopFive();
				setCatAvgs();
				resetMainTable();
			}
		});
		moviesFlexTable.setWidget(row, 3, removeMovieButton);

		// update dependent tables
		setTopFive();
		setCatAvgs();
		
		return 1;		
	}

	
	/**
	 * A set of movies to be pre-loaded into the system.
	 * Only 4 out of 6 are formatted properly to load. 
	 * 
	 */
	private void preload1 () {
		String[] movie1 = {"Fantasy", "Lord of the Rings", "9"};
		String[] movie2 = {"Romance", "The Notebook", "7"};
		String[] movie3 = {"Sci-Fi", "Star Trek", "7"};
		String[] movie4 = {"Fantasy", "The Princess Bride", "8"};
		String[] movie5 = {"", "Exception?", "0"};
		String[] movie6 = {};
		String[] movie7 = {"Comedy", "Little Miss Sunshine", "10"};
		String[] movie8 = {"Drama", "Little Miss Sunshine", "8"};

		int actual = 0;
		actual += this.addMovieFromArray(movie1);
		actual += this.addMovieFromArray(movie2);
		actual += this.addMovieFromArray(movie3);
		actual += this.addMovieFromArray(movie4);
		actual += this.addMovieFromArray(movie5);
		actual += this.addMovieFromArray(movie6);
		actual += this.addMovieFromArray(movie7);
		actual += this.addMovieFromArray(movie8);
		

		successLabel.setText(actual + " of 8 movies were loaded at launch.");	
	}
	

	/**
	 * Update the top 5 movies tables
	 * 
	 */
	private void setTopFive() {
		ArrayList<String[]> top5Sorted = movies;
		Collections.sort(top5Sorted, compareTitle);
		Collections.sort(top5Sorted, compareRating);
		
		for (int i = 0; i<5; i++) {
			if ((top5Sorted.size()) > i) {
				topFiveFlexTable.setText(i+1, 0, top5Sorted.get(i)[0]);
				topFiveFlexTable.setText(i+1, 1, top5Sorted.get(i)[1]);
				topFiveFlexTable.setText(i+1, 2, top5Sorted.get(i)[2]);
			} else {
				if ((topFiveFlexTable.isCellPresent(i+1,0))) {
					topFiveFlexTable.removeRow(i+1);
				}
			}
		}
		
		if (top5Sorted.size() < 5) {
			notEnoughMoviesLabel.setText("Only " + top5Sorted.size() + 
					" movie(s) have been rated.");
		} else {
			notEnoughMoviesLabel.setText("");
		}
		
	}
	
	/**
	 * Update the category stats table. 
	 * 
	 */
	private void setCatAvgs() {
		ArrayList<String[]> movieList = movies;	
		Collections.sort(movieList, compareTitle);
		ArrayList<String> catList = new ArrayList<String>();		
		
		// what are the categories?
		for (String[] movie: movieList) {
			if (!catList.contains(movie[0].toUpperCase())) {
				catList.add(movie[0].toUpperCase());
			}
		}
		
		Double[] catAvgs = new Double[catList.size()];
		String[] catTop = new String[catList.size()];
		for (int i = 0; i < catList.size(); i++) {
			ArrayList<Double> scores = new ArrayList<Double>();
			String top = "";
			for (String[] movie: movieList) {
				if (catList.get(i).equalsIgnoreCase(movie[0])) {
					Double score = Double.parseDouble(movie[2]);
					scores.add(score);
					if (score == Collections.max(scores)) {
						top = movie[1];
					}
				}	
			}
			catTop[i] = top;
			Double sum = 0.0;
			for (Double score: scores) {
				sum += score;
			}
			catAvgs[i] = sum/scores.size();
		}
		categoryFlexTable.removeAllRows();
		categoryFlexTable.setText(0, 0, "Category");
		categoryFlexTable.setText(0, 1, "Top Movie");
		categoryFlexTable.setText(0, 2, "Average Score");
		for (int i = 0; i<catList.size(); i++) {
			categoryFlexTable.setText(i+1, 0, catList.get(i));
			categoryFlexTable.setText(i+1, 1, catTop[i]);
			categoryFlexTable.setText(i+1, 2, catAvgs[i].toString());
		}		
		
		categoryFlexTable.setCellPadding(6);
		categoryFlexTable.getRowFormatter().addStyleName(0, "tableHeader");
	}
	
	
	/**
	 * Sort movies by title 
	 */
	public void sortTitleAsc() {
		successLabel.setText("Sorting...");
		Collections.sort(movies, compareTitle);
		resetMainTable();
		successLabel.setText("Sorted.");
	}
	
	/**
	 * Sort movies by category
	 */
	public void sortCatAsc() {
		successLabel.setText("Sorting...");
		Collections.sort(movies, compareCategory);
		resetMainTable();
		successLabel.setText("Sorted.");
	}
	
	/**
	 * Sort movies by rating
	 */
	public void sortRatAsc() {
		successLabel.setText("Sorting...");
		Collections.sort(movies, compareRating);
		resetMainTable();
		successLabel.setText("Sorted.");
	}
	
	
	/**
	 * Re-display the main table
	 * 
	 */
	public void resetMainTable() {
		moviesFlexTable.clear();
		moviesFlexTable.setText(0, 0, "Category");
		moviesFlexTable.setText(0, 1, "Title");
		moviesFlexTable.setText(0, 2, "Rating");
		moviesFlexTable.setText(0, 3, "Delete");
		
		moviesFlexTable.setWidget(1, 0, sortCatAscButton);
		moviesFlexTable.setWidget(1, 1, sortTitleAscButton);
		moviesFlexTable.setWidget(1, 2, sortRatAscButton);
		sortCatAscButton.addStyleDependentName("sort");
		sortTitleAscButton.addStyleDependentName("sort");
		sortRatAscButton.addStyleDependentName("sort");
		
		
		moviesFlexTable.setCellPadding(6);

		moviesFlexTable.getRowFormatter().addStyleName(0, "tableHeader");
		
		for (int i = 2; i < movies.size()+2; i++) {
			moviesFlexTable.setText(i, 0, movies.get(i-2)[0]);
			moviesFlexTable.setText(i, 1, movies.get(i-2)[1]);
			moviesFlexTable.setText(i, 2, movies.get(i-2)[2]);
			
			final String[] finalMovie = movies.get(i-2);
			
			Button removeMovieButton = new Button("x");
			removeMovieButton.addStyleDependentName("delete");
			removeMovieButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					int removedIndex = movies.indexOf(finalMovie);  
					moviesFlexTable.removeRow(removedIndex + 2);
					movies.remove(removedIndex);     
					
					// update dependent tables
					setTopFive();
					setCatAvgs();
				}
			});
			moviesFlexTable.setWidget(i, 3, removeMovieButton);
		}
		
		sortCatAscButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				sortCatAsc();
			}
		});
		
		sortTitleAscButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				sortTitleAsc();
			}
		});
		
		sortRatAscButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				sortRatAsc();
			}
		});
	}
	

	/**
	 * For sorting by movie rating.
	 * 
	 * @author jessicakane
	 *
	 */
	public class compareRatings implements Comparator<String[]> {

		@Override
		public int compare(String[] o1, String[] o2) {
			if ((o1.length != 3) || (o1.length != 3)) {
				return 0;
			} else {
				return Integer.parseInt(o2[2]) - Integer.parseInt(o1[2]);
			}
		}
	}

	/**
	 * For sorting by movie title.
	 * 
	 * @author jessicakane
	 *
	 */
	public class compareTitles implements Comparator<String[]> {

		@Override
		public int compare(String[] o1, String[] o2) {
			if ((o1.length != 3) || (o1.length != 3)) {
				return 0;
			} else 
				return o1[1].compareToIgnoreCase(o2[1]);
		}
	}

	/**
	 * For sorting by movie title.
	 * 
	 * @author jessicakane
	 *
	 */
	public class compareCategories implements Comparator<String[]> {

		@Override
		public int compare(String[] o1, String[] o2) {
			if ((o1.length != 3) || (o1.length != 3)) {
				return 0;
			} else 
				return o1[0].compareToIgnoreCase(o2[0]);
		}
	}



}

