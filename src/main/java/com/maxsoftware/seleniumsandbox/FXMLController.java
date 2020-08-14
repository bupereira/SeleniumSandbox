package com.maxsoftware.seleniumsandbox;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.MenuItem;


public class FXMLController implements Initializable {
    private WebTools webTools;
    private boolean modified;     
    
    @FXML
    private Label label;
    @FXML
    private Button button;
    @FXML
    private ComboBox<String> comboBox;
    @FXML
    private TextField unemploymentRate;
    @FXML
    private TextField unemploymentRateSearchTerms;
    @FXML
    private CheckBox unemploymentCheck ;
    @FXML
    private CheckBox yelpCheck ;
    @FXML
    private TextField searchTerms ;
    @FXML
    private TextField locationText ;
    @FXML
    private TextField numQueries;
    @FXML
    private TableView<YelpBusiness> yelpDataTableView;
    @FXML 
    private TableView<BBBBusiness> bBBDataTableView;
    @FXML
    private TableView<YPBusiness> yPDataTableView;
    @FXML 
    private Button excelBtn;
    @FXML
    private Button loadMoreBtn;
    @FXML
    private TextField consumerConfidence;
    @FXML
    private CheckBox consumerConfidenceCheck;
    @FXML
    private MenuItem updateExcelSpreadsheetMI;
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
        
        try {
            if(unemploymentCheck.isSelected()) {
                label.setText("Fetching unemploymentRate, please wait...");
                //Was if using the viaWeb ->unemploymentRate.setText(webTools.getUnemploymentRate(unemploymentRateSearchTerms.getText())); 
                unemploymentRate.setText(webTools.getUnemploymentRate());
                label.setText("");
            }
            if(yelpCheck.isSelected()) {
                startYelpQuery();
                //TODO see if I canthread this
                startBBBScrape();
                System.out.println("now starting yp scrape");
                startYellowPagesScrape();
                label.setText("");
            }
            
            if(consumerConfidenceCheck.isSelected()) {
                consumerConfidence.setText(webTools.getConsumerConfidence());
            }
        } catch ( Exception e ) {
            System.out.println("Error on controller while fetching data:" + e.getMessage());
        }
    }
    
    
    
    @Override
    public void initialize (URL url, ResourceBundle rb) {
        
        try{
            initCombo();
            this.webTools = new WebTools();
            setYelpColumns();
            setBBBColumns();
            setYPColumns();
        } catch(Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }    
    
    @FXML
    private void yelpKeyTyped(KeyEvent event) {
        if(yelpCheck.isSelected()&&searchTerms.getText().equals("")) {
            yelpCheck.setSelected(false);
        } else if ((!yelpCheck.isSelected())&&(!searchTerms.getText().equals(""))) {
            yelpCheck.setSelected(true);
        }
        if(!this.modified) {
            this.modified = true;
        }
    }
    
    @FXML private void yelpLocationKeyTyped(KeyEvent event) {
        if(!this.modified) {
            this.modified = true;
        }
    }
    
    @FXML
    private void excelAction (ActionEvent event) {
        try {
            webTools.exportToExcel();
        } catch(Exception e) {
            System.out.println("Error exporting to Excel:" + e.getMessage());
        }
    }
    
    @FXML 
    private void updateSpreadsheet (ActionEvent event) {
        webTools.updateSpreadsheet();
    }
    
    public void initCombo() {
        try {
          ObservableList<String> comboOptions = FXCollections.observableArrayList("Brasil", "USA");  
          comboBox.setItems(comboOptions);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    public void setYelpColumns() {
        yelpDataTableView.getColumns().clear(); // reset the tableView
        String[] headers = webTools.getYelpHeaders().split(",");
        String[] fields = webTools.getYelpFields().split(",");
        for (int i = 0; i < headers.length; i++) {
            TableColumn column = new TableColumn(headers[i]);
            // This next line maps the fields to the headers, since they don't have the same names
            // column.setCellValueFactory(new PropertyValueFactory<>(fields[i]));
            // Just out of curiosity, used to be 
            // column.setCellValueFactory(new PropertyValueFactory<YelpBusiness,String>(fields[i]));
            if(headers[i].equals("Transactions")) { // Transactions has 2 sub-fields: delivery and pickup
                TableColumn tDelivery = new TableColumn("Delivery");
                tDelivery.setCellValueFactory(new PropertyValueFactory<>("delivery"));
                tDelivery.setStyle("-fx-alignment: CENTER;");
                TableColumn tPickup = new TableColumn("Pickup");
                tPickup.setStyle("-fx-alignment: CENTER;");
                tPickup.setCellValueFactory(new PropertyValueFactory<>("pickup"));
                column.getColumns().addAll(tDelivery, tPickup);
            } else {
                column.setCellValueFactory(new PropertyValueFactory<>(fields[i]));
                if(headers[i].equals("Closed?")) {
                    column.setStyle("-fx-alignment: CENTER;");
                }
            }
            yelpDataTableView.getColumns().add(column);
        }
    }
    
    public void setBBBColumns() {
        String fields[] = webTools.getBBBFields().split(",");
        int i = 0;
        for (TableColumn column : bBBDataTableView.getColumns()) {
            column.setCellValueFactory(new PropertyValueFactory<>(fields[i]));
            i++;
        }
    }
    
    public void setYPColumns() {
        String fields[] = webTools.getYPFields().split(",");
        int i = 0;
        for (TableColumn column : yPDataTableView.getColumns()) {
            column.setCellValueFactory(new PropertyValueFactory<>(fields[i]));
            i++;
        }
    }
    
    private void startYelpQuery() throws Exception {
        if (this.modified) {
            this.clearCurrentResults();
        }
        label.setText("Now Querying Yelp businesses, please wait...");
        webTools.getYelpRatings(searchTerms.getText(), locationText.getText(), Integer.valueOf(numQueries.getText())); // Should print the return records
        label.setText("Adding results to TableView, please wait...");
        yelpDataTableView.setItems(webTools.getYelpResults());
        label.setText("");
    }
    
    
    private void startBBBScrape() throws Exception {
        //label.setText("Now Querying BBB businesses, please wait...");
        // WAS a void method. Now goes straight into setItems - webTools.getBBBRatings(searchTerms.getText(), locationText.getText(), Integer.valueOf(numQueries.getText()));
        label.setText("Now Querying BBB businesses, Adding results to TableView, please wait...");
        
        bBBDataTableView.setItems(webTools.getBBBRatings(searchTerms.getText(), locationText.getText(), Integer.valueOf(numQueries.getText())));
        
    }
    
    private void startYellowPagesScrape() throws Exception {
        label.setText("Now Querying Yellow Pages businesses, Adding results to TableView, please wait...");
        yPDataTableView.setItems(webTools.getYPRatings(searchTerms.getText(), locationText.getText(), Integer.valueOf(numQueries.getText())));
    }
    
    public void closeInstances() {
        System.out.println("Webtools.closeInstances running.");
        webTools.destroy();
        System.out.println("After webtools");
    }
    
    public void clearCurrentResults() {
        webTools.clearCurrentResults();
    }
}
