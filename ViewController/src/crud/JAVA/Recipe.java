package crud.JAVA;
/**
 * https://github.com/google/search-samples/blob/master/app-indexing/app/src/main/java/com/recipe_app/client/Recipe.java
 */

import com.gargoylesoftware.htmlunit.WebClient;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.tangosol.dev.assembler.Try;

import crud.application.DBConnectionFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.el.ValueExpression;

import oracle.adfmf.amx.event.ValueChangeEvent;
import oracle.adfmf.bindings.dbf.AmxAttributeBinding;
import oracle.adfmf.bindings.dbf.AmxIteratorBinding;
import oracle.adfmf.bindings.dbf.AmxTreeBinding;
import oracle.adfmf.bindings.iterator.BasicIterator;
import oracle.adfmf.dc.bean.ConcreteJavaBeanObject;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;
import oracle.adfmf.java.beans.ProviderChangeListener;
import oracle.adfmf.java.beans.ProviderChangeSupport;
import oracle.adfmf.util.Utility;
import oracle.adfmf.util.logging.Trace;

import org.jaxen.expr.Step;

import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;

public class Recipe {
   private int id = 0;
   private String title="";
   private String photo="";
   private String description="";
   private String prepTime="";
   private String recipeUrl="";
   private String item="";
   private List<Ingredient> ingredients = new ArrayList<Ingredient>();
   private static Map<String, Map<String, Ingredient>> shoppingItems = null;
   private static shoppingListProcess slp = new shoppingListProcess();
   private static int endOfIngredients;
   private static AisleService aisle = new AisleService();
   private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
   protected transient ProviderChangeSupport providerChangeSupport = new ProviderChangeSupport(this);  
   
   public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
   }
   
   public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
   }
   
   public void addProviderChangeListener(ProviderChangeListener l) {  
         providerChangeSupport.addProviderChangeListener(l);  
   } 
   
   public void removeProviderChangeListener(ProviderChangeListener l) {  
         providerChangeSupport.removeProviderChangeListener(l);  
   }  
      
   public Recipe() {
   }
   
   public Recipe(int id, String title, String url) {
         super();
         this.id = id;
         this.title = title;
         this.recipeUrl = url; 
    } 
   public Recipe(int id, String title, String url, String description, String prepTime) {
         super();
         this.id = id;
         this.title = title;
         this.recipeUrl = url; 
         this.description = description;
         this.prepTime = prepTime;
    } 
   public Recipe(int id) {
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
   public String getTitle() {
       
       if (title.trim().length()==0) return "";
       title = title.replace("\"", "");
       title = title.replace("'", "");
       return title;
   }
   public void setTitle(String title) {
       
       String oldTitle = this.title;
       this.title = title;
       //ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{applicationScope.selectedTitle}", String.class);
       //ve.setValue(AdfmfJavaUtilities.getELContext(), title);
       
       propertyChangeSupport.firePropertyChange("title", oldTitle, title);
   }
   public void setRecipeUrl(String url) {
       
       String oldRecipeUrl = this.recipeUrl;
       this.recipeUrl = url;
       //ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{applicationScope.selectedUrl}", String.class);
       //ve.setValue(AdfmfJavaUtilities.getELContext(), url);
       
       propertyChangeSupport.firePropertyChange("recipeUrl", oldRecipeUrl, url);
   }
   public String getRecipeUrl() {
       
       if (recipeUrl.trim().length()==0) return "";
       recipeUrl = recipeUrl.replace("\"", "");
       recipeUrl = recipeUrl.replace("'", "");
       return recipeUrl;
   }
   public String getPhoto() {
       return photo;
   }
   public String getItem() {
        return item;
    }
   public void setItem(String item) {
        String oldItem = this.item;
        this.item = item;
        propertyChangeSupport.firePropertyChange("photo", oldItem, item);
    }
   public String getDescription() {
       return description;
   }
   public void setDescription(String description) {
       String oldDescription = this.description;
       this.description = description;
       propertyChangeSupport.firePropertyChange("description", oldDescription, description);
   }
   public String getPrepTime() {
       return prepTime;
   }
   public void setPrepTime(String prepTime) {
       this.prepTime = prepTime;
   }
   
   public void setIngredients(List <Ingredient> ingredients) {
           
            List <Ingredient> oldIngredients = this.ingredients;
            this.ingredients = ingredients;
            propertyChangeSupport.firePropertyChange("ingredients", oldIngredients, ingredients);
    }
       
   public List <Ingredient> getIngredients() {
               
       if(ingredients.isEmpty()) {
           getIngredientsFromStore();
       }
       return ingredients;           
   }
       
   private List <Ingredient> getIngredientsFromStore() {
        
        Utility.ApplicationLogger.severe("getIngredientsFromStore RECIPE# " + this.getId() + " " + this.getRecipeUrl());
        ingredients.clear();
        Integer ct=0;
        
        try {
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM INGREDIENTS WHERE RID =" + this.getId();
            ResultSet result = stmt.executeQuery(sql);
            result.beforeFirst();
            
            if (!result.next()) {                
                getIngredientsFromWeb(this.getRecipeUrl());
            } 
            
            getIngredientsFromDB(); 
            Utility.ApplicationLogger.severe("Recipe #" + this.getId() + " Ingredients: " + ingredients.size());
            
        } catch (Exception ex) {
            Utility.ApplicationLogger.severe("EXCEPTION" + " " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }        
        return ingredients;
    }
    
   private void getIngredientsFromDB() {
        
        Utility.ApplicationLogger.severe("RECIPE# " + id + " " + this.getRecipeUrl());
        ingredients.clear();
        
        try {
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM INGREDIENTS WHERE RID =" + id;
            ResultSet result = stmt.executeQuery(sql);
            result.beforeFirst();
            while (result.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setRid(result.getInt("RID"));
                ingredient.setId(result.getInt("IID"));
                ingredient.setItem(result.getString("ITEM"));
                ingredient.setFoodGroup(result.getString("FOODGROUP"));
                ingredient.setMeasurement(result.getString("MEASUREMENT"));
                ingredient.setContainer(result.getString("CONTAINR"));
                ingredient.setQty(result.getDouble("QTY"));
                ingredient.setAmount(result.getDouble("AMOUNT"));
                ingredient.setNewItem(ingredient.getToString());
                ingredients.add(ingredient); 
                Utility.ApplicationLogger.severe("Ingredient #" + ingredient.getId() + " " + ingredient.getItem());
            }           
            Utility.ApplicationLogger.severe("Recipe #" + this.getId() + " Ingredients: " + ingredients.size());
            
        } catch (Exception ex) {
            Utility.ApplicationLogger.severe("getIngredientsFromDB EXCEPTION" + " " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    
   public String getIngredientFullString(Ingredient i) {
          
          String toString = Fractions.intFraction(i.getQty());
          String amt = Fractions.intFraction(i.getAmount());
          String meas = i.getMeasurement();
          String cont = i.getContainer();
          String item = i.getFoodItem();
          
          if (amt.trim().length()>0) toString += " " + amt;
          if (meas.trim().length()>0) toString += " " + meas;
          if (cont.trim().length()>0) toString += " " + cont;
          if (item.trim().length()>0) toString += " " + item;  
          return toString;
          
      }
    
   public void getIngredientsFromHTTP() throws MalformedURLException, ProtocolException, IOException {
        
        //http://www.rgagnon.com/javadetails/java-0085.html
        
        Utility.ApplicationLogger.severe("getIngredientsFromWeb: " + recipeUrl);
        
        URL url = new URL(recipeUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(100000 /* milliseconds */);
        conn.setConnectTimeout(250000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();  
        Utility.ApplicationLogger.severe("getIngredientsFromWeb: CONNECTED!");
        
        StringBuilder contentBuilder = new StringBuilder();
        try    { 
              String line;
              int idx = -1;
              Boolean foundIngredients=false;
              InputStreamReader IS = new InputStreamReader(conn.getInputStream());
              BufferedReader bufferedReader = new BufferedReader(IS);
              
              while(!bufferedReader.ready()){
                  //Wait;
              }
              
              if (bufferedReader.ready()) {
                  Utility.ApplicationLogger.severe("getIngredientsFromWeb: READING BUFFER!");
                  endOfIngredients = -1;
                  line = bufferedReader.readLine();
                  while (line!= null)      {
                      idx = line.toUpperCase().indexOf("INGREDIENT");                  
                      if (idx>-1){
                          foundIngredients = true;
                          //if (line.length()>=10) line = line.substring(idx+10);
                      }
                      if (foundIngredients) {
                          contentBuilder.append(line.replaceAll("\\<.*?\\>", "") + "\n");
                          //slp.setEndOfIngredients(contentBuilder.toString());
                      }
                      line = bufferedReader.readLine();
                  }  
                  
                  Utility.ApplicationLogger.severe("getIngredientsFromWeb: FINISHED READING BUFFER!");
                  String content = contentBuilder.toString();
                  if (endOfIngredients>-1) content = content.substring(0,endOfIngredients);
                  
                  shoppingListProcess.findIngredients(content);
                  shoppingItems = slp.getShoppingItems();
                  Utility.ApplicationLogger.severe("getIngredientsFromWeb: ShoppingItems Count:" + shoppingItems.size() + "\n");

                  loadIngredientsIntoDB ();
              }
        }
        catch(Exception e)     {
            Utility.ApplicationLogger.severe("getIngredientsFromWeb: ERROR " + recipeUrl + " " + e.getMessage());    
        }
        
    }
   
   private void getIngredientsFromWeb(String recipeUrl) throws IOException {
           
       Utility.ApplicationLogger.severe("getIngredientsFromWeb: " + recipeUrl);
    
        boolean bKeepGoing=true;
        int ct=0;
        String content = "";
        while (bKeepGoing) {
                ct++;
                if (ct==1) content = setContentFromJSoup(recipeUrl);
                if (ct==2) content = setContentFromUrl(recipeUrl);
                if (content.trim().length()>0 | ct==2) bKeepGoing = false;
        }
        
        if (content.trim().length()>0) {
            slp.setContent(content);
            shoppingItems = slp.getShoppingItems();
            if (shoppingItems!=null) {
                Utility.ApplicationLogger.severe("getIngredientsFromWeb: ShoppingItems Count:" + shoppingItems.size() + "\n");
                
                /** Load Ingredients Into Database **/                
                
                loadIngredientsIntoDB ();
            }
            else {
                Utility.ApplicationLogger.severe("getIngredientsFromWeb: ShoppingItems is NULL!\n");
            }
        }
    }
    
   private static String setContentFromJSoup(String recipeUrl) throws IOException {
    
            Utility.ApplicationLogger.severe("setContentFromJSoup: " + recipeUrl);
            String content = "";
            try {               
                    
                org.jsoup.nodes.Document doc = (org.jsoup.nodes.Document) Jsoup.connect(recipeUrl).get();
                org.jsoup.nodes.Document.OutputSettings settings = doc.outputSettings();
        
                //settings.prettyPrint(true);
                //settings.escapeMode(org.jsoup.nodes.Entities.EscapeMode.extended);
                //settings.charset("ASCII");
                String modifiedFileHtmlStr = doc.html();            
                content = new HtmlToPlainText().getPlainText(Jsoup.parse(modifiedFileHtmlStr));
            }
            catch (Exception e) {}   
            return content;
    
    }
    
   public static String setContentFromUrl(String inUrl) {
            
            Utility.ApplicationLogger.severe("setContentFromUrl: " + inUrl);
        
            StringBuilder contentBuilder = new StringBuilder();
            String content = "";
            try    {  
                URL urlx = new URL(inUrl);                
                HttpURLConnection connection = (HttpURLConnection)urlx.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
                connection.setDoOutput(true);  // You need to set it to true if you want to send (output) a request body, for example with POST or PUT requests. Sending the request body itself is done via the connection's output stream:<br />
                connection.connect();
              
              BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

              String line;
              while ((line = bufferedReader.readLine()) != null)      {
                      contentBuilder.append(line + "\n");
              }
              
              content = contentBuilder.toString();
              content = content.replaceAll("\\<.*?\\>", "");
              content = content.replaceAll("\n", "");
            }
            catch(Exception e)     {
            }
            return content;
    }
    
   public void loadIngredientsIntoDB () {
        
        Utility.ApplicationLogger.severe("recipeID - " + id + ", shopping Items: " + shoppingItems.size());
        
        Ingredient i;
        Map.Entry<String, Map<String, Ingredient>> foodGroupEntry=null;
        String foodGroup="";
        
        if (shoppingItems!=null) {
                 List<Map.Entry<String, Map<String, Ingredient>>> shoppingGroups = slp.getSortedShoppingItemGroupsMap();
                 for (int g = 0; g < shoppingGroups.size(); g++) {
                     
                     foodGroupEntry = shoppingGroups.get(g);
                     foodGroup = foodGroupEntry.getKey();
                     Map<String, Ingredient> foodGroupEntryItems = foodGroupEntry.getValue();
                     
                     List<Map.Entry<String, Ingredient>> shoppingItems = slp.getSortedShoppingItemsMap(foodGroupEntryItems);
                     for (int j = 0; j < shoppingItems.size(); j++) {
                             Map.Entry<String, Ingredient> s = shoppingItems.get(j);
                             if (s!=null){
                                 i = (Ingredient) s.getValue();
                                 if (i.getFoodItem().trim().length()>0){ 
                                     //Utility.ApplicationLogger.severe("ADDING Ingredient: " + i.getItem());
                                     //ingredient.setFoodGroup(foodGroup);
                                     i.setRid(id);
                                     addIngredientLocal(i);
                                 }
                             }
                     }
             }
        }
    }
   
    /** Used by CREATE Method **/ 
    
    public void addRecipe(Recipe r) {
        
        int newId = 0;
        try {
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT MAX(RID) FROM RECIPES");
            rs.beforeFirst();
            if (rs.next()) {
                newId = rs.getInt(1) + 1;
            }
            r.id = newId;
            r.title="";
            rs.close();
            
            Utility.ApplicationLogger.info("addRecipe ********** ID:" + newId);
        }
        catch (Exception e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "addRecipe",
                      "#####EXCEPTION RECIPE ID:  " + newId + " " + e.getMessage()); 
        }
    }
    
    /** Used by CREATE Method 
     *  This will add an entry to the Listview iterator
     *  This does not add a row to the Table
     **/ 
    
    public void addIngredient(Ingredient i) {
        
        int newId = 0;
        try {
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT MAX(IID) FROM INGREDIENTS");
            rs.beforeFirst();
            if (rs.next()) {
                newId = rs.getInt(1) + 1;
            }
            i.setId(newId);
            i.setRid(this.id);
            i.setNewItem("");
            i.setFoodGroup("Misc");
            rs.close();            
            Utility.ApplicationLogger.info("addIngredient ********** ID:" + newId);
        }
        catch (Exception e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "addIngredient",
                      "#####EXCEPTION INGREDIENT ID:" + newId + " " + e.getMessage()); 
        }        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "addIngredientToStore",
                  "RECIPE ID:" + this.id + "INGREDIENT ID:" + newId);      
        
    }
    
   public void addIngredientLocal(Ingredient ingredient) {
        try {
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            int newId = 1;

            ResultSet rs = stmt.executeQuery("SELECT MAX(IID) FROM INGREDIENTS");
            rs.beforeFirst();
            if (rs.next()) {
                newId = rs.getInt(1) + 1;
            }
            
            String  sql =
                "INSERT INTO INGREDIENTS (RID,IID,QTY,AMOUNT,CONTAINR,MEASUREMENT,ITEM,FOODGROUP) VALUES (" +
                ingredient.getRid() + "," + newId + "," + ingredient.getQty() + "," + ingredient.getAmount() + ",'" + 
                ingredient.getContainer() + "','" +  ingredient.getMeasurement() + "','" + 
                ingredient.getItem() + "','" + ingredient.getFoodGroup() + "')";
            
            int updateCount = stmt.executeUpdate(sql);
            if (updateCount == 0) {
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "addIngredient", "Insert Failed!");
            } 
            else {
                Utility.ApplicationLogger.severe("Recipe.addIngredientLocal Inserted RECIPE ID:" + id + ", INGREDIENT ID:" + newId + " " + ingredient.getItem() + "\n" + sql);
            }
            conn.commit();
            
        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "addIngredient",
                      "##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "addIngredient",
                      "##############Exception:  " + exception.getMessage());
        }
    }
   
    public void refreshIngredients() {
            
         Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "refreshIngredients", "");
         ingredients.clear();
         getIngredientsFromDB();
         propertyChangeSupport.firePropertyChange("ingredients", null, ingredients);
         providerChangeSupport.fireProviderRefresh("ingredients");
    }
   
   public void reloadIngredients(Integer rid) {
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "reloadIngredients",
                  " ***** Recipe:" + rid);
        this.id = rid;
        ingredients.clear();
        getIngredientsFromDB();
        propertyChangeSupport.firePropertyChange("ingredients", null, ingredients);
        providerChangeSupport.fireProviderRefresh("ingredients"); //Used to fire changes made to a collection
    }
    
   public void processIngredients() throws MalformedURLException, ProtocolException, IOException {
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "processIngredients",
                  "RECIPE ID: " + id);    
        
       for (Integer i=1; i<ingredients.size(); i++) {
            Ingredient ingredient = (Ingredient) ingredients.get(i);
            if (ingredient.getToString().trim().length()==0) {
                deleteFromStore(ingredient.getId());
            }
        }

    }
    
   public void removeIngredient(Integer id) throws MalformedURLException, ProtocolException, IOException {
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "removeIngredient",
                  "INGREDIENT ID: " + id);    
        
       for (Integer i=1; i<ingredients.size(); i++) {
            Ingredient ingredient = (Ingredient) ingredients.get(i);
            if (ingredient.getId() == id) {
                ingredients.remove(ingredient);
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "removeIngredient",
                          "REMOVED INGREDIENT ID: " + id + ", " + ingredient.getItem()); 
                break;
            }
        }
        
        /**
        AmxTreeBinding ingredientList = (AmxTreeBinding) AdfmfJavaUtilities.getELValue("#{bindings.ingredients}");
        AmxIteratorBinding amxListIterator =  ingredientList.getIteratorBinding();
        BasicIterator basicIterator = amxListIterator.getIterator();
        
        try  { 
            if (basicIterator.getRangeSize()>0) {
                for (Integer i=1; i<basicIterator.getRangeSize(); i++) {
                    basicIterator.setCurrentIndex(i);
                    Ingredient ingredient = (Ingredient) basicIterator.getDataProvider();
                    if (ingredient.getId() == id) {
                        ingredients.remove(ingredient);
                        break;
                    }
                }
            }
        }
        catch(Exception e)     {
            Utility.ApplicationLogger.severe("Recipe - removeIngredient: NO RESULTS " + e.getMessage());    
        }  
        **/
    }
    
    //public void removeIngredient(Ingredient ingredient) {
    //    ingredient.deleteFromStore();
    //}
    
   public void onIngredientItemChange(Integer ingredientID, String oldVal, String newVal) throws MalformedURLException, ProtocolException, IOException {
         
         Ingredient i = getCurrentIngredient();
         Utility.ApplicationLogger.severe("onIngredientItemChange: ID#" + i.getId() + " " + i.getItem() 
                                          + ", newVal:" + newVal); 
         try {                
             if (i.getFoodGroup().trim().length()==0){ //NEW ingredient          
                 i = i.buildIngredient(newVal);
                 String foodGroup = aisle.findFoodGroup(i.getItem());
                 i.setFoodGroup(foodGroup);
             } 
             i.setItem(newVal);
             i.saveIngredientToStore(); 
             removeIngredient(i.getId());
             addIngredientLocal(i);
             providerChangeSupport.fireProviderRefresh("ingredients");
         }
        catch(Exception e)     {
            Utility.ApplicationLogger.severe("EXCEPTION " + e.getMessage());    
        }
    }
    
    private Integer getCurrentIngredientID() {
        
        //ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{bindings.id.inputValue}", String.class);
                        
        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.selectedId}", Integer.class);
        Object obj1 = ve.getValue(AdfmfJavaUtilities.getELContext());
        String idStr = obj1.toString();
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "getCurrentIngredientID", 
                  " INGREDIENT ID:" + idStr);
        
        Integer id = Integer.parseInt(idStr);
        return id;
        
    }
    
    private Ingredient getCurrentIngredient() {
        
        Ingredient ingredient = new Ingredient();
        Integer id = getCurrentIngredientID();
        for (Integer i=1; i<ingredients.size(); i++) {
             ingredient = (Ingredient) ingredients.get(i);
             if (ingredient.getId()==id) {
                 return ingredient;
             }
         }
        return ingredient;
    }
    
    private Ingredient getCurrentIngredient(Integer id) {
        
        Ingredient ingredient = new Ingredient();
        for (Integer i=1; i<ingredients.size(); i++) {
             ingredient = (Ingredient) ingredients.get(i);
             if (ingredient.getId()==id) {
                 return ingredient;
             }
         }
        return ingredient;
    }
    
    private String getCurrentIngredientItem() {
        
        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.selectedItem}", String.class);
        Object obj1 = ve.getValue(AdfmfJavaUtilities.getELContext());
        String item = obj1.toString();
        return item;
        
    }
    
    private Ingredient getCurrentIngredient1() {
        
        AmxTreeBinding ingredientList = (AmxTreeBinding) AdfmfJavaUtilities.getELValue("#{bindings.ingredients}");
        AmxIteratorBinding amxListIterator =  ingredientList.getIteratorBinding();
        BasicIterator basicIterator = amxListIterator.getIterator();  
        Ingredient ingredient = (Ingredient) basicIterator.getDataProvider();
        return ingredient;
        
    }
    
    private Ingredient getCurrentIngredient2() {
        
        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{bindings.ingredientsIterator.currentRow.dataProvider}",  Object.class);
        Object obj = ve.getValue(AdfmfJavaUtilities.getELContext());
        Ingredient i = new Ingredient(); 
        if (obj instanceof ConcreteJavaBeanObject) {
            ConcreteJavaBeanObject cjbo = (ConcreteJavaBeanObject)obj;
            i = (Ingredient)cjbo.getInstance();
        }
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "getCurrentIngredient2", 
                  " INGREDIENT ID:" + i.getId() + " ITEM:" + i.getItem());
        return i;
        
    } 
    
    public void setCurrentIngredient(Integer id) {
        
        AmxTreeBinding ingredientList = (AmxTreeBinding) AdfmfJavaUtilities.getELValue("#{bindings.ingredients}");
        AmxIteratorBinding amxListIterator =  ingredientList.getIteratorBinding();
        BasicIterator basicIterator = amxListIterator.getIterator();
        
        try  { 
            if (basicIterator.getRangeSize()>0) {
                for (Integer i=1; i<basicIterator.getRangeSize(); i++) {
                    basicIterator.setCurrentIndex(i);
                    Ingredient ingredient = (Ingredient) basicIterator.getDataProvider();
                    if (ingredient.getId() == id) {
                        break;
                    }
                }
            }
        }
        catch(Exception e)     {
        }  
        
        /**
        for (Integer i=1; i<ingredients.size(); i++) {
             Ingredient ingredient = (Ingredient) ingredients.get(i);
             if (ingredient.getId() == id) {
                 break;
             }
         }
        **/
    }
    public void deleteIngredientFromStoreXXXX() throws SQLException, Exception {
         
         try {             
             Connection conn = DBConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             Integer ingredientID = getCurrentIngredientID();
             String item = getCurrentIngredientItem();
             
             Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "deleteIngredientFromStore", 
                       "RECIPE ID:" + id + ", INGREDIENT ID:" + ingredientID + " " + item);
             
             if (id>0) {
                 String sql = "DELETE FROM INGREDIENTS WHERE RID =" + id + " AND IID=" + ingredientID;
                 int updateCount = stmt.executeUpdate(sql);
                 if (updateCount == 0) {
                     Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "deleteIngredientFromStore",
                               "Delete Failed!");
                     Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "deleteIngredientFromStore",
                               sql);
                 }
                 else {
                     Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "deleteIngredientFromStore",
                               "Delete Passed! ID:" + ingredientID);
                     
                     /** REMOVE INGREDIENT **/
                     
                     removeIngredient(ingredientID);
                     providerChangeSupport.fireProviderRefresh("ingredients");
                 }
             }
             conn.commit();
             
         } catch (SQLException e) {
             Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "deleteIngredientFromStore",
                       "##############SQL Exception:  " + e.getMessage());
             e.printStackTrace();
         } catch (Exception exception) {
             Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "deleteIngredientFromStore",
                       "##############Exception:  " + exception.getMessage());

         }
     }    
    
    public void reloadFromStore() {
            
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "reloadFromStore",
                  "RECIPE ID:  " + this.id);
        
        try {                
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM RECIPES WHERE RID = " + this.id);
            
            rs.beforeFirst();
            if (rs.next()) {                    
                
                if (!rs.getString("RECIPEURL").isEmpty()) {  
                    
                    Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "reloadFromStore",
                              "RECIPEURL:  " + rs.getString("RECIPEURL"));
                    
                    this.setTitle(rs.getString("TITLE"));
                    this.setRecipeUrl(rs.getString("RECIPEURL"));
                    this.setDescription(rs.getString("DESCRIPTION"));
                    this.setPrepTime(rs.getString("PREPTIME"));
                    //this.setItem(rs.getString("ITEM"));
                    
                    if (getTitle().equals("null")) {
                        this.setTitle("");
                    }
                   
                    if (getRecipeUrl().equals("null")) {
                        this.setRecipeUrl("");
                    }
                    
                    if (getDescription().equals("null")) {
                        this.setDescription("");
                    }
                    
                    if (getPrepTime().equals("null")) {
                        this.setPrepTime("");
                    }
                }
            }
        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "reloadFromStore",
                      "##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "reloadFromStore",
                      "##############Exception:  " + exception.getMessage());
         }
    }
    
    public void saveToStore() {
        try {
            
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "saveToStore", "Recipe: " + this.id);
            
            RecipeService rSvc = new RecipeService();
            rSvc.clearParms();
            
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = null;
            String sql = "DELETE FROM RECIPES WHERE RID = " + this.id;
            
            if (this.getTitle().length()==0) {
                rs = stmt.executeQuery(sql);
                conn.commit();
                return;
            }
            
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "saveToStore", "title: " + this.getTitle());
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "saveToStore", "url: " + this.getRecipeUrl());
            
            rs = stmt.executeQuery("SELECT * FROM RECIPES WHERE RID = " + this.id);
            rs.beforeFirst();
            if (rs.next()) { 
                sql = 
                        "UPDATE RECIPES SET TITLE='" + this.getTitle() + "', RECIPEURL='" + this.getRecipeUrl() +
                        "', DESCRIPTION='" + this.getDescription() + "', PREPTIME='" + this.getPrepTime() + 
                        "' WHERE RID=" + this.id;
            } else {
                sql =                    
                        "INSERT INTO RECIPES (RID,TITLE,RECIPEURL,DESCRIPTION,PREPTIME) VALUES (" +
                        this.id + ",'" + this.getTitle() + "','" + this.getRecipeUrl() + "','" + 
                        this.getDescription() + "','" + getPrepTime() + "')";
            }             
           
            int updateCount = stmt.executeUpdate(sql);
            if (updateCount == 0) {
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "saveToStore", "Update Failed!");
            }
            else {
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "saveToStore", "Update PASSED!");
            }
            
            conn.commit();
            
        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "saveToStore",
                      "##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "saveToStore",
                      "##############Exception:  " + exception.getMessage());

        }
    }
   
    public void deleteFromStore(Integer id) {
            
            try {
                if (this.getTitle().trim().length()==0) {
                    
                    Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "deleteFromStore",
                              "RECIPE ID:  " + id);        
                    
                    Connection conn = DBConnectionFactory.getConnection();
                    Statement stmt = conn.createStatement();            
                    ResultSet rs = stmt.executeQuery("SELECT * FROM RECIPES WHERE RID = " + id);
                    rs.beforeFirst();
                    if (rs.next()) {
                        String sql = "DELETE FROM RECIPES WHERE RID = " + id;    
                        int updateCount = stmt.executeUpdate(sql);
                        if (updateCount == 0) {
                            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "deleteFromStore",
                                      "Delete Failed!");
                        }
                        else {
                            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "deleteFromStore",
                                                  "DELETED Recipe ID: " + id);
                        }
                    }
                    conn.commit();
                }
            } catch (SQLException e) {
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "deleteIngredientFromStore",
                          "##############SQL Exception:  " + e.getMessage());
                e.printStackTrace();
            } catch (Exception exception) {
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "deleteIngredientFromStore",
                          "##############Exception:  " + exception.getMessage());

            }
        }
}
