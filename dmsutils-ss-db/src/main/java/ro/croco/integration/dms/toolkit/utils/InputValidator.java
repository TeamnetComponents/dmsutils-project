package ro.croco.integration.dms.toolkit.utils;

import ro.croco.integration.dms.commons.exceptions.StoreServiceException;

import java.math.BigDecimal;

/**
 * Created by battamir.sugarjav on 2/26/2015.
 */
public abstract class InputValidator {

    public static void validateIdentifierPath(String path,String functionIdentifier){
        if(path != null){
            if(path.split("_").length != 2)
                throw new StoreServiceException(functionIdentifier + "Document path identifier is not correctly provided: X_Y.");
        }
    }

    public static void validateIdentiferId(String id,String functionIdentifier){
        if(id != null){
            if(id.split("_").length != 2)
                throw new StoreServiceException(functionIdentifier + "Document id identifer is not corectly provided : X_Y.");

            try{
                new BigDecimal(id.split("_")[0]);
                new BigDecimal(id.split("_")[1]);
            }
            catch(NumberFormatException nfe){
                throw new StoreServiceException(functionIdentifier + "Document id identifier is not correctly provided : at least one of composer is not a number.");
            }
        }
    }

    public abstract void validateInputs() throws StoreServiceException;
}
