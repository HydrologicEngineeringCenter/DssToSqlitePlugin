/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dssToSqlitePlugin;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 * @author WatPowerUser
 */
public class dssToSqliteMessages {
    public static final String Bundle_Name = dssToSqliteI18n.BUNDLE_NAME;
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(Bundle_Name);
    public static final String Plugin_Name = "dssToSqlitePlugin.Name";
    public static final String Plugin_Description = "dssToSqlitePlugin.Description";
    public static final String Plugin_Short_name = "dssToSqlitePlugin.ShortName";
    private dssToSqliteMessages(){
        super();
    }
    public static String getString(String key){
        try
        {
            return RESOURCE_BUNDLE.getString(key);
        }
        catch(MissingResourceException e)
        {
            return '!' + key + '!';
        }
    }
}
