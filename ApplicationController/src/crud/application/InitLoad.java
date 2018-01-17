package crud.application;

import java.io.FileInputStream;
import java.io.InputStream;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import oracle.adfmf.util.Utility;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class InitLoad {
    
    //FOUND under 'Application Resources', 'Descriptors'
    private static String recipeFolder="C:\\oracle\\Demo\\CRUDDemo\\.adf\\META-INF";
    private static final String foodGroups_fileName= ".adf/META-INF/foodItems.xml";
    private static final String foodGroups_xmlName= recipeFolder + "\\foodItems.xml";
        
    public InitLoad() {
        super();
    }
    
    public static void loadReferenceTables (Integer xmlLocation) {
        
        Utility.ApplicationLogger.info("****** Loading FOOD GROUPS!!! ");
        Integer idx=0;
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder parser = factory.newDocumentBuilder();
            Document dc = null;
            switch (xmlLocation){
                case 1:                
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    InputStream is = cl.getResourceAsStream(foodGroups_fileName); 
                    if (is == null) {
                        Utility.ApplicationLogger.severe("ERROR " + "############## Could not look up : " + foodGroups_fileName);
                    }
                    dc= parser.parse(is);
                    break;
                case 2:
                    FileInputStream fis = new FileInputStream(foodGroups_xmlName);
                    if (fis == null) {
                        Utility.ApplicationLogger.severe("ERROR " + "############## Could not look up : " + foodGroups_xmlName);
                    }
                    dc= parser.parse(fis);
                    break;
            }
            
            Node topNode = dc.getDocumentElement();
            NodeList nodeList = topNode.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node groupNode = nodeList.item(i);
                if (groupNode.getNodeType() == Node.ELEMENT_NODE) {
                        if (groupNode.getNodeName().equals("FoodGroup")) {
                          
                          String foodGroup = groupNode.getAttributes().item(0).getNodeValue();
                          Integer fgid = insertAisle(foodGroup);
                            
                          NodeList itemList = groupNode.getChildNodes();
                          for (int j = 0; j < itemList.getLength(); j++) {
                              Node itemNode = itemList.item(j);
                              if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                                  String foodItem = itemNode.getTextContent();
                                  foodItem = singular(foodItem);
                                  if (foodItem.contains("dinner")){
                                        foodItem = singular(foodItem);
                                  }
                                  insertFoodItem(foodItem, fgid);
                                  idx++;
                              }
                          }
                        }
                }
            }
            Utility.ApplicationLogger.info("****** COMPLETE. Food Groups Added: " + idx);
            
        } catch (Exception e) {
            Utility.ApplicationLogger.severe("EXCEPTION.  " + e.getMessage());
        }
    }
    
    public static Integer insertAisle(String foodGroup) {
        
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
                if (!foodGroup.isEmpty()) {
                    String sql = "INSERT INTO AISLES (FGID, FOODGROUP) VALUES (" +
                            FGID + ",'" + foodGroup + "')";
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
    
    public static void insertFoodItem(String item, Integer fgid) {
        try {
                    
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            int newId = 1;
            ResultSet rs = stmt.executeQuery("SELECT MAX(FID) FROM FOODITEMS");
            rs.beforeFirst();
            if (rs.next()) {
                newId = rs.getInt(1) + 1;
            }
            
            if (!item.isEmpty()) {
                String sql = "INSERT INTO FOODITEMS (FID, FGID, ITEM) VALUES (" +
                        newId + "," + fgid + ",'" + item + "')";
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
}
