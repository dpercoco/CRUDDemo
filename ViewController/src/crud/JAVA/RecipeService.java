package crud.JAVA;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import crud.application.DBConnectionFactory;

import crud.application.LifeCycleListenerImpl;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;

import java.io.UnsupportedEncodingException;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;

import java.net.URLConnection;

import java.net.URLDecoder;
import java.net.URLEncoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import javax.el.ValueExpression;

import jxl.read.biff.BiffException;

import oracle.adf.model.binding.DCBindingContainer;

import oracle.adfmf.amx.event.ActionEvent;
import oracle.adfmf.amx.event.ValueChangeEvent;
import oracle.adfmf.bindings.dbf.AmxAttributeBinding;
import oracle.adfmf.bindings.dbf.AmxCollectionModel;
import oracle.adfmf.bindings.dbf.AmxIteratorBinding;
import oracle.adfmf.bindings.dbf.AmxTreeBinding;
import oracle.adfmf.bindings.iterator.BasicIterator;
import oracle.adfmf.dc.bean.ConcreteJavaBeanObject;
import oracle.adfmf.framework.FeatureContext;
import oracle.adfmf.framework.api.AdfmfContainerUtilities;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;
import oracle.adfmf.util.Utility;
import oracle.adfmf.util.logging.Trace;
//import java.beans.PropertyChangeSupport;
//import java.beans.PropertyChangeListener;

public class RecipeService {
    
    private Recipe recipe;    
    private Ingredient ingredient;
    private List<Recipe> recipes = new ArrayList<Recipe>();
    private List<Ingredient> ingredients = new ArrayList<Ingredient>();
    private static Map<String, Map<String, Ingredient>> shoppingItems = null;
    private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    //private transient ProviderChangeSupport providerChangeSupport = new ProviderChangeSupport(this);
    private static shoppingListProcess slp = new shoppingListProcess();
    List<Google> googleResults = new ArrayList<Google>(); //This will automatically produce CREATE, DELETE methods in Data Controls, Operations
    String googleCriteria = "";
    String selectedUrl = "";
    String selectedTitle = "";
    
    public RecipeService() {
        super();
    }
    public List<Recipe> getRecipes() {

        /**
        if (initCount==0) {
            Utility.ApplicationLogger.severe("****** Loading FOOD GROUPS!!! ");
            AisleService aisle = new AisleService();
            aisle.loadFoodGroups_XML(1);
        }
        initCount++;
        **/
    
        if (recipes.isEmpty()) {
            executeQuery();
        }
        return recipes;
    }
    
    
    public void deleteRecipe() {
        
        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.selectedRid}", Integer.class);
        Object obj1 = ve.getValue(AdfmfJavaUtilities.getELContext());        
        Integer recipeID = (Integer)obj1;         
        recipe.deleteFromStore(recipeID);
    }

    public void reloadRecipes() {

        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "reloadRecipes", "");
        recipes.clear();
        recipe = new Recipe();
        recipes = recipe.getRecipesFromStore();
        
    }
    
    public void reloadIngredients() {

        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "reloadRecipes", "");
        ingredients.clear();
        recipe = new Recipe();
        ingredients = recipe.getIngredientsFromStore();
        
    }
    
    public void executeQuery() {

        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "executeQuery", "");
        recipes.clear();
        recipe = new Recipe();        
        recipes = recipe.getRecipesFromStore();
    }

    // This method is called by the framework when the Create operation is called
    // The framework will take care of firing the provider change event for us
    
    public void addRecipe(Recipe recipe) {
        
        Recipe newRecipe = new Recipe();
        newRecipe = recipe.createRecipe(recipe);
    }    
    
    public void setCurrentIngredientIterator() {
        
        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.selectedId}", Integer.class);
        Object obj1 = ve.getValue(AdfmfJavaUtilities.getELContext());        
        Integer ingredientID = (Integer)obj1; 
        
        //ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.selectedRowKey}", Integer.class);
        //Object obj2 = ve.getValue(AdfmfJavaUtilities.getELContext());        
        //Integer rowKey = (Integer)obj2; 
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "setCurrentIngredient",
                  "INGREDIENT ID:  " + ingredientID);        
        
        try  { 
            BasicIterator iterator = (BasicIterator) AdfmfJavaUtilities.getELValue("#{bindings.ingredients.iterator}"); 
            if (iterator!=null){
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "############## setCurrentIngredient", " Found ingredients.Iterator");
                //iterator.setCurrentRowWithKey("");
                iterator.setCurrentIndex(ingredientID);
            } 
        }
        catch(Exception e)     {
            Utility.ApplicationLogger.severe("##########RecipeService - setCurrentIngredient: ERROR while loading {bindings.ingredients} " + e.getMessage());    
        }
        
    }
    
    private void getIngredientIteratorXXX () {
        
        BasicIterator iterator = (BasicIterator) AdfmfJavaUtilities.getELValue("#{bindings.ingredients.iterator}"); 
        if (iterator!=null){
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "##############getIngredientIterator", " Found ingredients.Iterator");
            //iterator.setCurrentRowWithKey((String) currentRowKey);
        }
        
        /**
        BasicIterator basicIterator = null;
        try  { 
            AmxAttributeBinding ingredientList = (AmxAttributeBinding) AdfmfJavaUtilities.getELValue("#{bindings.ingredientsIterator}");
            //AmxTreeBinding ingredientList = (AmxTreeBinding) AdfmfJavaUtilities.getELValue("#{bindings.ingredients}");
            AmxIteratorBinding amxListIterator =  ingredientList.getIteratorBinding();
            basicIterator = amxListIterator.getIterator();
            
            if (basicIterator.getRangeSize()>0) {
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "##############getIngredientIterator", " Found ingredients.Iterator");
                /**
                for (Integer i=1; i<basicIterator.getRangeSize(); i++) {
                    Ingredient ingredient = (Ingredient) basicIterator.getDataProvider();
                    if (ingredient.getId() == IngredientId) {
                        basicIterator.setCurrentIndex(i);   
                        //basicIterator.removeCurrentRow();
                        //ingredients.remove(ingredient);
                        //AdfmfJavaUtilities.setELValue("ingredients", ingredients);
                        break;
                    }
                }
                **/
            //}
        //}
        //catch(Exception e)     {
        //    Utility.ApplicationLogger.severe("##########RecipeService - setCurrentIngredient: ERROR while loading {bindings.ingredients} " + e.getMessage());    
        //}
        
        
        Ingredient ingredient= new Ingredient();
        //ingredient = ingredients.get(IngredientId);
        //ingredients.set(IngredientId, ingredient);
        //Iterator it = ingredients.iterator();        
               
        /**
        Iterator it = ingredients.iterator();        
        try  {
            Ingredient obj;
            while (it.hasNext()) {
                 obj = (Ingredient) it.next();
                 if (obj.getId() == IngredientId) {
                    break;
                }
            }
        }
        catch(Exception e)     {
        }
        **/
        
    }
    
    public void prepareIngredientToEdit(Ingredient ingredientToEdit) throws CloneNotSupportedException {
        ingredient = ingredientToEdit;
        ingredient.newItem = ingredient.item;
    }
    
    public void prepareIngredientToAdd() {
        
        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.selectedRid}", Integer.class);
        Object obj1 = ve.getValue(AdfmfJavaUtilities.getELContext());  
        Integer recipeID = (Integer)obj1; 
        
        ingredient = new Ingredient();
        ingredient.setId();
        ingredient.setRid(recipeID);
        ingredient.setNewItem("new");
        ingredient.setItem(" ");
        ingredient.setFoodGroup("Misc");
        
    }    
    
    public void saveIngredient(Ingredient ingredient) {
        
        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.newModeIngredient}", Boolean.class);
        Object obj = ve.getValue(AdfmfJavaUtilities.getELContext());        
        String newMode = obj.toString();
        
        if (newMode == "true") {
            insertIngredient(ingredient);   
        }
        else {
            updateIngredient(ingredient);    
        }
        
        ve.setValue(AdfmfJavaUtilities.getELContext(), "false");
    }
    
    private void insertIngredient(Ingredient ingredient) {
        boolean success = ingredient.saveIngredientToStore(ingredient);
        if (success) {
            //ingredients.add(ingredient);
            ingredients.clear();
            getIngredients(); //To ensure sort order by foodGroup
        }
    }

    private void updateIngredient(Ingredient ingredient) {
        boolean success = ingredient.saveIngredientToStore(ingredient);
        if (success) {
            int index = ingredients.indexOf(ingredient);
            ingredients.set(index, ingredient);
        }
    }
    public Ingredient getIngredient() {
        return ingredient;
    }
    
    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }
    
    public void deleteIngredient() { //MOVE TO Ingredient.java
        
        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.selectedId}", Integer.class);
        Object obj1 = ve.getValue(AdfmfJavaUtilities.getELContext());        
        Integer IngredientId = (Integer)obj1; 
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "##############deleteIngredient", " Recipe:" + recipe.getTitle()
                  + ", Ingredient ID:" + IngredientId);
        
        Ingredient ingredient = getCurrentIngredient(IngredientId);
        Boolean success = ingredient.deleteIngredientFromStore(ingredient);
        
        //reloadIngredients(); NOT NECESSARY
        
        /**
        if (success) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "deleteIngredient", " Recipe:" + recipe.getTitle()
                      + ", Number of ingredients (before):" + ingredients.size());
            ingredients.remove(ingredient);
            
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "deleteIngredient", " Recipe:" + recipe.getTitle()
                      + ", Number of ingredients (after):" + ingredients.size());
            
            //AdfmfJavaUtilities.setELValue("ingredients", ingredients);
            //setIngredients(newIngredients);
            //providerChangeSupport.fireProviderDelete("ingredients", rowKey);
            //providerChangeSupport.fireProviderRefresh("ingredients");
            //propertyChangeSupport.firePropertyChange("ingredients", null, ingredients);
        }
        **/
    }
    
    private Ingredient getCurrentIngredient(Integer id) {
        
        Ingredient ingredient = new Ingredient();
        for (Integer i = 1; i < ingredients.size(); i++) {
            ingredient = (Ingredient) ingredients.get(i);
            if (ingredient.getId() == id) {
                return ingredient;
            }
        }
        return ingredient;
    }  
    
    public void callGoogle() throws MalformedURLException, UnsupportedEncodingException, IOException {
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "callGoogle",  "### criteria:  " + googleCriteria);
        
        googleResults.clear();
        GoogleService gs = new GoogleService();
        
        gs.setGoogleCriteria(googleCriteria);
        setGoogleResults(gs.findSearchResults());        
    }
    
    public void setGoogleResults(List<Google> googleResults) {
        
        List<Google> oldGoogleResults = this.googleResults;
        this.googleResults = googleResults;
        Utility.ApplicationLogger.severe("RecipeService - setGoogleResults: '" + googleCriteria 
                        + "' count: " + this.googleResults.size());
    } 
    
    public void openUrl(String url) {
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "openUrl",  "### URL:  " + url);
          
        try {
           
            if (url.trim().length()>0) {
                AdfmfContainerUtilities.invokeContainerJavaScriptFunction(
                    AdfmfJavaUtilities.getFeatureId(), "displayUrl", new Object[] {url});
                
                //AdfmfContainerUtilities.invokeContainerJavaScriptFunction(
                //    AdfmfJavaUtilities.getFeatureId(), "function(){cordova.InAppBrowser.open('" + 
                //                url + "','_blank','location=yes');}", new Object[] {});
            }
            else {
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "openUrl",
                          "##############ERROR: URL IS BLANK ");    
            }
        
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "openUrl",
                      "##############Exception:  " + exception.getMessage());

        }   
    }
    public void processSelectedGoogleUrl(String urlIn) throws MalformedURLException, ProtocolException, IOException {
        
        /** Show on EditRecipe.amx page **/
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "processSelectedGoogleUrl","");
        
        try {
            ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.url}", String.class);
            Object obj1 = ve.getValue(AdfmfJavaUtilities.getELContext());
            String url = obj1.toString();
            
            if (url.trim().length()>0) {
                String prepTime="";
                GoogleService gs = new GoogleService();
                prepTime = gs.getPrepTime(url);
                ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.prepTime}", String.class);            
                ve.setValue(AdfmfJavaUtilities.getELContext(), prepTime);
                            
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "processSelectedGoogleUrl",  "URL:  " + url );             
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "processSelectedGoogleUrl",  "prepTime:  " + prepTime );          
            }   
        }
        catch(Exception e)     {
            Utility.ApplicationLogger.severe("RecipeService - processSelectedGoogleUrl: NO RESULTS " + e.getMessage());    
        }                
    }
    
    public void clearParms() {
     
        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.title}", String.class);
        ve.setValue(AdfmfJavaUtilities.getELContext(), "");
        
        ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.url}", String.class);
        ve.setValue(AdfmfJavaUtilities.getELContext(), "");
        
        ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.prepTime}", String.class);
        ve.setValue(AdfmfJavaUtilities.getELContext(), "");
             
    }
    private String getCurrentUrl() {
        
        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{bindings.googleResultsIterator.currentRow.dataProvider}",  Object.class);
        Object obj = ve.getValue(AdfmfJavaUtilities.getELContext());
        Recipe r = new Recipe(); 
        if (obj instanceof ConcreteJavaBeanObject) {
            ConcreteJavaBeanObject cjbo = (ConcreteJavaBeanObject)obj;
            r = (Recipe)cjbo.getInstance();
        }
        String url = r.getRecipeUrl(); 
        return url;
        
    }
    
    /**
    public void processSelectedSearchResults() throws MalformedURLException, ProtocolException, IOException {
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "processSelectedSearchResults",
                  "READING SELECTED GOOGLE RESULTS:  ");    
        
        try {
            GoogleService gs = new GoogleService();
            //gs.processSelectedSearchResults();
        }
        catch(Exception e)     {
            Utility.ApplicationLogger.severe("RecipeService - processSelectedSearchResults: NO RESULTS " + e.getMessage());    
        }                
    }
    **/
    
    public List<Google> getGoogleResults() {
        return googleResults;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    public void setGoogleCriteria(String googleCriteria) {
        String oldGoogleCriteria = this.googleCriteria;
        this.googleCriteria = googleCriteria;
        propertyChangeSupport.firePropertyChange("googleCriteria", oldGoogleCriteria, googleCriteria);
    }

    public String getGoogleCriteria() {
        return googleCriteria;
    }

    public void setSelectedUrl(String selectedUrl) {
        
        String oldSelectedUrl = this.selectedUrl;
        this.selectedUrl = selectedUrl;
        propertyChangeSupport.firePropertyChange("selectedUrl", oldSelectedUrl, selectedUrl);
    }

    public String getSelectedUrl() {
        return selectedUrl;
    }
    
    public void setSelectedTitle(String selectedTitle) {
        
        String oldSelectedTitle = this.selectedTitle;
        this.selectedTitle = selectedTitle;
        propertyChangeSupport.firePropertyChange("selectedTitle", oldSelectedTitle, selectedTitle);
    }

    public String getSelectedTitle() {
        return selectedTitle;
    }   
    
    public void prepareRecipeToEdit(Recipe recipeToEdit) throws CloneNotSupportedException {
        recipe = recipeToEdit;
    }
    
    public void prepareRecipeToAdd() {
        
        recipe = new Recipe();
        recipe.setId();
        
    }   
    
    public void saveRecipe(Recipe recipe) {
        
        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.newMode}", Boolean.class);
        Object obj = ve.getValue(AdfmfJavaUtilities.getELContext());        
        String newMode = obj.toString();
        
        if (newMode == "true") {
            insertRecipe(recipe);   
        }
        else {
            updateRecipe(recipe);    
        }
        
        ve.setValue(AdfmfJavaUtilities.getELContext(), "false");
    }
    
    private void insertRecipe(Recipe recipe) {
        boolean success = recipe.saveRecipeToStore(recipe);
        if (success) {
            recipes.add(recipe);
        }
    }

    private void updateRecipe(Recipe recipe) {
        boolean success = recipe.saveRecipeToStore(recipe);
        if (success) {
            int index = recipes.indexOf(recipe);
            recipes.set(index, recipe); 
        }
    }
    
    public void setRecipe() {  
        
        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.selectedRid}", Integer.class);
        Object obj1 = ve.getValue(AdfmfJavaUtilities.getELContext());  
        Integer recipeID = (Integer)obj1; 
        
        for (Integer i=1; i<recipes.size(); i++) {
             Recipe r = (Recipe) recipes.get(i);
             if (r.getId()==recipeID) {
                 //recipe = recipes.get(recipeID);
                 recipe = r;
                 break;
             }
         }        
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "##############setRecipe",  
            recipe.getTitle() + ", Recipe ID," + recipe.getId());  
    }
        
    public void setRecipe(Recipe recipe) {
        Recipe oldRecipe = this.recipe;
        this.recipe = recipe;
        propertyChangeSupport.firePropertyChange("recipe", oldRecipe, recipe);
    }

    public Recipe getRecipe() {
        return recipe;
    }
    
    public void setIngredients(List<Ingredient> ingredients) {
        List<Ingredient> oldIngredients = this.ingredients;
        this.ingredients = ingredients;
        //propertyChangeSupport.firePropertyChange("ingredients", oldIngredients, ingredients);
    }

    public List<Ingredient> getIngredients() {
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "##############getIngredients", "URL: " 
            + recipe.getRecipeUrl() + ", Recipe ID," + recipe.getId());       
        
        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.newModeIngredient}", Boolean.class);
        Object obj = ve.getValue(AdfmfJavaUtilities.getELContext());        
        String newMode = obj.toString();
        
        if (newMode=="true") {
            //ingredients.clear();
        }
        else {
            if(ingredients.isEmpty()) {
                ingredients = recipe.getIngredients();
            }
        }
        
        return ingredients;
    }

    
}
