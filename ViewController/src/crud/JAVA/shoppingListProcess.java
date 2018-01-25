package crud.JAVA;

import com.tangosol.dev.assembler.Case;

import javax.xml.*;

import crud.application.DBConnectionFactory;
import crud.application.LifeCycleListenerImpl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import java.sql.Statement;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import oracle.adfmf.util.Utility;
import oracle.adfmf.util.logging.Trace;

import org.apache.poi.poifs.filesystem.Entry;
import org.apache.tika.exception.TikaException;

import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

public class shoppingListProcess {
	
	private static String content="";
	private static String fileDelimiter="\\";
	private static String FOUND_FOOD_ITEM="";
	private static Map<String, String> foodGroups = null;
        private static Integer totalIngredients=0;
        private static AisleService aisle = new AisleService();
        private static Map<String, Map<String, IngredientL>> shoppingItemsLocal = new HashMap<String, Map<String, IngredientL>>(); //<foodGroup,<food item, qty>>
        private static Map<String, Map<String, Ingredient>> shoppingItems = new HashMap<String, Map<String, Ingredient>>(); //<foodGroup,<food item, qty>>
        private static String recipeFolder="C:\\oracle\\Demo\\CRUDDemo\\.adf\\META-INF";
        private static String OUTPUT_RECIPE = recipeFolder + "output\\shoppingList.csv";
        private static String OUTPUT_FILE = recipeFolder + "output\\shoppingListRaw.txt";
	private static BufferedWriter output;
        private static BufferedWriter outputText;
        private static shoppingListProcess slp = new shoppingListProcess();
        private static int endOfIngredients;
        private static int begOfIngredients;
    /**
     * http://www.grocerylistmaker.com/
     */
	
    public static void main(String[] args) throws Exception {
        
        /**  **/
        String url = "http://www.foodnetwork.com/recipes/alton-brown/baked-macaroni-and-cheese-recipe";
        url = "http://www.foodnetwork.com/recipes/alton-brown/cocoa-brownies-recipe";
        url = "http://www.foodnetwork.com/recipes/paula-deen/homestyle-meatloaf-recipe.html";
        htmlParser_local(url);
        setContent_Local();
        outputShoppingItems();
        
    }
    
    private static void initOutputFile() throws IOException {
            
        File outfile = new File(OUTPUT_RECIPE);
        output = new BufferedWriter(new FileWriter(outfile));	
        File outTextfile = new File(OUTPUT_FILE);
        outputText = new BufferedWriter(new FileWriter(outTextfile));       
    }
    
    public static void htmlParser_local(String inUrl) throws MalformedURLException, ProtocolException, IOException {
        
        //http://www.rgagnon.com/javadetails/java-0085.html
        
        System.out.println("htmlParser: " + inUrl);
        
        URL url = new URL(inUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(100000 /* milliseconds */);
        conn.setConnectTimeout(150000 /* milliseconds */);       
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();        
        //Utility.ApplicationLogger.severe("htmlParser: CONNECTED!");
        System.out.println("htmlParser: CONNECTED");
        
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
                  System.out.println("htmlParser: READING BUFFER!");
                  line = bufferedReader.readLine();
                  while (line!= null)      {
                      idx = line.toUpperCase().indexOf("INGREDIENT");                  
                      contentBuilder.append(line.replaceAll("\\<.*?\\>", "") + "\n");
                      line = bufferedReader.readLine();
                  }  
                  
                  System.out.println("htmlParser: FINISHED READING BUFFER!");
                  content = contentBuilder.toString();
                  
                  //outputText.write(content);
                  System.out.println("htmlParser: BUFFER CONTENT LENGTH - " + content.length());
                  //content = content.replaceAll("\\<.*?\\>", "");
                  //findIngredients_Local(content);
                  //System.out.println("htmlParser: ShoppingItems Count:" + shoppingItems.size() + "\n");
              }
        }
        catch(Exception e)     {
            Utility.ApplicationLogger.severe("htmlParser: ERROR " + inUrl + " " + e.getMessage());    
        }
        
    }
    
    private static void readDirectory(File file) throws Exception {
            
        if (file.isDirectory()) {
                if (file.canRead()) {
                    File[] files = file.listFiles();
                    Arrays.sort(files);
                    //System.out.println("\n");
                            for (File temp : files) {
                                    if (temp.isDirectory()) {
                                            //System.out.println("\nReading directory: (" + new SimpleDateFormat("hh:mm:ss").format(new Date())
                                            //			+ ") " + temp.getAbsoluteFile() + "\n");
                                            readDirectory(temp);
                                } 
                                else { /**It's a file **/
                                    if(temp.getPath().toLowerCase().indexOf(".pdf")>-1|
                                       temp.getPath().toLowerCase().indexOf(".doc")>-1) {
                                            processRecipe(temp);				    	
                                    }
                                }
                        }
            } 
            else {
                    //System.out.println(file.getAbsoluteFile() + "Permission Denied");
            }
        }
            else {/**It's a file **/
                    processRecipe(file);
            }
    }    
    
    public static void processRecipe(File f) throws IOException, SAXException, TikaException{    	
                    
        String fn = f.getPath();
        ConvertFile c = new ConvertFile(fn);
        content = c.getContent();
        
        int idx=fn.lastIndexOf(fileDelimiter);
        String suffix = fn.toLowerCase().substring(idx+1).replace(".pdf", "");
        suffix = fn.toLowerCase().substring(idx+1).replace(".doc", "");
        
        content = content.replaceAll("[^A-Za-z0-9()/ \n]", " ");
        content = content.replace("  ", " ");
        
        idx = content.toUpperCase().indexOf("INGREDIENTS");
        if (idx>-1){
                content = content.substring(idx+12);
        }
        idx = content.toUpperCase().indexOf("INSTRUCTIONS");
        if (idx>-1){
                content = content.substring(0,idx);
        }		
        idx = content.toUpperCase().indexOf("DIRECTIONS");
        if (idx>-1){
                content = content.substring(0,idx);
        }
        idx = content.toUpperCase().indexOf("NUTRITION");
        if (idx>-1){
                content = content.substring(0,idx);
        }
        idx = content.toUpperCase().indexOf("CALORIE");
        if (idx>-1){
                content = content.substring(0,idx);
        }

        //System.out.println(content);
        try {
                String [] lines = content.split("\n");
                content = content.replaceAll("\n", "");
                findIngredients_Local(content);
                //for (String s:lines) {
                //   if (s.length()>0) findIngredients(s);
                //}
        } catch (Exception e) {	
            Utility.ApplicationLogger.severe("############## EXCEPTION : " + e.getMessage());
        }
    }
    
    private static String setContentFromJSoup(String recipeUrl) throws IOException {
    
            //Utility.ApplicationLogger.severe("setContentFromJSoup: " + recipeUrl);
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
            catch (Exception e) {
                Utility.ApplicationLogger.severe("############## EXCEPTION : " + e.getMessage());
            }   
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
            catch(Exception e) {
                Utility.ApplicationLogger.severe("############## EXCEPTION : " + e.getMessage());
            }
            return content;
    }
   
    public void setContent(String InContent) {
       
        shoppingItems = new HashMap<String, Map<String, Ingredient>>(); 
        foodGroups = aisle.loadFoodGroups();
        totalIngredients = 0;
        
        //Utility.ApplicationLogger.severe("loaded Food Groups : " + foodGroups.size());
        
        content = InContent;
        content = content.replaceAll("½", "1/2"); 
        content = content.replaceAll("¾", "3/4");
        content = content.replaceAll("⅛", "1/8");
        content = content.replaceAll("⅓", "1/3");
        content = content.replaceAll("¼", "1/4");
        content = content.replaceAll("⅔", "2/3");
        content = content.replaceAll(",", " ");
        
        endOfIngredients = -1;
        begOfIngredients = -1;
        
        setBegEndOfIngredients();
        
        String[] splitContent = content.split("\n");
        String contentLine = "";
        if (splitContent.length>0){
             for (int i=0; i<splitContent.length; i++) {                
                 
                if (splitContent[i].trim().length()>0) {
                    Pattern p = Pattern.compile("[A-Za-z']{1,50}", Pattern.CASE_INSENSITIVE);
                    Matcher m = p.matcher(splitContent[i].trim()); 
                    if (m.find()) {
                        contentLine += " " + splitContent[i].trim();
                        contentLine = contentLine.replaceAll("[^A-Za-z0-9- \\/]", "");
                        findIngredients(contentLine);   /** Find Ingredients **/
                        contentLine = "";
                    }
                    else {
                        contentLine = splitContent[i].trim();
                    }
                }
             }
        }
        
        Utility.ApplicationLogger.severe("######### Total Ingredients:" + totalIngredients);
    }
   
    public static void setContent_Local() {
       
        shoppingItemsLocal = new HashMap<String, Map<String, IngredientL>>(); 
        foodGroups = aisle.loadFoodGroups();
        totalIngredients=0;
        
        System.out.println("loadFoodGroups_XML " + "############## foodGroups : " + foodGroups.size());
                 
        content = content.replaceAll("½", "1/2"); 
        content = content.replaceAll("¾", "3/4");
        content = content.replaceAll("⅛", "1/8");
        content = content.replaceAll("⅓", "1/3");
        content = content.replaceAll("¼", "1/4");
        content = content.replaceAll("⅔", "2/3");
        content = content.replaceAll(",", " ");
        
        endOfIngredients = -1;
        begOfIngredients = -1;
        
        setBegEndOfIngredients();
        
        String[] splitContent = content.split("\n");
        String contentLine = "";
        if (splitContent.length>0){
             for (int i=0; i<splitContent.length; i++) {                
                 
                if (splitContent[i].trim().length()>0) {
                    Pattern p = Pattern.compile("[A-Za-z']{1,50}", Pattern.CASE_INSENSITIVE);
                    Matcher m = p.matcher(splitContent[i].trim()); 
                    if (m.find()) {
                        contentLine += " " + splitContent[i].trim();
                        contentLine = contentLine.replaceAll("[^A-Za-z0-9- \\/]", "");
                        /** Find Ingredients **/
                        findIngredients_Local(contentLine);
                        contentLine = "";
                    }
                    else {
                        contentLine = splitContent[i].trim();
                    }
                }
             }
        }
    }
    
    public static void findIngredients(String line) {
        
        //Utility.ApplicationLogger.severe("shoppingListProcess - findIngredients!!!!\n");
        
        int ct = 0;
        line = line.replace("cans", "can").trim();
        line = line.replace("-", " ").trim();
        
        String ingredient="";
        String nums1 ="(?:[0-9]{1,2} )?[1-9]{1,2}(?:/[1-9]{1,1})?(?: to)?(?: [0-9]{1,2} (oz|ounce|cup|cups|(oz)))?";
        String contentsWithinParenthesis = "(?:\\([^()]*\\))?";
        String anyWord = "[A-Za-z -']{1,50}";                 
        String regex = "[1-9]{1,2}(?:[0-9])?$";
        String foodGroup = "";
                        
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(line);
        Boolean bFoundIngredient = false;
        
        while (m.find()) {
            String numsAtEndFound = m.group();
            int startPos = m.start();
            line = m.group();
            try {
                if (startPos>-1 && line.length()> startPos) {
                    line = m.group() + " " + line.substring(0, startPos);
                }
            }
            catch(Exception e)     {
                Utility.ApplicationLogger.severe("ERROR 1 " + ingredient + " " + e.getMessage());    
            }
        }

        regex = nums1 + anyWord + contentsWithinParenthesis + anyWord;
        p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        m = p.matcher(line);
        
        try {
            while (m.find()) {
                   ct++;
                   bFoundIngredient = true;
                
                   //Utility.ApplicationLogger.severe("shoppingListProcess - foundIngredient: " + m.group());
               
                   ingredient = m.group();
                   ingredient = ingredient.replace("  "," ");
                   ingredient = ingredient.toLowerCase().replace("ingredients","");
                   if (ingredient.toLowerCase().indexOf("servings")>-1) {
                   }
                   else {
                       foodGroup = aisle.findFoodGroup(foodGroups, ingredient);
                       //foodGroup  = aisle.findFoodGroup(ingredient);
                       try {
                           if (FOUND_FOOD_ITEM.trim().length()>0){
                                   int idx= ingredient.indexOf(FOUND_FOOD_ITEM);
                                   int len= FOUND_FOOD_ITEM.length();
                                   if (idx>-1) ingredient = ingredient.substring(0, idx+len);
                                   addShoppingItem(foodGroup, ingredient); 
                           }
                           else {
                               if (foodGroup.trim().length()>0){
                                    addShoppingItem(foodGroup, ingredient); 
                               }
                           }
                       }
                       catch(Exception e)     {
                           Utility.ApplicationLogger.severe("ERROR 2 " + ingredient + " , foodgroup:" + foodGroup + " " + e.getMessage());    
                       }
                   }
           }
           
           if (!bFoundIngredient) {
                   line = line.replaceAll("[*.-/?]", "");
                   foodGroup = aisle.findFoodGroup(foodGroups, line);
                   if (foodGroup.trim().length()>0){
                        addShoppingItem(foodGroup, line); 
                   }
           }
        }
        catch(Exception e)     {
            Utility.ApplicationLogger.severe("ERROR 3 " + ingredient + " " + e.getMessage());    
        }
    }
    
    public static void findIngredients_Local(String line) {
        
        //System.out.println("findIngredients_Local:" + line);
        
        int ct = 0;
        line = line.replace("cans", "can").trim();
        line = line.replace("-", " ").trim();
        
        String ingredient="";
        String nums1 ="(?:[0-9]{1,2} )?[1-9]{1,2}(?:/[1-9]{1,1})?(?: to)?(?: [0-9]{1,2} (oz|ounce|cup|cups|(oz)))?";
        String contentsWithinParenthesis = "(?:\\([^()]*\\))?";
        String anyWord = "[A-Za-z -']{1,50}";                 
        String regex = "[1-9]{1,2}(?:[0-9])?$";
        String foodGroup = "";
                        
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(line);
        Boolean bFoundIngredient = false;
        
        while (m.find()) {
            String numsAtEndFound = m.group();
            int startPos = m.start();
            line = m.group();
            try {
                if (startPos>-1 && line.length()> startPos) {
                    line = m.group() + " " + line.substring(0, startPos);
                }
            }
            catch(Exception e)     {
                //Utility.ApplicationLogger.severe("shoppingListProcess findIngredients_Local ERROR 1 " + ingredient + " " + e.getMessage());    
            }
        }

        regex = nums1 + anyWord + contentsWithinParenthesis + anyWord;
        p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        m = p.matcher(line);
        
        try {
            while (m.find()) {
                   ct++;
                   bFoundIngredient = true;
                   ingredient = m.group();
                   ingredient = ingredient.replace("  "," ");
                   ingredient = ingredient.toLowerCase().replace("ingredients","");
                   if (ingredient.toLowerCase().indexOf("servings")>-1) {
                   }
                   else {
                       foodGroup = aisle.findFoodGroup(foodGroups, ingredient);
                       //System.out.println(ingredient + " - " + foodGroup);
                       try {
                           if (FOUND_FOOD_ITEM.trim().length()>0){
                                   int idx= ingredient.indexOf(FOUND_FOOD_ITEM);
                                   int len= FOUND_FOOD_ITEM.length();
                                   if (idx>-1) ingredient = ingredient.substring(0, idx+len);
                                   addShoppingItemLocal(foodGroup, ingredient); 
                           }
                           else {
                               if (foodGroup.trim().length()>0){
                                    addShoppingItemLocal(foodGroup, ingredient); 
                               }
                           }
                       }
                       catch(Exception e)     {
                           //Utility.ApplicationLogger.severe("ERROR 2 " + ingredient + " , foodgroup:" + foodGroup + " " + e.getMessage());    
                       }
                   }
           }
           
           if (!bFoundIngredient) {
                   line = line.replaceAll("[*.-/?]", "");
                   foodGroup = aisle.findFoodGroup(foodGroups, line);
                   if (foodGroup.trim().length()>0){
                        addShoppingItemLocal(foodGroup, line); 
                   }
           }
        }
        catch(Exception e)     {
            //Utility.ApplicationLogger.severe("ERROR 3 " + ingredient + " " + e.getMessage());    
        }
    }
    
    private static void cleanContent() {
            
            int idx=0;
            content = content.replace("cans", "can").trim();        
            content = content.replaceAll("[^A-Za-z0-9()/ \n]", " ");
            content = content.replace("  ", " ");
            
            idx = content.toUpperCase().indexOf("INGREDIENTS");
            if (idx>-1){
                    content = content.substring(idx+12);
            }
            idx = content.toUpperCase().indexOf("INSTRUCTIONS");
            if (idx>-1){
                    content = content.substring(0,idx);
            }               
            idx = content.toUpperCase().indexOf("DIRECTIONS");
            if (idx>-1){
                    content = content.substring(0,idx);
            }
            idx = content.toUpperCase().indexOf("NUTRITION");
            if (idx>-1){
                    content = content.substring(0,idx);
            }
            idx = content.toUpperCase().indexOf("CALORIE");
            if (idx>-1){
                    content = content.substring(0,idx);
            }
            idx = content.toUpperCase().indexOf("PREPARATION");
            if (idx>-1){
                    content = content.substring(0,idx);
            }
    }
    
    public static void setEndOfIngredientsStatic(String line) {
            
        int idx=0;
        idx = line.toUpperCase().indexOf("INSTRUCTIONS");
        if (idx>-1){
                 endOfIngredients = idx;
        }               
        idx = line.toUpperCase().indexOf("DIRECTIONS");
        if (idx>-1){
                endOfIngredients = idx;
        }
        idx = line.toUpperCase().indexOf("NUTRITION");
        if (idx>-1){
                endOfIngredients = idx;
        }
        idx = line.toUpperCase().indexOf("CALORIE");
        if (idx>-1){
                endOfIngredients = idx;
        }
        idx = line.toUpperCase().indexOf("PREPARATION");
        if (idx>-1){
                endOfIngredients = idx;
        }
    }
    
    public static void setBegEndOfIngredients() {
        
        int idx=0;
        boolean begFound = false;
        boolean stopLoop = false;
        String initContent = content;
        String[] begWords = {"Ingredients","INGREDIENTS"};
        int loopCt = 0;
               
        for (String bW: begWords) {
                idx = initContent.indexOf(bW); 
                while (!stopLoop){
                    loopCt++;
                    if (idx>-1){
                         begOfIngredients = idx+11;
                         initContent = initContent.substring(begOfIngredients);
                         begFound = findInitIngredient(initContent);
                         if (begFound) {
                                stopLoop = true;
                                content = initContent;
                                break;
                         }
                         else idx = initContent.indexOf(bW);
                    }
                    else {
                        stopLoop = true;
                    }
                }
        }        
        
        //System.out.println("BEG OF INGREDIENTS________________________________\n" + initContent.substring(0,500));
        content = initContent;        
        setEndOfIngredients();
        
        //System.out.println("END OF INGREDIENTS" + endOfIngredients+ ", loopCt:" + loopCt);
        if (endOfIngredients>-1) initContent = initContent.substring(0,endOfIngredients);
        content = initContent;
        
        //System.out.println("END OF INGREDIENTS________________________________\n" + content); 
        
    }
    
    private static boolean findInitIngredient(String line) {        
                       
           line = line.replace("cans", "can").trim();
           line = line.replace("-", " ").trim();
           line = line.replace("\n", " ").trim();
           
           String ingredient="";
           String nums1 ="(?:[0-9]{1,2} )?[1-9]{1,2}(?:/[1-9]{1,1})?(?: to)?(?: [0-9]{1,2} (oz|ounce|cup|cups|(oz)))?";
           String contentsWithinParenthesis = "(?:\\([^()]*\\))?";
           String anyWord = "[A-Za-z -']{1,50}";                   
           String regex = "[1-9]{1,2}(?:[0-9])?$";                 
           Boolean bFoundIngredient = false;
           
           Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
           Matcher m = p.matcher(line); 
           while (m.find()) {
                   int startPos = m.start();
                   line = m.group() + " " + line.substring(0,startPos);
                   //System.out.println("REMOVE1: findInitIngredient - " + line);
           }               
           
           regex = nums1 + anyWord + contentsWithinParenthesis + anyWord; 
           p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
           m = p.matcher(line); 
           if (m.find()) {
                   ingredient = m.group();
                   String foodGroup = aisle.findFoodGroup(foodGroups, ingredient);
                   //System.out.println("REMOVEA: findInitIngredient - " + ingredient);
                   if (foodGroup.trim().length()>0){
                         bFoundIngredient = true;
                   }                                                       
           }
           
           //System.out.println("REMOVE2: findInitIngredient - " + ingredient + " " 
           //         + FOUND_FOOD_ITEM + " " + bFoundIngredient);         
           return bFoundIngredient;
     }
    
    public static Boolean setEndOfIngredients() {
            
            //CHANGE FEB 2017
            int idx=-1;
            int endIng = -1;
            idx = content.toUpperCase().indexOf("INSTRUCTIONS");
            if (idx>-1){
                    endIng = idx;
            }  
            if (endIng>endOfIngredients) {
                    endOfIngredients = endIng;
                    return true;
            }
                    
            
            idx = content.toUpperCase().indexOf("DIRECTIONS");
            if (idx>-1){
                    endIng = idx;
            }
            if (endIng>endOfIngredients) {
                    endOfIngredients = endIng;
                    return true;
            }
            
            
            idx = content.toUpperCase().indexOf("NUTRITION");
            if (idx>-1){
                    endIng = idx;
            }
            if (endIng>endOfIngredients) {
                    endOfIngredients = endIng;
                    return true;
            }
            
            
            idx = content.toUpperCase().indexOf("CALORIE");
            if (idx>-1){
                    endIng = idx;
            }
            if (endIng>endOfIngredients) {
                    endOfIngredients = endIng;
                    return true;
            }
            
            
            idx = content.toUpperCase().indexOf("PREPARATION");
            if (idx>-1){
                    endIng = idx;
            }
            if (endIng>endOfIngredients) {
                    endOfIngredients = endIng;
                    return true;
            }
            
            return false;
    }    
    
    private static void addShoppingItem(String foodGroup, String foodItemRaw) {
	
    //Utility.ApplicationLogger.severe("shoppingListProcess - addShoppingItem: " + foodItemRaw);
        	 
    try {
       Map<String, Ingredient> temp = new HashMap<String, Ingredient>();
       String foodItem=""; 
       Ingredient ingredient = new Ingredient(), prevIngredient = null;
        
       if (shoppingItems.containsKey(foodGroup)) {
              temp = shoppingItems.get(foodGroup);
              if(temp == null)
                 temp = new HashMap<String, Ingredient>();
              
            //ingredient = ingredient.buildIngredient(foodItemRaw);               
              ingredient = ingredient.buildIngredient(foodGroup, foodItemRaw);
              
              foodItem = ingredient.getFoodItem();
              if (temp.containsKey(foodItem)) {
                 prevIngredient = temp.get(foodItem);
                 ingredient.setAmount(prevIngredient.getAmount()+ingredient.getAmount());
                 ingredient.setQty(prevIngredient.getQty()+ingredient.getQty());
              }
              temp.put(foodItem, ingredient);
              shoppingItems.remove(foodGroup);
              shoppingItems.put(foodGroup,temp);
              totalIngredients++;
           } 
           else {
                  temp = new HashMap<String, Ingredient>();
                  ingredient = ingredient.buildIngredient(foodGroup, foodItemRaw);
                  foodItem = ingredient.getFoodItem();
                  temp.put(foodItem, ingredient);
                  shoppingItems.put(foodGroup,temp);
                  totalIngredients++;
           }
           //Utility.ApplicationLogger.severe("NEW INGREDIENT ADDED!" + foodGroup + "/" + foodItemRaw);
        }
        catch (Exception e) {
            Utility.ApplicationLogger.severe("ERROR food group/food item " + foodGroup + "/" + foodItemRaw + " " + e.getMessage()); 
        }
    }
    
    private static void addShoppingItemLocal(String foodGroup, String foodItemRaw) {
                 
    try {
       Map<String, IngredientL> temp = new HashMap<String, IngredientL>();
       String foodItem=""; 
       IngredientL ingredient = new IngredientL(), prevIngredient = null;             
        
       if (shoppingItemsLocal.containsKey(foodGroup)) {
              temp = shoppingItemsLocal.get(foodGroup);
              if(temp == null)
                 temp = new HashMap<String, IngredientL>();
              ingredient = ingredient.buildIngredient(foodGroup, foodItemRaw);
              foodItem = ingredient.getFoodItem();
              if (temp.containsKey(foodItem)) {
                 prevIngredient = temp.get(foodItem);
                 ingredient.setAmount(prevIngredient.getAmount()+ingredient.getAmount());
                 ingredient.setQty(prevIngredient.getQty()+ingredient.getQty());
              }
              temp.put(foodItem, ingredient);
              shoppingItemsLocal.remove(foodGroup);
              shoppingItemsLocal.put(foodGroup,temp);
              totalIngredients++;
           } 
           else {
                  temp = new HashMap<String, IngredientL>();
                  ingredient = ingredient.buildIngredient(foodGroup, foodItemRaw);
                  foodItem = ingredient.getFoodItem();
                  temp.put(foodItem, ingredient);
                  shoppingItemsLocal.put(foodGroup,temp);
                  totalIngredients++;
           }
                
            System.out.println("addShoppingItem: NEW INGREDIENT ADDED!" + foodItemRaw);
        }
        catch (Exception e) {
            System.out.println("addShoppingItem: ERROR food group/food item " + foodGroup + "/" + foodItemRaw + " " + e.getMessage()); 
        }
    }
    
    private static void outputShoppingItems() throws IOException {
          Map.Entry<String, Map<String, IngredientL>> foodGroupEntry=null;
          String foodGroup="";;
          IngredientL i = null;
         
          System.out.println("\nTotal Food Groups:" + shoppingItemsLocal.size());
          System.out.println("Total Ingredients:" + totalIngredients);
          
          List<Map.Entry<String, Map<String, IngredientL>>> shoppingGroups = getSortedShoppingItemGroupsMapStatic();
          for (int g = 0; g < shoppingGroups.size(); g++) {
          
                  foodGroupEntry = shoppingGroups.get(g);
                  foodGroup = foodGroupEntry.getKey();
                  Map<String, IngredientL> foodGroupEntryItems = foodGroupEntry.getValue();
                  
                  List<Map.Entry<String, IngredientL>> shoppingItems = getSortedShoppingItemsMapStatic((Map) foodGroupEntryItems);
                  for (int j = 0; j < shoppingItems.size(); j++) {
                          Map.Entry<String, IngredientL> s = shoppingItems.get(j);
                  i = s.getValue();
                  if (i.getFoodItem().trim().length()>0){
                        String outputItem = foodGroup + ": " + Fractions.intFraction(i.getQty()) + Fractions.intFraction(i.getAmount()) + i.getMeasurement() + 
                                          i.getContainer() + i.getFoodItem();
                        System.out.println(outputItem.replaceAll("  ", " "));
                  }
             }
          }
          System.out.println("\nDone!");
            
     }
    
    public Map<String, Map<String, Ingredient>> getShoppingItems() {
            return shoppingItems; //<foodGroup,<food item, ingredient>>
    }
     
   public List<Map.Entry<String, Ingredient>> getSortedShoppingItemsMap(Map<String, Ingredient> mp) {          
                
           final int size = mp.size();
           final List<Map.Entry<String, Ingredient>> list = new ArrayList<Map.Entry<String, Ingredient>>(size);
           list.addAll(mp.entrySet());
           final ShoppingItemsComparator<String> cmp = new ShoppingItemsComparator<String>();
           Collections.sort(list, cmp);
           return list;
    }
    
    private static List<Map.Entry<String, IngredientL>> getSortedShoppingItemsMapStatic(Map<String, IngredientL> mp) {    	  
                
           final int size = mp.size();
           final List<Map.Entry<String, IngredientL>> list = new ArrayList<Map.Entry<String, IngredientL>>(size);
           list.addAll(mp.entrySet());
           final ShoppingItemsComparatorLocal<String> cmp = new ShoppingItemsComparatorLocal<String>();
           Collections.sort(list, cmp);
           return list;
    }
     
    public List<Map.Entry<String, Map<String, Ingredient>>> getSortedShoppingItemGroupsMap() {    	  
                
           final int size = shoppingItems.size();
           final List<Map.Entry<String, Map<String, Ingredient>>> list = new ArrayList<Map.Entry<String, Map<String, Ingredient>>>(size);
           list.addAll(shoppingItems.entrySet());
           final ShoppingItemGroupsComparator<String> cmp = new ShoppingItemGroupsComparator<String>();
           Collections.sort(list, cmp);
           return list;
    }
    
    private static List<Map.Entry<String, Map<String, IngredientL>>> getSortedShoppingItemGroupsMapStatic() {       
                
           final int size = shoppingItemsLocal.size();
           final List<Map.Entry<String, Map<String, IngredientL>>> list = new ArrayList<Map.Entry<String, Map<String, IngredientL>>>(size);
           list.addAll(shoppingItemsLocal.entrySet());
           final ShoppingItemGroupsComparatorLocal<String> cmp = new ShoppingItemGroupsComparatorLocal<String>();
           Collections.sort(list, cmp);
           return list;
    } 
    
    private static boolean isNumeric (char C) {
            
        char[] chars = {'0','1','2','3','4','5','6','7','8','9','-'};
        for (char c: chars) {
                if (c == C){
                        return true;
                }
        }
        return false;
    }
}

final class ShoppingItemGroupsComparatorLocal<V extends Comparable<? super V>> implements Comparator<Map.Entry<String, Map<String, IngredientL>>> {
    public int compare(Map.Entry<String, Map<String, IngredientL>> o1, Map.Entry<String, Map<String, IngredientL>> o2) {
        return ((String) o1.getKey()).compareTo((String) o2.getKey());
    }
}
final class ShoppingItemsComparatorLocal<V extends Comparable<? super V>> implements Comparator<Map.Entry<String, IngredientL>> {
    public int compare(Map.Entry<String, IngredientL> o1, Map.Entry<String, IngredientL> o2) {
        return ((String) o1.getKey()).compareTo((String) o2.getKey());
    }

}
