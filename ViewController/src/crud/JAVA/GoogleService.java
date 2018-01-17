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
import java.util.logging.Level;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;

import jxl.read.biff.BiffException;

import oracle.adfmf.amx.event.ActionEvent;
import oracle.adfmf.bindings.dbf.AmxAttributeBinding;
import oracle.adfmf.bindings.dbf.AmxIteratorBinding;
import oracle.adfmf.bindings.dbf.AmxTreeBinding;
import oracle.adfmf.bindings.iterator.BasicIterator;
import oracle.adfmf.framework.api.AdfmfContainerUtilities;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;
import oracle.adfmf.java.beans.ProviderChangeListener;
import oracle.adfmf.java.beans.ProviderChangeSupport;
import oracle.adfmf.util.Utility;
import oracle.adfmf.util.logging.Trace;

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

public class GoogleService {
    List<Google> googleResults = new ArrayList<Google>();
    String googleCriteria = "";
    String selectedTitle = "";
    private PropertyChangeSupport _propertyChangeSupport = new PropertyChangeSupport(this);

    public void setGoogleResults(List<Google> googleResults) {
        
        List<Google> oldGoogleResults = this.googleResults;
        this.googleResults = googleResults;
        _propertyChangeSupport.firePropertyChange("googleResults", oldGoogleResults, googleResults);
    }
    public List<Google> getGoogleResults() throws MalformedURLException, UnsupportedEncodingException, IOException {
       
        //if(googleResults.isEmpty()) {
            findSearchResults(); 
        //}
        return googleResults;
    }
    
    public void setGoogleCriteria(String googleCriteria) {
        
        String oldGoogleCriteria = this.googleCriteria;
        this.googleCriteria = googleCriteria;
        _propertyChangeSupport.firePropertyChange("googleCriteria", oldGoogleCriteria, googleCriteria);
    }

    public String getGoogleCriteria() {
        return googleCriteria;
    }
    protected transient ProviderChangeSupport providerChangeSupport = new ProviderChangeSupport(this);
    
    public GoogleService() throws MalformedURLException, UnsupportedEncodingException, IOException {
        super();
    }
    public void addProviderChangeListener(ProviderChangeListener l) {
        providerChangeSupport.addProviderChangeListener(l);
    }

    public void removeProviderChangeListener(ProviderChangeListener l) {
        providerChangeSupport.removeProviderChangeListener(l);
    }
     
    public List<Google> findSearchResults() throws MalformedURLException, UnsupportedEncodingException, IOException  {
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, GoogleService.class, "findSearchResults",
                  "CRITERIA:  " + this.getGoogleCriteria());
        
        String criteria = this.getGoogleCriteria();
        if (criteria.trim().length()>0) {
        
            googleResults.clear();
            String googleURI = "http://www.google.com/search?q=";
            String charset = "UTF-8";
            String userAgent = "ExampleBot 1.0 (+http://thejesustoday.com)"; // example.com
            String search = "", c="";
            int ct = 0;
            
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, GoogleService.class, "findSearchResults"," "); 
            
            for (int i=0; i<4; i++){
                
                c = URLEncoder.encode(criteria, charset);
            	search = googleURI + c + "&start=" + i*10;
            	if (i==0)search=googleURI + c;
                
                Elements links = Jsoup.connect(search).userAgent(userAgent).get().select(".g>.r>a");
                for (Element link : links) {
                    ct++;                
                    String title = link.text();
                    String url = link.absUrl("href"); // Google returns URLs in format "http://www.google.com/url?q=<url>&sa=U&ei=<someKey>".
                    url = URLDecoder.decode(url.substring(url.indexOf('=') + 1, url.indexOf('&')), "UTF-8");
        
                    //if (!url.startsWith("http")) continue; // Ads/news/etc.
                                   
                    Google google = new Google();
                    google.setTitle(title);
                    google.setUrl(url);
                    google.setChecked(false);
                    //google.setPrepTime(getPrepTime(url));
                    //if (ct%2==0) google.setChecked(true);
                    googleResults.add(google);            
                }
            }
            
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, GoogleService.class, "findSearchResults",
                      "Google Results Added:  " + googleResults.size()); 
            
        }
        return googleResults;       
    }
    
    public void processSelectedSearchResultsXXX() throws MalformedURLException, ProtocolException, IOException {
        
        //https://blogs.oracle.com/mobile/entry/accessing_selected_values_in_a
        
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, GoogleService.class, "processSelectedSearchResults",
                  "READING SELECTED GOOGLE RESULTS:  ");    
        
        AmxTreeBinding googleList = (AmxTreeBinding) AdfmfJavaUtilities.getELValue("#{bindings.googleResults}");
        AmxIteratorBinding amxListIterator =  googleList.getIteratorBinding();
        BasicIterator basicIterator = amxListIterator.getIterator();    
        
        try  { 
            if (basicIterator.getRangeSize()>0) {
                for (Integer i=1; i<basicIterator.getRangeSize(); i++) {
                    basicIterator.setCurrentIndex(i);
                    Google g = (Google) basicIterator.getDataProvider();
                    
                    if (g.isChecked()) {
                        String title = getURLTitle(g.getUrl());
                        String recipeUrl = g.getUrl();
                        Trace.log(Utility.ApplicationLogger, Level.SEVERE, GoogleService.class, "processSelectedSearchResults",
                                  "Selected URL:  " + recipeUrl);    
                        Trace.log(Utility.ApplicationLogger, Level.SEVERE, GoogleService.class, "processSelectedSearchResults",
                                  "Selected TITLE:  " + title); 
                        
                        ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{bindings.recipeUrl.inputValue}", String.class);
                        ve.setValue(AdfmfJavaUtilities.getELContext(), recipeUrl);
                        
                        ve = AdfmfJavaUtilities.getValueExpression("#{bindings.title.inputValue}", String.class);
                                        ve.setValue(AdfmfJavaUtilities.getELContext(), title);
                    }
                }
            }
        }
        catch(Exception e)     {
            Utility.ApplicationLogger.severe("GoogleService - processSelectedSearchResults: NO RESULTS " + e.getMessage());    
        }
        
        googleResults.clear();
        
    }
    /**
    public void processSelectedGoogleUrl(String url) throws MalformedURLException, ProtocolException, IOException {
                
        try {
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, GoogleService.class, "processSelectedGoogleUrl",
                      "Selected URL:  " + url);    
            
            this.selectedTitle = getURLTitle(url);
            Trace.log(Utility.ApplicationLogger, Level.SEVERE, GoogleService.class, "processSelectedSearchResults",
                                      "Selected TITLE:  " + this.selectedTitle); 
                            
            ValueExpression ve = AdfmfJavaUtilities.getValueExpression("#{bindings.recipeUrl.inputValue}", String.class);
            ve.setValue(AdfmfJavaUtilities.getAdfELContext(), url);
            ve = AdfmfJavaUtilities.getValueExpression("#{applicationScope.selectedUrl}", String.class);
            ve.setValue(AdfmfJavaUtilities.getAdfELContext(), url);
            
            ve = AdfmfJavaUtilities.getValueExpression("#{bindings.title.inputValue}", String.class);
            ve.setValue(AdfmfJavaUtilities.getAdfELContext(), this.selectedTitle);
            ve = AdfmfJavaUtilities.getValueExpression("#{applicationScope.selectedTitle}", String.class);
            ve.setValue(AdfmfJavaUtilities.getAdfELContext(), this.selectedTitle);
            
        }
        catch(Exception e)     {
            Utility.ApplicationLogger.severe("GoogleService - processSelectedGoogleUrl: NO RESULTS " + e.getMessage());    
        }
       
    }
    **/
    
    public String getPrepTime(String recipeUrl) throws MalformedURLException, ProtocolException, IOException {
        
        Utility.ApplicationLogger.severe("GoogleService - getPrepTime: " + recipeUrl);
        
        URL url = new URL(recipeUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(100000 /* milliseconds */);
        conn.setConnectTimeout(250000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect(); 
       
        
        String prepTime = "";
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
                  line = bufferedReader.readLine();
                  while (line!= null)      {
                        idx = line.toUpperCase().indexOf("PREP");                  
                        if (idx==-1){ 
                            //idx = line.toUpperCase().indexOf("TIME"); 
                        }                        
                        if (idx>-1){ 
                            prepTime = line.substring(idx);
                            //System.out.println(title);
                            String noHTMLString = prepTime.replaceAll("\\<.*?>","");
                            String nums ="[1-9]{1,2} (min|m|sec|hour|hr|h)";                             
                            Pattern p = Pattern.compile(nums, Pattern.CASE_INSENSITIVE);
                            Matcher m = p.matcher(noHTMLString); 
                             
                            while (m.find()) {
                                prepTime= m.group();                                
                                Utility.ApplicationLogger.severe("GoogleService - getPrepTime: " + prepTime);
                                return prepTime;                                
                            }
                        }
                        line = bufferedReader.readLine();
                  }
              }
        }
        catch(Exception e)     {
            Utility.ApplicationLogger.severe("GoogleService - getPrepTime: ERROR " + recipeUrl + " " + e.getMessage());    
        }
        return "";
        
    }
    
    public String getURLTitle(String recipeUrl) throws MalformedURLException, ProtocolException, IOException {
        
        //http://www.rgagnon.com/javadetails/java-0085.html
        
        Utility.ApplicationLogger.severe("GoogleService - getUrlTitle: " + recipeUrl);
        
        URL url = new URL(recipeUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(100000 /* milliseconds */);
        conn.setConnectTimeout(250000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect(); 
       
        
        String title = "";
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
                  line = bufferedReader.readLine();
                  while (line!= null)      {
                      idx = line.toUpperCase().indexOf("<TITLE>");                  
                      if (idx>-1){
                          title = line.substring(idx+7);
                          idx = title.toUpperCase().indexOf("</TITLE>");
                          if (idx>-1){
                              title = title.substring(0,idx);
                          }
                          return title;
                      }
                      line = bufferedReader.readLine();
                  }
              }
        }
        catch(Exception e)     {
            Utility.ApplicationLogger.severe("GoogleService - getUrlTitle: ERROR " + recipeUrl + " " + e.getMessage());    
        }
        return "";
        
    }
    
    /**
    public static Object invokeEL(String el, Class[] paramTypes, Object[] params) {
            
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ELContext elContext = facesContext.getELContext();
            ExpressionFactory expressionFactory = facesContext.getApplication().getExpressionFactory();
            MethodExpression exp = expressionFactory.createMethodExpression(elContext, el, Object.class, paramTypes);

            return exp.invoke(elContext, params);
     }
    **/
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        _propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        _propertyChangeSupport.removePropertyChangeListener(l);
    }

    public void setSelectedTitle(String selectedTitle) {
        this.selectedTitle = selectedTitle;
    }

    public String getSelectedTitle() {
        return selectedTitle;
    }
}
