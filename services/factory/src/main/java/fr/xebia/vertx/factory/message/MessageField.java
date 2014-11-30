/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.xebia.vertx.factory.message;

/**
 *
 * @author xebia
 */
public enum MessageField {
    
    FROM("from"),
    ACTION("action"),
    QUANTITY("quantity"),
    COST("cost"),
    TEAM("team"),
    VERSION("version"),
    TYPE("type");
    
    private final String fieldName;
    
    private MessageField(String fieldName){
        this.fieldName = fieldName;
    }
    
    /**
     * 
     * @return the field name
     */
    public String getFieldName(){
        return fieldName;
    }
    
}
