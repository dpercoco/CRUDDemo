package crud.JAVA;
/**
 * https://github.com/google/search-samples/blob/master/app-indexing/app/src/main/java/com/recipe_app/client/Recipe.java
 */

import crud.application.DBConnectionFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.el.ValueExpression;

import oracle.adfmf.bindings.dbf.AmxIteratorBinding;
import oracle.adfmf.bindings.dbf.AmxTreeBinding;
import oracle.adfmf.bindings.iterator.BasicIterator;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;
import oracle.adfmf.java.beans.ProviderChangeListener;
import oracle.adfmf.java.beans.ProviderChangeSupport;
import oracle.adfmf.util.Utility;
import oracle.adfmf.util.logging.Trace;
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
   private String googleRecipe="";
   private List<Ingredient> recipeIngredients = new ArrayList<Ingredient>();
   private static Map<String, Map<String, Ingredient>> shoppingItems = null;
   private static shoppingListProcess slp = new shoppingListProcess();
   private static AisleService aisle = new AisleService();
   protected transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
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
       this.id = -1;
       this.title = "";
       this.recipeUrl = ""; 
       this.prepTime = "";
   }
   
   public Recipe(int id, String title, String url) {
         super();
         this.id = id;
         this.title = title;
         this.recipeUrl = url; 
    } 
   public Recipe(int id, String title, String url, String description, String prepTime, String googleRecipe) {
         super();
         this.id = id;
         this.title = title;
         this.recipeUrl = url; 
         this.description = description;
         this.prepTime = prepTime;
         this.googleRecipe = googleRecipe;
    } 
   public Recipe(int id) {
       this.id = id;
   }
   public int getId() {
       return id;
   }
   public void setId() {
        this.id = getRecipeID();
   }
   public Integer getRecipeID(){
        
        Integer newId=0;       
    
        try {
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT MAX(RID) FROM RECIPES");
            rs.beforeFirst();
            if (rs.next()) {
                newId = rs.getInt(1) + 1;
            }
        }
        catch (Exception e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "getRecipeID",
                      "#####EXCEPTION RECIPE ID:" + newId + " " + e.getMessage()); 
        }  
        return newId;
        
    }
   public void setId(int id) {
        int oldId = this.id;
        this.id = id;
        propertyChangeSupport.firePropertyChange("id", oldId, id);
    }
   public String getTitle() {
       
       if (title.trim().length()==0) return "";
       title = title.replace("\"", "");
       title = title.replace("'","");
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
       recipeUrl = recipeUrl.replace("'","''");
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
       
       description = description.replace("\"", "");
       description = description.replace("'","");
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
   public String getKey() {
        Integer i = new Integer(id);
        return i.toString();        
   } 
    
   public List<Ingredient> getIngredients() {
       return getIngredientsFromStore();  
   }
   
   public Recipe createRecipe(Recipe recipe) {
       
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "addRecipe", recipe.getTitle());
        //Recipe recipe = new Recipe();
        try {
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            int newId = 1;

            ResultSet rs = stmt.executeQuery("SELECT MAX(RID) FROM RECIPES");
            rs.beforeFirst();
            if (rs.next()) {
                newId = rs.getInt(1) + 1;
            }
            recipe.setId(newId);

        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "addRecipe",
                      "##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "addRecipe",
                      "##############Exception:  " + exception.getMessage());
        }
        return recipe;
    }
   
    public Recipe (String url, String prepTime, String title) {
        
       //Recipe recipe = new Recipe(); 
       this.setId(0);
       this.setPrepTime(prepTime);
       this.setRecipeUrl(url);
       this.setTitle(title);
       
   }
    
   public List<Recipe> getRecipesFromStore() {

        List<Recipe> recipes = new ArrayList<Recipe>();
        try {
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM RECIPES ORDER BY TITLE");

            rs.beforeFirst();
            while (rs.next()) {
                int id = rs.getInt("RID");
                String title = rs.getString("TITLE");
                if (title.equals("null")) {
                    title = "";
                }
                String url = rs.getString("RECIPEURL");
                if (url.equals("null")) {
                    url = "";
                }
                String description = rs.getString("DESCRIPTION");
                if (description.equals("null")) {
                    description = "";
                }
                String preptime = rs.getString("PREPTIME");
                if (preptime.equals("null")) {
                    preptime = "";
                }
                String googleRecipe = rs.getString("GOOGLERECIPE");
                if (googleRecipe.equals("null")) {
                    googleRecipe = "";
                }
                Recipe r = new Recipe(id, title, url, description, preptime, googleRecipe);

                if (!title.isEmpty()) {
                    recipes.add(r);
                }
            }
            //providerChangeSupport.fireProviderRefresh("recipes");

        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "reloadRecipes",
                      "##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "reloadRecipes",
                      "##############Exception:  " + exception.getMessage());

        }
        return recipes;
    }
    public List<Ingredient> getIngredientsFromStore() {
        
        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.selectedRid}", Integer.class);
        Object obj1 = ve.getValue(AdfmfJavaUtilities.getELContext());
        Integer recipeId = (Integer) obj1;
        this.id = recipeId;

        Utility.ApplicationLogger.severe("getIngredientsFromStore RECIPE# " + this.getId());
        recipeIngredients.clear();
        
        try {
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM INGREDIENTS WHERE RID =" + this.getId();
            ResultSet result = stmt.executeQuery(sql);
            result.beforeFirst();

            if (!result.next() && this.getRecipeUrl()!=null) {
                getIngredientsFromWeb(this.getRecipeUrl());
            }

            getIngredientsFromDB();
            Utility.ApplicationLogger.severe("Recipe #" + this.getId() + " Ingredients: " + recipeIngredients.size());

        } catch (Exception ex) {
            Utility.ApplicationLogger.severe("EXCEPTION" + " " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        return recipeIngredients;
    }

   private void getIngredientsFromDB() {
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "##############getIngredientsFromDB", "RECIPE# " + id);
        
        recipeIngredients.clear();
        
        try {
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM INGREDIENTS WHERE RID =" + id + " ORDER BY FOODGROUP, IID";;
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
                recipeIngredients.add(ingredient); 
                //Utility.ApplicationLogger.severe("Ingredient #" + ingredient.getId() + " " + ingredient.getItem());
            }           
            Utility.ApplicationLogger.severe("Recipe #" + this.getId() + " Ingredients: " + recipeIngredients.size());
            
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
        int endOfIngredients;        
        Utility.ApplicationLogger.severe("getIngredientsFromHTTP: " + recipeUrl);
        
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
                  Utility.ApplicationLogger.severe("getIngredientsFromHTTP: READING BUFFER!");
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
                  
                  Utility.ApplicationLogger.severe("getIngredientsFromHTTP: FINISHED READING BUFFER!");
                  
                  String content = contentBuilder.toString();
                  if (endOfIngredients>-1) { //NEW JAN 2018
                      content = content.substring(0,endOfIngredients);                  
                      shoppingListProcess.findIngredients(content);
                      shoppingItems = slp.getShoppingItems();
                      Utility.ApplicationLogger.severe("getIngredientsFromHTTP: ShoppingItems Count:" + shoppingItems.size() + "\n");
                      if (shoppingItems.size()>0)
                        loadIngredientsIntoDB ();
                  }
                  else {
                      Utility.ApplicationLogger.severe("getIngredientsFromHTTP: ##### DID NOT FIND ANY INGREDIENTS!");
                  }
              }
        }
        catch(Exception e)     {
            Utility.ApplicationLogger.severe("getIngredientsFromWeb: ERROR " + recipeUrl + " " + e.getMessage());    
        }
        
    }
   
   private void getIngredientsFromWeb(String recipeUrl) throws IOException {
        
        if (recipeUrl.trim().length()==0)   {
            Utility.ApplicationLogger.severe("RECIPE - getIngredientsFromWeb: BLANK RECIPE URL");
            return;   
        }
        
        Utility.ApplicationLogger.severe("RECIPE - getIngredientsFromWeb: " + recipeUrl);
    
        boolean bKeepGoing=true;
        int ct=0;
        String content = "";
        while (bKeepGoing) {
                ct++;
                if (ct==1 | ct==3) content = setContentFromJSoup(recipeUrl);
                if (ct==2 | ct==4) content = setContentFromUrl(recipeUrl);
                if (content.trim().length()>0 | ct==4) bKeepGoing = false;
        }
        
        if (content.trim().length()>0) {
            slp.setContent(content);
            shoppingItems = slp.getShoppingItems();
            if (shoppingItems!=null) {
                Utility.ApplicationLogger.severe("getIngredientsFromWeb: " + recipeUrl + " ###ShoppingItems Count:" + shoppingItems.size() + "\n");
                
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
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "loadIngredientsIntoDB",
                  "recipeID - " + id + ", shopping Items: " + shoppingItems.size());
        
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
                //Utility.ApplicationLogger.severe("Recipe.addIngredientLocal Inserted RECIPE ID:" + id + ", INGREDIENT ID:" + newId + " " + ingredient.getItem() + "\n" + sql);
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
   
   public void reloadIngredientsXXX(Integer rid) {
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "reloadIngredients",
                  " ***** Recipe:" + rid);
        
        this.id = rid;
        recipeIngredients.clear();
        getIngredientsFromDB(); 
        //propertyChangeSupport.firePropertyChange("ingredients", null, recipeIngredients);
        //providerChangeSupport.fireProviderRefresh("ingredients"); //Used to fire changes made to a collection
        
    }    
  
   public void removeIngredient() throws MalformedURLException, ProtocolException, IOException {
        
        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.selectedId}", Integer.class);
        Object obj1 = ve.getValue(AdfmfJavaUtilities.getELContext());        
        Integer ingredientId = (Integer)obj1; 
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "removeIngredient",
                  "INGREDIENT ID: " + ingredientId);    
        
       for (Integer i=1; i<recipeIngredients.size(); i++) {
            Ingredient ingredient = (Ingredient) recipeIngredients.get(i);
            if (ingredient.getId() == ingredientId) {
                recipeIngredients.remove(ingredient);
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "removeIngredient",
                          "##############REMOVED INGREDIENT ID: " + ingredientId + ", " + ingredient.getItem()); 
                break;
            }
        }
        
        propertyChangeSupport.firePropertyChange("ingredients", null, recipeIngredients);
        //providerChangeSupport.fireProviderRefresh("ingredients");
        
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
  
    
    public void reloadFromStoreXXX() {
            
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
    
    public Boolean saveRecipeToStore(Recipe recipe) {
        
        Boolean success = false;
        String sqlType = "UPDATE";
        this.id = recipe.id;
        this.title = recipe.title;
        this.recipeUrl = recipe.recipeUrl;
        this.description = recipe.description;
        this.prepTime = recipe.prepTime;
        int newId = 1;

        try {
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            
            ResultSet rs = stmt.executeQuery("SELECT MAX(RID) FROM RECIPES");
            rs.beforeFirst();
            if (rs.next()) {
                newId = rs.getInt(1) + 1;
                this.id = newId;
            }
            
        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "addRecipe",
                      "##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "addRecipe",
                      "##############Exception:  " + exception.getMessage());
        }
        
        try {            
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "saveRecipeToStore", "Recipe: " + this.id + ", " + this.title);
            
            //RecipeService rSvc = new RecipeService();
            //rSvc.clearParms();
            
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = null;
            String sql = "DELETE FROM RECIPES WHERE RID = " + this.id;
            
            if (this.title.length()==0) {
                rs = stmt.executeQuery(sql);
                conn.commit();
                return false;
            }
            
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "saveToStore", "title: " + this.getTitle()
                        + ", url: " + this.getRecipeUrl() + ",ID:" + this.id);
            
            rs = stmt.executeQuery("SELECT * FROM RECIPES WHERE RID = " + this.id);
            rs.beforeFirst();
            if (rs.next()) { 
                sql = 
                        "UPDATE RECIPES SET TITLE='" + this.getTitle() + "', RECIPEURL='" + this.getRecipeUrl() +
                        "', DESCRIPTION='" + this.getDescription() + "', PREPTIME='" + this.getPrepTime() + 
                        "' WHERE RID=" + this.id;
            } else {
                sqlType = "INSERT";
                sql =                    
                        "INSERT INTO RECIPES (RID,TITLE,RECIPEURL,DESCRIPTION,PREPTIME) VALUES (" +
                        this.id + ",'" + this.getTitle() + "','" + this.getRecipeUrl() + "','" + 
                        this.getDescription() + "','" + getPrepTime() + "')";
            }             
           
            int updateCount = stmt.executeUpdate(sql);
            if (updateCount == 0) {
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "saveRecipeToStore", sqlType + " Failed! Recipe ID:" + this.id + " " + this.title + ", prepTime:" + this.prepTime);
            }
            else {
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "saveRecipeToStore", sqlType + " PASSED! Recipe ID:" + this.id + " " + this.title + ", prepTime:" + this.prepTime);
                success = true;
            }
            
            conn.commit();

            ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.id}", String.class);
            ve.setValue(AdfmfJavaUtilities.getELContext(), newId);

            ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.url}", String.class);
            ve.setValue(AdfmfJavaUtilities.getELContext(), "");

            ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.title}", String.class);
            ve.setValue(AdfmfJavaUtilities.getELContext(), "");
            
            ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.prepTime}", String.class);
            ve.setValue(AdfmfJavaUtilities.getELContext(), "");

            
        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "saveRecipeToStore",
                      "##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "saveRecipeToStore",
                      "##############Exception:  " + exception.getMessage());

        }
        return success;
    }
   
    public void deleteFromStore(Integer id) {

        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "deleteFromStore", "RECIPE ID:  " + id);

        try {
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM RECIPES WHERE RID = " + id);
            rs.beforeFirst();
            if (rs.next()) {
                //if (rs.getString("TITLE").trim().length()==0) {
                    String sql = "DELETE FROM RECIPES WHERE RID = " + id;
                    int updateCount = stmt.executeUpdate(sql);
                    if (updateCount == 0) {
                        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "deleteFromStore",
                                  "Delete Failed!");
                    }
                    else {
                        Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "deleteFromStore",
                                  "Delete Passed!");
                    }
                //}
            }
            conn.commit();
            //}
        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "deleteRecipe",
                      "##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "deleteRecipe",
                      "##############Exception:  " + exception.getMessage());

        }
    }
    
    public void setGoogleRecipe(String googleRecipe) {
        
        String oldGoogleRecipe = this.googleRecipe;
        this.googleRecipe = googleRecipe;
        propertyChangeSupport.firePropertyChange("googleRecipe", oldGoogleRecipe, googleRecipe);
    }
    
    public String getGoogleRecipe() {
        return googleRecipe;
    }
}
