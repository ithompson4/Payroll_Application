import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;

public class AddEmployees extends JFrame{
   // JDBC driver and database URL
   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
   static final String DATABASE_URL = "jdbc:mysql://localhost:3306/employees";
   static final String USERNAME = "root";
   static final String PASSWORD = "jhtp7";

   // default query retrieves all data from employees table
   static final String DEFAULT_QUERY = "SELECT * FROM employees";
   
   private Connection connection;
   private Statement statement;
   private ResultSetTableModel tableModel;
   private JTable table;
   private JButton addEmp;
   private JButton addSalariedEmp;
   private JButton addCommissionEmp;
   private JButton addBasePlusCommissionEmp;
   private JButton addHourlyEmp;
   
   // constructor 
   public AddEmployees(){
	   super("Add Employees");
	
      // create ResultSetTableModel with default JDBC driver, database URL and query
      try{
    	  // create TableModel for results of query SELECT * FROM employees
          tableModel = new ResultSetTableModel(JDBC_DRIVER, DATABASE_URL, USERNAME, PASSWORD, DEFAULT_QUERY);
          connection = tableModel.getConnection();
      } // end try
      catch(ClassNotFoundException classNotFound){
    	  System.err.println("Failed to load JDBC driver.");
    	  classNotFound.printStackTrace();
          System.exit(1); // terminate application
      } // end catch
      catch(SQLException sqlException){
    	  System.err.println("Unable to connect");
    	  sqlException.printStackTrace();
          System.exit(1); // terminate application
      } // end catch

      // set up GUI if connected to database    
      JPanel topPanel = new JPanel();
      addEmp = new JButton("Add Generic Employee");
      addEmp.addActionListener(new ButtonHandler());
      topPanel.add(addEmp);

      // create buttons to add a specific employee type
      addSalariedEmp = new JButton("Add Salaried Employee");
      addSalariedEmp.addActionListener(new ButtonHandler());

      addCommissionEmp = new JButton("Add Commission Employee");
      addCommissionEmp.addActionListener(new ButtonHandler());

      addBasePlusCommissionEmp = new JButton("Add Base Plus Commission Employee");
      addBasePlusCommissionEmp.addActionListener(new ButtonHandler());

      addHourlyEmp = new JButton("Add Hourly Employee");
      addHourlyEmp.addActionListener(new ButtonHandler());

      // add the buttons to centerPanel
      JPanel centerPanel = new JPanel();
      centerPanel.add(addSalariedEmp);
      centerPanel.add(addCommissionEmp);
      centerPanel.add(addBasePlusCommissionEmp);
      centerPanel.add(addHourlyEmp);

      JPanel inputPanel = new JPanel();
      inputPanel.setLayout(new BorderLayout());
      inputPanel.add(topPanel, BorderLayout.NORTH);
      inputPanel.add(centerPanel, BorderLayout.CENTER );

      table = new JTable(tableModel);

      setLayout(new BorderLayout());
      add(inputPanel, BorderLayout.NORTH);
      add(new JScrollPane(table), BorderLayout.CENTER);

      setSize(1200, 500);
      setVisible(true);

      // dispose of window when user quits application (this overrides
      // the default of HIDE_ON_CLOSE)
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      
      // ensure database connection is closed when user quits application
      addWindowListener(
    		  new WindowAdapter(){
    			  // disconnect from database and exit when window has closed
    			  public void windowClosed(WindowEvent event){
    				  tableModel.disconnectFromDatabase();
                      System.exit(0);
                  } // end method windowClosed
              } // end WindowAdapter inner class
      ); // end call to addWindowListener
   } // end AddEmployees constructor

   // add employee to database
   private void addEmployee(String query){
	   try{
		   statement = connection.createStatement();
           statement.executeUpdate(query); 
           tableModel.setQuery(DEFAULT_QUERY);
       } // end try
       catch(SQLException sqlException){
    	   sqlException.printStackTrace();
       } // end catch
   } // end addEmployee

    // inner class ButtonHandler
   private class ButtonHandler implements ActionListener{
	   public void actionPerformed(ActionEvent event){
		   String socialSecurityNumber = JOptionPane.showInputDialog("socialSecurityNumber");
           String insert = "";
           String display = "";

           // add generic employee to table employees
           if(event.getSource() == addEmp){
        	   String firstName = JOptionPane.showInputDialog("First Name");
               String lastName = JOptionPane.showInputDialog("Last Name");
               String birthday = JOptionPane.showInputDialog("Birthday");
               String employeeType = JOptionPane.showInputDialog("Employee Type");
               String department = JOptionPane.showInputDialog("Department Name");
               
               insert = "INSERT INTO employees VALUES ('" + socialSecurityNumber + "', '" + firstName + "', '" +
                                                            lastName + "', '" + birthday + "', '" + employeeType + "', '" + department + "')";
               display = "SELECT socialSecurityNumber, firstName, lastName, birthday, employeeType, departmentName FROM employees";               
          } // end if        
         

          // add salaried employee to table salariedEmployees
          else if(event.getSource() == addSalariedEmp){
        	  double weeklySalary = Double.parseDouble(JOptionPane.showInputDialog("Weekly Salary:"));
        	  insert = "INSERT INTO salariedEmployees VALUES ('" + socialSecurityNumber + "', '" + weeklySalary + "', '0')";
        	  display = "SELECT employees.socialSecurityNumber, employees.firstName, employees.lastName, employees.employeeType, salariedEmployees.weeklySalary " +
                        "FROM employees, salariedEmployees " +
                        "WHERE employees.socialSecurityNumber = salariedEmployees.socialSecurityNumber";
         } // end else if

         // add commission employee to table commissionEmployees
         else if(event.getSource() == addCommissionEmp){
        	 int grossSales = Integer.parseInt(JOptionPane.showInputDialog("Gross Sales:"));
        	 double commissionRate = Double.parseDouble(JOptionPane.showInputDialog("Commission Rate:"));
        	 insert = "INSERT INTO commissionEmployees VALUES ('" + socialSecurityNumber + "', '" + grossSales + "', '" + commissionRate + "', '0' )";
        	 display = "SELECT employees.socialSecurityNumber, employees.firstName, employees.lastName, " + 
                              "employees.employeeType, commissionEmployees.grossSales, commissionEmployees.commissionRate " +
                       "FROM employees, commissionEmployees " +
                       "WHERE employees.socialSecurityNumber = commissionEmployees.socialSecurityNumber";
         } // end else if

         // add base plus commission employee to table basePlusCommissionEmployees
         else if(event.getSource() == addBasePlusCommissionEmp){
        	 int grossSales = Integer.parseInt(JOptionPane.showInputDialog("Gross Sales:"));
             double commissionRate = Double.parseDouble(JOptionPane.showInputDialog("Commission Rate:"));
             double baseSalary = Double.parseDouble(JOptionPane.showInputDialog("Base Salary:"));
             insert = "INSERT INTO basePlusCommissionEmployees VALUES ('" + socialSecurityNumber + "', '" + grossSales + "', '" + commissionRate + "', '" + baseSalary + "', '0')";
             display = "SELECT employees.socialSecurityNumber, employees.firstName, employees.lastName, employees.employeeType, " +
             		          "basePlusCommissionEmployees.baseSalary, basePlusCommissionEmployees.grossSales, " +
             		          "basePlusCommissionEmployees.commissionRate " +
                       "FROM employees, basePlusCommissionEmployees" +
                       "WHERE employees.socialSecurityNumber = basePlusCommissionEmployees.socialSecurityNumber";
         } // end else if

         // add hourly employee to table hourlyEmployees
         else if(event.getSource() == addHourlyEmp){
        	 int hours = Integer.parseInt(JOptionPane.showInputDialog("Hours:"));
             double wage = Double.parseDouble(JOptionPane.showInputDialog("Wage:"));
             insert = "INSERT INTO hourlyEmployees VALUES ('" + socialSecurityNumber + "', '" + hours + "', '" + wage + "', '0')";
             display = "SELECT employees.socialSecurityNumber, employees.firstName, employees.lastName, employees.employeeType, " +
             		          "hourlyEmployees.hours, hourlyEmployees.wage " +
             		   "FROM employees, hourlyEmployees " +
                       "WHERE employees.socialSecurityNumber = hourlyEmployees.socialSecurityNumber";
         } // end else if

         // execute insert query and display employee information
         try{
        	 statement = connection.createStatement();
             statement.executeUpdate(insert);   

             // display the employee info
             tableModel.setQuery(display);
         } // end try
         catch(SQLException SQLException){
        	 JOptionPane.showMessageDialog(null, SQLException.toString());
         } // end catch
      } // end method actionPerformed
   } // end inner class ButtonHandler
   
   public static void main(String[] args){
	   new AddEmployees();
   } // end main
} // end class AddEmployees

