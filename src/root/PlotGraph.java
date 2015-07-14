/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//ref: https://docs.oracle.com/javafx/2/swing/jtable-barchart.htm

package root;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 *
 * @author admin
 */
public class PlotGraph extends javax.swing.JFrame {

	public JFXPanel chartFxPanel = new JFXPanel();
	public Expression exprF, exprG;
	public String strF[];
	public String[] functionPlotTitle = new String[]{"Function Plot -  f(x)", "Fixed Point Iteration Function Plot - g(x)"};
	public String[] errorPlotLabel = new String[]{"Newton Raphson", "Secant", "Aitken", "Steffensen"};
	public int functionNumber;
	public double ans;

	public double range; //plot range
	public int sceneWidth; //Scene Width
	public int sceneHeight; //Scene Height
	public ArrayList<ArrayList<RootData>> list = new ArrayList<>(); //ArrayList of (ArrayList of RootData)

	/**
	 * Creates new form PlotGraph
	 * @param option
	 */
	public PlotGraph(int option) {
		initComponents();
		this.setExtendedState(JFrame.MAXIMIZED_BOTH); //Maximized
		
		//add ArrayList to ArrayList of ArrayList
		list.add(RootCalc.raphsonData);
		list.add(RootCalc.secantData);
		list.add(RootCalc.aitkenData);
		list.add(RootCalc.steffensenData);
		
		if (option == 1) {
			errorPlot();
		}
		else if (option == 2) {
			fxPlot();
		}
	}

	public PlotGraph(String str, double ans) {
		initComponents();
		this.setExtendedState(JFrame.MAXIMIZED_BOTH); //Maximized
		
		strF = new String[1]; //create 1 String
		strF[0] = str; //function f(x) string
		this.functionNumber = 1; //number of function plot
		this.ans = ans; //root of equation

		exprF = new ExpressionBuilder(str).variables("x", "e").build();	//create f(x) expression	

		functionPlot(); //plot
	}

	public PlotGraph(String str0, String str1, double ans) {
		initComponents();
		this.setExtendedState(JFrame.MAXIMIZED_BOTH); //Maximized
		
		strF = new String[2]; //create 2 String
		strF[0] = str0; //function f(x) string
		strF[1] = str1; //function g(x) string
		this.functionNumber = 2; //number of function plot
		this.ans = ans; //root of equation

		exprF = new ExpressionBuilder(str0).variables("x", "e").build(); //create f(x) expression	
		exprG = new ExpressionBuilder(str1).variables("x", "e").build(); //create g(x) expression	

		functionPlot(); //plot
	}

	private void functionPlot() {
		range = RootCalc.range;

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				createFunctionPlot();
			}
		});

		setLayout(new FlowLayout()); //set FlowLayout
		add(chartFxPanel); //add to JFrame
	}

	private void createFunctionPlot() {
		//defining the axes
		NumberAxis xAxis = new NumberAxis(); //create NumberAxis for X
		NumberAxis yAxis = new NumberAxis(); //create NumberAxis for y
		xAxis.setLabel("x"); //set X Axis label
		yAxis.setLabel("y"); //set Y Axis label
		
		//Zero Coordinate is not always included
		xAxis.setForceZeroInRange(false);
		yAxis.setForceZeroInRange(false);

		//creating the chart
		LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
		
		//Set Chart and JFrame title
		lineChart.setTitle(functionPlotTitle[functionNumber - 1]);
		this.setTitle(functionPlotTitle[functionNumber - 1]);

		//defining a series
		Series series[] = new Series[2];

		//populating the series with data
		double increment = 0.1;
		for (int num = 0; num < functionNumber; num++) {
			series[num] = new Series(); //instantiate series
			series[num].setName(strF[num]); //set Series Name
			
			for (double i = ans - range; i <= ans + range; i += increment) {
				double value;
				if (num == 0) {
					value = f(i); //get value of f(x) for Y Axis
				} else {
					value = g(i); //get value of g(x) for Y Axis
				}
				series[num].getData().add(new Data(i, value)); //add Data to Series
			}
			
			//add Series to LineChart
			lineChart.getData().add(series[num]);
		}

		lineChart.setCreateSymbols(false); //hide Line Point Symbol
		chartFxPanel.setScene(new Scene(lineChart, this.getWidth(), this.getHeight() - 80)); //create and add scene to JFXPanel
	}
	
	private void errorPlot() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				createErrorPlot();
			}
		});

		setLayout(new FlowLayout()); //set FlowLayout
		add(chartFxPanel); //add to JFrame
	}
	
	private void createErrorPlot() {
		//defining the axes
		NumberAxis xAxis = new NumberAxis(); //create NumberAxis for X
		NumberAxis yAxis = new NumberAxis(); //create NumberAxis for y
		xAxis.setLabel("Iteration"); //set X Axis label
		yAxis.setLabel("Approximation Error (Ea)%"); //set Y Axis label

		//creating the chart
		LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
		
		//Set Chart and JFrame title
		lineChart.setTitle("Approximation Error Plot - (Ea)%");
		this.setTitle("Approximation Error Plot - (Ea)%");

		//defining a series
		Series series[] = new Series[4];

		//populating the series with data
		for (int num = 0; num < 4; num++) {
			series[num] = new Series(); //instantiate series
			series[num].setName(errorPlotLabel[num]); //set Series Name
			
			ArrayList<RootData> rooDataList = list.get(num); //get rootData list			
			for (int i = 0; i < rooDataList.size(); i++) {
				int iteration = rooDataList.get(i).i; //get iteration for X Axis
				double ea = rooDataList.get(i).ea; //get ea for Y Axis
				
				if (Double.isNaN(ea) == false) {
					series[num].getData().add(new Data(iteration, ea)); //add Data to Series
				}
			}
			
			//add Series to LineChart
			lineChart.getData().add(series[num]);
		}

		chartFxPanel.setScene(new Scene(lineChart, this.getWidth(), this.getHeight() - 80)); //create and add scene to JFXPanel
	}
	
	private void fxPlot() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				createFxPlot();
			}
		});

		setLayout(new FlowLayout()); //set FlowLayout
		add(chartFxPanel); //add to JFrame
	}
	
	private void createFxPlot() {
		//defining the axes
		NumberAxis xAxis = new NumberAxis(); //create NumberAxis for X
		NumberAxis yAxis = new NumberAxis(); //create NumberAxis for y
		xAxis.setLabel("Iteration"); //set X Axis label
		yAxis.setLabel("f(x)"); //set Y Axis label

		//creating the chart
		LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
		
		//Set Chart and JFrame title
		lineChart.setTitle("f(x) Plot");
		this.setTitle("f(x) Plot");

		//defining a series
		Series series[] = new Series[4];

		//populating the series with data
		for (int num = 0; num < 4; num++) {
			series[num] = new Series(); //instantiate series
			series[num].setName(errorPlotLabel[num]); //set Series Name
			
			ArrayList<RootData> rooDataList = list.get(num); //get rootData list			
			for (int i = 0; i < rooDataList.size(); i++) {
				int iteration = rooDataList.get(i).i; //get iteration for X Axis
				double ea = rooDataList.get(i).fx; //get fx for Y Axis
				
				if (Double.isNaN(ea) == false) {
					series[num].getData().add(new Data(iteration, ea)); //add Data to Series
				}
			}
			
			//add Series to LineChart
			lineChart.getData().add(series[num]);
		}

		chartFxPanel.setScene(new Scene(lineChart, this.getWidth(), this.getHeight() - 80)); //create and add scene to JFXPanel
	}

	private double f(double x) {
		//Set expression variable
		exprF.setVariable("x", x);
		exprF.setVariable("e", Math.E);

		//Evaluate expression
		double result = exprF.evaluate();
		return result;
	}

	private double g(double x) {
		//Calculate g(x)
		exprG.setVariable("x", x);
		exprG.setVariable("e", Math.E);

		//Evaluate expression
		double result = exprG.evaluate();
		return result;
	}
	
	//http://www.java-tips.org/java-se-tips/java.awt/how-to-capture-screenshot.html
	public void shot(String imgName) {
		try {
			File imagesDir = new File("images");
			// if the directory does not exist, create it
			if (!imagesDir.exists()) {
				imagesDir.mkdir();
			}
			
			Robot robot = new Robot();
			// Capture the screen shot of the area of the screen defined by the rectangle
			BufferedImage bi=robot.createScreenCapture(new Rectangle(0, 60, this.getWidth()-20, this.getHeight() - 80));
			String fileName = "images/" + imgName + ".png";
			ImageIO.write(bi, "png", new File(fileName));
			
			//Add notification
			txtNotif.setText("Saved!");
		} catch (AWTException | IOException ex) {
			Logger.getLogger(PlotGraph.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnSave = new javax.swing.JButton();
        txtImgName = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        txtNotif = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Plot");

        btnSave.setText("Save Plot as Image");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        txtImgName.setText("img01");
        txtImgName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtImgNameActionPerformed(evt);
            }
        });

        jLabel1.setText("Image Name: ");

        txtNotif.setForeground(new java.awt.Color(115, 214, 10));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(441, 441, 441)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(txtImgName, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(btnSave)
                .addGap(46, 46, 46)
                .addComponent(txtNotif, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(330, 330, 330))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave)
                    .addComponent(txtImgName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(txtNotif, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(672, Short.MAX_VALUE))
        );

        setSize(new java.awt.Dimension(1382, 744));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
		shot(txtImgName.getText());
    }//GEN-LAST:event_btnSaveActionPerformed

    private void txtImgNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtImgNameActionPerformed
        // TODO add your handling code here:
		btnSave.doClick();
    }//GEN-LAST:event_txtImgNameActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSave;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField txtImgName;
    private javax.swing.JLabel txtNotif;
    // End of variables declaration//GEN-END:variables
}
