package crud.JAVA;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import javax.el.ValueExpression;
import oracle.adfmf.amx.event.ActionEvent;
import oracle.adfmf.amx.event.ValueChangeEvent;
import oracle.adfmf.bindings.DataControl;
import oracle.adfmf.bindings.dbf.AmxCollectionModel;
import oracle.adfmf.bindings.iterator.BasicIterator;
import oracle.adfmf.dc.bean.ConcreteJavaBeanObject;
import oracle.adfmf.framework.FeatureContext;
import oracle.adfmf.framework.api.AdfmfContainerUtilities;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;
import oracle.adfmf.util.Utility;
import oracle.adfmf.util.logging.Trace;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GeneralBean {
    
    boolean isPopupOpen = false;
    List<Google> googleResults = new ArrayList<Google>();
    String googleCriteria = ""; 
    String selectedUrl = ""; 
    String rowKey="";
    private Map<String, String> foodGroups = null;
    private AisleService aisle = new AisleService();
    private PropertyChangeSupport _propertyChangeSupport = new PropertyChangeSupport(this);

    public GeneralBean() {
        super();
    }
    
    public void getUrl(ActionEvent actionEvent) {
        
        Object urlObj = AdfmfJavaUtilities.getELValue("#{bindings.recipeUrl.inputValue}");
        String url = urlObj.toString();
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, GeneralBean.class, "getUrl",
                  "##############URL:  " + urlObj.toString());
        
        //applicationContext=MainActivity.this;
        AdfmfContainerUtilities.invokeContainerJavaScriptFunction(
            AdfmfJavaUtilities.getFeatureId(), "displayUrl", new Object[] {url});
        
        //AdfmfContainerUtilities.invokeContainerJavaScriptFunction(
        //    AdfmfJavaUtilities.getFeatureId(), "function(){cordova.InAppBrowser.open('" + 
        //                url + "','_blank','location=yes');}", new Object[] {});
        
    }
    
    public void loadUrl(ActionEvent actionEvent) {
        
        String url = this.selectedUrl;
        //Object urlObj = AdfmfJavaUtilities.getELValue("#{bindings.recipeUrl.inputValue}");
        //String url = urlObj.toString();
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, GeneralBean.class, "loadUrl",  "### URL:  ");
        
        try {
            /**
            ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{bindings.googleResultsIterator1.currentRow.dataProvider}",  Object.class);
            Object obj = ve.getValue(AdfmfJavaUtilities.getAdfELContext());
            Recipe r = new Recipe(); 
            if (obj instanceof ConcreteJavaBeanObject) {
                ConcreteJavaBeanObject cjbo = (ConcreteJavaBeanObject)obj;
                r = (Recipe)cjbo.getInstance();
            }
            String url = r.getRecipeUrl(); 
            **/
           
            if (url.trim().length()>0) {
                //applicationContext=MainActivity.this;
                AdfmfContainerUtilities.invokeContainerJavaScriptFunction(
                    AdfmfJavaUtilities.getFeatureId(), "displayUrl", new Object[] {url});
                
                //AdfmfContainerUtilities.invokeContainerJavaScriptFunction(
                //    AdfmfJavaUtilities.getFeatureId(), "function(){cordova.InAppBrowser.open('" + 
                //                url + "','_blank','location=yes');}", new Object[] {});
            }
            else {
                Trace.log(Utility.ApplicationLogger, Level.SEVERE, GeneralBean.class, "loadUrl",
                          "##############ERROR: URL IS BLANK ");    
            }
        
        } catch (Exception exception) {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, GeneralBean.class, "loadUrl",
                      "##############Exception:  " + exception.getMessage());

        }   
    }
    
    public void onIngredientItemChange(ValueChangeEvent valueChangeEvent) throws MalformedURLException, ProtocolException, IOException {
          
          //See https://blogs.oracle.com/mobile/entry/quick_note_how_to_validate
          //http://prsync.com/oracle/how-to-access-selected-values-in-a-multi-select-list-668104/
          
          String newVal = (String) valueChangeEvent.getNewValue();
          String oldVal = (String) valueChangeEvent.getOldValue();
          Ingredient i;
          Object Obj;
          String currentRowKey="";
          String rowID = "";
        
          Trace.log(Utility.ApplicationLogger, Level.SEVERE, GeneralBean.class, "onIngredientItemChange",
            "############## rowKey:" + rowKey + ", oldVal:" + oldVal + ", newVal:" + newVal);        
          
          try {
              //if (AdfmfJavaUtilities.getELValue("#{pageFlowScope.rowKey}")!=null) {
              //    Obj = AdfmfJavaUtilities.getELValue("#{pageFlowScope.rowKey}");
              //    rowKey = Obj.toString();    
              //}
              //else {
              //    Utility.ApplicationLogger.severe("NULL pageFlowScope.rowKey !");
              //    return;
              //}
              
              if (AdfmfJavaUtilities.getELValue("#{pageFlowScope.selectedId}")!=null) {
                 Obj = AdfmfJavaUtilities.getELValue("#{pageFlowScope.selectedId}");
                 rowID = Obj.toString();    
              }
              else {
                 Utility.ApplicationLogger.severe("NULL pageFlowScope.selectedId !");
                //return;
              }
              
              Utility.ApplicationLogger.severe("FOUND rowKey:" + rowKey + ", rowID:" + rowID);  
                  
              BasicIterator iterator = (BasicIterator) AdfmfJavaUtilities.getELValue("#{bindings.ingredients.iterator}"); 
              if (iterator!=null){
                  Object objCurrentRowKey = iterator.getCurrentRowKey();
                  if (objCurrentRowKey!=null){
                      currentRowKey = objCurrentRowKey.toString();  
                      Utility.ApplicationLogger.severe("FOUND currentRowKey:" + currentRowKey); 
                      if (rowKey.trim().length()>0)iterator.setCurrentRowWithKey(rowKey);
                      i = (Ingredient) iterator.getDataProvider();  
                      i.setItem(newVal);
                      iterator.setCurrentRowWithKey((String) currentRowKey);
                  }
                  else {
                    Utility.ApplicationLogger.severe("NULL objCurrentRowKey !");
                    return;
                  }
              }
             else {
               Utility.ApplicationLogger.severe("NULL ingredients.iterator !");
               return;
             }
          
              Trace.log(Utility.ApplicationLogger, Level.SEVERE, GeneralBean.class, "onIngredientItemChange",
                  "############## ID:" + i.getId() + " " + i.getFoodGroup() + ", oldVal:" + oldVal + ", newVal:" + newVal);        
            
              DataControl dc = AdfmfJavaUtilities.getDataControlById(iterator.getDataControl().getName());
              RecipeService dcImpl = (RecipeService)dc.getDataProvider();
              //dcImpl.refreshIngredients();
        }
        catch (Exception e) {
            Utility.ApplicationLogger.severe("######## EXCEPTION: " + e.getMessage());    
        }

          //Trace.log(Utility.ApplicationLogger, Level.SEVERE, GeneralBean.class, "onIngredientItemChange",
          //      "############## ID:" + i.getId() + ", oldVal:" + oldVal + ", newVal:" + newVal);        
        
          //Recipe r = new Recipe();
          //r.onIngredientItemChange(i.getId(), oldVal, newVal);
         
          //Send ERROR message                
          //    throw new AdfException("The working time cannot be greater than 8 hours", AdfException.ERROR);
    }
    
    public void onIngredientItemChange() throws MalformedURLException, ProtocolException, IOException {
          
          Ingredient i;
          Object Obj;
          String currentRowKey="";
          String rowID = "";
        
          Trace.log(Utility.ApplicationLogger, Level.SEVERE, GeneralBean.class, "onIngredientItemChange",
            "############## rowKey:" + rowKey );        
          
          try {
              
              if (AdfmfJavaUtilities.getELValue("#{pageFlowScope.selectedId}")!=null) {
                 Obj = AdfmfJavaUtilities.getELValue("#{pageFlowScope.selectedId}");
                 rowID = Obj.toString();    
              }
              else {
                 Utility.ApplicationLogger.severe("NULL pageFlowScope.selectedId !");
                //return;
              }
              
              Utility.ApplicationLogger.severe("FOUND rowKey:" + rowKey + ", rowID:" + rowID);  
                  
              BasicIterator iterator = (BasicIterator) AdfmfJavaUtilities.getELValue("#{bindings.ingredients.iterator}"); 
              if (iterator!=null){
                  Object objCurrentRowKey = iterator.getCurrentRowKey();
                  if (objCurrentRowKey!=null){
                      currentRowKey = objCurrentRowKey.toString();  
                      Utility.ApplicationLogger.severe("FOUND currentRowKey:" + currentRowKey); 
                      if (rowKey.trim().length()>0)iterator.setCurrentRowWithKey(rowKey);
                      iterator.setCurrentRowWithKey((String) currentRowKey);
                  }
                  else {
                    Utility.ApplicationLogger.severe("NULL objCurrentRowKey !");
                    return;
                  }
              }
             else {
               Utility.ApplicationLogger.severe("NULL ingredients.iterator !");
               return;
             }
          
              DataControl dc = AdfmfJavaUtilities.getDataControlById(iterator.getDataControl().getName());
              RecipeService dcImpl = (RecipeService)dc.getDataProvider();
              List<Recipe> recipes = (List<Recipe>) dcImpl.getRecipes();
              //dcImpl.refreshIngredients();
        }
        catch (Exception e) {
            Utility.ApplicationLogger.severe("######## EXCEPTION: " + e.getMessage());    
        }

          //Trace.log(Utility.ApplicationLogger, Level.SEVERE, GeneralBean.class, "onIngredientItemChange",
          //      "############## ID:" + i.getId() + ", oldVal:" + oldVal + ", newVal:" + newVal);        
        
          //Recipe r = new Recipe();
          //r.onIngredientItemChange(i.getId(), oldVal, newVal);
         
          //Send ERROR message                
          //    throw new AdfException("The working time cannot be greater than 8 hours", AdfException.ERROR);
    }
    
    public List<Google> getGoogleResults() {
        return googleResults;
    } 
    
    public void setGoogleResults(List<Google> googleResults) {
        List<Google> oldGoogleResults = this.googleResults;
        this.googleResults = googleResults;
        _propertyChangeSupport.firePropertyChange("googleResults", oldGoogleResults, googleResults);
    }
        
    public void setGoogleCriteria(String googleCriteria) {
        
        String oldGoogleCriteria = this.googleCriteria;
        this.googleCriteria = googleCriteria;
        _propertyChangeSupport.firePropertyChange("googleCriteria", oldGoogleCriteria, googleCriteria);
    }

    public String getGoogleCriteria() {
        return googleCriteria;
    }        
    
    public void fetchSelectedRecipes(ValueChangeEvent valueChangeEvent) {
        
        //http://deepakcs.blogspot.com/2013/02/adf-mobile-email-send-multiple-file.html
        
           Object[] objArr = (Object[])valueChangeEvent.getNewValue();
           ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{bindings.fileAttachment1.collectionModel}", AmxCollectionModel.class);
           AmxCollectionModel model = (AmxCollectionModel)ve.getValue(AdfmfJavaUtilities.getELContext());
       
            StringBuffer sUrl = new StringBuffer();
            StringBuffer sTitle = new StringBuffer();
            for (int x = 0; x < objArr.length; x++) {
                    Object obj = objArr[x]; 

                    Map provider = (Map)model.getProviders().get(obj);
                    String filePathVal = provider.get("url").toString();
                    sUrl.append("," + filePathVal.toString());

                    String fileNameval = provider.get("title").toString();
                    sTitle.append("," + fileNameval.toString());
            }
                
           //ValueExpression ve1 = AdfmfJavaUtilities.getValueExpression("#{pageFlowScope.filePaths}", String.class);
           //ve1.setValue(AdfmfJavaUtilities.getAdfELContext(), sUrl.substring(1));

    }
    
    public void searchButtonPressed(ActionEvent actionEvent) throws  MalformedURLException, UnsupportedEncodingException, IOException {
        
         //this.setGoogleCriteria(criteria);
         List<Google> googleResults = new ArrayList<Google>();
         googleResults.clear();
         Trace.log(Utility.ApplicationLogger, Level.SEVERE, GeneralBean.class, "setGoogleSearchResults",
                   "SEARCH CRITERIA:  " + this.getGoogleCriteria());
         
         //List returnValue = new ArrayList();
         String googleURI = "http://www.google.com/search?q=";
         String charset = "UTF-8";
         String userAgent = "ExampleBot 1.0 (+http://example.com/bot)"; // Change this to your company's name and bot homepage!
         int ct = 0;
         Elements links = Jsoup.connect(googleURI + URLEncoder.encode(this.getGoogleCriteria(), charset)).userAgent(userAgent).get().select(".g>.r>a");

         for (Element link : links) {
             String title = link.text();
             String url = link.absUrl("href"); // Google returns URLs in format "http://www.google.com/url?q=<url>&sa=U&ei=<someKey>".
             url = URLDecoder.decode(url.substring(url.indexOf('=') + 1, url.indexOf('&')), "UTF-8");

             if (!url.startsWith("http")) {
                 continue; // Ads/news/etc.
             }
             ct++;
             Google google = new Google();
             google.setTitle(title);
             google.setUrl(url);
             googleResults.add(google);
             
             //returnValue.add(google);
             //System.out.println("Title: " + title);
             //System.out.println("URL: " + url);
             if (ct==10) break;
         }
         
         setGoogleResults(googleResults);        
         Trace.log(Utility.ApplicationLogger, Level.SEVERE, GeneralBean.class, "RefreshActionHandler",
                   "Google Results Count:  " + googleResults.size());                

           
     }
    
    public void onPopupShowHideAction_OLD(ActionEvent actionEvent) {
        
         Trace.log(Utility.ApplicationLogger, Level.SEVERE, GeneralBean.class, "onPopupShowHideAction",
                   "##############ActionEvent:  " + actionEvent.toString());
         
        //if (!isPopupOpen) { //popupUtilsShowPopup

            AdfmfContainerUtilities.invokeContainerJavaScriptFunction(
                    AdfmfJavaUtilities.getFeatureId(), "displayUrl", new Object[] {"_popShowId"});
                
            isPopupOpen = true;

            //wait a few seconds and then close the popup
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                  closePopup();
                  this.cancel();
                }

            },5000);

        //}
     }

    private void closePopup() {

        if (isPopupOpen) {
            AdfmfContainerUtilities.invokeContainerJavaScriptFunction(
                    AdfmfJavaUtilities.getFeatureId(), "popupUtilsHidePopup", new Object[] {"_popCloseId"});
            isPopupOpen = false;

        }

    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        _propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        _propertyChangeSupport.removePropertyChangeListener(l);
    }

    public void setSelectedUrl(String selectedUrl) {
        String oldSelectedUrl = this.selectedUrl;
        this.selectedUrl = selectedUrl;
        _propertyChangeSupport.firePropertyChange("selectedUrl", oldSelectedUrl, selectedUrl);
    }

    public String getSelectedUrl() {
        return selectedUrl;
    }

    public void setRowKey(String rowKey) {
        String oldRowKey = this.rowKey;
        this.rowKey = rowKey;
        _propertyChangeSupport.firePropertyChange("rowKey", oldRowKey, rowKey);
    }

    public String getRowKey() {
        return rowKey;
    }
}
