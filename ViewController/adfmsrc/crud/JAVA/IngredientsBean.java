package crud.JAVA;

import java.util.logging.Level;

import oracle.adfmf.amx.event.ActionEvent;
import oracle.adfmf.bindings.iterator.BasicIterator;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.util.Utility;
import oracle.adfmf.util.logging.Trace;

public class IngredientsBean {
    public IngredientsBean() {
    }

    public void onIngredientSelect(ActionEvent actionEvent) {
        
        BasicIterator iterator = (BasicIterator) AdfmfJavaUtilities.getELValue("#{bindings.ingredients.iterator}"); 
        Ingredient selectedIngredient = (Ingredient)iterator.getDataProvider();
        iterator.setCurrentIndex(selectedIngredient.getId());
        Trace.log(Utility.ApplicationLogger, Level.SEVERE, IngredientsBean.class, "##############onIngredientSelect", 
                  " Ingredient ID:" + selectedIngredient.getId());
    }
}
