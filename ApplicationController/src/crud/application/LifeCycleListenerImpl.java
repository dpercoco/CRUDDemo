package crud.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import oracle.adfmf.application.LifeCycleListener;
import oracle.adfmf.util.Utility;
import oracle.adfmf.util.logging.Trace;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

/**
 * The application life cycle listener provides the basic structure for developers needing
 * to include their own functionality during the different stages of the application life
 * cycle.  Please note that there is <b>no user</b> associated with any of the following
 * methods.
 *
 * Common examples of functionality that might be added:
 *
 * start:
 *   1. determine if the application has updates
 *   2. determine if there already exists a local application database image
 *   3. setup any one time state for the application
 *
 * activate:
 *   1. read any application cache stores and re-populate state (if needed)
 *   2. establish/re-establish any database connections and cursors
 *   3. process any pending web-service requests
 *   4. obtain any required resources
 *
 * deactivate:
 *   1. write any restorable state to an application cache store
 *   2. close any database cursors and connections
 *   3. defer any pending web-service requests
 *   4. release held resources
 *
 * stop:
 *   1. logoff any remote services
 *
 * NOTE:
 * 1. In order for the system to recognize an application life cycle listener
 *    it must be registered in the maf-application.xml file.
 * 2. Application assemblers must implement this interface if they would like to
 *    receive notification of application start, hibernation, and application resume.
 *    If a secure web service is needed, you will need to do this from your 'default'
 *    feature where you will have an associated user.
 *
 * @see oracle.adfmf.application.LifeCycleListener
 */

public class LifeCycleListenerImpl implements LifeCycleListener {
    public LifeCycleListenerImpl() {
    }

    /**
     * The start method will be called at the start of the application.
     *
     * NOTE:
     * 1. This is a <b>blocking</b> call and will freeze the user interface
     *    while the method is being executed.  If you have any longer running
     *    items you should create a background thread and do the work there.
     * 2. Only the application controller's classes will be available in this
     *    method.
     * 3. At this stage, only an anonymous user is associated with the application
     *    so do not attempt to call any secure web services in this method.
     */
    public void start() {
        
        Trace.log(Utility.ApplicationLogger, Level.INFO, LifeCycleListenerImpl.class, "start",
                  "##############App Start");
        try {
            Connection connection = DBConnectionFactory.getConnection();
            Statement stat = DBConnectionFactory.getConnection().createStatement();
            ResultSet rs = stat.executeQuery("SELECT PERID FROM PEOPLE WHERE PERID = -1;");
            
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "start",
                      "##############Database does not exist, creating it");
          
            InitPeopleDB();            
            InitRecipesDB();
            InitIngredientsDB(); 
            InitAislesDB();
            InitMenusDB();
            InitLoad.loadReferenceTables(1);
            
        } catch (Exception e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "start",
                      "##############Exception:  " + e.getMessage());
        }
        Trace.log(Utility.ApplicationLogger, Level.INFO, LifeCycleListenerImpl.class, "start",
                  "##############App Start Complete");
    }

    /**
     * The stop method will be called at the termination of the application.
     *
     * NOTE:
     * 1. Depending on how the application is being shutdown, this method may
     *    or may not be called. For example, if a user kills the Application from
     *    the iOS multitask UI then stop will not be called.  Because of this, each
     *    feature should save off their state in the deactivate handler.
     * 2. Only the application controller's classes will be available in this
     *    method.
     * 3. At this stage, only an anonymous user is associated with the application
     *    so do not attempt to call any secure web services in this method.
     */
    public void stop() {
        // Add code here...
    }

    /**
     * The activate method will be called when the application is started (post
     * the start method) and when an application is resumed by the operating
     * system.  If the application supports checkpointing, this is a place where
     * the application should read the checkpoint information and resume the process.
     *
     * NOTE:
     * 1. This is a <b>blocking</b> call and will freeze the user interface
     *    while the method is being executed.  If you have any longer running
     *    items you should create a background thread and do the work there.
     * 2. Only the application controller's classes will be available in this
     *    method.
     * 3. At this stage, only an anonymous user is associated with the application
     *    so do not attempt to call any secure web services in this method.
     * 4. Once an application is activated, the visible feature's activate lifecycle
     *    method will be executed (if configured) post this method being called.
     */
    public void activate() {
        // Add code here...
    }

    /**
     * The deactivate method will be called as part of the application shutdown
     * process or when the application is being deactivated/hibernated by the
     * operating system.  This is the place where application developers would
     * write application checkpoint information in either a database or a "device
     * only" file so if the application is terminated while in the background
     * the application can resume the process when the application is reactivated.
     *
     * NOTE:
     * 1. This is a <b>blocking</b> call and will freeze the user interface
     *    while the method is being executed.  If you have any longer running
     *    items you should create a background thread and do the work there.
     * 2. Only the application controller's classes will be available in this
     *    method.
     * 3. At this stage, only an anonymous user is associated with the application
     *    so do not attempt to call any secure web services in this method.
     * 4. When an application is being deactivated, the visible feature's
     *    deactivate lifecycle method will be executed (if configured) prior to
     *    this method being called.
     */
    public void deactivate() {
        // Add code here...
    }

    private void InitPeopleDB() {
        try {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "InitPeopleDB",
                      "##############initDB Start");
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream is = cl.getResourceAsStream(".adf/META-INF/people.sql");
            if (is == null) {
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "InitPeopleDB",
                          "##############Could not look up : /META-INF/people.sql");
                return;
            }

            BufferedReader bReader = new BufferedReader(new InputStreamReader(is));
            List<String> stmts = new ArrayList<String>();
            String strstmt = "";
            String ln = bReader.readLine();
            while (ln != null) {
                if (ln.startsWith("REM") || ln.startsWith("COMMIT")) {
                    ln = bReader.readLine();
                    continue;
                }
                strstmt = strstmt + ln;
                if (strstmt.endsWith(";")) {
                    Trace.log(Utility.ApplicationLogger, Level.INFO, LifeCycleListenerImpl.class, "InitPeopleDB",
                              "##############" + strstmt);
                    stmts.add(strstmt);
                    strstmt = "";
                    ln = bReader.readLine();
                    continue;
                }
                ln = bReader.readLine();
            }

            for (int i = 0; i < stmts.size(); i++) {
                Statement pStmt = DBConnectionFactory.getConnection().createStatement();
                pStmt.executeUpdate((String) stmts.get(i));

            }
            DBConnectionFactory.getConnection().commit();
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "InitPeopleDB",
                      "##############InitPeopleDB Complete");
        } catch (Exception e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "InitPeopleDB",
                      "##############Exception:  " + e.getMessage());
        }

    }
    private void InitRecipesDB() {
        try {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "InitRecipesDB",
                      "##############initDB Start");
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream is = cl.getResourceAsStream(".adf/META-INF/recipes.sql");
            if (is == null) {
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "InitRecipesDB",
                          "##############Could not look up : /META-INF/recipes.sql");
                return;
            }

            BufferedReader bReader = new BufferedReader(new InputStreamReader(is));
            List<String> stmts = new ArrayList<String>();
            String strstmt = "";
            String ln = bReader.readLine();
            while (ln != null) {
                if (ln.startsWith("REM") || ln.startsWith("COMMIT")) {
                    ln = bReader.readLine();
                    continue;
                }
                strstmt = strstmt + ln;
                if (strstmt.endsWith(";")) {
                    Trace.log(Utility.ApplicationLogger, Level.INFO, LifeCycleListenerImpl.class, "InitRecipesDB",
                              "##############" + strstmt);
                    stmts.add(strstmt);
                    strstmt = "";
                    ln = bReader.readLine();
                    continue;
                }
                ln = bReader.readLine();

            }

            for (int i = 0; i < stmts.size(); i++) {
                Statement pStmt = DBConnectionFactory.getConnection().createStatement();
                pStmt.executeUpdate((String) stmts.get(i));

            }
            DBConnectionFactory.getConnection().commit();
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "InitRecipesDB",
                      "##############InitDB Complete");
        } catch (Exception e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "InitRecipesDB",
                      "##############Exception:  " + e.getMessage());
        }

    }
    
    private void InitAislesDB() {
        try {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "InitAislesDB",
                      "##############InitAislesDB Start");
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream is = cl.getResourceAsStream(".adf/META-INF/aisle.sql");
            if (is == null) {
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "InitAislesDB",
                          "##############Could not look up : /META-INF/aisle.sql");
                return;
            }

            BufferedReader bReader = new BufferedReader(new InputStreamReader(is));
            List<String> stmts = new ArrayList<String>();
            String strstmt = "";
            String ln = bReader.readLine();
            while (ln != null) {
                if (ln.startsWith("REM") || ln.startsWith("COMMIT")) {
                    ln = bReader.readLine();
                    continue;
                }
                strstmt = strstmt + ln;
                if (strstmt.endsWith(";")) {
                    Trace.log(Utility.ApplicationLogger, Level.INFO, LifeCycleListenerImpl.class, "InitAislesDB",
                              "##############" + strstmt);
                    stmts.add(strstmt);
                    strstmt = "";
                    ln = bReader.readLine();
                    continue;
                }
                ln = bReader.readLine();

            }

            for (int i = 0; i < stmts.size(); i++) {
                Statement pStmt = DBConnectionFactory.getConnection().createStatement();
                pStmt.executeUpdate((String) stmts.get(i));

            }
            
            DBConnectionFactory.getConnection().commit();
            
            loadAisles_From_XML (); 
            
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "InitAislesDB",
                      "##############InitAislesDB Complete");
        } catch (Exception e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "InitAislesDB",
                      "##############Exception:  " + e.getMessage());
        }

    }
    
    public void loadAisles_From_XML () throws ParserConfigurationException, SAXException, IOException {
        
        Trace.log(Utility.ApplicationLogger, Level.INFO, LifeCycleListenerImpl.class, "loadAisles_From_XML", "");
        
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream is = cl.getResourceAsStream(".adf/META-INF/foodItems.xml");
        
        if (is == null) {
            Utility.ApplicationLogger.severe("loadAisles_From_XML ERROR " + "############## Could not look up : /META-INF/foodItems.xml");
            return;
        }
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder parser = factory.newDocumentBuilder();
        Document dc= parser.parse(is);
        int idx=0;
        int fgid=0;
        
        try {
            Node topNode = dc.getDocumentElement();
            NodeList nodeList = topNode.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node groupNode = nodeList.item(i);
                if (groupNode.getNodeType() == Node.ELEMENT_NODE) {
                        if (groupNode.getNodeName().equals("FoodGroup")) {
                          
                          String foodGroup = groupNode.getAttributes().item(0).getNodeValue();
                          fgid=addAisle(foodGroup);
                         
                          NodeList itemList = groupNode.getChildNodes();
                          for (int j = 0; j < itemList.getLength(); j++) {
                              Node itemNode = itemList.item(j);
                              if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                                  String item = itemNode.getTextContent();
                                  addFoodItem(fgid,item);
                              }
                          }
                     }
                }
            }
        } catch (Exception e) {
            Utility.ApplicationLogger.severe("loadAisles_From_XML EXCEPTION. " +  e.getMessage());
        }  
        Trace.log(Utility.ApplicationLogger, Level.INFO, LifeCycleListenerImpl.class, "loadAisles_From_XML", "Loaded food groups count: " + idx);
        
    }
    
    public Integer addAisle(String foodGroup) {
        
        int FGID = 1;
        try {            
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            
            ResultSet rs = stmt.executeQuery("SELECT FGID FROM AISLES WHERE FOODGROUP = '" + foodGroup + "'");
            rs.beforeFirst();
            if (rs.next()) {
                FGID = rs.getInt(1);   
            }  
            else {
                rs = stmt.executeQuery("SELECT MAX(FGID) FROM AISLES");
                rs.beforeFirst();
                if (rs.next()) {
                    FGID = rs.getInt(1) + 1;
                }
                String sql = "INSERT INTO AISLES (FGID, FOODGROUP) VALUES (" +
                        FGID + ",'" + foodGroup + "')";
                rs = stmt.executeQuery(sql);
                conn.commit();
                rs.close();
            }
            
        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "addAisle",
                      "##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "addAisle",
                      "##############Exception:  " + exception.getMessage());

        } 
        return FGID;
    }
    
    public Integer getAisle(String foodGroup) {
        
        int Id = -1;
        try {            
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT FGID FROM AISLES WHERE FOODGROUP='" + foodGroup + "'");
            rs.beforeFirst();
            if (rs.next()) {
                Id = rs.getInt(1) + 1;
            }
            rs.close();
            
        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "getAisle",
                      "##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "getAisle",
                      "##############Exception:  " + exception.getMessage());

        } 
        return Id;
        
    }
    
    public void addFoodItem(int fgid, String item) {
        
        String sql ="";
        
        try {
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            int newId = 1;
            ResultSet rs = stmt.executeQuery("SELECT MAX(FID) FROM FOODITEMS");
            rs.beforeFirst();
            if (rs.next()) {
                newId = rs.getInt(1) + 1;
            }
            item = item.replaceAll("[^A-Za-z0-9 ]", ""); 
            sql = "INSERT INTO FOODITEMS (FID, FGID, ITEM) VALUES (" +
                    newId + "," + fgid + ",'" + item + "')";
            rs = stmt.executeQuery(sql);
            conn.commit();
            rs.close();
            
        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "addFoodItem",
                      "##############SQL Exception:  " + sql + " - " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "addFoodItem",
                      "##############Exception:  " + exception.getMessage());

        }   
    }
    
    private void InitMenusDB() {
        try {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "InitMenusDB",
                      "##############InitMenusDB Start");
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream is = cl.getResourceAsStream(".adf/META-INF/menu.sql");
            if (is == null) {
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "InitMenusDB",
                          "##############Could not look up : /META-INF/menu.sql");
                return;
            }

            BufferedReader bReader = new BufferedReader(new InputStreamReader(is));
            List<String> stmts = new ArrayList<String>();
            String strstmt = "";
            String ln = bReader.readLine();
            while (ln != null) {
                if (ln.startsWith("REM") || ln.startsWith("COMMIT")) {
                    ln = bReader.readLine();
                    continue;
                }
                strstmt = strstmt + ln;
                if (strstmt.endsWith(";")) {
                    Trace.log(Utility.ApplicationLogger, Level.INFO, LifeCycleListenerImpl.class, "InitMenusDB",
                              "##############" + strstmt);
                    stmts.add(strstmt);
                    strstmt = "";
                    ln = bReader.readLine();
                    continue;
                }
                ln = bReader.readLine();

            }

            for (int i = 0; i < stmts.size(); i++) {
                Statement pStmt = DBConnectionFactory.getConnection().createStatement();
                pStmt.executeUpdate((String) stmts.get(i));

            }
            
            DBConnectionFactory.getConnection().commit();            
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "InitMenusDB",
                      "##############InitMenusDB Complete");
        } catch (Exception e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "InitMenusDB",
                      "##############Exception:  " + e.getMessage());
        }

    }
    private void InitIngredientsDB() {
        try {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "InitIngredientsDB",
                      "##############InitIngredientsDB Start");
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream is = cl.getResourceAsStream(".adf/META-INF/ingredients.sql");
            if (is == null) {
                Trace.log(Utility.ApplicationLogger, Level.INFO, LifeCycleListenerImpl.class, "InitIngredientsDB",
                          "##############Could not look up : /META-INF/ingredients.sql");
                return;
            }

            BufferedReader bReader = new BufferedReader(new InputStreamReader(is));
            List<String> stmts = new ArrayList<String>();
            String strstmt = "";
            String ln = bReader.readLine();
            while (ln != null) {
                if (ln.startsWith("REM") || ln.startsWith("COMMIT")) {
                    ln = bReader.readLine();
                    continue;
                }
                strstmt = strstmt + ln;
                if (strstmt.endsWith(";")) {
                    Trace.log(Utility.ApplicationLogger, Level.INFO, LifeCycleListenerImpl.class, "InitIngredientsDB",
                              "##############" + strstmt);
                    stmts.add(strstmt);
                    strstmt = "";
                    ln = bReader.readLine();
                    continue;
                }
                ln = bReader.readLine();

            }

            for (int i = 0; i < stmts.size(); i++) {
                Statement pStmt = DBConnectionFactory.getConnection().createStatement();
                pStmt.executeUpdate((String) stmts.get(i));

            }
            DBConnectionFactory.getConnection().commit();
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "InitIngredientsDB",
                      "##############InitIngredientsDB Complete");
        } catch (Exception e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, LifeCycleListenerImpl.class, "InitIngredientsDB",
                      "##############Exception:  " + e.getMessage());
        }

    }
}
