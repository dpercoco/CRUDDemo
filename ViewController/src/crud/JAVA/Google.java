package crud.JAVA;
import java.util.List;

import oracle.adfmf.java.beans.PropertyChangeListener;
import oracle.adfmf.java.beans.PropertyChangeSupport;

public class Google {

    private String url="";
    private String title="";
    private String prepTime="";
    private boolean checked=false;
    private PropertyChangeSupport _propertyChangeSupport = new PropertyChangeSupport(this);

    public String getUrl() { 
        return url; 
        }
    
    public String getTitle() { 
        return title; 
        }
    
    public void setUrl(String url) { 
        this.url = url; 
        }
    
    public void setTitle(String title) { 
        this.title = title; 
        }
    
    public void setPrepTime(String prepTime) {
        String oldPrepTime = this.prepTime;
        this.prepTime = prepTime;
        _propertyChangeSupport.firePropertyChange("prepTime", oldPrepTime, prepTime);
    }

    public String getPrepTime() {
        return prepTime;
    }
    public String toString() { 
        return "Result[url:" + url +",title:" + title + "]"; 
        }

    public void setChecked(boolean selected) {
        
        boolean oldSelected = this.checked;
        this.checked = selected;
        _propertyChangeSupport.firePropertyChange("selected", oldSelected, selected);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        _propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        _propertyChangeSupport.removePropertyChangeListener(l);
    }

    public boolean isChecked() {
        return checked;
    }
}
