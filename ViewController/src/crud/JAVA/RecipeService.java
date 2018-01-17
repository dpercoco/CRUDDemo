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
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import javax.el.ValueExpression;

import jxl.read.biff.BiffException;

import oracle.adfmf.amx.event.ActionEvent;
import oracle.adfmf.amx.event.ValueChangeEvent;
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
import oracle.adfmf.java.beans.ProviderChangeListener;
import oracle.adfmf.java.beans.ProviderChangeSupport;
import oracle.adfmf.util.Utility;
import oracle.adfmf.util.logging.Trace;

import oracle.net.aso.s;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.android.AndroidDriver;
import org.openqa.selenium.android.library.AndroidWebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.html5.BrowserConnection;

public class RecipeService {

    List<Recipe> recipes = new ArrayList<Recipe>();
    private Integer initCount = 0;
    private static Map<String, Map<String, Ingredient>> shoppingItems = null;
    private transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    //protected transient ProviderChangeSupport providerChangeSupport = new ProviderChangeSupport(this);
    private static shoppingListProcess slp = new shoppingListProcess();
    List<Google> googleResults = new ArrayList<Google>(); //This will automatically produce CREATE, DELETE methods in Data Controls, Operations
    String googleCriteria = "";
    String selectedUrl = "";
    
    public RecipeService() {
        super();
    }
/**
    public void addProviderChangeListener(ProviderChangeListener l) {
        providerChangeSupport.addProviderChangeListener(l);
    }

    public void removeProviderChangeListener(ProviderChangeListener l) {
        providerChangeSupport.removeProviderChangeListener(l);
    }
**/
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

    public void executeQuery() {

        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "executeQuery", "");
        recipes.clear();
        
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

                Recipe r = new Recipe(id, title, url, description, preptime);

                if (!title.isEmpty()) {
                    recipes.add(r);
                }
                //else {
                //removeRecipe(r);
                //}
            }
            //providerChangeSupport.fireProviderRefresh("recipe");

        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "executeQuery",
                      "##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "executeQuery",
                      "##############Exception:  " + exception.getMessage());

        }
    }

    // This method is called by the framework when the Create operation is called
    // The framework will take care of firing the provider change event for us
    public void addRecipe(Recipe recipe) {

        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "addRecipe", recipe.getTitle());

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
            
            ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.id}", String.class);
            ve.setValue(AdfmfJavaUtilities.getELContext(), newId);
            
            ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.url}", String.class);
            ve.setValue(AdfmfJavaUtilities.getELContext(), "");
            
            ve = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.title}", String.class);
            ve.setValue(AdfmfJavaUtilities.getELContext(), "");
            
        } catch (SQLException e) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "addRecipe",
                      "##############SQL Exception:  " + e.getMessage());
            e.printStackTrace();
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "addRecipe",
                      "##############Exception:  " + exception.getMessage());

        }
    }
    
    public void deleteRecipe(Integer id) {

        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "deleteRecipe", "RECIPE ID:  " + id);

        try {
            Connection conn = DBConnectionFactory.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM RECIPES WHERE RID = " + id);
            rs.beforeFirst();
            if (rs.next()) {
                if (rs.getString("TITLE").trim().length()==0) {
                    String sql = "DELETE FROM RECIPES WHERE RID = " + id;
                    int updateCount = stmt.executeUpdate(sql);
                    if (updateCount == 0) {
                        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "deleteRecipe",
                                  "Delete Failed!");
                    }
                }
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
    
    public void refreshIngredients() {
            
         Trace.log(Utility.ApplicationLogger, Level.SEVERE, Recipe.class, "refreshIngredients", "");
         //propertyChangeSupport.firePropertyChange("ingredients", null, ingredients);
         //providerChangeSupport.fireProviderRefresh("ingredients");
    }
    
    public void removeRecipe(Recipe recipe) {
        recipe.deleteFromStore(recipe.getId());
    }

    public void reloadRecipes() {

        Trace.log(Utility.ApplicationLogger, Level.SEVERE, RecipeService.class, "reloadRecipes", "");
        recipes.clear();

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

                Recipe r = new Recipe(id, title, url, description, preptime);

                if (!title.isEmpty()) {
                    recipes.add(r);
                //} else {
                //    removeRecipe(r);
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
        
        //providerChangeSupport.fireProviderRefresh("googleResults");
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
}
