package crud.JAVA;

import com.asn1c.core.Null;

import crud.application.DBConnectionFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.logging.Level;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.parsers.ParserConfigurationException;

import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;
import oracle.adfmf.java.beans.ProviderChangeListener;
import oracle.adfmf.java.beans.ProviderChangeSupport;
import oracle.adfmf.util.Utility;
import oracle.adfmf.util.logging.Trace;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

public class AisleService {
    
    //private static String foodGroup;
    List<Aisle> aisles = new ArrayList<Aisle>();
    private static Map<String, String> aislesInternal = new HashMap<String, String>();
    private transient ProviderChangeSupport providerChangeSupport = new ProviderChangeSupport(this);
    private static Map<String, String> foodGroups = null;
    private static String recipeFolder="C:\\oracle\\Demo\\CRUDDemo\\.adf\\META-INF";
    private static final String foodGroups_fileName= ".adf/META-INF/foodItems.xls";
    private static final String foodGroups_xmlName= recipeFolder + "\\foodItems.xml";
    private PropertyChangeSupport _propertyChangeSupport = new PropertyChangeSupport(this);

    public AisleService() {
        super();
    }
    
    public void addProviderChangeListener(ProviderChangeListener l) {
        providerChangeSupport.addProviderChangeListener(l);
    }

    public void removeProviderChangeListener(ProviderChangeListener l) {
        providerChangeSupport.removeProviderChangeListener(l);
    }
    
    public List<Aisle> getAisles() {

        if (aisles.isEmpty()) {
             loadFoodGroups_DB_Aisles();
             //loadFoodGroups_DB();
             //providerChangeSupport.fireProviderRefresh("aisles");
        }
        Collections.sort(aisles, Aisle.FoodGroupComparator);
        return aisles;
    }
    
    public void setAisles(List<Aisle> aisles) {
        List<Aisle> oldAisles = this.aisles;
        this.aisles = aisles;
         _propertyChangeSupport.firePropertyChange("aisles", oldAisles, aisles);
    }
    
     
     public Map<String, String> loadFoodGroups() {
         
         if (foodGroups!= null){
             if (foodGroups.size()>0) {
                 //Utility.ApplicationLogger.severe("Load pre-loaded food Groups " + foodGroups.size());
                 return foodGroups;
             }
         }  
         
         foodGroups = loadFoodGroups_DB();
         if (foodGroups.size()>0) {
             Utility.ApplicationLogger.severe("Load DB food Groups " + foodGroups.size());
             return foodGroups;
         } 
         
         //foodGroups = loadFoodGroups_XML(xmlLocation);
         //Utility.ApplicationLogger.severe("Load XML food Groups " + foodGroups.size());
         return foodGroups;
     }
    
    public Map<String, String> getFoodGroups_NOTUSED() {
        
        aislesInternal.clear();
        //Utility.ApplicationLogger.severe("getFoodGroups ");

        int idx=0;
        int fgid=0;
        String foodGroup="", item="";
        try {            
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "SELECT FGID, FOODGROUP FROM AISLES ORDER BY FOODGROUP";
            ResultSet rs = stmt.executeQuery(sql);
            rs.beforeFirst();
            while (rs.next()) {
                foodGroup = rs.getString("FOODGROUP");
                fgid = rs.getInt("FGID");
                Utility.ApplicationLogger.severe("Retrieved food items for: " + foodGroup + "(" + fgid +")"); 
                sql = "SELECT ITEM FROM FOODITEMS WHERE FGID=" + fgid + " ORDER BY ITEM";
                ResultSet rs2 = stmt.executeQuery(sql);
                rs2.beforeFirst();
                while (rs2.next()){
                    item = rs2.getString("ITEM");
                    aislesInternal.put(item,foodGroup);
                    idx++;
                }
            } 
            Utility.ApplicationLogger.severe("Retrieved food groups: " + aislesInternal.size());            
        } 
        
        catch (Exception e) {
           Utility.ApplicationLogger.severe("SQL EXCEPTION. " +  e.getMessage());
         } 
        return aislesInternal; 
        
    }    
    
    public Map<String, String> loadFoodGroups_DB() {

        aislesInternal.clear();

        int idx=0;
        String sql="";
        try {            
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            sql = "SELECT AISLES.FGID, ITEM, FOODGROUP FROM AISLES INNER JOIN FOODITEMS ";
            sql += "ON AISLES.FGID = FOODITEMS.FGID ORDER BY ITEM";
            ResultSet rs = stmt.executeQuery(sql);
            rs.beforeFirst();
            while (rs.next()) {
                Aisle a = new Aisle();
                a.setFoodGroup(rs.getString("FOODGROUP"));
                a.setFgid(rs.getInt("FGID"));
                aislesInternal.put(rs.getString("ITEM"),rs.getString("FOODGROUP"));
                //Utility.ApplicationLogger.severe("item-" + rs.getString("ITEM")+ ", foodgroup-" + rs.getString("FOODGROUP")); 
                idx++;
            } 
            Utility.ApplicationLogger.severe("Retrieved food groups: " + aislesInternal.size()); 
        }
        catch (SQLException eSql) {
            Utility.ApplicationLogger.severe("SQL EXCEPTION. " + eSql.getMessage() + " " + sql);
         }
        catch (Exception e) {
            Utility.ApplicationLogger.severe("EXCEPTION. " + e.getMessage() + " " + sql);
         } 
        return aislesInternal;
    } 
    
    public void loadFoodGroups_DB_Aisles() {

        aisles.clear();
        int idx=0;
        String sql="";
        try {            
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            sql = "SELECT FGID, FOODGROUP FROM AISLES";
            ResultSet rs = stmt.executeQuery(sql);
            rs.beforeFirst();
            while (rs.next()) {
                Aisle a = new Aisle();
                a.setFoodGroup(rs.getString("FOODGROUP"));
                a.setFgid(rs.getInt("FGID"));
                aisles.add(a);
                idx++;
            } 
            Utility.ApplicationLogger.severe("Retrieved food groups: " + aisles.size()); 
        }
        catch (SQLException eSql) {
            Utility.ApplicationLogger.severe("SQL EXCEPTION. " + eSql.getMessage() + " " + sql);
         }
        catch (Exception e) {
            Utility.ApplicationLogger.severe("EXCEPTION. " + e.getMessage() + " " + sql);
         };
    }    
    
    /** Used by CREATE Method **/
    
    public void addAisle(Aisle a) {
        
       try {     
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            int newId = 1;
            ResultSet rs = stmt.executeQuery("SELECT MAX(FGID) FROM AISLES");
            rs.beforeFirst();
            if (rs.next()) {
                newId = rs.getInt(1) + 1;
            }
            a.setFgid(newId);
            a.setFoodGroup("");
            a.setFoodItems(new ArrayList<FoodItem>());
            rs.close();
            
            Utility.ApplicationLogger.info("addAisle ********** ID:" + newId);
            
        } catch (SQLException e) {
            Utility.ApplicationLogger.severe("addAisle ##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Utility.ApplicationLogger.severe("addAisle ##############Exception:  " + exception.getMessage());
        }
    }    
    
    public Integer insertAisle(Aisle a) {
        
        int FGID = 1;
        try {            
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();            
            ResultSet rs = stmt.executeQuery("SELECT FGID FROM AISLES WHERE FOODGROUP = '" + a.getFoodGroup() + "'");
            rs.beforeFirst();
            if (rs.next()) {
                FGID = rs.getInt(1);   
                a.setFgid(FGID);
            } 
            else {
                rs = stmt.executeQuery("SELECT MAX(FGID) FROM AISLES");
                rs.beforeFirst();
                if (rs.next()) {
                    FGID = rs.getInt(1) + 1;
                    a.setFgid(FGID);
                }
                if (!a.getFoodGroup().isEmpty()) {
                    String sql = "INSERT INTO AISLES (FGID, FOODGROUP) VALUES (" +
                            FGID + ",'" + a.getFoodGroup() + "')";
                    rs = stmt.executeQuery(sql);
                    conn.commit();                    
                }
            }
            rs.close();
            
        } catch (SQLException e) {
            Utility.ApplicationLogger.severe("insertAisle ##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Utility.ApplicationLogger.severe("insertAisle ##############Exception:  " + exception.getMessage());
        } 
        return FGID;
        
    }
    
    public void insertFoodItem(FoodItem f) {
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
            
            if (!f.getItem().isEmpty()) {
                String sql = "INSERT INTO FOODITEMS (FID, FGID, ITEM) VALUES (" +
                        f.getId() + "," + f.getFgid() + ",'" + f.getItem() + "')";
                rs = stmt.executeQuery(sql);
                conn.commit();
                rs.close();
            }
            
        } catch (SQLException e) {
            Utility.ApplicationLogger.severe("insertFoodItem #############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Utility.ApplicationLogger.severe("insertFoodItem #############Exception:  " + exception.getMessage());
        }   
    }
    
    /**
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
           trace.log(Utility.ApplicationLogger, Level.SEVERE, AisleService.class, "getAisle",
                      "##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
           trace.log(Utility.ApplicationLogger, Level.SEVERE, AisleService.class, "getAisle",
                      "##############Exception:  " + exception.getMessage());

        } 
        return Id;
        
    }
    **/
    
    public void removeFoodItem( FoodItem f) {
        f.deleteFromStore(f.getId());
    }
    
    public void deleteFoodItem(Integer id) {
        
       Utility.ApplicationLogger.info("deleteFoodItem ##############Exception: FOODITEM ID:  " + id);

        try {
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM FOODITEMS WHERE FID = " + id);
            rs.beforeFirst();
            if (rs.next()) {
                String sql = "DELETE FROM FOODITEMS WHERE FID = " + id;
                int updateCount = stmt.executeUpdate(sql);
                if (updateCount == 0) {
                    Utility.ApplicationLogger.severe("deleteFoodItem ############## Delete Failed! FOODITEM ID:  " + id);
                }
            }
            conn.commit();
            //}
        } catch (SQLException e) {
            Utility.ApplicationLogger.severe("deleteFoodItem ############## Delete Failed! SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Utility.ApplicationLogger.severe("deleteFoodItem ############## Delete Failed! Exception:  " + exception.getMessage());
        }   
    }
    
    public String findFoodGroup (Map<String, String> inFoodGroups, String foodItem) {
        
        String foodGroup="", FOUND_FOOD_ITEM = "";
        foodGroups = inFoodGroups;
        //Utility.ApplicationLogger.severe("foodItem:" + foodItem + ", foodGroups : " + foodGroups.size());
         
        try {    
             String regex=" (" + getSortedFoodItems() + ")(s|es)? ";     
             //Utility.ApplicationLogger.severe("sortedFoodItems : " + regex);
            
             if (foodItem.toLowerCase().contains("frozen")){
                 FOUND_FOOD_ITEM = foodItem;
                 return "Frozen Foods";    
             }
             
             Pattern p = Pattern.compile(regex);
             Matcher m = p.matcher(foodItem.toLowerCase() + " "); 
             while (m.find()) {
                     FOUND_FOOD_ITEM = m.group();
                     String singularfoodItem = singular(m.group());
                     //Utility.ApplicationLogger.severe("singularfoodItem : " + singularfoodItem);
                     if (foodGroups.containsKey(singularfoodItem)){
                        foodGroup = foodGroups.get(singularfoodItem);
                     }
                     else {
                        foodGroup ="Misc";
                     }
             }
             return foodGroup;
         }
         catch(Exception e)     {
             Utility.ApplicationLogger.severe("findFoodGroup: (size " + foodGroups.size() + "), ERROR " + foodItem + " " + e.getMessage());    
         }
         return foodGroup;      
     }
    
    public String findFoodGroup (String foodItem) {
        
        String foodGroup="Misc", FOUND_FOOD_ITEM = "";
        foodGroups = loadFoodGroups();
        
        try {    
             String regex=" (" + getSortedFoodItems() + ")(s|es)? ";             
             if (foodItem.toLowerCase().contains("frozen")){
                 FOUND_FOOD_ITEM = foodItem;
                 return "Frozen Foods";    
             }
             
             Pattern p = Pattern.compile(regex);
             Matcher m = p.matcher(foodItem.toLowerCase() + " "); 
             while (m.find()) {
                     FOUND_FOOD_ITEM = m.group();
                     String singularfoodItem = singular(m.group());
                     if (foodGroups.containsKey(singularfoodItem)){
                         foodGroup = foodGroups.get(singularfoodItem);
                     }
                     else {
                          //foodGroup ="Misc";
                     }
             }
             return foodGroup;
         }
         catch(Exception e)     {
             Utility.ApplicationLogger.severe("findFoodGroup: (size " + foodGroups.size() + "), ERROR " + foodItem + " " + e.getMessage());    
         }
         return foodGroup;      
     }
    
    private static String getSortedFoodItems() {
            
        int ct=0;
        String foodItems="",foodItem="";
        
        try {
            List<Map.Entry<String, String>> list = getSortedFoodItemsMap();
            for (int i = 0; i < list.size(); i++) {
                ct++;
                Map.Entry<String, String> e = list.get(i);
                foodItem = (String) e.getKey();
                foodItem = foodItem.trim().toLowerCase();
                if (ct==1)
                    foodItems = foodItem;
                else
                    foodItems += "|" + foodItem;
            } 
        }
        catch (Exception e) {
             Utility.ApplicationLogger.severe("getSortedFoodItems: ERROR " + foodItem + " " + e.getMessage()); 
        }
        return foodItems;
     }
    
    private static List<Map.Entry<String, String>> getSortedFoodItemsMap() {              
                
           final int size = foodGroups.size();
           //Utility.ApplicationLogger.info("getSortedFoodItemsMap: foodGroups size: " + size); 
           final List<Map.Entry<String, String>> list = new ArrayList<Map.Entry<String, String>>(size);
           list.addAll(foodGroups.entrySet());
           final ValueComparator<String> cmp = new ValueComparator<String>();
           Collections.sort(list, cmp);
           return list;
    } 
    
    private static String singular(String foodItem) {
      
      foodItem = foodItem.trim().replaceAll("[^ A-Za-z1-9%]", "");
      
      String lastChar = foodItem.substring(foodItem.length()-2);
      if (lastChar.equals("es")) {
              foodItem = foodItem.substring(0,foodItem.length()-1);
      }
    
      lastChar = foodItem.substring(foodItem.length()-1);
      if (lastChar.equals("s")) {
              foodItem = foodItem.substring(0,foodItem.length()-1);
      }
      return foodItem.toLowerCase();
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
         _propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
         _propertyChangeSupport.removePropertyChangeListener(l);
    }
    /**
    public void setFoodGroup(String foodGroup) {
        String oldFoodGroup = AisleService.foodGroup;
        AisleService.foodGroup = foodGroup;
        _propertyChangeSupport.firePropertyChange("foodGroup", oldFoodGroup, foodGroup);
    }

    public static String getFoodGroup() {
        return foodGroup;
    }
    **/
}

