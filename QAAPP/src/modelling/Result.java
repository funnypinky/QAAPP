/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelling;

import java.util.HashMap;

/**
 *
 * @author shaesler
 */
public abstract class Result {
    private HashMap<String,Object> details = new HashMap<>();
    
    public HashMap<String, Object> getDetails() {
        return details;
    }

    public void setDetails(HashMap<String, Object> details) {
        this.details = details;
    }
}
