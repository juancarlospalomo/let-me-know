package com.applilandia.letmeknow;

import android.test.AndroidTestCase;

import com.applilandia.letmeknow.cross.LocalDate;
import com.applilandia.letmeknow.models.Task;
import com.applilandia.letmeknow.models.ValidationResult;

import java.util.List;

/**
 * Created by JuanCarlos on 20/02/2015.
 */
public class testValidation extends AndroidTestCase {

    public void testValidateTaskEntity() {
        Task task = new Task();
        //Fail because name is empty
        List<ValidationResult> validationResults = task.validate();
        assertTrue(validationResults.size() == 1);
        ValidationResult expectedValidationResult = new ValidationResult("name", ValidationResult.ValidationCode.Empty);
        ValidationResult resultValidationResult = validationResults.get(0);
        assertEquals(expectedValidationResult, resultValidationResult);
        //Fail because name is greater than 50 chars
        task.name = "task greater than fifty characters, so it has to fail in validation";
        validationResults = task.validate();
        assertTrue(validationResults.size() == 1);
        expectedValidationResult = new ValidationResult("name", ValidationResult.ValidationCode.GreaterThanRange);
        resultValidationResult = validationResults.get(0);
        assertEquals(expectedValidationResult, resultValidationResult);
        //Ok because name isn't empty and has equal or less than 50 chars
        task.name = "task equal to fifty characters, so it has to be ok";
        validationResults = task.validate();
        assertTrue(validationResults == null);
        //Task empty and date less than right now
        task.name = "";
        task.targetDateTime = new LocalDate().addDays(-1);
        validationResults = task.validate();
        assertTrue(validationResults.size() == 2);
        expectedValidationResult = new ValidationResult("name", ValidationResult.ValidationCode.Empty);
        resultValidationResult = validationResults.get(0);
        assertEquals(expectedValidationResult, resultValidationResult);
        expectedValidationResult = new ValidationResult("targetDateTime", ValidationResult.ValidationCode.LessThanRange);
        resultValidationResult = validationResults.get(1);
        assertEquals(expectedValidationResult, resultValidationResult);
        //Ok data
        task.name = "task create";
        task.targetDateTime = new LocalDate().addDays(1);
        validationResults = task.validate();
        assertTrue(validationResults == null);
    }

}
