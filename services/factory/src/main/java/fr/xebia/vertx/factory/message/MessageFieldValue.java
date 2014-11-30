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
public enum MessageFieldValue {
    HELLO("hello"),
    REQUEST("request"),
    ACQUITTEMENT("acquittement"),
    RESPONSE("response"),
    PURCHASE("purchase"),
    SALE("sale"),
    COST("cost");
    
    private final String fieldValue;
    
    private MessageFieldValue(String fieldValue){
        this.fieldValue = fieldValue;
    }
    
    /**
     * 
     * @return the field value.
     */
    public String getFieldValue(){
        return fieldValue;
    }
    
}
