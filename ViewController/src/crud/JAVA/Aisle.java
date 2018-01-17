package crud.JAVA;

import crud.application.DBConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import java.util.logging.Level;

import javax.el.ValueExpression;

import oracle.adfmf.dc.bean.ConcreteJavaBeanObject;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;
import oracle.adfmf.java.beans.ProviderChangeListener;
import oracle.adfmf.java.beans.ProviderChangeSupport;
import oracle.adfmf.util.Utility;
import oracle.adfmf.util.logging.Trace;

public class Aisle {    
    private Integer fgid=0;
    private String foodGroup="";
    private List<FoodItem> foodItems = new ArrayList<FoodItem>();
    private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    
    public Aisle() {
        super();
    }

    public void setFgid(Integer id) {
        Integer oldId = this.fgid;
        this.fgid = id;
        propertyChangeSupport.firePropertyChange("id", oldId, id);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }
    public Integer getFgid() {
        return fgid;
    }

    public void setFoodGroup(String foodGroup) {
        String oldFoodGroup = this.foodGroup;
        this.foodGroup = foodGroup;
        propertyChangeSupport.firePropertyChange("foodGroup", oldFoodGroup, foodGroup);
    }

    public String getFoodGroup() {
        return foodGroup;
    }
    
    public void setFoodItems(List<FoodItem> foodItems) {
        List<FoodItem> oldfoodItems = this.foodItems;
        this.foodItems = foodItems;
        propertyChangeSupport.firePropertyChange("foodItems", oldfoodItems, foodItems);
    }

    public List<FoodItem> getFoodItems() {
        
        Utility.ApplicationLogger.severe("Aisle - getFoodItems AISLE# " + this.getFgid());
        //if (foodItems.isEmpty()){
            getFoodItemsFromDB();
        //}                            
        return foodItems;
    }
    
    private List<FoodItem> getFoodItemsFromDB() {
        
        foodItems.clear();
        Integer fgid = 0;
        Integer ct=0;
        
        //Trace.log(Utility.ApplicationLogger, Level.INFO, Aisle.class, "getFoodItemsFromDB", "AISLE# " + this.getFgid());
        Utility.ApplicationLogger.severe("Aisle getFoodItemsFromDB AISLE# " + this.getFgid());
        fgid = this.getFgid();
        
        //ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.fgid}", Integer.class);
        //Object obj1 = ve.getValue(AdfmfJavaUtilities.getAdfELContext());
        //fgid = (Integer)obj1;        
        //Trace.log(Utility.ApplicationLogger, Level.INFO, Aisle.class, "getFoodItemsFromDB", "AISLE# " + fgid);
        
        try {
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM FOODITEMS ";
            sql += "WHERE FGID=" + fgid + " ORDER BY ITEM";
            ResultSet rs = stmt.executeQuery(sql);
            rs.beforeFirst();
            while (rs.next()) {
                int id = rs.getInt("FID");
                String item = rs.getString("ITEM");
                if (item.equals("null")) {
                    item = "";
                }
                //int fgid = rs.getInt("FGID");
                FoodItem f = new FoodItem(id,item,fgid);
                foodItems.add(f);
                ct++;
            }
            Utility.ApplicationLogger.severe("Aisle getFoodItemsFromDB - added fooditems: " + ct);
            
            
        } catch (Exception ex) {
            Utility.ApplicationLogger.severe("getFoodItemsFromDB" + " " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        
        return foodItems;
    }
   
    public List<FoodItem> reloadFoodItems() {
        
        foodItems.clear();
        Utility.ApplicationLogger.info("reloadFoodItems AISLE# " + this.getFgid());
        
        List returnValue = new ArrayList();
        try {
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM FOODITEMS ";
            sql += "WHERE FGID=" + this.getFgid() + " ORDER BY ITEM";
            ResultSet rs = stmt.executeQuery(sql);
            rs.beforeFirst();
            while (rs.next()) {
                int id = rs.getInt("FID");
                String item = rs.getString("ITEM");
                if (item.trim().length()>0) {
                    FoodItem f = new FoodItem(id,item);
                    foodItems.add(f);
                }
            }
            Utility.ApplicationLogger.severe("reloadFoodItems" + " " + foodItems.size());
            List<FoodItem> oldfoodItems = this.foodItems;
            propertyChangeSupport.firePropertyChange("foodItems", oldfoodItems, foodItems);

        } catch (Exception ex) {
            Utility.ApplicationLogger.severe("reloadFoodItems" + " " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        
        return foodItems;
    }
    
    /** Used by CREATE method **/
    
    public void createAisle() {
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Aisle.class, "createAisle"," ");
        
        try {                    
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            int newId = 1;
            ResultSet rs = stmt.executeQuery("SELECT MAX(FGID) FROM AISLES");
            rs.beforeFirst();
            if (rs.next()) {
                newId = rs.getInt(1) + 1;
            }            
            String sql = "INSERT INTO AISLES (FGID, FOODGROUP) VALUES (" +
                    newId + ",'" + this.foodGroup + "')";
            rs = stmt.executeQuery(sql);
            conn.commit();
            rs.close();
            
            this.setFgid(newId);
            this.setFoodGroup("");
            
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Aisle.class, "createAisle",
                      "CREATED Aisle ID:  " + newId);
            
        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Aisle.class, "createAisle",
                      "##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Aisle.class, "createAisle",
                      "##############Exception:  " + exception.getMessage());

        }   
    }
    
    public void addFoodItem(FoodItem f) {
        
        try {            
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            int newId = 1;
            ResultSet rs = stmt.executeQuery("SELECT MAX(FID) FROM FOODITEMS");
            rs.beforeFirst();
            if (rs.next()) {
                newId = rs.getInt(1) + 1;
            }
            f.setId(newId);
            Trace.log(Utility.ApplicationLogger, Level.INFO, AisleService.class, "addFoodItem",
                      "********** ID:" + newId);            
            
        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Aisle.class, "addFoodItem",
                      "##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Aisle.class, "addFoodItem",
                      "##############Exception:  " + exception.getMessage());

        }   
    }
    
    public void saveToStore() {
        
        String sql = "";        
        try {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Aisle.class, "saveToStore", "FoodGroup: "
                            + this.foodGroup + ", ID:"+ this.fgid);
            
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
           
            if (this.foodGroup.isEmpty()){
                deleteFromStore(this.fgid);    
            }
            else {
                ResultSet rs = stmt.executeQuery("SELECT FGID FROM AISLES WHERE FGID=" + this.fgid);
                rs.beforeFirst();
                if (rs.next()) {
                    sql = "UPDATE AISLES SET FOODGROUP='" + this.foodGroup + "' WHERE FGID=" + this.fgid;
                } else {
                    sql = "INSERT INTO AISLES (FGID, FOODGROUP) VALUES (" +
                            this.getFgid() + ",'" + this.getFoodGroup() + "')";
                }            
            }
            
            int updateCount = stmt.executeUpdate(sql);
            if (updateCount == 0) {
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, Aisle.class, "saveToStore", "Update Failed!");
            }
            else {
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, Aisle.class, "saveToStore", "Update PASSED!");
            }            
            conn.commit();
            
        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Aisle.class, "saveToStore",
                      "##############SQL Exception:  " + e.getMessage() + " " + sql);
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Aisle.class, "saveToStore",
                      "##############Exception:  " + exception.getMessage());

        }
    }
    public void reloadFromStore() {
        
        try {
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM AISLES WHERE FGID = " + this.fgid);

            rs.beforeFirst();
            if (rs.next()) {
                this.setFoodGroup(rs.getString("FOODGROUP"));
                this.setFgid(rs.getInt("FGID"));
            }
        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Person.class, "reloadFromStore",
                      "##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Person.class, "reloadFromStore",
                      "##############Exception:  " + exception.getMessage());
        }
    }
    public void deleteFromStore(Integer fgid) {
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Aisle.class, "deleteFromStore", "FoodGroup " 
            + this.foodGroup + ", ID:" +  this.fgid);
        
        try {
            if (this.foodGroup.trim().length()==0) {
                Connection conn = DBConnectionFactory.getConnection();
                Statement stmt = conn.createStatement();
    
                ResultSet rs = stmt.executeQuery("SELECT * FROM AISLES WHERE FGID = " + fgid);
                rs.beforeFirst();
                if (rs.next()) {
                    String sql = "DELETE FROM AISLES WHERE FGID = " + fgid;
                    int updateCount = stmt.executeUpdate(sql);
                    if (updateCount == 0) {
                        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Aisle.class, "deleteFromStore",
                                  "Delete Failed!");
                    }
                    else {
                        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Aisle.class, "deleteFromStore",
                                              "DELETED Food Group ID: " + fgid);
                    }
                }
                conn.commit();
            }
        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Aisle.class, "deleteFromStore",
                      "##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Aisle.class, "deleteFromStore",
                      "##############Exception:  " + exception.getMessage());
        }
    }
    
    public static Comparator<Aisle> FoodGroupComparator = new Comparator<Aisle>() {

            public int compare(Aisle s1, Aisle s2) {
               String fg1 = s1.getFoodGroup().toUpperCase();
               String fg2 = s2.getFoodGroup().toUpperCase();
               //ascending order
               return fg1.compareTo(fg2);
               //descending order
               //return StudentName2.compareTo(StudentName1);
        }};

    /**
    public void setFgid(Integer fgid) {
        Integer oldFgid = this.fgid;
        this.fgid = fgid;
        propertyChangeSupport.firePropertyChange("fgid", oldFgid, fgid);
    }

    public Integer getFgid() {
        return fgid;
    }
    **/
}
