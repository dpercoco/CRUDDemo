package crud.JAVA;
/**
 * https://github.com/google/search-samples/blob/master/app-indexing/app/src/main/java/com/recipe_app/client/Recipe.java
 */

import com.gargoylesoftware.htmlunit.WebClient;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import crud.application.DBConnectionFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;
import oracle.adfmf.util.Utility;
import oracle.adfmf.util.logging.Trace;

import org.jaxen.expr.Step;

/** TO BE RUN LOCALLY ONLY. DOES NOT INVOKE ANY PROPERTY CHANGES, DB UPDATES **/

public class IngredientL {
       private int id;
       private int rid;
       private double qty;
       private double amount;
       private String container;
       private String measurement;
       private String item;
       private String foodGroup;
       private String toString;
    
    public IngredientL() {
       
    }
    
    public IngredientL(int rid, int id, double qty, double amount, String container, String measurement, String item, String foodGroup) {
        this.rid = rid;
        this.id = id;
        this.qty = qty;
        this.amount = amount;
        this.container = container;
        this.measurement = measurement;
        this.item = item;
        this.foodGroup = foodGroup;
    }
   
   private List<IngredientL> ingredients = new ArrayList<IngredientL>();
    
    public IngredientL(int id) {
       this.id = id;
   }
    
    public int getId() {
        return id;
   }
    
    public void setId(int id) {
         int oldId = this.id;
         this.id = id;
   }
    
    public int getRid() {
       return rid;
   }
    
    public void setRid(int rid) {
        int oldRid = this.rid;
        this.rid = rid;
    }
   
    public String getQtyString() {
        if (qty==0) {
                return "";
        }
        return qty + " ";
    }
    
    public void setQuantity(String instr){
        int idx = instr.indexOf(" ");
        String numPart="";
        String fractionPart="";
        
        if (idx>-1) {
                numPart = instr.substring(0,idx).trim();
                fractionPart = instr.substring(idx).trim();
        }
        else {
                fractionPart = instr;
        }
        
        fractionPart = fractionPart.replaceAll("[^0-9/]", "");
        try {
                int num = 0;
                if (numPart.trim().length()>0) {
                        num = Integer.parseInt(numPart);
                }
                this.qty = num + parse(fractionPart);
        }
        catch(Exception e){}
    }
        
    public void setAmountMeasurement(String inStr) {
        String temp = inStr.replaceAll("[^0-9/ ]", "").trim();
        try {
                if (temp.indexOf("/")>-1 && temp.indexOf(" ")>-1) {
                        String[] f = temp.split(" ");
                        String integerPart = f[0];
                        String fractionPart = f[1];
                        String[] fraction = fractionPart.split("/");
                        int integer = Integer.parseInt(integerPart);
                        int numerator = Integer.parseInt(fraction[0]);
                        int denominator = Integer.parseInt(fraction[1]);
                        double fractionDbl = (numerator * 100 / denominator);
                        this.amount = integer + fractionDbl/100;
                }
                else this.amount = parse(temp);
        }
        catch(Exception e){}
        String m = inStr.replaceAll("[^A-Za-z]", "").trim();
        this.measurement = m;
    } 
    
    double parse(String ratio) {
        if (ratio.contains("/")) {
            String[] rat = ratio.split("/");
            return Double.parseDouble(rat[0]) / Double.parseDouble(rat[1]);
        } else {
            return Double.parseDouble(ratio);
        }
    }
    
    public String getFoodItem() {
        return item.trim();
    }

    public void setFoodItem(String foodItem) {
        this.item = foodItem;
    }
    
    public String getFoodGroup() {
        return foodGroup;
    }

    public void setFoodGroup(String foodGroup) {
        this.foodGroup = foodGroup;
    }
        
    public void setQty(double qty) {
        double oldqty = this.qty;
        this.qty = qty;
    }

    public double getQty() {
        return qty;
    }

    public void setAmount(double amount) {
        double oldamount = this.amount;
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public void setMeasurement(String measurement) {
        String oldmeasurement = this.measurement;
        this.measurement = measurement;
    }

    public String getMeasurement() {
        return measurement;
    }

    public void setItem(String item) {
        String olditem = this.item;
        this.item = item;
    }

    public String getItem() {
        return item;
    }
    
    public String getContainer() {
       return container;
    }
    
    public void setContainer(String container) {
       String oldContainer = this.container;
       this.container = container;
    }

    public List<IngredientL> getIngredients() {
       return ingredients;
    }

    public void addIngredient(IngredientL ingredient) {
       ingredients.add(ingredient);
    }
    public IngredientL buildIngredient(String foodGroup, String foodItem) {
        
        IngredientL i = buildIngredient(foodItem);   
        i.setFoodGroup(foodGroup);
        return i;
          
    }
    
    public IngredientL buildIngredient(String foodItem) {
        
        IngredientL i = new IngredientL();
          
        try {   
              //String numeric ="(?:[0-9]{1,2} )?[1-9]{1,2}(?:/[1-9]{1,1})?";            
              String numeric = "[0-9]{1,2}(?:/[1-9]{1,1})?(?: [1-9]{1,1}/[1-9]{1,1})?";             
              String measurements = "( )?(tbs|tablespoon|tsp|teaspoon|pound|oz|ounce)s? ";
              String containers = " (can|cup|bunch|box|bottle)s? ";
              String adjectives = "(chopped|crushed) ";
              String remaining = "", qty="";
              int idx=-1;
              
              foodItem = foodItem.replaceAll("[()]","");
              remaining = foodItem;
              
              String regex = numeric + measurements;          
              Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
              Matcher m = p.matcher(foodItem); 
              if (m.find()){
                      remaining = remaining.replace(m.group(), "");
                      i.setAmountMeasurement(m.group());
                      idx=m.start();
                      if (idx>0) {
                              qty = foodItem.substring(0,idx);
                              i.setQuantity(qty);
                              remaining = remaining.replaceAll("^" + qty, "");
                      }
              }
              else {
                      regex = "^" + numeric;          
                      p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                      m = p.matcher(foodItem); 
                      if (m.find()){
                              qty = m.group();
                              i.setQuantity(qty);
                              remaining = remaining.replaceAll("^" + qty, "");
                      }
              }
              
              regex = containers;     
              p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
              m = p.matcher(foodItem); 
              if (m.find()){
                      i.setContainer(m.group());
                      remaining = remaining.replace(m.group(), " ");
              }
              
              regex = adjectives;     
              p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
              m = p.matcher(foodItem); 
              if (m.find()){
                      i.setContainer(m.group());
                      remaining = remaining.replace(m.group(), " ");
              }
              
              i.setFoodItem(remaining);
              
              //System.out.println("qty:" + i.getQty() + ", amt:" + i.getAmount() + 
              //                           ", measurement:" + i.getMeasurement() +                    
              // 
         }
         catch (Exception e) {
             Utility.ApplicationLogger.severe("setIngredient: ERROR " + foodItem + " " + e.getMessage()); 
         }
        
         return i;
     }
    public void reloadFromStore() {
        try {
            
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM INGREDIENTS WHERE RID = " + this.rid);

            rs.beforeFirst();
            if (rs.next()) {
                
                this.setId(rs.getInt("IID"));
                this.setRid(rs.getInt("RID"));
                this.setQty(rs.getDouble("QTY"));
                this.setAmount(rs.getDouble("AMOUNT"));
                this.setContainer(rs.getString("CONTAINER"));
                this.setMeasurement(rs.getString("MEASUREMENT"));
                this.setItem(rs.getString("ITEM"));
                this.setFoodGroup(rs.getString("FOODGROUP"));
                
                if (getContainer().equals("null")) {
                    this.setContainer("");
                }
               
                if (getMeasurement().equals("null")) {
                    this.setMeasurement("");
                }
                
                if (getItem().equals("null")) {
                    this.setItem("");
                }
                
                if (getFoodGroup().equals("null")) {
                    this.setFoodGroup("");
                }
                
                if (rs.wasNull()) {
                    //this.setBirthDate(null);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception exception) {
        }
    }
    
    public void saveToStore() {
        try {
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "";           
            
            ResultSet rs = stmt.executeQuery("SELECT * FROM INGREDIENTS WHERE IID = " + this.id);
            rs.beforeFirst();
            if (rs.next()) {
                sql =  
                    "UPDATE INGREDIENTS SET QTY=" + this.getQty() + ", AMOUNT=" + this.getAmount() +
                    ", RID=" + this.getRid() +
                    ", CONTAINER='" + this.getContainer() + "', MEASUREMENT='" + this.getMeasurement() + 
                    "', ITEM='" + this.getItem() + "', FOODGROUP='" + this.getFoodGroup() +
                    "' WHERE IID=" + this.getId();
            } else {
                sql =                    
                    "INSERT INTO INGREDIENTS (RID,IID,QTY,AMOUNT,CONTAINER,MEASUREMENT,ITEM,FOODGROUP) VALUES (" +
                    this.getRid() + "," + this.getId() + "," + this.getAmount() + ",'" + this.getContainer() + "','" + 
                    this.getMeasurement() + "','" + getItem() + "','" + getFoodGroup() + "')";
            }             
           
            int updateCount = stmt.executeUpdate(sql);
            if (updateCount == 0) {}            
            
            conn.commit();
            
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception exception) {

        }
    }
    
    public void deleteFromStore() {
            try {
                Connection conn = DBConnectionFactory.getConnection();
                Statement stmt = conn.createStatement();
    
                ResultSet rs = stmt.executeQuery("SELECT * FROM INGREDIENTS WHERE IID = " + this.id);
                rs.beforeFirst();
                if (rs.next()) {
                    String sql = "DELETE FROM INGREDIENTS WHERE IID = " + this.id;
    
                    int updateCount = stmt.executeUpdate(sql);
                    if (updateCount == 0) {
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                
            } catch (Exception exception) {
              
            }
            
        }

    public void setToString(String toString) {
        this.toString = toString;
    }

    public String getToString() {
        
        toString = Fractions.intFraction(this.getQty());
        String amt = Fractions.intFraction(this.getAmount());
        String meas = this.getMeasurement();
        String cont = this.getContainer();
        String item = this.getFoodItem();
        
        if (amt.trim().length()>0) toString += " " + amt;
        if (meas.trim().length()>0) toString += " " + meas;
        if (cont.trim().length()>0) toString += " " + cont;
        if (item.trim().length()>0) toString += " " + item;
        
        return toString;
    }
}
