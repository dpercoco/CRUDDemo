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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.el.ValueExpression;

import oracle.adfmf.bindings.iterator.BasicIterator;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;
import oracle.adfmf.util.Utility;
import oracle.adfmf.util.logging.Trace;

import org.jaxen.expr.Step;

public class Ingredient {
    protected int id;
    protected int rid;
    protected double qty;
    protected double amount;
    protected String qtyStr;
    protected String amountStr;    
    protected String container;
    protected String measurement;
    protected String item;
    protected String foodGroup;
    protected String toString;
    protected String newItem;    
    protected static final AtomicLong sKeySequence = new AtomicLong(0);
    protected static Map<String, String> foodGroups = null;
    protected Long key;
    AisleService aisle = new AisleService();
    protected transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
         propertyChangeSupport.addPropertyChangeListener(l);
     }
    public void removePropertyChangeListener(PropertyChangeListener l) {
         propertyChangeSupport.removePropertyChangeListener(l);
     } 
    public Ingredient() {
        
        this.qty = 0;
        this.amount = 0;
        this.container = "";
        this.measurement = "";
        this.item = "";
        this.foodGroup = "";
       
    }
    public Ingredient(int rid, int id, double qty, double amount, String container, String measurement, String item, String foodGroup) {
        this.rid = rid;
        this.id = id;
        this.qty = qty;
        this.amount = amount;
        this.container = container;
        this.measurement = measurement;
        this.item = item;
        this.foodGroup = foodGroup;
    }  
  
    public Ingredient(int rid, int id) {
        
        this.rid = rid;
        this.id = id;
        this.qty = 0;
        this.amount = 0;
        this.container = "";
        this.measurement = "";
        this.item = "";
        this.foodGroup = "";
    }  
    
   public Ingredient(int id) {
       this.id = id;
   }
   public int getId() {
        return id;
   }
   public void setId(int id) {
       int oldId = this.id;
       this.id = id;
       propertyChangeSupport.firePropertyChange("id", oldId, id);
   }
   public int getRid() {
       return rid;
   }
   public void setRid(int rid) {
        int oldRid = this.rid;
        this.rid = rid;
        propertyChangeSupport.firePropertyChange("rid", oldRid, rid);
    }
    public void setQtyStr(String qtyStr) {
        String oldqtyStr= this.qtyStr;
        this.qtyStr = qtyStr;
        propertyChangeSupport.firePropertyChange("qtyStr", oldqtyStr, qtyStr);
    }

    public String getQtyStr() {
        if (this.qty == 0) return "";
        String trimQty = Fractions.intFraction(qty) + " ";
        return trimQty.trim();
    }

    public void setAmountStr(String amountStr) {
        String oldamountStr= this.amountStr;
        this.amountStr = amountStr;
        propertyChangeSupport.firePropertyChange("amountStr", oldamountStr, amountStr);
    }

    public String getAmountStr() {
        if (this.amount == 0) return "";        
        String trimAmount = Fractions.intFraction(amount) + " ";
        return trimAmount.trim();
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

            String m = inStr.replaceAll("[^A-Za-z]", "").trim();
            this.measurement = m;
        }
        catch(Exception e){
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Ingredient.class, "setAmountMeasurement",
                      "##############Exception:  " + e.getMessage());
        }
} 

   double parse(String ratio) {
        if (ratio.contains("/")) {
            String[] rat = ratio.split("/");
            return Double.parseDouble(rat[0]) / Double.parseDouble(rat[1]);
        } else {
            return Double.parseDouble(ratio);
        }
   }
    
    public String getItem() {
        return item.trim();
    }

    public void setItem(String item) {
        String oldItem = this.item;
        this.item = item;
        propertyChangeSupport.firePropertyChange("item", oldItem, item);
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
        propertyChangeSupport.firePropertyChange("qty", oldqty, qty);
    }

    public double getQty() {
        return qty;
    }

    public void setAmount(double amount) {
        double oldamount = this.amount;
        if (amount<0) this.amount=0;
        else this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }
    public void setMeasurement(String measurement) {
        String oldmeasurement = this.measurement;
        this.measurement = measurement;
        propertyChangeSupport.firePropertyChange("measurement", oldmeasurement, measurement);
    }

    public String getMeasurement() {
        if (measurement==null) return " ";
        return measurement;
    }
    public void setFoodItem(String item) {
        String oldItem = this.item;
        this.item = item;
        propertyChangeSupport.firePropertyChange("item", oldItem, item);
    }
    public String getFoodItem() {
        return item;
    }
   public String getContainer() {
       if (container==null) return " ";
       return container;
   }
   public void setContainer(String container) {
       String oldContainer = this.container;
       this.container = container;
       propertyChangeSupport.firePropertyChange("container", oldContainer, container);
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
     
   public void setToString(String toString) {
         String oldtoString = this.toString;
         this.toString = toString;
         propertyChangeSupport.firePropertyChange("toString", oldtoString, toString);
    }
    
    public Ingredient buildIngredient(String foodGroup, String foodItem) {
                
        Ingredient i = buildIngredient(foodItem);   
        i.setFoodGroup(foodGroup);
        return i;
    }
    
    public Ingredient buildIngredient(String foodItem) {
        
        Ingredient i = new Ingredient();
        i.setRid(this.rid);
        i.setId(this.id);
        
        try {   
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
            
              String foodGroup = aisle.findFoodGroup(i.getFoodItem());
              i.setFoodGroup(foodGroup);
              Utility.ApplicationLogger.severe("buildIngredient: foodItem " + i.getFoodItem() + ", foodGroup " + foodGroup); 
             
         }
         catch (Exception e) {
             Utility.ApplicationLogger.severe("setIngredient: ERROR " + foodItem + " " + e.getMessage()); 
         }
        
         return i;
     }    
    
    public void addIngredientToStore_XXX() { 
        
        int newId = 0;
        try {
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT MAX(IID) FROM INGREDIENTS");
            rs.beforeFirst();
            if (rs.next()) {
                newId = rs.getInt(1) + 1;
            }
        }
        catch (Exception e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Ingredient.class, "addIngredientToStore",
                      "#####EXCEPTION RECIPE ID:  " + rid + ", INGREDIENT ID:" + newId + " " + e.getMessage()); 
        }
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Ingredient.class, "addIngredientToStore",
                  "RECIPE ID:  " + rid + ", INGREDIENT ID:" + newId);   
               
        //Ingredient ingredient = new Ingredient(id, newId);
        //addIngredient(ingredient);
        //ingredients.add(ingredient);
        //providerChangeSupport.fireProviderRefresh("ingredients"); //Used to fire changes made to a collection
          
        /**        
        try {
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT MAX(IID) FROM INGREDIENTS WHERE RID="+id);
            rs.beforeFirst();
            if (rs.next()) {
                newId = rs.getInt(1) + 1;
            }
            
            String  sql =
                "INSERT INTO INGREDIENTS (RID,IID) VALUES (" + this.getId() + "," + newId + ")";
            
            int updateCount = stmt.executeUpdate(sql);
            if (updateCount == 0) {
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "addIngredientToStore", "Insert Failed!");
            }           
            else {
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "addIngredientToStore", "Insert PASSED! id:" + newId);
            }
            conn.commit();
            stmt.close();
            conn.close();
            
            this.reloadIngredients();
            
        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "addIngredientToStore",
                      "##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "addIngredientToStore",
                      "##############Exception:  " + exception.getMessage());
        }
        **/
    }
    public void saveIngredientToStore() {
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Ingredient.class, "saveIngredientToStore", 
                  "##############saveIngredientToStore Recipe ID: " + this.rid + ", Ingredient ID: " + this.id
                  + ", item:" + this.newItem);
        
        //ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.newModeIngredient}", Boolean.class);
        //Object obj1 = ve.getValue(AdfmfJavaUtilities.getELContext());        
        //String newMode = obj1.toString();
        //if (newMode=="true") i = buildIngredient(this.newItem);
        
        Ingredient i = buildIngredient(this.newItem);
        
        this.foodGroup = i.foodGroup;
        this.qty = i.qty;
        this.amount = i.amount;
        this.measurement = i.measurement;
        this.container = i.container;
        this.item = i.item;
        
        try {
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = ""; 
                        
            if (this.item.isEmpty()){ 
                deleteIngredient();  
            }
            else {
                sql = "SELECT * FROM INGREDIENTS WHERE IID = " + this.id;
                ResultSet rs = stmt.executeQuery(sql);
                rs.beforeFirst();
                if (rs.next()) {
                    sql =  
                        "UPDATE INGREDIENTS SET QTY=" + i.getQty() + ", AMOUNT=" + i.getAmount() +
                        ", RID=" + i.getRid() +
                        ", CONTAINR='" + i.getAmount()+ "', MEASUREMENT='" + i.getMeasurement() + 
                        "', ITEM='" + i.getItem() + "', FOODGROUP='" + i.getFoodGroup() +
                        "' WHERE IID=" + i.getId();
                } else {
                    sql =                    
                        "INSERT INTO INGREDIENTS (RID,IID,QTY,AMOUNT,CONTAINR,MEASUREMENT,ITEM,FOODGROUP) VALUES (" +
                        i.getRid() + "," + i.getId() + "," + i.getQty()+ "," +i.getAmount() + ",'" + i.getContainer() + "','" + 
                        i.getMeasurement() + "','" + i.getItem() + "','" + i.getFoodGroup() + "')";
                }             
                
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, Ingredient.class, "saveIngredientToStore - SQL:", sql);
                
                int updateCount = stmt.executeUpdate(sql);                
                if (updateCount == 0) {
                    Trace.log(Utility.ApplicationLogger, Level.SEVERE, Ingredient.class, "##############saveIngredientToStore", "saveIngredientToStore Update Failed!");
                }
                else {
                    Trace.log(Utility.ApplicationLogger, Level.SEVERE, Ingredient.class, "##############saveIngredientToStore", "saveIngredientToStore Update Passed! " + i.getToString());
                }
                conn.commit();
                
                Recipe r = new Recipe();
                r.reloadIngredients(this.rid); //NEW
            }
            
        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Ingredient.class, "saveIngredientToStore",
                      "##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Ingredient.class, "saveIngredientToStore",
                      "##############Exception:  " + exception.getMessage());
        }
    }
    
    public void deleteIngredient() {
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Ingredient.class, "deleteIngredientFromStore", "Food Group: " 
            + this.foodGroup + ", Ingredient ID:" +  this.id );
        try {
            //if (this.item.trim().length()==0) {
                Connection conn = DBConnectionFactory.getConnection();
                Statement stmt = conn.createStatement();
    
                ResultSet rs = stmt.executeQuery("SELECT * FROM INGREDIENTS WHERE IID=" + this.id);
                rs.beforeFirst();
                if (rs.next()) {
                    String sql = "DELETE FROM INGREDIENTS WHERE IID=" + this.id;
                    int updateCount = stmt.executeUpdate(sql);
                    if (updateCount == 0) {
                        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Ingredient.class, "deleteIngredientFromStore",
                                  "Delete Failed!");
                    }
                    else {
                        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Ingredient.class, "deleteIngredientFromStore",
                                              "DELETED Ingredient ID: " + this.id);
                    }
                }
                conn.commit();
            //}
        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Ingredient.class, "deleteIngredientFromStore",
                      "##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Ingredient.class, "deleteFromStore",
                      "##############Exception:  " + exception.getMessage());
        }
    }


    public void deleteIngredientFromStore() {
        
        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.selectedId}", Integer.class);
        Object obj1 = ve.getValue(AdfmfJavaUtilities.getELContext());        
        Integer ingredientId = (Integer)obj1; 
        
        ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.selectedItem}", String.class);
        obj1 = ve.getValue(AdfmfJavaUtilities.getELContext());        
        String ingredientItem = obj1.toString(); 
                
        //Trace.log(Utility.ApplicationLogger, Level.SEVERE, Ingredient.class, "deleteIngredientFromStore", "Food Group: " 
        //    + this.foodGroup + ", Ingredient ID:" +  this.id + " " + this.toString);
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Ingredient.class, "deleteIngredientFromStore", "Food Group: " 
            + this.foodGroup + ", Ingredient ID:" +  ingredientId + " " + ingredientItem);
        try {
            //if (this.item.trim().length()==0) {
                Connection conn = DBConnectionFactory.getConnection();
                Statement stmt = conn.createStatement();
    
                ResultSet rs = stmt.executeQuery("SELECT * FROM INGREDIENTS WHERE RID=" + this.rid + 
                                " AND IID = " + ingredientId);
                rs.beforeFirst();
                if (rs.next()) {
                    String sql = "DELETE FROM INGREDIENTS WHERE RID=" + this.rid + " AND IID = " + ingredientId;
                    int updateCount = stmt.executeUpdate(sql);
                    if (updateCount == 0) {
                        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Ingredient.class, "deleteIngredientFromStore",
                                  "Delete Failed!");
                    }
                    else {
                        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Ingredient.class, "deleteIngredientFromStore",
                                              "DELETED Ingredient ID: " + ingredientId + " " + ingredientItem);
                    }
                }
                conn.commit();
            //}
        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Ingredient.class, "deleteIngredientFromStore",
                      "##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Ingredient.class, "deleteFromStore",
                      "##############Exception:  " + exception.getMessage());
        }
    }
    
    public String getKey() {
        Integer i = new Integer(id);
        return i.toString();        
    }
    /** 
    public Long getKey()  {
         
         //NEW 03-07-2017
         //https://github.com/oracle/mobile-persistence/blob/master/Projects/Framework/Runtime/src/oracle/ateam/sample/mobile/v2/persistence/model/Entity.java
         
        if (key == null) {
              key = sKeySequence.getAndIncrement();
        }
        return key;
    }
    **/
    
    public void setNewItem(String newItem) {
        String oldNewItem = this.newItem;
        this.newItem = newItem;
        propertyChangeSupport.firePropertyChange("newItem", oldNewItem, newItem);
    }

    public String getNewItem() {
        return newItem;
    }
}

