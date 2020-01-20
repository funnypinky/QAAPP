/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelling;

import java.util.concurrent.Callable;

/**
 *
 * @author shaesler
 * @param <T>
 */
public abstract class AnalyseModul<T extends Result> implements Callable<T>{

    private String name;

    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

  
    @Override
    public abstract T call();
    
}
