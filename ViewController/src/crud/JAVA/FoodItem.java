package crud.JAVA;

import crud.application.DBConnectionFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.logging.Level;

import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;
import oracle.adfmf.util.Utility;
import oracle.adfmf.util.logging.Trace;

public class FoodItem {
    private int id = 0;
    private int fgid = 0;
    private String item;
    private String foodGroup;
    private PropertyChangeSupport _propertyChangeSupport = new PropertyChangeSupport(this);

    public FoodItem() {
        super();
    }
    public FoodItem(String item, String foodGroup) {
         super();
         this.item = item;
         this.foodGroup = foodGroup;
    } 
    public FoodItem(int id, String item) {
         super();
         this.id = id;
         this.item = item;
    }
    public FoodItem(int id, String item, int fgid) {
         super();
         this.id = id;
         this.item = item;
         this.fgid = fgid; 
    } 
    public void setId(int id) {
        int oldId = this.id;
        this.id = id;
        _propertyChangeSupport.firePropertyChange("id", oldId, id);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        _propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        _propertyChangeSupport.removePropertyChangeListener(l);
    }

    public int getId() {
        return id;
    }

    public void setItem(String item) {
        String oldItem = this.item;
        this.item = item;
        _propertyChangeSupport.firePropertyChange("item", oldItem, item);
    }

    public String getItem() {
        return item;
    }
    
    /**
    public void setFoodGroup(String foodGroup) {
        String oldFoodGroup = this.foodGroup;
        this.foodGroup = foodGroup;
        _propertyChangeSupport.firePropertyChange("foodGroup", oldFoodGroup, foodGroup);
    }

    public String getFoodGroup() {
        return foodGroup;
    }
    **/
    
    public void createFoodItem() {
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, FoodItem.class, "createFoodItem", " Add FoodItem to " 
            + " FoodGroup:" + this.fgid);
        
        try {                    
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            int newId = 1;
            ResultSet rs = stmt.executeQuery("SELECT MAX(FID) FROM FOODITEMS");
            rs.beforeFirst();
            if (rs.next()) {
                newId = rs.getInt(1) + 1;
            }            
            String sql = "INSERT INTO FOODITEMS (FID, FGID, ITEM) VALUES (" +
                    newId + "," + this.fgid + ",'')";
            rs = stmt.executeQuery(sql);
            conn.commit();
            rs.close();
            
            this.setId(newId);
            this.setItem("");
            
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, FoodItem.class, "createFoodItem",
                      "CREATED FoodItem ID:  " + newId);
            
        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, FoodItem.class, "createFoodItem",
                      "##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, FoodItem.class, "createFoodItem",
                      "##############Exception:  " + exception.getMessage());

        }   
    }
    
    public void saveToStore() {
        
        String sql = "";        
        try {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, FoodItem.class, "saveToStore", "FoodItem: " 
                + this.item + ", ID:" + this.id + ", FoodGroup:" + this.fgid);
            
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
           
            if (this.item.isEmpty()){
                deleteFromStore(this.id);    
            }
            else {
                ResultSet rs = stmt.executeQuery("SELECT * FROM FOODITEMS WHERE FID = " + this.id);
                rs.beforeFirst();
                if (rs.next()) {
                    sql = "UPDATE FOODITEMS SET ITEM='" + this.getItem() 
                          + "', FGID=" + this.fgid + " WHERE FID=" + this.id;
                } else {
                    sql =
                        "INSERT INTO FOODITEMS (FID,ITEM,FGID) VALUES (" +
                        this.id + ",'" + this.getItem() + "'," + this.fgid + ")";
                }            
            }
            
            int updateCount = stmt.executeUpdate(sql);
            if (updateCount == 0) {
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, FoodItem.class, "saveToStore", "Update Failed!");
            }
            else {
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, FoodItem.class, "saveToStore", "Update PASSED!");
            }            
            conn.commit();
        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, FoodItem.class, "saveToStore",
                      "##############SQL Exception:  " + e.getMessage() + " " + sql);
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, FoodItem.class, "saveToStore",
                      "##############Exception:  " + exception.getMessage());

        }
    }
    
    public void deleteFromStore(Integer id) {
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, FoodItem.class, "deleteFromStore", "FoodItem: " 
            + this.item + ", ID:" + id + ", FoodGroup:" + this.fgid);
        
        try {
            if (this.item.isEmpty()) {
                Connection conn = DBConnectionFactory.getConnection();
                Statement stmt = conn.createStatement();
    
                ResultSet rs = stmt.executeQuery("SELECT * FROM FOODITEMS WHERE FID = " + id);
                rs.beforeFirst();
                if (rs.next()) {
                    String sql = "DELETE FROM FOODITEMS WHERE FID = " + id;
                    int updateCount = stmt.executeUpdate(sql);
                    if (updateCount == 0) {
                        Trace.log(Utility.ApplicationLogger, Level.SEVERE, FoodItem.class, "deleteFromStore",
                                  "Delete Failed!");
                    }
                    else {
                        Trace.log(Utility.ApplicationLogger, Level.SEVERE, FoodItem.class, "deleteFromStore",
                                              "DELETED Food ID: " + id);
                    }
                }
                conn.commit();
            }
        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, FoodItem.class, "deleteFromStore",
                      "##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, FoodItem.class, "deleteFromStore",
                      "##############Exception:  " + exception.getMessage());
        }
    }

    public void setFgid(int fgid) {
        int oldFgid = this.fgid;
        this.fgid = fgid;
        _propertyChangeSupport.firePropertyChange("fgid", oldFgid, fgid);
    }

    public int getFgid() {
        return fgid;
    }
}
