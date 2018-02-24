package crud.JAVA;

//import com.gargoylesoftware.htmlunit.WebClient;
//import com.gargoylesoftware.htmlunit.html.HtmlPage;
//import crud.application.DBConnectionFactory;
//import crud.application.LifeCycleListenerImpl;
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.io.UnsupportedEncodingException;
//import java.net.HttpURLConnection;
//import java.net.InetSocketAddress;

import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import javax.el.ValueExpression;

/***
import java.util.Map;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import jxl.read.biff.BiffException;

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
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adfmf.amx.event.ActionEvent;
import oracle.adfmf.amx.event.ValueChangeEvent;
import oracle.adfmf.bindings.dbf.AmxAttributeBinding;
import oracle.adfmf.bindings.dbf.AmxCollectionModel;
import oracle.adfmf.bindings.dbf.AmxIteratorBinding;
import oracle.adfmf.bindings.dbf.AmxTreeBinding;
**/
import oracle.adfmf.bindings.iterator.BasicIterator;
import oracle.adfmf.dc.bean.ConcreteJavaBeanObject;
import oracle.adfmf.framework.FeatureContext;
import oracle.adfmf.framework.api.AdfmfContainerUtilities;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;
import oracle.adfmf.java.beans.ProviderChangeListener;
import oracle.adfmf.java.beans.ProviderChangeSupport;
import oracle.adfmf.util.Utility;
import oracle.adfmf.util.logging.Trace;
//import java.beans.PropertyChangeSupport;
//import java.beans.PropertyChangeListener;

public class RecipeService {
    
    private Recipe recipe;    
    private Ingredient ingredient;
    private List<Recipe> recipes = new ArrayList<Recipe>();
    private List<Ingredient> ingredients = new ArrayList<Ingredient>();
    private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private transient ProviderChangeSupport providerChangeSupport = new ProviderChangeSupport(this);
    List<Google> googleResults = new ArrayList<Google>(); //This will automatically produce CREATE, DELETE methods in Data Controls, Operations
    String googleCriteria = "";
    String selectedUrl = "";
    String selectedTitle = "";
    boolean displayUrl = true;
    boolean isPopupOpen = false;
    
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
        
        setRecipe(); //Set recipe based on pageFlowScope.selectedRid
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "######## deleteRecipe", 
                  recipe.getTitle() + " (" + recipe.getId() + ")");
        
        recipes.remove(recipe);
        providerChangeSupport.fireProviderRefresh("recipes");
        AdfmfJavaUtilities.flushDataChangeEvent();
        deleteRecipeFromStore();
    }
    
    public void deleteRecipeFromStore() {
        
        //ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.selectedRid}", Integer.class);
        //Object obj1 = ve.getValue(AdfmfJavaUtilities.getELContext());        
        //Integer recipeID = (Integer)obj1;         
        recipe.deleteFromStore(recipe.getId());
        //reloadRecipes();
    }

    public void reloadRecipes() {
        
        try {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "reloadRecipes", "");
            recipes.clear();
            recipe = new Recipe();
            recipes = recipe.getRecipesFromStore(); 
        }
        catch (Exception e){
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "reloadRecipes", e.getMessage());
        }
    }
    
    public void reloadIngredients() {

        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "reloadRecipes", "");
        ingredients.clear();
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
        
        
        //Ingredient ingredient= new Ingredient();
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
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "###prepareIngredientToEdit",
                  ingredientToEdit.getItem() + " (" + ingredientToEdit.getId() + ")");
        
        ingredient = ingredientToEdit;
        ingredient.newItem = ingredientToEdit.item;
    }
    
    public void prepareIngredientToAdd() {
        
        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.selectedRid}", Integer.class);
        Object obj1 = ve.getValue(AdfmfJavaUtilities.getELContext());  
        Integer recipeID = (Integer)obj1; 
        
        ingredient = new Ingredient();
        ingredient.setId();
        ingredient.setRid(recipeID);
        ingredient.setNewItem(" ");
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
    
    public void deleteIngredient() { 
        
        setIngredient(); //Set ingredient object based on pageFlowScope.selectedId
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "##############deleteIngredient", " Recipe:" 
                  + recipe.getTitle() + ", Ingredient :" + ingredient.item + "(" + ingredient.id + ")");
        
        Boolean success = ingredient.deleteIngredientFromStore(ingredient);
        if (success) {
            ingredients.remove(ingredient);
            providerChangeSupport.fireProviderRefresh("ingredients");
            AdfmfJavaUtilities.flushDataChangeEvent();
        }
    }
    
    private Ingredient getCurrentIngredientXXX(Integer id) {
        
        Ingredient ingredient = new Ingredient();
        for (Integer i = 1; i < ingredients.size(); i++) {
            ingredient = ingredients.get(i);
            if (ingredient.getId() == id) {
                return ingredient;
            }
        }
        return ingredient;
    }  
    
    
    public void clearGoogle() throws MalformedURLException, UnsupportedEncodingException, IOException {
        
        try {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "callGoogle",  "### criteria:  " + googleCriteria);
            this.googleCriteria = "";
            googleResults.clear();
            
            ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.prepTime}", String.class);
            ve.setValue(AdfmfJavaUtilities.getELContext(), "");
    
            ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.url}", String.class);
            ve.setValue(AdfmfJavaUtilities.getELContext(), "");
    
            ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.title}", String.class);
            ve.setValue(AdfmfJavaUtilities.getELContext(), "");
    
            ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.googleCriteria}", String.class);
            ve.setValue(AdfmfJavaUtilities.getELContext(), "");   
        }
        catch (Exception e){
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "clearGoogle", e.getMessage());
        }
    }
    
    public void callGoogle(String googleCriteria) throws MalformedURLException, UnsupportedEncodingException, IOException {
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "callGoogle",  "### criteria:  " + googleCriteria);
        this.googleCriteria = googleCriteria;
        googleResults = loadGoogleResultsFromWeb();
        providerChangeSupport.fireProviderRefresh("googleResults");
        AdfmfJavaUtilities.flushDataChangeEvent();
        
    }
    public List<Google> loadGoogleResultsFromWeb() throws MalformedURLException, UnsupportedEncodingException,   IOException {       
        
        List<Google> googleResults = new ArrayList<Google>();
        
        try {
            if (googleCriteria.trim().length()>0) {
                GoogleService gs = new GoogleService();        
                gs.setGoogleCriteria(googleCriteria);
                googleResults = gs.findSearchResults();      
                Utility.ApplicationLogger.severe("RecipeService - loadGoogleResultsFromWeb: '" + googleCriteria 
                                + "' count: " + googleResults.size());
            }
            else
                Utility.ApplicationLogger.severe("RecipeService - loadGoogleResultsFromWeb: 'CRITERIA NOT SPECIFIED'");
        }
        catch (Exception e) {
            Utility.ApplicationLogger.severe("RecipeService - loadGoogleResultsFromWeb: " + e.getMessage());
        }
        return googleResults;
    }
    
    public List<Google> getGoogleResults() throws MalformedURLException, UnsupportedEncodingException, IOException {
    
        if (googleResults.isEmpty()) 
            googleResults = loadGoogleResultsFromWeb();
        
       return googleResults;
    }     
    
    public void setGoogleResults(List<Google> googleResults) {
        
        //List<Google> oldGoogleResults = this.googleResults;
        this.googleResults = googleResults;
    } 
    
    public void openUrl(String url) {
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "openUrl",  "### URL:  " + url);
          
        try {
           
            if (url.trim().length()>0) { //Open Browser
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
    public Boolean processSelectedGoogleUrl(Google googleResult) throws MalformedURLException, ProtocolException, IOException {
        
        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.url}", String.class);
        Object obj1 = ve.getValue(AdfmfJavaUtilities.getELContext());        
        String parmUrl = (String)obj1; 
        Boolean success = false;
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "processSelectedGoogleUrl",googleResult.getUrl()
                  + ", parmUrl:" + parmUrl);
        
        try {
            if (googleResult.getUrl().trim().length()>0) { 
                Recipe recipe = new Recipe(googleResult.getUrl(), googleResult.getPrepTime(), googleResult.getTitle());
                success = recipe.saveRecipeToStore(recipe); 
                if (success) showPopup();
            }   
        }
        catch(Exception e)     {
            Utility.ApplicationLogger.severe("RecipeService - processSelectedGoogleUrl: NO RESULTS " + e.getMessage());    
        } 
        return success;
        
    }
    
    public void showPopup() {
        //Used by Google.amx
        if (!isPopupOpen) {
            Object errorMsg = AdfmfContainerUtilities.invokeContainerJavaScriptFunction(FeatureContext.getCurrentFeatureId(),
                           "popupUtilsShowPopup", new Object[] {"_popShowId" });
            isPopupOpen = true;
            //wait a few seconds and then close the popup
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                  closePopup();
                  this.cancel();
                }

            },2000);
        }
    }
    private void closePopup() {

        if (isPopupOpen) {
            Object errorMsg = AdfmfContainerUtilities.invokeContainerJavaScriptFunction(FeatureContext.getCurrentFeatureId(),
                   "popupUtilsHidePopup", new Object[] {"_popCloseId" });
            isPopupOpen = false;
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
    private String getCurrentUrlXXX() {
        
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
        
        setRecipe(); //Set recipe based on pageFlowScope.selectedRid
        
        setDisplayRecipeURL("false");
        
        String recipeTitle = recipe.getTitle();
        Integer recipeID = recipe.getId();
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "####prepareRecipeToEdit",
                  recipeTitle + " (" + recipeID + ")" + ", googleRecipe:" + recipe.getGoogleRecipe());
        
        //recipe = recipeToEdit;
    }
    
    public void setDisplayRecipeURL(String newMode) {
        
        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.newMode}", Boolean.class);
        String googleRecipe = recipe.getGoogleRecipe();
        String showUrl = "";
        
        ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.displayURL}", Boolean.class);
        if (newMode == "false" && googleRecipe.equals("N")) { 
            displayUrl = false;
            showUrl = "N";
            ve.setValue(AdfmfJavaUtilities.getELContext(), "false");
        }
        else  {
            displayUrl = true; 
            showUrl = "Y";
            ve.setValue(AdfmfJavaUtilities.getELContext(), "true"); 
        }
        
        //AdfmfJavaUtilities.flushDataChangeEvent();
        AdfmfContainerUtilities.invokeContainerJavaScriptFunction(FeatureContext.getCurrentFeatureId(),
                                   "showHideText", new Object[] {"_recipeUrl", showUrl});
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "#######setDisplayRecipeURL",
                  recipe.getTitle() + " (" + recipe.getId() + "), newMode:" + newMode 
                  + ", googleRecipe:" + googleRecipe
                  + ", displayURL:" + displayUrl);
    }
    
    public void prepareRecipeToAdd() {
        
        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.googleRecipe}", Boolean.class);
        Object obj = ve.getValue(AdfmfJavaUtilities.getELContext());        
        String googleRecipeObj = obj.toString();                
        String googleRecipe = "N";
        if (googleRecipeObj == "true") googleRecipe = "Y";
        
        recipe = new Recipe();
        recipe.setId();
        recipe.setTitle("");
        recipe.setRecipeUrl("");
        recipe.setDescription("");
        recipe.setPrepTime("");
        recipe.setGoogleRecipe(googleRecipe);
        ingredients.clear();
        
        setDisplayRecipeURL("true");
        
        providerChangeSupport.fireProviderRefresh("recipe");
        providerChangeSupport.fireProviderRefresh("ingredients");
        AdfmfJavaUtilities.flushDataChangeEvent();
        
        clearParms();        
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "###prepareRecipeToAdd",
                  recipe.getTitle() + " (" + recipe.getId() + ")" + ", googleRecipe:" + googleRecipe);
    }   
    
    public void saveRecipe(Recipe r) {
        
        ValueExpression veNewMode = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.newMode}", Boolean.class);
        Object obj = veNewMode.getValue(AdfmfJavaUtilities.getELContext());        
        String newMode = obj.toString();
        
        if (newMode == "true") {
            recipe.setTitle(recipe.getRecipeUrl());
            recipe.setRecipeUrl("");
            
            //recipe.setGoogleRecipe("N");
            //if (googleRecipe == "true") {
            //    recipe.setGoogleRecipe("Y");
            //} 
            
            insertRecipe(recipe);
            
            ValueExpression veRid = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.selectedRid}", Integer.class);
            veRid.setValue(AdfmfJavaUtilities.getELContext(), recipe.getId());
            
            setRecipe();
            
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "###saveRecipe",
                      recipe.getTitle() + " (" + recipe.getId() + ")" + ", googleRecipe:" + recipe.getGoogleRecipe());        
        }
        else {
            updateRecipe(recipe);    
        }

        ingredients.clear();
        
        veNewMode.setValue(AdfmfJavaUtilities.getELContext(), "false"); //Set newMode == false
        
        setDisplayRecipeURL("false");
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
    
    public void setIngredient() {
        
        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.selectedId}", Integer.class);
        Object obj1 = ve.getValue(AdfmfJavaUtilities.getELContext());        
        Integer IngredientId = (Integer)obj1; 
        
        for (Integer i=1; i<ingredients.size(); i++) {
             Ingredient ing = ingredients.get(i);
             if (ing.getId()==IngredientId) {
                 ingredient = ing;
                 break;
             }
         }        
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "##############setIngredient",  
            ingredient.getItem() + " (" + ingredient.getId() + "), Recipe ID," + recipe.getId()); 
        
    }
    
    public void setRecipe() {  
        
        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.selectedRid}", Integer.class);
        Object obj1 = ve.getValue(AdfmfJavaUtilities.getELContext());  
        Integer recipeID = (Integer)obj1; 
        
        for (Integer i=1; i<recipes.size(); i++) {
             Recipe r = recipes.get(i);
             if (r.getId()==recipeID) {
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
        
        //List<Ingredient> oldIngredients = this.ingredients;
        this.ingredients = ingredients;
        //propertyChangeSupport.firePropertyChange("ingredients", oldIngredients, ingredients);
    }

    public List<Ingredient> getIngredients() {        
        
        //ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.newModeIngredient}", Boolean.class);
        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.newMode}", Boolean.class);
        Object obj = ve.getValue(AdfmfJavaUtilities.getELContext());        
        String newMode = obj.toString();
        
        ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.googleRecipe}", Boolean.class);
        //obj = ve.getValue(AdfmfJavaUtilities.getELContext());        
        //String googleRecipe = obj.toString();
                
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "##############getIngredients", "URL: " 
            + recipe.getRecipeUrl() + ", Recipe ID," + recipe.getId()
            + ", newMode:" + newMode + ", googleRecipe:" + recipe.getGoogleRecipe()
            + ", ingredientsEmpty:" + ingredients.isEmpty());    
        
        if (newMode=="true") {
            //ingredients.clear();
        }
        else {
            //if(ingredients.isEmpty()) { Ingredients could be empty if you just added a recipe without ingredients
                ingredients = recipe.getIngredients();
            //}
        }        
        return ingredients;
    }
    
    public void addProviderChangeListener(ProviderChangeListener listener)  {
      providerChangeSupport.addProviderChangeListener(listener);
    }

    public void removeProviderChangeListener(ProviderChangeListener listener)   {
      providerChangeSupport.removeProviderChangeListener(listener);
    }

    public void setDisplayUrl(boolean displayUrl) {
        boolean oldDisplayUrl = this.displayUrl;
        this.displayUrl = displayUrl;
        propertyChangeSupport.firePropertyChange("displayUrl", oldDisplayUrl, displayUrl);
    }

    public boolean isDisplayUrl() {
        return displayUrl;
    }
}
