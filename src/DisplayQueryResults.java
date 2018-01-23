import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class DisplayQueryResults extends JFrame{
    // JDBC driver and database URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DATABASE_URL = "jdbc:mysql://localhost:3306/employees";
    static final String USERNAME = "root";
    static final String PASSWORD = "jhtp7";

    // default query retrieves all data from employees table
    static final String DEFAULT_QUERY = "SELECT * FROM employees";

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private ResultSetTableModel tableModel;
    private JTable resultTable;
    private JComboBox inputButton;
    private JButton submitButton;
    private JTextField input;
    
    public DisplayQueryResults(){
    	super("Select Query. Press SUBMIT.");

        // create ResultSetTableModel with default JDBC driver, database URL and query
        try{
        	// create TableModel for results of query SELECT * FROM employees
            tableModel = new ResultSetTableModel(JDBC_DRIVER, DATABASE_URL, USERNAME, PASSWORD, DEFAULT_QUERY);

            connection = tableModel.getConnection();
        } // end try
        catch(ClassNotFoundException classNotFound){
        	System.err.println("Failed to load JDBC driver");
        	classNotFound.printStackTrace();
            System.exit(1); // terminate application
        } // end catch
        catch(SQLException sqlException){
        	System.err.println("Not Connected to Database");
        	sqlException.printStackTrace();
            System.exit(1); // terminate application
        } // end catch

        String queries[] = {"Select all employees working in Department SALES",
        		            "Select hourly employees working over 30 hours", 
                            "Select all comission employees in descending order of the comission rate",
                            "Increase base salary by 10% for all base plus comission employees",
                            "If the employee's birthday is in the current month, add a $100 bonus",
                            "For all comission employee whose gross sales over 10000, add $100 bonus", 
                            "Specify a query"};
     
        inputButton = new JComboBox(queries);
        submitButton = new JButton("SUBMIT");
        submitButton.addActionListener(
        		new ActionListener(){
        			public void actionPerformed(ActionEvent event){
        				getTable();
                    } // end method actionPerformed
                } // end anonymous inner class
        ); // end call to addActionListener

        JPanel topPanel = new JPanel();
        input = new JTextField(60);
        input.addActionListener(
        		new ActionListener(){
        			public void actionPerformed(ActionEvent event){
        				// execute query in JTextField
                        try{
                        	String query = input.getText();

                            if(query.substring(0, 6).equalsIgnoreCase("SELECT")){
                            	tableModel.setQuery(query);
                            }
                            else{ 
                            	statement.executeUpdate(query);    
                            }
                        } // end try
                        catch(SQLException sqlException){
                        	sqlException.printStackTrace();
                        } // end catch
                    } // end method actionPerformed
                } // end anonymous inner class
        ); // end call to addActionListener

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new FlowLayout());
        centerPanel.add(new JLabel("Enter query:"));
        centerPanel.add(input);
        topPanel.setLayout(new BorderLayout());
        topPanel.add(inputButton, BorderLayout.NORTH);
        topPanel.add(centerPanel, BorderLayout.CENTER);
        topPanel.add(submitButton, BorderLayout.SOUTH);
 
        resultTable = new JTable(tableModel);
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultTable), BorderLayout.CENTER);      

        getTable();

        setSize(1200, 500);
        setVisible(true);

        // dispose of window when user quits application (this overrides the default of HIDE_ON_CLOSE)
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
    } // end constructor DisplayQueryResult

    private void getTable(){
    	// define each query
        try{
        	int choice = inputButton.getSelectedIndex();
            String query = null;

            switch(choice){
                // select employees from the SALES department
            	case 0: 
            		query = "SELECT * FROM employees WHERE departmentName = 'SALES'";
                    break;
                // select hourly employees working over 30 hours
                case 1: 
                	query = "SELECT * FROM hourlyEmployees WHERE hours >= 30";
                    break;
                // select commission employees in descending order of rate
                case 2: 
                	query = "SELECT * FROM commissionEmployees ORDER BY commissionRate DESC";
                    break;
                // increase base salary of basePlusCommissionEmployees
                case 3: 
                	query = "UPDATE basePlusCommissionEmployees SET baseSalary = baseSalary * 1.1";
                    break;
                // employee birthday is current month, add $100 bonus
                case 4: 
                	addBirthdayBonus();
                    break;
                // add 100 to comissionEmployee gross sales over 10000
                case 5: 
                	query = "UPDATE commissionEmployees SET bonus = bonus + 100.00" +
                			"WHERE grossSales >= 10000";
                    break;
                // user query    
                case 6: 
                	query = input.getText();
                    break;
            } // end switch
           
            statement = connection.createStatement();

            if(query != null){
            	if(query.substring(0, 6).equalsIgnoreCase("SELECT")){
            		tableModel.setQuery(query);
            	}
                else{
                	statement.executeUpdate(query);
                }
            } // end if
        } // end try
        catch(SQLException sqlException){
        	sqlException.printStackTrace();
        } // end catch
    } // end method getTable

    private void addBirthdayBonus(){
    	// current month
        int curMonth = Integer.parseInt(JOptionPane.showInputDialog("Current month: "));

        // validation
        while(!(curMonth >= 1 && curMonth <= 12)){
        	curMonth = Integer.parseInt(JOptionPane.showInputDialog("Current month: "));
        }
        // add $100 bonus to employee whose birthday matches current month
        try{
        	String getEmp = "SELECT * FROM employees";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(getEmp);         
            String bDay; // birthday
            Vector< String > birthdayList = new Vector< String >();
 
            // find employee whose birthday match current month
            while(resultSet.next()){
            	bDay = resultSet.getDate("birthday").toString();
                int month = Integer.parseInt(bDay.substring(5, 7));

                if(month == curMonth){
                	birthdayList.add(resultSet.getString("socialSecurityNumber"));
                    birthdayList.add(resultSet.getString("employeeType") + "s");
                } // end if
            } // end while

            addBonus(birthdayList);
        } // end try
        catch(SQLException exception){
        	exception.printStackTrace();
        } // end catch
    }  // end addBirthdayBonus

    private void addBonus(Vector< String > vector){
    	String socialSecNum; // social security number
    	String empType; // employee type 

        // add bonus to all employees in the vector
        try{
        	// add $100 to each employee listed in the vector
            for(int i = 0; i < vector.size() / 2; i++){
            	socialSecNum = vector.elementAt(i * 2);
            	empType = vector.elementAt(i * 2 + 1);

                // add $100 bonus
                statement = connection.createStatement();
                statement.executeUpdate("UPDATE employeeType SET bonus = bonus + 100.00" +
                		                "WHERE socialSecurityNumber = socialSecurityNumber");
                // display after update
                tableModel.setQuery("SELECT * FROM employeeType");
            } // end for
        } // end try
        catch(SQLException exception){
        	exception.printStackTrace();
        } // end catch
    } // end method addBonus

    public static void main(String args[]){
    	new DisplayQueryResults();
    } // end main
} // end class DisplayQueryResults